package FeaturesTest;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import UserFeatures.Edit;
import UserFeatures.EditHistoryList;
import UserFeatures.Propose;

public class TestPropose {

    private Propose propose;
    private File proposalFile;

    @BeforeEach
    void setUp() {
        propose = new Propose("Finance");
        proposalFile = propose.getProposalFile();

        // καθάρισμα αρχείου
        if (proposalFile.exists()) {
            proposalFile.delete();
        }

        // reset proposal state
        Edit.history = new EditHistoryList();
        Propose.sharedBalance = 1000;
    }

    @AfterEach
    void tearDown() {
        if (proposalFile.exists()) {
            proposalFile.delete();
        }
        Edit.history.clear();
    }

    @Test
    void testSubmitEditsProposalBlockCreatesFile() {
        Edit e = new Edit("Ministry of Finance", "Increase", 100, "fixed");
        Edit.history.addEdit(e);

        propose.submitEditsProposalBlock("Need more funds");

        assertTrue(proposalFile.exists(), "Proposal file should exist");
    }

    @Test
    void testProposalBlockStructureIsWritten() throws Exception {
        Edit e = new Edit("Ministry of Finance", "Decrease", 50, "fixed");
        Edit.history.addEdit(e);

        propose.submitEditsProposalBlock("Budget adjustment");

        List<String> lines = Files.readAllLines(proposalFile.toPath());

        assertTrue(lines.stream().anyMatch(l -> l.startsWith("PROPOSAL|")));
        assertTrue(lines.stream().anyMatch(l -> l.startsWith("EDIT|")));
        assertTrue(lines.stream().anyMatch(l -> l.startsWith("REASON|Budget adjustment")));
        assertTrue(lines.stream().anyMatch(l -> l.equals("ENDPROPOSAL")));
    }

    @Test
    void testSubmitEditsThrowsIfNoEdits() {
        assertThrows(IllegalStateException.class, () ->
                propose.submitEditsProposalBlock("Nothing to submit")
        );
    }

    @Test
    void testMultipleProposalBlocksAppend() throws Exception {
        Edit.history.addEdit(
                new Edit("Ministry of Finance", "Increase", 100, "fixed")
        );
        propose.submitEditsProposalBlock("First proposal");

        Edit.history.clear();
        Edit.history.addEdit(
                new Edit("Ministry of Finance", "Decrease", 50, "fixed")
        );
        propose.submitEditsProposalBlock("Second proposal");

        List<String> lines = Files.readAllLines(proposalFile.toPath());

        long proposalCount = lines.stream()
                .filter(l -> l.startsWith("PROPOSAL|"))
                .count();

        assertEquals(2, proposalCount);
    }

    @Test
    void testProposalDirectoryExists() {
        File dir = proposalFile.getParentFile();
        assertTrue(dir.exists());
        assertTrue(dir.isDirectory());
    }
}