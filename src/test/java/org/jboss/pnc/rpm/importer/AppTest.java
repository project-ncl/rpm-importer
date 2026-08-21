package org.jboss.pnc.rpm.importer;

import static eu.maveniverse.domtrip.maven.MavenPomElements.Elements.BUILD;
import static eu.maveniverse.domtrip.maven.MavenPomElements.Elements.PLUGINS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.LogRecord;

import org.commonjava.atlas.maven.ident.ref.SimpleArtifactRef;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jboss.pnc.bacon.auth.client.PncClientHelper;
import org.jboss.pnc.bacon.config.Config;
import org.jboss.pnc.bacon.config.ConfigProfile;
import org.jboss.pnc.bacon.config.PncConfig;
import org.jboss.pnc.mavenmanipulator.common.util.ManifestUtils;
import org.jboss.pnc.rpm.importer.model.Macros;
import org.jboss.pnc.rpm.importer.utils.Utils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.Element;
import eu.maveniverse.domtrip.maven.PomEditor;
import io.quarkus.test.LogCollectingTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.junit.QuarkusTest;

@SuppressWarnings("ResultOfMethodCallIgnored")
@QuarkusTest
@QuarkusTestResource(
        value = LogCollectingTestResource.class,
        restrictToAnnotatedClass = true,
        initArgs = @ResourceArg(name = LogCollectingTestResource.LEVEL, value = "FINE"))
class AppTest {
    @Test
    void testUpdateSpecName(@TempDir Path tempDir) throws IOException {
        File spec = new File(tempDir.toString(), "example.spec");
        spec.createNewFile();
        String source = Utils.readTemplate();

        App app = new App();
        app.repository = tempDir;
        String result = app.updateSpecName(source);

        assertTrue(result.contains("def spec = new File(\"${project.build.directory}/spec/example.spec"));
    }

    @Test
    void testAddMacros(@TempDir Path tempDir) throws IOException {
        String source = Utils.readTemplate();
        Document document = Document.of(source);
        PomEditor pomEditor = new PomEditor(document);
        Element plugins = pomEditor.findChildElement(pomEditor.findChildElement(pomEditor.root(), BUILD), PLUGINS);
        Macros macros = new Macros(Collections.singletonMap("dist", "MY-CUSTOM-MACRO"));

        App app = new App();
        app.repository = tempDir;
        app.updateMacros(pomEditor, plugins, macros);

        String result = pomEditor.toXml();
        assertTrue(result.contains("""
                          <macros>
                            <dist>MY-CUSTOM-MACRO</dist>
                          </macros>
                """));
    }

    @Test
    void testAddDistMacroAddsLeadingDot(@TempDir Path tempDir) throws IOException {
        String source = Utils.readTemplate();
        Document document = Document.of(source);
        PomEditor pomEditor = new PomEditor(document);
        Element plugins = pomEditor.findChildElement(pomEditor.findChildElement(pomEditor.root(), BUILD), PLUGINS);
        Macros macros = new Macros(Collections.singletonMap("dist", "el9eap"));

        App app = new App();
        app.repository = tempDir;
        app.updateMacros(pomEditor, plugins, macros);

        assertTrue(pomEditor.toXml().contains("<dist>.el9eap</dist>"));
    }

    @Test
    void testHandleInjectSources(@TempDir Path tempDir) throws IOException {
        File spec = new File(tempDir.toString(), "example.spec");
        spec.createNewFile();
        String source = Utils.readTemplate();

        App app = new App();
        String result = app.injectSourcesMacro(Optional.of(SimpleArtifactRef.parse("org.foo:bar:1.2")), source);
        assertTrue(result.contains("Source100: bar-${wrappedBuild}.pom"));

        result = app.injectSourcesMacro(Optional.of(SimpleArtifactRef.parse("org.foo:bar:tar.gz:1.2:sources")), source);
        assertTrue(result.contains("Source100: bar-${wrappedBuild}-sources.tar.gz"));

        result = app.injectSourcesMacro(Optional.empty(), source);
        assertFalse(result.contains("Source100:"));
    }

    @Test
    void testRepositoryExistingBranch(@TempDir Path tempDir) throws IOException, GitAPIException {
        String branch = "jb-eap-8.0-rhel-9";

        // Initialize a new Git repository
        try (Git git = Git.init().setDirectory(tempDir.toFile()).call()) {
            // Create an initial commit (required before creating branches)
            File initialFile = new File(tempDir.toFile(), ".gitkeep");
            initialFile.createNewFile();
            Files.writeString(initialFile.toPath(), "");

            git.add().addFilepattern(".gitkeep").call();
            git.commit()
                    .setMessage("Initial commit")
                    .setAuthor("Test User", "test@example.com")
                    .call();

            // Create a new branch
            git.branchCreate().setName(branch).call();

            // Checkout the new branch
            git.checkout().setName(branch).call();

            App app = spy(new App());
            app.customMacros = Collections.singletonMap("dist", "MY-CUSTOM-MACRO");
            app.repository = tempDir;
            app.branch = branch;
            app.overrideVersionOptions = new App.OverrideVersionOptions();
            app.overrideVersionOptions.gavOverride = "com.google.guava:guava-parent:1.0.redhat-1";
            app.overrideVersionOptions.originalVersionOverride = "1.0";

            // Mock getDependencies to return an empty list
            doReturn(Collections.emptyList()).when(app).getDependencies(any(), any(), any());

            //            File spec = new File(tempDir.toString(), "example.spec");
            //            spec.createNewFile();
            try (MockedStatic<Config> mockConfig = mockStatic(Config.class);
                    MockedStatic<PncClientHelper> mockPncClientHelper = mockStatic(PncClientHelper.class);
                    MockedStatic<ManifestUtils> mockManifestUtils = mockStatic(ManifestUtils.class)) {
                Config config = new Config();
                ConfigProfile profile = new ConfigProfile();
                PncConfig pncConfig = new PncConfig();
                profile.setPnc(pncConfig);
                config.setActiveProfile(profile);
                mockConfig.when(Config::instance).thenReturn(config);
                mockPncClientHelper.when(PncClientHelper::getPncConfiguration).thenReturn(null);
                mockManifestUtils.when(() -> ManifestUtils.getManifestInformation(App.class)).thenReturn("DEV-VERSION");

                app.run();
            }
            List<LogRecord> logRecords = LogCollectingTestResource.current().getRecords();
            assertTrue(
                    logRecords.stream()
                            .anyMatch(r -> LogCollectingTestResource.format(r).contains("No spec file found in")));
            assertTrue(
                    logRecords.stream()
                            .anyMatch(
                                    r -> LogCollectingTestResource.format(r)
                                            .contains(
                                                    "Setting groupId : artifactId to comprise of: com.google.guava:guava-parent-rpm-jb-eap-8-0-rhel-9")));

            Path pom = Path.of(tempDir.toString(), "pom.xml");
            assertTrue(Files.readString(pom).contains("Generated using RPM-Importer DEV-VERSION from PNC build null"));
        }
    }
}
