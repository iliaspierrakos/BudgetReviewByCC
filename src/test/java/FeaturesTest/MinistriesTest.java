package FeaturesTest;

import UserFeatures.Ministries;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MinistriesTest {

    private static final String FILE_PATH =
            "src/main/resources/NecessaryFilesAndData/ministries.txt";

    @Test
    void testMinlistCreatesFileAndWritesContent() throws IOException {
        Ministries ministries = new Ministries();

        // Act
        ministries.minlist();

        File file = new File(FILE_PATH);

        // Assert: file exists
        assertTrue(file.exists(), "ministries.txt should exist");

        // Read content
        List<String> lines = Files.readAllLines(file.toPath());

        // Assert: σωστό πλήθος γραμμών
        assertEquals(20, lines.size(), "Should contain 20 ministries");

        // Assert: έλεγχος μερικού περιεχομένου
        assertEquals("Ministry of Interior", lines.get(0));
        assertTrue(lines.contains("Ministry of Health"));
        assertTrue(lines.contains("Ministry of Climate Crisis and Civil Protection"));
    }
}
