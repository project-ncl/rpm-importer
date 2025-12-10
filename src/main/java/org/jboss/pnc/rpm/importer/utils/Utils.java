package org.jboss.pnc.rpm.importer.utils;

import io.smallrye.common.process.ProcessBuilder;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.EmptyCommitException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

public class Utils {
    private static final Logger log = LoggerFactory.getLogger(Utils.class);

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
    public static String parseVersionReleaseSerial(Path path) throws IOException {
        String found = Files.readString(Paths.get(path.toString(), "version-release-serial")).trim();
        return found.split(" ")[1];
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
        String found = Files.readString(Paths.get(path.toString(), "mead-pkg-name")).trim();
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
                    .setAuthor("ProjectNCL", "")
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

}
