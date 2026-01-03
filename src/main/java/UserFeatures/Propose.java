package UserFeatures;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Propose
 *
 * GUI-friendly backend (backward compatible with CLI usage).
 */
public class Propose {

    private static final String BASE_DIR =
            "src/main/resources/NecessaryFilesAndData/ProposalsFromMinisters/";

    private String ministryName;

    /* =========================
       CONSTRUCTORS
       ========================= */

    // ✔ Default constructor (for existing code)
    public Propose() {
        createDirectories();
    }

    // ✔ New constructor (for GUI usage)
    public Propose(String ministryName) {
        this.ministryName = ministryName;
        createDirectories();
    }

    /* =========================
       BACKWARD COMPATIBILITY
       ========================= */

    /**
     * Old method used by ViewEditBudget
     */
    public void editProposal(String proposalText) {
        if (ministryName == null || ministryName.isBlank()) {
            throw new IllegalStateException(
                    "Ministry name not set. Use Propose(String ministryName)."
            );
        }
        submitProposal(proposalText);
    }

    /* =========================
       GUI-FRIENDLY API
       ========================= */

    public void setMinistryName(String ministryName) {
        this.ministryName = ministryName;
    }

    public String getMinistryName() {
        return ministryName;
    }

    public void submitProposal(String proposalText) {
        if (proposalText == null || proposalText.trim().isEmpty()) {
            throw new IllegalArgumentException("Proposal text cannot be empty.");
        }
        saveProposalToFile(proposalText.trim());
    }

    public File getProposalFile() {
        return new File(BASE_DIR + "MinisterFor" + ministryName + ".txt");
    }

    public List<String> getAllProposals() {
        File file = getProposalFile();
        List<String> proposals = new ArrayList<>();

        if (!file.exists()) {
            return proposals;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isBlank()) {
                    proposals.add(line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading proposals file.", e);
        }

        return proposals;
    }

    /* =========================
       INTERNAL HELPERS
       ========================= */

    private void createDirectories() {
        File dir = new File(BASE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private void saveProposalToFile(String proposal) {
        File file = getProposalFile();

        try (PrintWriter pw = new PrintWriter(new FileWriter(file, true))) {
            pw.println(proposal);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save proposal.", e);
        }
    }
}
