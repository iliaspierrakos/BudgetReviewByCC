package FeaturesTest;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import UserFeatures.RecommendationSystem;

public class TestRecommendationSystem {

    @BeforeEach
    void initOptions() throws Exception {
        // Reflection για να γεμίσουμε το OPTIONS map
        Field optionsField = RecommendationSystem.class.getDeclaredField("OPTIONS");
        optionsField.setAccessible(true);
        Map<String, String[]> options = (Map<String, String[]>) optionsField.get(null);

        options.clear();
        options.put("Ministry of Health", new String[]{"Option 1", "Option 2", "Option 3", "Option 4", "Option 5"});
        options.put("Ministry of Education", new String[]{"Option A", "Option B", "Option C", "Option D", "Option E"});
    }

    @Test
    void testAvailableMinistriesNotEmpty() {
        RecommendationSystem rs = new RecommendationSystem();
        assertTrue(rs.getAvailableMinistries().contains("Ministry of Health"));
    }

    @Test
    void testOptionsForValidMinistry() {
        RecommendationSystem rs = new RecommendationSystem();
        assertEquals(5, rs.getOptionsForMinistry("Ministry of Health").length);
    }

    @Test
    void testOptionsForInvalidMinistry() {
        RecommendationSystem rs = new RecommendationSystem();
        assertEquals(0, rs.getOptionsForMinistry("Non Existing Ministry").length);
    }

    @Test
    void testSubmitRecommendationIncreasesVotes() {
        RecommendationSystem rs = new RecommendationSystem();
        int before = rs.getTotalVotesForMinistry("Ministry of Health");
        rs.submitRecommendation("Ministry of Health", 0);
        assertEquals(before + 1, rs.getTotalVotesForMinistry("Ministry of Health"));
    }

    @Test
    void testSubmitRecommendationInvalidInputDoesNothing() {
        RecommendationSystem rs = new RecommendationSystem();
        int before = rs.getTotalVotesForMinistry("Ministry of Health");
        rs.submitRecommendation("Invalid Ministry", 0);
        rs.submitRecommendation("Ministry of Health", 10); // εκτός bounds
        assertEquals(before, rs.getTotalVotesForMinistry("Ministry of Health"));
    }
}
