package FeaturesTest;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import UserFeatures.CreatingMinistries;
import UserFeatures.Edit;
import UserFeatures.Ministry;

public class TestCreatingMinistries {

    @BeforeEach
    void resetState() {
        CreatingMinistries.ministries2026 = new Ministry[20];
        Edit.balance = 0;
    }

    @Test
    void testMinistryCreationFromLoadedBudgets() throws Exception {
        // Arrange: fake CSV budgets
        Path csv = Files.createTempFile("MinistriesBudgets2026", ".csv");
        Files.write(csv, List.of("Υπουργείο Υγείας 3000", "Υπουργείο Παιδείας 4000", "Υπουργείο Οικονομικών 5000"),
                StandardCharsets.UTF_8);

        // Act
        CreatingMinistries.ministryCreationFromLoadedBudgets(2026);

        // Assert
        Ministry m0 = CreatingMinistries.ministries2026[0];
        Ministry m1 = CreatingMinistries.ministries2026[1];
        Ministry m2 = CreatingMinistries.ministries2026[2];

        assertNotNull(m0);
        assertNotNull(m1);
        assertNotNull(m2);

        assertTrue(m0.getBudget() >= 0);
        assertTrue(m1.getBudget() >= 0);
        assertTrue(m2.getBudget() >= 0);
    }

    @Test
    void testLoadUserBudgetsParsesBalanceAndBudgets() throws Exception {
        // Arrange
        CreatingMinistries.ministries2026[0] = new Ministry("Ministry of Health", 1000);
        CreatingMinistries.ministries2026[1] = new Ministry("Ministry of Education", 1000);

        Path csv = Files.createTempFile("UserBudgets", ".csv");
        Files.write(csv, List.of("BALANCE,5000", "\"Ministry of Health\",2500", "\"Ministry of Education\",1500"),
                StandardCharsets.UTF_8);

        // Act
        CreatingMinistries.loadUserBudgets(csv, 2026);

        // Assert
        assertEquals(5000, Edit.balance, 0.01);
        assertEquals(2500, CreatingMinistries.ministries2026[0].getBudget(), 0.01);
        assertEquals(1500, CreatingMinistries.ministries2026[1].getBudget(), 0.01);
    }

    @Test
    void testSaveCurrentBudgetsAsOfficial() throws Exception {
        // Arrange
        CreatingMinistries.ministries2026[0] = new Ministry("Ministry of Health", 1234.5);
        CreatingMinistries.ministries2026[1] = new Ministry("Ministry of Education", 5678.9);

        Path out = Files.createTempFile("Governor_2026", ".csv");

        // Act
        CreatingMinistries.saveCurrentBudgetsAsOfficial(out, 2026);

        // Assert
        List<String> lines = Files.readAllLines(out);
        assertFalse(lines.isEmpty());
        assertTrue(lines.get(0).contains("Ministry of Health"));
        assertTrue(lines.get(1).contains("Ministry of Education"));
    }

    @Test
    void testMinistryArrayIsNotNullAfterLoading() throws Exception {
        // Arrange & Act
        CreatingMinistries.ministryCreationFromLoadedBudgets(2026);

        // Assert: όλο το array δεν έχει null
        for (int i = 0; i < CreatingMinistries.ministries2026.length; i++) {
            assertNotNull(CreatingMinistries.ministries2026[i], "Ministry at index " + i + " is null!");
        }
    }
}
