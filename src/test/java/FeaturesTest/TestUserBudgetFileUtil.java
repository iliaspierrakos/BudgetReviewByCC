package FeaturesTest;

import UserFeatures.CreatingMinistries;
import UserFeatures.Edit;
import UserFeatures.Ministry;
import UserFeatures.UserBudgetFileUtil;
import UserManagement.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestUserBudgetFileUtil {

    private static final int YEAR = 2026;

    @AfterEach
    void cleanup() throws Exception {
        Path dir = Path.of("src/main/resources/NecessaryFilesAndData/UserBudgets");
        if (Files.exists(dir)) {
            Files.walk(dir)
                    .filter(Files::isRegularFile)
                    .forEach(p -> {
                        try { Files.deleteIfExists(p); } catch (Exception ignored) {}
                    });
        }
    }

    @Test
    void testGetUserBudgetFileForCitizen() {
        User user = new User("testuser", "pass", User.Role.CITIZEN);

        Path file = UserBudgetFileUtil.getUserBudgetFile(user, YEAR);

        assertTrue(file.toString().endsWith("testuser_2026.csv"));
    }

    @Test
    void testGetUserBudgetFileForGovernor() {
        User user = new User("someone", "pass", User.Role.GOVERNOR);

        Path file = UserBudgetFileUtil.getUserBudgetFile(user, YEAR);

        assertTrue(file.toString().endsWith("Governor_2026.csv"));
    }

    @Test
    void testSaveUserBudgetCreatesFileWithCorrectContent() throws Exception {
        // Arrange
        User user = new User("budgetuser", "pass", User.Role.CITIZEN);

        CreatingMinistries.ministries2026 = new Ministry[]{
                new Ministry("Ministry of Health", 1000),
                new Ministry("Ministry of Education", 2000)
        };

        Edit.balance = 500;

        // Act
        UserBudgetFileUtil.saveUserBudget(user, YEAR);

        Path file = UserBudgetFileUtil.getUserBudgetFile(user, YEAR);
        assertTrue(Files.exists(file));

        List<String> lines = Files.readAllLines(file);

        // Assert
        assertFalse(lines.isEmpty());
        assertEquals("BALANCE,500.0", lines.get(0));

        assertTrue(lines.stream().anyMatch(l -> l.contains("Ministry of Health,1000.0")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("Ministry of Education,2000.0")));
    }
}
