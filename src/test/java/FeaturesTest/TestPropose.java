package FeaturesTest;

import UserFeatures.Propose;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestPropose {

    private static final String BASE_DIR =
            "src/main/resources/NecessaryFilesAndData/ProposalsFromMinisters/";

    @AfterEach
    void cleanup() throws Exception {
        // Καθαρίζουμε test αρχεία για να μη συσσωρεύονται
        File dir = new File(BASE_DIR);
        if (dir.exists()) {
            for (File f : dir.listFiles()) {
                if (f.getName().startsWith("MinisterFor")) {
                    Files.deleteIfExists(f.toPath());
                }
            }
        }
    }

    @Test
    void testSubmitProposalAndReadBack() {
        Propose propose = new Propose("Ministry of Health");

        propose.submitProposal("Increase hospital funding");

        File proposalFile = propose.getProposalFile();
        assertTrue(proposalFile.exists());

        List<String> proposals = propose.getAllProposals();
        assertEquals(1, proposals.size());
        assertEquals("Increase hospital funding", proposals.get(0));
    }

    @Test
    void testMultipleProposalsAppended() {
        Propose propose = new Propose("Ministry of Education");

        propose.submitProposal("Renovate schools");
        propose.submitProposal("Hire more teachers");

        List<String> proposals = propose.getAllProposals();

        assertEquals(2, proposals.size());
        assertTrue(proposals.contains("Renovate schools"));
        assertTrue(proposals.contains("Hire more teachers"));
    }

    @Test
    void testSubmitProposalRejectsEmptyText() {
        Propose propose = new Propose("Ministry of Culture");

        assertThrows(IllegalArgumentException.class,
                () -> propose.submitProposal("   "));
    }

    @Test
    void testGetProposalFileWithoutMinistryNameFails() {
        Propose propose = new Propose();

        assertThrows(IllegalStateException.class,
                propose::getProposalFile);
    }

    @Test
    void testSafeFileNameGeneration() {
        Propose propose = new Propose("Ministry of Climate Crisis & Civil Protection");

        File file = propose.getProposalFile();

        // Δεν πρέπει να έχει κενά ή ειδικούς χαρακτήρες
        assertTrue(file.getName().matches("MinisterFor[A-Za-z0-9]+\\.txt"));
    }
}
