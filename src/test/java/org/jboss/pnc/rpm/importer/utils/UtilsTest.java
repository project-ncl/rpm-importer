package org.jboss.pnc.rpm.importer.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        //noinspection ResultOfMethodCallIgnored
        pom.createNewFile();

        Utils.commitAndPushRepository(result, false);

        List<LogRecord> logRecords = LogCollectingTestResource.current().getRecords();

        assertTrue(
                logRecords.stream()
                        .anyMatch(r -> LogCollectingTestResource.format(r).contains("Added and committed pom.xml")));
    }

    @Test
    void testRemoteCheck() {
        String url = "https://github.com/project-ncl/rpm-importer.git";

        assertTrue(Utils.checkForRemoteRepositoryAndBranch(url, "main"));
        assertFalse(Utils.checkForRemoteRepositoryAndBranch(url, "main-INVALID"));
        assertFalse(Utils.checkForRemoteRepositoryAndBranch(url + "-INVALID", "main"));
    }

    @Test
    void testReadTemplate() throws IOException {
        String template = Utils.readTemplate();
        assertTrue(template.contains("""
                                <groupId>org.jboss.pnc</groupId>
                                <artifactId>rpm-builder-maven-plugin</artifactId>
                """));
    }
}
