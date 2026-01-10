package FeaturesTest;

import UserFeatures.MinistryProgress;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public class TestMinistryProgress {

    @Test
    void testViewMinistryProgressPrintsTableStructure() {
        // Arrange: capture System.out
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        try {
            // Act: χρησιμοποιούμε ministry που πιθανότατα δεν υπάρχει
            MinistryProgress.viewMinistryProgress("Non Existing Ministry");

            String output = outputStream.toString();

            // Assert: βασικά στοιχεία του πίνακα
            assertTrue(output.contains("BUDGET PROGRESS: Non Existing Ministry"));
            assertTrue(output.contains("YEAR"));
            assertTrue(output.contains("BUDGET"));
            assertTrue(output.contains("="));
            assertTrue(output.contains("-"));

        } finally {
            // Restore System.out
            System.setOut(originalOut);
        }
    }
}
