package FeaturesTest;

import UserFeatures.ClearHistory;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestClearHistory {

    @Test
    void testClearFileEmptiesExistingFile() throws Exception {
        // Arrange
        Path tempFile = Files.createTempFile("history", ".txt");
        Files.write(tempFile, List.of(
                "Line 1",
                "Line 2",
                "Line 3"
        ), StandardCharsets.UTF_8);

        assertTrue(Files.size(tempFile) > 0);

        // Act
        ClearHistory.clearFile(tempFile);

        // Assert
        assertEquals(0, Files.size(tempFile));
    }

    @Test
    void testClearFileCreatesEmptyFileIfNotExists() throws Exception {
        // Arrange
        Path tempFile = Files.createTempFile("history", ".txt");
        Files.delete(tempFile); // τώρα δεν υπάρχει

        assertFalse(Files.exists(tempFile));

        // Act
        ClearHistory.clearFile(tempFile);

        // Assert
        assertTrue(Files.exists(tempFile));
        assertEquals(0, Files.size(tempFile));
    }

    @Test
    void testClearFileDoesNotThrow() {
        // Arrange
        Path tempFile = Path.of("non_existing_file.txt");

        // Act & Assert
        assertDoesNotThrow(() -> ClearHistory.clearFile(tempFile));
    }
}
