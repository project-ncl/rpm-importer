package org.jboss.pnc.rpm.importer;

import static org.maveniverse.domtrip.maven.MavenPomElements.Elements.ARTIFACT_ID;
import static org.maveniverse.domtrip.maven.MavenPomElements.Elements.BUILD;
import static org.maveniverse.domtrip.maven.MavenPomElements.Elements.CLASSIFIER;
import static org.maveniverse.domtrip.maven.MavenPomElements.Elements.DEPENDENCIES;
import static org.maveniverse.domtrip.maven.MavenPomElements.Elements.DEPENDENCY_MANAGEMENT;
import static org.maveniverse.domtrip.maven.MavenPomElements.Elements.GROUP_ID;
import static org.maveniverse.domtrip.maven.MavenPomElements.Elements.NAME;
import static org.maveniverse.domtrip.maven.MavenPomElements.Elements.PLUGINS;
import static org.maveniverse.domtrip.maven.MavenPomElements.Elements.PROPERTIES;
import static org.maveniverse.domtrip.maven.MavenPomElements.Elements.TYPE;
import static org.maveniverse.domtrip.maven.MavenPomElements.Elements.VERSION;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.commonjava.atlas.maven.ident.ref.SimpleArtifactRef;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logmanager.Level;
import org.jboss.pnc.api.reqour.dto.TranslateRequest;
import org.jboss.pnc.api.reqour.dto.TranslateResponse;
import org.jboss.pnc.bacon.auth.client.PncClientHelper;
import org.jboss.pnc.bacon.common.Constant;
import org.jboss.pnc.bacon.config.Config;
import org.jboss.pnc.bacon.config.PncConfig;
import org.jboss.pnc.bacon.config.ReqourConfig;
import org.jboss.pnc.client.Configuration;
import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.SCMRepository;
import org.jboss.pnc.dto.requests.CreateAndSyncSCMRequest;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.RepositoryCreationResponse;
import org.jboss.pnc.rpm.importer.clients.OrchService;
import org.jboss.pnc.rpm.importer.clients.ReqourService;
import org.jboss.pnc.rpm.importer.model.brew.BuildInfo;
import org.jboss.pnc.rpm.importer.utils.Brew;
import org.jboss.pnc.rpm.importer.utils.Utils;
import org.maveniverse.domtrip.maven.PomEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.Element;
import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine;
import picocli.CommandLine.Option;

/**
 * The entrypoint of the RPM importer.
 */
@TopCommand
@CommandLine.Command(
        name = "rpm-importer",
        description = "",
        mixinStandardHelpOptions = true,
        usageHelpWidth = 160,
        versionProvider = VersionProvider.class)
public class App implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @RestClient
    ReqourService reqourService;

    @RestClient
    OrchService orchService;

    @Option(names = { "-v", "--verbose" }, description = "Verbose output")
    boolean verbose;

    @Option(names = "--profile", description = "PNC Configuration profile")
    private String profile = "default";

    @Option(names = { "-p", "--configPath" }, description = "Path to PNC configuration folder")
    private String configPath = null;

    @Option(names = "--url", description = "External URL to distgit repository", required = true)
    private String url;

    @Option(names = "--branch", description = "Branch in distgit repository")
    private String branch;

    @Option(
            names = "--repository",
            description = "Skips cloning and uses existing repository")
    private Path repository;

    @Option(
            names = "--skip-sync",
            description = "Skips any syncing and only clones the repository and performs the patching")
    private boolean skipSync;

    @Option(
            names = "--overwrite",
            description = "Overwrites existing pom. Dangerous!")
    private boolean overwrite;

    @Option(
            names = "--push",
            description = "Pushes changes to the remote repository. Will still commit")
    private boolean push;

    // TODO: Should we create an option to pass in a set of macros.

    @Override
    public void run() {
        if (verbose) {
            java.util.logging.Logger.getLogger("org.jboss.pnc.rpm").setLevel(Level.FINE);
            log.debug("Log level set to DEBUG");
        }
        if (StringUtils.isEmpty(branch)) {
            log.warn("No branch specified; unable to proceed");
            return;
        }

        if (configPath != null) {
            setConfigLocation(configPath, "flag");
        } else if (System.getenv(Constant.CONFIG_ENV) != null) {
            setConfigLocation(System.getenv(Constant.CONFIG_ENV), "environment variable");
        } else {
            setConfigLocation(Constant.DEFAULT_CONFIG_FOLDER, "constant");
        }
        PncConfig pncConfig = Config.instance().getActiveProfile().getPnc();
        Configuration pncConfiguration = PncClientHelper.getPncConfiguration();
        ReqourConfig reqourConfig = Config.instance().getActiveProfile().getReqour();
        if (reqourConfig == null) {
            log.error("""
                    Configure reqour within the Bacon config file i.e.:
                      reqour:
                         url: "https://reqour.pnc.<as other URLS...>"
                    """);
            throw new RuntimeException("No reqour configuration found.");
        }
        TranslateResponse translateResponse = reqourService.external_to_internal(
                reqourConfig.getUrl(),
                TranslateRequest.builder().externalUrl(url).build());

        RepositoryCreationResponse repositoryCreationResponse;
        String internalUrl = translateResponse.getInternalUrl();

        log.info("For external URL {} retrieved internal {}", url, internalUrl);

        if (repository == null) {
            // We search using the internal URL in case the scm repository hasn't been setup to
            // sync and doesn't have the external URL listed.
            Optional<SCMRepository> internalUrlOpt = (orchService.getAll(
                    pncConfig.getUrl(),
                    pncConfiguration.getBearerTokenSupplier().get(),
                    internalUrl)).getContent().stream().findFirst();
            log.info("Retrieved from pnc repository information: {}", internalUrlOpt.orElse(null));

            // If present, the repository is already synced to internal.
            if (!skipSync && internalUrlOpt.isEmpty()) {
                CreateAndSyncSCMRequest createAndSyncSCMRequest = CreateAndSyncSCMRequest.builder().scmUrl(url).build();
                repositoryCreationResponse = orchService
                        .createNew(
                                pncConfig.getUrl(),
                                "Bearer " + pncConfiguration.getBearerTokenSupplier().get(),
                                createAndSyncSCMRequest);
                if (repositoryCreationResponse.getTaskId() != null) {
                    log.info("Looping until sync is complete");
                    for (int i = 0; i < 5; i++) {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        if (Utils.checkForRemoteRepositoryAndBranch(internalUrl, branch)) {
                            break;
                        }
                    }
                }
            } else if (skipSync && internalUrlOpt.isEmpty()) {
                log.error("Skipping repository creation but {} is not available internally", internalUrl);
                throw new RuntimeException("Internal repository does not exist");
            }
            repository = Utils.cloneRepository(internalUrl, branch);
        } else {
            log.info("Using existing repository {}", repository);
            try (var jGit = Git.init().setDirectory(repository.toFile()).call()) {
                jGit.checkout().setName(branch).call();
            } catch (GitAPIException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            // While we have the last-mead-build value this is not reversible into a GAV. However if we call onto
            // brew we can obtain the GAV from the NVR.
            String lastMeadBuildFile = Files.readString(Paths.get(repository.toString(), "last-mead-build")).trim();
            BuildInfo lastMeadBuild = MAPPER.readValue(
                    Brew.getBuildInfo(lastMeadBuildFile),
                    BuildInfo.class);
            log.info(
                    "Found last-mead-build {} with GAV {}:{}:{}",
                    lastMeadBuildFile,
                    lastMeadBuild.getExtra().getTypeinfo().getMaven().getGroupId(),
                    lastMeadBuild.getExtra().getTypeinfo().getMaven().getArtifactId(),
                    lastMeadBuild.getExtra().getTypeinfo().getMaven().getVersion());
            String version = Utils.parseVersionReleaseSerial(repository);
            log.info("Found version: {}", version);
            // We want to ensure the artifact names are completely unique. Unlike in brew if
            // we build multiple branches they still need to be differentiated.
            // TODO: Decide on the best way to differentiate. One option ends up as
            //           org.jboss.pnc.rpm : org.apache.sshd-sshd-jb-eap-7.4-rhel-7
            //       Another (currently chosen) also uses the groupId e.g.
            //           org.jboss.pnc.rpm.org.apache.sshd : sshd-jb-eap-7.4-rhel-7
            //       Latter needs NVR -> GAV conversion
            String groupId = "org.jboss.pnc.rpm." + lastMeadBuild.getExtra().getTypeinfo().getMaven().getGroupId();
            String artifactId = lastMeadBuild.getExtra().getTypeinfo().getMaven().getArtifactId() + "-" + branch;
            log.info(
                    "Setting groupId : artifactId to comprise of scoped groupId and branch name: {}:{}",
                    groupId,
                    artifactId);
            List<SimpleArtifactRef> dependencies = getDependencies(pncConfig, pncConfiguration, lastMeadBuild);

            String source;
            try (InputStream x = App.class.getClassLoader().getResourceAsStream("pom-template.xml")) {
                assert x != null;
                source = new String(x.readAllBytes(), StandardCharsets.UTF_8);
            }

            // Replace the "template.spec" marker in the template. Easier to do via
            // string replace.
            try (Stream<Path> stream = Files.walk(repository, 1)) {
                var r = stream.filter(m -> m.toFile().getName().endsWith(".spec")).toList();
                if (r.size() > 1) {
                    log.error("Multiple spec files found: {}", r);
                } else {
                    log.info("Replacing template.spec marker with: {}", r.getFirst().toFile().getName());
                    source = source.replaceAll("template.spec", r.getFirst().toFile().getName());
                }
            }

            // Replace the Source100 marker in the template. Easier to do via string
            // replace rather than searching for the element.
            Optional<SimpleArtifactRef> projectSources = dependencies.stream()
                    .filter(a -> "project-sources".equals(a.getClassifier()))
                    .findFirst();
            if (projectSources.isPresent()) {
                String projectSourcesInjection = projectSources.get().getArtifactId() + "-" +
                        projectSources.get().getVersionString() + "-" + projectSources.get().getClassifier()
                        + "." + projectSources.get().getType();
                log.info("Injecting under Source100 marker project sources: {}", projectSourcesInjection);
                // e.g. Source100: sshd-2.14.0.redhat-00002-project-sources.tar.gz
                source = source.replaceAll(
                        "Source100:",
                        "Source100: " + projectSourcesInjection);
            } else {
                log.warn(
                        "Unable to find artifact with project-sources classifier to substitute Source100 marker in spec file.");
            }

            File target = new File(repository.toFile(), "pom.xml");
            if (target.exists() && !overwrite) {
                log.error("pom.xml already exists and not overwriting");
                return;
            }

            // Using https://github.com/maveniverse/domtrip as Maven MavenXpp3Reader/Writer
            // does not preserve comments. Another alternative would be PME POMIO but that
            // brings in quite a lot.
            Document document = Document.of(source);
            PomEditor pomEditor = new PomEditor(document);
            pomEditor.findChildElement(pomEditor.root(), NAME).textContent(Utils.parseMeadPkgName(repository));
            pomEditor.findChildElement(pomEditor.root(), GROUP_ID).textContent(groupId);
            pomEditor.findChildElement(pomEditor.root(), ARTIFACT_ID).textContent(artifactId);
            // TODO: Should we have the RPM build match the version of the wrapped build? It bears
            //     no relation so I think should be distinct.
            pomEditor.findChildElement(pomEditor.root(), VERSION).textContent("1.0.0");
            pomEditor.findChildElement(pomEditor.findChildElement(pomEditor.root(), PROPERTIES), "wrappedBuild")
                    .textContent(version);
            Element depMgmt = pomEditor.findChildElement(pomEditor.root(), DEPENDENCY_MANAGEMENT);
            Element deps = pomEditor.findChildElement(depMgmt, DEPENDENCIES);
            pomEditor.addDependency(
                    deps,
                    lastMeadBuild.getExtra().getTypeinfo().getMaven().getGroupId(),
                    lastMeadBuild.getExtra().getTypeinfo().getMaven().getArtifactId(),
                    "${wrappedBuild}");

            Element plugins = pomEditor.findChildElement(pomEditor.findChildElement(pomEditor.root(), BUILD), PLUGINS);
            // findFirst as the template only has one plugin with this artifactId
            var plugin = plugins.children()
                    .filter(
                            element -> pomEditor.findChildElement(element, "artifactId")
                                    .textContent()
                                    .equals("maven-dependency-plugin"))
                    .findFirst();
            // We know the template is a specific format so don't need to use isPresent.
            @SuppressWarnings("OptionalGetWithoutIsPresent")
            Element artifactItems = plugin.get()
                    .child("executions")
                    .get()
                    .child("execution")
                    .get()
                    .child("configuration")
                    .get()
                    .child("artifactItems")
                    .get();

            dependencies.forEach(artifactRef -> {
                Element artifactItem = pomEditor.insertMavenElement(artifactItems, "artifactItem");
                pomEditor.insertMavenElement(artifactItem, GROUP_ID, artifactRef.getGroupId());
                pomEditor.insertMavenElement(artifactItem, ARTIFACT_ID, artifactRef.getArtifactId());
                pomEditor.insertMavenElement(artifactItem, VERSION, "${wrappedBuild}");
                if (StringUtils.isNotEmpty(artifactRef.getClassifier())) {
                    pomEditor.insertMavenElement(artifactItem, CLASSIFIER, artifactRef.getClassifier());
                }
                if (StringUtils.isNotEmpty(artifactRef.getType()) && !artifactRef.getType().equals("jar")) {
                    pomEditor.insertMavenElement(artifactItem, TYPE, artifactRef.getType());
                }
            });

            Files.writeString(target.toPath(), pomEditor.toXml());

            Utils.commitAndPushRepository(repository, push);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    List<SimpleArtifactRef> getDependencies(
            PncConfig pncConfig,
            Configuration pncConfiguration,
            BuildInfo lastMeadBuild) {
        // Unfortunately, this is somewhat heavyweight. We need all the artifacts produced by this
        // build. I think this is currently only possible by retrieving the artifactId for the GAV,
        // then the artifact for that Id and finally using the buildId from the previous, retrieve all
        // built artifacts.
        var allArtifacts = orchService.getArtifactsFiltered(
                pncConfig.getUrl(),
                pncConfiguration.getBearerTokenSupplier().get(),
                String.format(
                        "%s:%s:%s:%s",
                        lastMeadBuild.getExtra().getTypeinfo().getMaven().getGroupId(),
                        lastMeadBuild.getExtra().getTypeinfo().getMaven().getArtifactId(),
                        "pom",
                        lastMeadBuild.getExtra().getTypeinfo().getMaven().getVersion()));

        var found = allArtifacts.getContent().stream().findFirst();
        if (found.isPresent()) {
            String artifactId = found.get().getId();
            log.info("Retrieved artifact {}", artifactId);
            Artifact artifact = orchService.getSpecific(
                    pncConfig.getUrl(),
                    pncConfiguration.getBearerTokenSupplier().get(),
                    artifactId);

            if (artifact.getBuild() == null) {
                // Likely an import
                log.error("Unable to find build information for artifact (Import: {})", artifact.getImportDate());
                return Collections.emptyList();
            }
            String buildId = artifact.getBuild().getId();
            log.info(
                    "For artifact {} found artifactId {} with buildId {}",
                    lastMeadBuild.getExtra().getTypeinfo().getMaven(),
                    artifactId,
                    buildId);

            Page<Artifact> artifacts = orchService.getBuiltArtifacts(
                    pncConfig.getUrl(),
                    pncConfiguration.getBearerTokenSupplier().get(),
                    buildId);
            var deps = artifacts.getContent()
                    .stream()
                    .map(c -> SimpleArtifactRef.parse(c.getIdentifier()))
                    .sorted()
                    .toList();
            log.info("Found dependencies {}", deps);
            return deps;
        } else {
            // TODO: Should this be an error? This would imply there is no existing build in PNC to be wrapped.
            log.error("Unable to find an artifact from GAV {}", lastMeadBuild.getExtra().getTypeinfo().getMaven());
        }
        return Collections.emptyList();
    }

    private void setConfigLocation(String configLocation, String source) {
        Config.configure(configLocation, Constant.CONFIG_FILE_NAME, profile);
        log.debug("Config file set from {} with profile {} to {}", source, profile, Config.getConfigFilePath());
    }
}
