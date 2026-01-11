package FeaturesTest;

import UserFeatures.CreatingMinistries;
import UserFeatures.ViewEditBudgetInitializer;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class TestViewEditBudgetInitializer {

    @Test
    void testEnsureInitializedDoesNotThrow() {
        assertDoesNotThrow(ViewEditBudgetInitializer::ensureInitialized);
    }

    @Test
    void testEnsureInitializedIdempotent() {
        assertDoesNotThrow(ViewEditBudgetInitializer::ensureInitialized);
        assertDoesNotThrow(ViewEditBudgetInitializer::ensureInitialized);
        assertDoesNotThrow(ViewEditBudgetInitializer::ensureInitialized);
    }

    @Test
    void testDirectoriesAreCreated() {
        ViewEditBudgetInitializer.ensureInitialized();

        assertTrue(Files.exists(
                Path.of("src/main/resources/NecessaryFilesAndData")
        ));

        assertTrue(Files.exists(
                Path.of("src/main/resources/NecessaryFilesAndData/UserBudgets")
        ));

        assertTrue(Files.exists(
                Path.of("src/main/resources/NecessaryFilesAndData/OriginalBudget")
        ));
    }

    @Test
    void testMinistries2026Initialized() {
        ViewEditBudgetInitializer.ensureInitialized();

        assertNotNull(CreatingMinistries.ministries2026);
        assertTrue(
                CreatingMinistries.ministries2026.length > 0,
                "ministries2026 array should be initialized"
        );
    }
}
