package FeaturesTest;

import UserFeatures.*;

import org.junit.Assert;
import org.junit.Test;
import java.io.File;

public class TestPropose {

    @Test
    public void testGetProposalFileLogic() {
        Propose p = new Propose("Ministry of Education");
        File f = p.getProposalFile();
        
        String path = f.getPath();
        Assert.assertTrue("failure - filename should be cleaned", path.contains("MinistryofEducation"));
        Assert.assertTrue("failure - filename extension missing", path.endsWith(".txt"));
    }

    @Test
    public void testSubmitProposalValidation() {
        Propose p = new Propose("Health");
        
        try {
            p.submitProposal("   ");
            Assert.fail("failure - empty proposal should throw exception");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue("failure - wrong error message", e.getMessage().contains("empty"));
        }
    }

    @Test
    public void testMinistryNameHandling() {
        Propose p = new Propose();
        try {
            p.getProposalFile();
            Assert.fail("failure - should throw exception if ministry not set");
        } catch (IllegalStateException e) { }
    }
}