package FeaturesTest;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import UserFeatures.RecommendationSystem;

class TestRecommendationSystem {

    private RecommendationSystem system;

    private static final String BASE_DIR = "data/recommendation/";

    @BeforeEach
    void setUp() {
        deleteDirectory(new File(BASE_DIR));
        system = new RecommendationSystem();
    }

    @Test
    void testConstructorCreatesSystem() {
        assertNotNull(system);
    }

    @Test
    void testAvailableMinistriesNotNull() {
        List<String> ministries = system.getAvailableMinistries();
        assertNotNull(ministries);
    }

    @Test
    void testGetOptionsForInvalidMinistryReturnsEmptyArray() {
        String[] options = system.getOptionsForMinistry("InvalidMinistry");
        assertNotNull(options);
        assertEquals(0, options.length);
    }

    @Test
    void testSubmitRecommendationIncreasesVoteCount() {
        String ministry = system.getAvailableMinistries().get(0);

        system.submitRecommendation(ministry, 0);

        assertEquals(1, system.getTotalVotesForMinistry(ministry));
    }

    @Test
    void testSubmitMultipleVotes() {
        String ministry = system.getAvailableMinistries().get(0);

        system.submitRecommendation(ministry, 0);
        system.submitRecommendation(ministry, 1);
        system.submitRecommendation(ministry, 1);

        assertEquals(3, system.getTotalVotesForMinistry(ministry));
    }

    @Test
    void testSubmitRecommendationInvalidMinistryDoesNothing() {
        system.submitRecommendation("Invalid", 0);

        assertEquals(0, system.getTotalVotesForMinistry("Invalid"));
    }

    @Test
    void testSubmitRecommendationInvalidOptionDoesNothing() {
        String ministry = system.getAvailableMinistries().get(0);

        system.submitRecommendation(ministry, -1);
        system.submitRecommendation(ministry, 99);

        assertEquals(0, system.getTotalVotesForMinistry(ministry));
    }

    @Test
    void testGetResultsForMinistryInitiallyZero() {
        String ministry = system.getAvailableMinistries().get(0);

        List<String> results = system.getResultsForMinistry(ministry);

        assertNotNull(results);
        assertFalse(results.isEmpty());

        for (String line : results) {
            assertTrue(line.contains("0 votes"));
        }
    }

    @Test
    void testGetResultsForInvalidMinistryReturnsEmptyList() {
        List<String> results = system.getResultsForMinistry("Invalid");
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testVotesPersistBetweenInstances() {
        String ministry = system.getAvailableMinistries().get(0);

        system.submitRecommendation(ministry, 0);
        system.submitRecommendation(ministry, 1);

        RecommendationSystem newSystem = new RecommendationSystem();

        assertEquals(
                2,
                newSystem.getTotalVotesForMinistry(ministry),
                "Votes should persist after reload"
        );
    }

    private static void deleteDirectory(File dir) {
        if (!dir.exists()) return;

        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteDirectory(f);
                } else {
                    f.delete();
                }
            }
        }
        dir.delete();
    }
}