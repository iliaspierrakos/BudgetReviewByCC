package FeaturesTest;

import UserFeatures.CreatingMinistries;
import UserFeatures.Edit;
import UserFeatures.Ministry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestCreatingMinistries {

    @BeforeEach
    void resetState() {
        CreatingMinistries.ministries2026 = new Ministry[20];
        Edit.balance = 0;
    }

    @Test
    void testMinistryCreationFromBudgetFile() throws Exception {
        // Arrange: temp budget file (simulates MinistriesBudgets2026.csv or txt)
        Path budgetFile = Files.createTempFile("BudgetReview2026", ".txt");

        List<String> budgets = List.of(
                "Some text 1.000,50",
                "Other text 2.500",
                "Invalid line abc"
        );
        Files.write(budgetFile, budgets, StandardCharsets.UTF_8);

        // Ensure ministries.txt exists (from resources)
        Path ministriesFile =
                Path.of("src/main/resources/NecessaryFilesAndData/ministries.txt");
        assertTrue(Files.exists(ministriesFile),
                "ministries.txt must exist in resources");

        // Act
        CreatingMinistries.ministryCreation(budgetFile);

        // Assert
        Ministry m0 = CreatingMinistries.ministries2026[0];
        Ministry m1 = CreatingMinistries.ministries2026[1];

        assertNotNull(m0);
        assertNotNull(m1);

        assertTrue(m0.getBudget() > 0);
        assertTrue(m1.getBudget() > 0);
    }

    @Test
    void testMinistryCreationFromLoadedBudgets() throws Exception {
        // Arrange: fake CSV
        Path csv = Files.createTempFile("MinistriesBudgets2026", ".csv");
        Files.write(csv, List.of(
                "101 Υπουργείο Υγείας 3000",
                "102 Υπουργείο Παιδείας 4000"
        ), StandardCharsets.UTF_8);

        // Ensure ministries.txt exists
        Path ministriesFile =
                Path.of("src/main/resources/NecessaryFilesAndData/ministries.txt");
        assertTrue(Files.exists(ministriesFile));

        // Copy CSV where code expects it
        Path target =
                Path.of("src/main/resources/NecessaryFilesAndData/MinistriesBudgets2026.csv");
        Files.write(target, Files.readAllLines(csv));

        // Act
        CreatingMinistries.ministryCreationFromLoadedBudgets(2026);

        // Assert
        Ministry m = CreatingMinistries.ministries2026[0];
        assertNotNull(m);
        assertTrue(m.getBudget() >= 0);
    }

    @Test
    void testLoadUserBudgetsParsesBalanceAndBudgets() throws Exception {
        // Arrange
        CreatingMinistries.ministries2026[0] =
                new Ministry("Ministry of Health", 1000);

        Path csv = Files.createTempFile("UserBudgets", ".csv");
        Files.write(csv, List.of(
                "BALANCE,5000",
                "\"Ministry of Health\",2500"
        ), StandardCharsets.UTF_8);

        // Act
        CreatingMinistries.loadUserBudgets(csv, 2026);

        // Assert
        assertEquals(5000, Edit.balance);
        assertEquals(2500,
                CreatingMinistries.ministries2026[0].getBudget());
    }

    @Test
    void testSaveCurrentBudgetsAsOfficial() throws Exception {
        // Arrange
        CreatingMinistries.ministries2026[0] =
                new Ministry("Ministry of Health", 1234.5);

        Path out = Files.createTempFile("Governor_2026", ".csv");

        // Act
        CreatingMinistries.saveCurrentBudgetsAsOfficial(out, 2026);

        // Assert
        List<String> lines = Files.readAllLines(out);
        assertFalse(lines.isEmpty());
        assertTrue(lines.get(0).contains("Ministry of Health"));
    }
}
