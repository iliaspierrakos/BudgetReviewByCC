package FeaturesTest;

import UserFeatures.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.util.List;

public class TestRecommendationSystem {
    private RecommendationSystem recSystem;
    private final String MINISTRY_NAME = "Ministry of Health";

    @Before
    public void setup() {
        recSystem = new RecommendationSystem();
    }

    @Test
    public void testVoteSubmission() {

        int initialVotes = recSystem.getTotalVotesForMinistry(MINISTRY_NAME);

        recSystem.submitRecommendation(MINISTRY_NAME, 1);
        
        Assert.assertEquals("failure - total votes not incremented", initialVotes + 1, recSystem.getTotalVotesForMinistry(MINISTRY_NAME));
    }

    @Test
    public void testResultFormatting() {
        recSystem.submitRecommendation(MINISTRY_NAME, 0);
        recSystem.submitRecommendation(MINISTRY_NAME, 0);

        List<String> results = recSystem.getResultsForMinistry(MINISTRY_NAME);

        Assert.assertFalse("failure - results list is empty", results.isEmpty());
        Assert.assertEquals("failure - options count mismatch", 5, results.size());
    }

    @Test
    public void testInvalidVote() {
        int initialVotes = recSystem.getTotalVotesForMinistry(MINISTRY_NAME);
        

        recSystem.submitRecommendation(MINISTRY_NAME, -1);
        
        Assert.assertEquals("failure - invalid vote was counted", initialVotes, recSystem.getTotalVotesForMinistry(MINISTRY_NAME));
    }
}