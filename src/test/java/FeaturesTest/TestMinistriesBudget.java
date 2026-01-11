package FeaturesTest;

import UserFeatures.MinistriesBudgets;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestMinistriesBudget {

    @Test
    void testBudgetCreatesFilteredCsvFile() throws IOException {
        // Arrange: δημιουργούμε προσωρινό input αρχείο
        Path inputFile = Files.createTempFile("BudgetReview2026", ".txt");

        List<String> inputLines = List.of(
                "101 Υπουργείο Υγείας 1000",
                "102 Υπουργείο Παιδείας 2000",
                "999 unknown ministry",
                "10X Υπουργείο Πολιτισμού 3000",
                "10 Υπουργείο Τουρισμού 4000",
                "20 Υπουργείο Δικαιοσύνης 5000"
        );

        Files.write(inputFile, inputLines, StandardCharsets.UTF_8);

        MinistriesBudgets budgets = new MinistriesBudgets();

        // Act
        budgets.budget(inputFile);

        // Output path που ΠΡΕΠΕΙ να δημιουργηθεί
        Path outputFile =
                Path.of("src/main/resources/NecessaryFilesAndData/MinistriesBudgets2026.csv");

        // Assert: υπάρχει το αρχείο
        assertTrue(Files.exists(outputFile), "Output CSV file should exist");

        List<String> outputLines = Files.readAllLines(outputFile, StandardCharsets.UTF_8);

        // Assert: κρατά μόνο γραμμές που ξεκινούν με 10 ΚΑΙ περιέχουν Υπουργείο
        assertTrue(outputLines.stream().allMatch(
                line -> line.startsWith("10") && line.contains("Υπουργείο")
        ));

        // Assert: συγκεκριμένες γραμμές υπάρχουν
        assertTrue(outputLines.contains("101 Υπουργείο Υγείας 1000"));
        assertTrue(outputLines.contains("102 Υπουργείο Παιδείας 2000"));
        assertTrue(outputLines.contains("10 Υπουργείο Τουρισμού 4000"));

        // Assert: άσχετες γραμμές δεν υπάρχουν
        assertFalse(outputLines.contains("999 Κάτι άσχετο"));
        assertFalse(outputLines.contains("20 Υπουργείο Δικαιοσύνης 5000"));
    }
}
