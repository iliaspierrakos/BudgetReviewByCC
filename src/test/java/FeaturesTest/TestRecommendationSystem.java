package FeaturesTest;

import UserFeatures.RecommendationSystem;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestRecommendationSystem {

    @Test
    void testAvailableMinistriesNotEmpty() {
        RecommendationSystem rs = new RecommendationSystem();

        List<String> ministries = rs.getAvailableMinistries();

        assertNotNull(ministries);
        assertFalse(ministries.isEmpty());
        assertTrue(ministries.contains("Ministry of Health"));
    }

    @Test
    void testOptionsForValidMinistry() {
        RecommendationSystem rs = new RecommendationSystem();

        String[] options = rs.getOptionsForMinistry("Ministry of Health");

        assertNotNull(options);
        assertEquals(5, options.length);
    }

    @Test
    void testOptionsForInvalidMinistry() {
        RecommendationSystem rs = new RecommendationSystem();

        String[] options = rs.getOptionsForMinistry("Non Existing Ministry");

        assertNotNull(options);
        assertEquals(0, options.length);
    }

    @Test
    void testSubmitRecommendationIncreasesVotes() {
        RecommendationSystem rs = new RecommendationSystem();

        String ministry = "Ministry of Health";
        int before = rs.getTotalVotesForMinistry(ministry);

        rs.submitRecommendation(ministry, 0);

        int after = rs.getTotalVotesForMinistry(ministry);

        assertEquals(before + 1, after);
    }

    @Test
    void testSubmitRecommendationInvalidInputDoesNothing() {
        RecommendationSystem rs = new RecommendationSystem();

        int before = rs.getTotalVotesForMinistry("Ministry of Health");

        rs.submitRecommendation("Invalid Ministry", 0);
        rs.submitRecommendation("Ministry of Health", 10);

        int after = rs.getTotalVotesForMinistry("Ministry of Health");

        assertEquals(before, after);
    }

    @Test
    void testGetResultsForMinistry() {
        RecommendationSystem rs = new RecommendationSystem();

        rs.submitRecommendation("Ministry of Health", 1);
        rs.submitRecommendation("Ministry of Health", 2);

        List<String> results = rs.getResultsForMinistry("Ministry of Health");

        assertNotNull(results);
        assertEquals(5, results.size());

        // Έλεγχος ότι περιέχει "votes"
        assertTrue(results.get(0).contains("votes"));
    }

    @Test
    void testGetResultsForInvalidMinistry() {
        RecommendationSystem rs = new RecommendationSystem();

        List<String> results = rs.getResultsForMinistry("Invalid Ministry");

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
}
