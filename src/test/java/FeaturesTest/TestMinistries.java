package FeaturesTest;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import UserFeatures.Ministries;

/**
 * Unit tests for {@link Ministries}.
 *
 * <p>
 * These tests verify that the ministries list file is correctly created
 * and populated with the expected ministry names.
 * </p>
 */
public class TestMinistries {

    private Path outputFile;

    /**
     * Ensures a clean output file before each test execution.
     */
    @BeforeEach
    void resetFile() throws IOException {
        outputFile = Path.of("src/main/resources/NecessaryFilesAndData/ministries.txt");

        Path parent = outputFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        Files.deleteIfExists(outputFile);
    }

    /**
     * Verifies that the ministries file is created successfully.
     */
    @Test
    void testMinistriesFileIsCreated() {
        new Ministries().minlist();

        assertTrue(Files.exists(outputFile),
                "ministries.txt file should be created");
    }

    /**
     * Verifies that the ministries file contains all expected ministry names
     * in the correct order.
     */
    @Test
    void testMinistriesFileContent() throws IOException {
        new Ministries().minlist();

        List<String> lines = Files.readAllLines(outputFile, StandardCharsets.UTF_8);

        assertEquals(20, lines.size(),
                "ministries.txt should contain exactly 20 ministries");

        assertEquals("Ministry of Interior", lines.get(0));
        assertEquals("Ministry of Foreign Affairs", lines.get(1));
        assertEquals("Ministry of National Defense", lines.get(2));
        assertEquals("Ministry of Health", lines.get(3));
        assertEquals("Ministry of Justice", lines.get(4));
        assertEquals("Ministry of Education, Religious Affairs, and Sports", lines.get(5));
        assertEquals("Ministry of Culture", lines.get(6));
        assertEquals("Ministry of National Economy and Finance", lines.get(7));
        assertEquals("Ministry of Rural Development and Food", lines.get(8));
        assertEquals("Ministry of Environment and Energy", lines.get(9));
        assertEquals("Ministry of Labor and Social Security", lines.get(10));
        assertEquals("Ministry of Social Cohesion and Family", lines.get(11));
        assertEquals("Ministry of Development", lines.get(12));
        assertEquals("Ministry of Infrastructure and Transport", lines.get(13));
        assertEquals("Ministry of Shipping and Island Policy", lines.get(14));
        assertEquals("Ministry of Tourism", lines.get(15));
        assertEquals("Ministry of Digital Governance", lines.get(16));
        assertEquals("Ministry of Migration and Asylum", lines.get(17));
        assertEquals("Ministry of Citizen Protection", lines.get(18));
        assertEquals("Ministry of Climate Crisis and Civil Protection", lines.get(19));
    }

    /**
     * Verifies that calling {@code minlist()} multiple times
     * overwrites the file instead of appending duplicate entries.
     */
    @Test
    void testMinistriesFileIsOverwritten() throws IOException {
        Ministries m = new Ministries();

        m.minlist();
        m.minlist(); // call twice

        List<String> lines = Files.readAllLines(outputFile, StandardCharsets.UTF_8);

        assertEquals(20, lines.size(),
                "Calling minlist() multiple times should not duplicate entries");
    }
}
