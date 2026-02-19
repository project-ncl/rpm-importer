package org.jboss.pnc.rpm.importer.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.EmptyCommitException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.jboss.pnc.rpm.importer.model.brew.BuildInfo;
import org.jboss.pnc.rpm.importer.model.brew.Typeinfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smallrye.common.process.ProcessBuilder;

public class Utils {
    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    private static final String RPM_BUILDER_PLUGIN_METADATA_URL = "https://repo1.maven.org/maven2/org/jboss/pnc/rpm-builder-maven-plugin/maven-metadata.xml";
    private static final Pattern LATEST_VERSION_PATTERN = Pattern.compile("<latest>([^<]+)</latest>");
    private static final Pattern RELEASE_VERSION_PATTERN = Pattern.compile("<release>([^<]+)</release>");

    public static Path createTempDirForCloning() {
        return createTempDir("clone-", "cloning");
    }

    public static Path createTempDir(String prefix, String activity) {
        try {
            return Files.createTempDirectory(prefix);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create temporary directory for " + activity, e);
        }
    }

    public static String readTemplate() throws IOException {
        try (InputStream x = Utils.class.getClassLoader().getResourceAsStream("pom-template.xml")) {
            assert x != null;
            return new String(x.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * The format of the file is
     *
     * <pre>
     * {@code <meadversion> <namedversion> <meadalpha> <meadrel> <serial> <namedversionrel>}
     * </pre>
     *
     * We only want the namedversion.
     *
     * @param path the directory where the ETT files are
     * @return a parsed String RH version
     */
    public static String parseNamedVersionFromVersionReleaseSerial(Path path) throws IOException {
        String found = Files.readString(Paths.get(path.toString(), ETT.VERSION_RELEASE_SERIAL)).trim();
        return found.split(" ")[1];
    }

    /**
     * The format of the file is
     *
     * <pre>
     * {@code <meadversion> <namedversion> <meadalpha> <meadrel> <serial> <namedversionrel>}
     * </pre>
     *
     * Returns the original version by subtracting the final field (namedversionrel) and one extra
     * character from the namedversion.
     *
     * @param path the directory where the ETT files are
     * @return a parsed String original version
     */
    public static String parseOriginalVersionFromVersionReleaseSerial(Path path) throws IOException {
        String found = Files.readString(Paths.get(path.toString(), ETT.VERSION_RELEASE_SERIAL)).trim();
        String[] fields = found.split(" ");
        String namedVersion = fields[1];
        String namedVersionRel = fields[fields.length - 1];
        // Subtract the final field and 1 extra character (the separator) from the named version
        String suffix = "." + namedVersionRel;
        if (namedVersion.matches(".*" + suffix + "$")) {
            return namedVersion.substring(0, namedVersion.length() - suffix.length());
        }
        log.error("Found namedVersion {} and suffix {} ", namedVersion, suffix);
        throw new RuntimeException(
                "Invalid version-release-serial format; unable to determine original version from " + found);
    }

    /**
     * The format of the file is
     *
     * <pre>
     * {@code <pkg> <optionalTag>}
     * </pre>
     *
     * We only want the mead package.
     *
     * @param path the directory where the ETT files are
     * @return a parsed String version
     */
    public static String parseMeadPkgName(Path path) throws IOException {
        String found = Files.readString(Paths.get(path.toString(), ETT.MEAD_PKG_NAME)).trim();
        return found.split(" ")[0];
    }

    /**
     * Clones a repository to a temporary location.
     *
     * @param url The repository to clone
     * @param branch The branch to switch to.
     * @return the path of the cloned repository
     */
    public static Path cloneRepository(String url, String branch) {
        Path path = createTempDirForCloning();
        log.info("Using {} for repository", path);
        StringWriter writer = new StringWriter();
        var repoClone = Git.cloneRepository()
                .setURI(url)
                .setProgressMonitor(getMonitor(writer))
                .setBranch(branch)
                .setDirectory(path.toFile());
        try (var ignored = repoClone.call()) {
            log.info("Clone summary:\n{}", writer.toString().replaceAll("(?m)^\\s+", ""));
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
        return path;
    }

    /**
     * Commits the pom.xml to the repository and optionally pushes it.
     *
     * @param repository the path to the repository.
     * @param push whether to push changes to external repository
     */
    public static void commitAndPushRepository(Path repository, boolean push) {
        try (var jGit = Git.init().setDirectory(repository.toFile()).call()) {
            jGit.add().addFilepattern("pom.xml").call();
            var revCommit = jGit.commit()
                    .setNoVerify(true)
                    .setMessage("RPM-Importer - POM Generation")
                    .setAllowEmpty(false)
                    .call();
            log.info("Added and committed pom.xml ({})", revCommit.getName());
            if (push) {
                StringWriter writer = new StringWriter();
                jGit.push().setProgressMonitor(getMonitor(writer)).call();
                log.info("Push summary:\n{}", writer.toString().replaceAll("(?m)^\\s+", ""));
            }
        } catch (EmptyCommitException ex) {
            // avoid empty commit to avoid PNC rebuilds
            log.info("Nothing to commit");
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Verifies whether a remote repository and branch exist
     *
     * @param url the repository
     * @param branch the branch
     * @return true if it exists, false otherwise
     */
    public static boolean checkForRemoteRepositoryAndBranch(String url, String branch) {
        var holder = new Object() {
            int exitCode;
        };
        ProcessBuilder.newBuilder("git")
                .arguments(
                        "ls-remote",
                        "--exit-code",
                        "--heads",
                        url,
                        branch)
                .output()
                .gatherOnFail(false)
                .discard()
                .error()
                .gatherOnFail(false)
                .discard()
                .exitCodeChecker(ec -> {
                    holder.exitCode = ec;
                    return true;
                })
                .run();
        return holder.exitCode == 0;
    }

    /**
     * Fetches the latest version of org.jboss.pnc:rpm-builder-maven-plugin from Maven Central.
     *
     * @return the latest version string (e.g. "1.5")
     * @throws IOException if the request fails or the response cannot be parsed
     * @throws InterruptedException if the request is interrupted
     */
    public static String getLatestRpmBuilderMavenPluginVersion() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(RPM_BUILDER_PLUGIN_METADATA_URL))
                .GET()
                .build();
        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != 200) {
            throw new IOException(
                    "Failed to fetch maven-metadata.xml: HTTP " + response.statusCode() + " "
                            + RPM_BUILDER_PLUGIN_METADATA_URL);
        }
        String body = response.body();
        String version = extractFirstMatch(LATEST_VERSION_PATTERN, body);
        if (version == null) {
            version = extractFirstMatch(RELEASE_VERSION_PATTERN, body);
        }
        if (version == null) {
            throw new IOException(
                    "Could not parse latest or release version from maven-metadata.xml: "
                            + RPM_BUILDER_PLUGIN_METADATA_URL);
        }
        return version;
    }

    private static String extractFirstMatch(Pattern pattern, String body) {
        Matcher m = pattern.matcher(body);
        return m.find() ? m.group(1).trim() : null;
    }

    private static TextProgressMonitor getMonitor(StringWriter writer) {
        TextProgressMonitor monitor = new TextProgressMonitor(writer) {
            // Don't want percent updates, just final summaries.
            protected void onUpdate(String taskName, int workCurr, Duration duration) {
            }

            protected void onUpdate(String taskName, int cmp, int totalWork, int pcnt, Duration duration) {
            }
        };
        monitor.showDuration(true);
        return monitor;
    }

    /**
     * This function validates that the buildInfo read from Brew contains a valid Maven object. If
     * only a legacy Maven block is found (i.e. Extra/Maven instead of Extra/TypeInfo/Maven) then
     * it will also copy that to the typeInfo/Maven block to make future processing easier.
     *
     * @param buildInfo BuildInfo object to validate
     * @return true if a valid Maven block is found, else false
     */
    public static boolean validateBuildInfo(BuildInfo buildInfo) {
        if (buildInfo.getExtra() != null) {
            if (buildInfo.getExtra().getTypeinfo() != null && buildInfo.getExtra().getTypeinfo().getMaven() != null) {
                return true;
            } else {
                if (buildInfo.getExtra().getMaven() != null) {
                    log.warn("Legacy typeinfo detected for {}", buildInfo.getName());
                    Typeinfo typeInfo = new Typeinfo();
                    typeInfo.setMaven(buildInfo.getExtra().getMaven());
                    buildInfo.getExtra().setTypeinfo(typeInfo);
                    return true;
                }
            }
        }
        return false;
    }
}
