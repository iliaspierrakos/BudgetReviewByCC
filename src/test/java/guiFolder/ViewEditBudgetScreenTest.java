package guiFolder;

import UserFeatures.Edit;
import UserFeatures.EditHistoryList;
import UserManagement.User;
import UserManagement.UserManager;
import org.junit.jupiter.api.*;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ViewEditBudgetScreenTest {

    private static final Path BASE = Path.of("src/main/resources/NecessaryFilesAndData");
    private static final Path HISTORY = BASE.resolve("edithistory.txt");

    @BeforeEach
    void prepareFiles() throws Exception {
        Files.createDirectories(BASE);

        Files.writeString(HISTORY, "something\n");

        for (int y = 2020; y <= 2026; y++) {
            Files.writeString(BASE.resolve("view" + y + ".txt"), "budget stuff\n");
        }

        for (int y1 = 2020; y1 <= 2026; y1++) {
            for (int y2 = 2020; y2 <= 2026; y2++) {
                Path p = BASE.resolve("compare" + y1 + "with" + y2 + ".txt");
                Files.writeString(p, "compare file\n");
            }
        }

        Edit.balance = 999;
        Edit.history = new EditHistoryList();
    }

    @AfterEach
    void cleanupTestFiles() throws Exception {
        if (Files.exists(BASE)) {
            Files.walk(BASE)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(p -> {
                        try { Files.deleteIfExists(p); } catch (Exception ignored) {}
                    });
        }
    }

    @Test
    void cleanupOnLogout_clearsFiles_andResetsEditState() throws Exception {
        ViewEditBudgetScreen screen = new ViewEditBudgetScreen(dummyUser(), (UserManager) null);

        Method m = ViewEditBudgetScreen.class.getDeclaredMethod("cleanupOnLogout");
        m.setAccessible(true);
        m.invoke(screen);

        assertTrue(!Files.exists(HISTORY) || Files.size(HISTORY) == 0);

        for (int y = 2020; y <= 2026; y++) {
            Path p = BASE.resolve("view" + y + ".txt");
            assertTrue(!Files.exists(p) || Files.size(p) == 0, "view file not cleared: " + p);
        }

        for (int y1 = 2020; y1 <= 2026; y1++) {
            for (int y2 = 2020; y2 <= 2026; y2++) {
                Path p = BASE.resolve("compare" + y1 + "with" + y2 + ".txt");
                assertFalse(Files.exists(p), "compare file still exists: " + p);
            }
        }
        assertEquals(0, Edit.balance);
        assertNotNull(Edit.history);
    }

    private static User dummyUser() {
        return new User("testUser", "123456", User.Role.CITIZEN);
    }
}
