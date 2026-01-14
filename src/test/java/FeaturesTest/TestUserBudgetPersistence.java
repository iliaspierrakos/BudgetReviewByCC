package FeaturesTest;

import UserFeatures.Edit;
import UserFeatures.Ministry;
import UserFeatures.UserBudgetFileUtil;
import UserFeatures.UserBudgetPersistence;
import UserManagement.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestUserBudgetPersistence {

    private static final int YEAR = 2026;

    @AfterEach
    void cleanup() throws Exception {
        Path dir = Path.of("src/main/resources/NecessaryFilesAndData/UserBudgets");
        if (Files.exists(dir)) {
            Files.walk(dir).filter(Files::isRegularFile).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (Exception ignored) {
                }
            });
        }
    }

    @Test
    void testSaveUserBudgetsWritesCorrectCsv() throws Exception {
        // Arrange
        User user = new User("persistuser", "pass", User.Role.CITIZEN);

        Ministry[] ministries = new Ministry[] { new Ministry("Ministry of Health", 1500), null,
                new Ministry("Ministry of Education", 2500) };

        Edit.balance = 700;

        // Act
        UserBudgetPersistence.saveUserBudgets(user, ministries, YEAR);

        Path file = UserBudgetFileUtil.getUserBudgetFile(user, YEAR);
        assertTrue(Files.exists(file));

        List<String> lines = Files.readAllLines(file);

        // Assert
        assertFalse(lines.isEmpty());
        assertEquals("BALANCE,700.0", lines.get(0));

        assertTrue(lines.stream().anyMatch(l -> l.equals("Ministry of Health,1500.0")));

        assertTrue(lines.stream().anyMatch(l -> l.equals("Ministry of Education,2500.0")));

        // Δεν πρέπει να υπάρχει γραμμή για null ministry
        assertEquals(3, lines.size());
    }

    @Test
    void testSaveUserBudgetsDoesNotThrowOnEmptyMinistries() {
        User user = new User("emptyuser", "pass", User.Role.CITIZEN);
        Ministry[] ministries = new Ministry[0];
        Edit.balance = 0;

        assertDoesNotThrow(() -> UserBudgetPersistence.saveUserBudgets(user, ministries, YEAR));
    }
}
