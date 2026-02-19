package org.jboss.pnc.rpm.importer.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.quarkus.test.LogCollectingTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(
        value = LogCollectingTestResource.class,
        restrictToAnnotatedClass = true,
        initArgs = @ResourceArg(name = LogCollectingTestResource.LEVEL, value = "FINE"))
class UtilsTest {

    @BeforeEach
    public void clearLogs() {
        LogCollectingTestResource.current().clear();
    }

    @Test
    void testClone() {
        Path result = Utils.cloneRepository("https://github.com/project-ncl/rpm-importer.git", "main");

        List<LogRecord> logRecords = LogCollectingTestResource.current().getRecords();

        assertTrue(result.toFile().exists());
        assertTrue(new File(result.toString(), "README.md").exists());
        assertTrue(logRecords.stream().anyMatch(r -> LogCollectingTestResource.format(r).contains("Clone summary")));
    }

    @Test
    void testCommit() throws IOException {
        Path result = Utils.cloneRepository("https://github.com/project-ncl/rpm-importer.git", "main");

        File pom = new File(result.toString(), "pom.xml");
        Files.write(pom.toPath(), List.of("The first line"), StandardCharsets.UTF_8);

        Utils.commitAndPushRepository(result, false);

        List<LogRecord> logRecords = LogCollectingTestResource.current().getRecords();

        assertTrue(
                logRecords.stream()
                        .anyMatch(r -> LogCollectingTestResource.format(r).contains("Added and committed pom.xml")));
    }

    @Test
    void testEmptyCommit() throws IOException {
        Path result = Utils.cloneRepository("https://github.com/project-ncl/rpm-importer.git", "main");

        Utils.commitAndPushRepository(result, false);

        List<LogRecord> logRecords = LogCollectingTestResource.current().getRecords();

        assertTrue(
                logRecords.stream()
                        .anyMatch(r -> LogCollectingTestResource.format(r).contains("Nothing to commit")));
    }

    @Test
    void testRemoteCheck() {
        String httpUrl = "https://github.com/project-ncl/rpm-importer.git";
        String sshUrl = "ssh://git@github.com/project-ncl/rpm-importer.git";

        assertTrue(Utils.checkForRemoteRepositoryAndBranch(httpUrl, "main"));
        assertFalse(Utils.checkForRemoteRepositoryAndBranch(httpUrl, "main-INVALID"));
        assertFalse(Utils.checkForRemoteRepositoryAndBranch(sshUrl + "-INVALID", "main"));
    }

    @Test
    void testReadTemplate() throws IOException {
        String template = Utils.readTemplate();
        assertTrue(template.contains("""
                        <groupId>org.jboss.pnc</groupId>
                        <artifactId>rpm-builder-maven-plugin</artifactId>
                """));
    }

    @Test
    void testGetLatestRpmBuilderMavenPluginVersion() throws Exception {
        String version = Utils.getLatestRpmBuilderMavenPluginVersion();
        assertNotNull(version);
        assertTrue(
                version.matches("[\\d.]+"),
                "Version should be numeric (e.g. 1.5), got: " + version);
        assertFalse(version.isEmpty(), "Version should not be empty");
    }

    @Test
    void testParseNamedVersionFromVersionReleaseSerial(@TempDir Path tempDir) throws IOException {
        // Load the test resource file
        InputStream resourceStream = UtilsTest.class.getClassLoader()
                .getResourceAsStream("version-release-serial");
        assert resourceStream != null;
        String resourceContent = new String(resourceStream.readAllBytes(), StandardCharsets.UTF_8);

        // Write the resource file to the temporary directory
        Path versionReleaseSerialFile = tempDir.resolve("version-release-serial");
        Files.writeString(versionReleaseSerialFile, resourceContent);

        // Call the method under test
        String result = Utils.parseNamedVersionFromVersionReleaseSerial(tempDir);

        // Assert the result - should be the second field (index 1) from the space-separated line
        assertEquals("2.0.1.Final-redhat-00001", result);
    }

    @Test
    void testParseOriginalVersionFromVersionReleaseSerial(@TempDir Path tempDir) throws IOException {
        // Load the test resource file
        InputStream resourceStream = UtilsTest.class.getClassLoader()
                .getResourceAsStream("version-release-serial");
        assert resourceStream != null;
        String resourceContent = new String(resourceStream.readAllBytes(), StandardCharsets.UTF_8);

        Path versionReleaseSerialFile = tempDir.resolve("version-release-serial");
        Files.writeString(versionReleaseSerialFile, resourceContent);

        String result = Utils.parseOriginalVersionFromVersionReleaseSerial(tempDir);
        assertEquals("2.0.1.Final", result);

        Files.writeString(
                versionReleaseSerialFile,
                resourceContent.replace("2.0.1.Final-redhat-00001", "2.0.1.redhat-00001"));

        result = Utils.parseOriginalVersionFromVersionReleaseSerial(tempDir);

        List<LogRecord> logRecords = LogCollectingTestResource.current().getRecords();
        assertEquals("2.0.1", result);
    }
}
