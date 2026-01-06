package UserFeatures;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Propose
 *
 * Supports both:
 * 1) CLI/legacy flow: collect edits (Edit.history) and write them + reasoning.
 * 2) GUI-friendly flow: submit plain proposal text and read proposals back.
 */
public class Propose {

    private static final String BASE_DIR =
            "src/main/resources/NecessaryFilesAndData/ProposalsFromMinisters/";

    private String ministryName;

    // proposal-only balance (does not affect global app balance permanently)
    public static double sharedBalance = 0;

    // used only for CLI legacy flow (reasoning prompt)
    private final Scanner s = new Scanner(System.in);

    /* =========================
       CONSTRUCTORS
       ========================= */

    // Default constructor (backward compatible)
    public Propose() {
        createDirectories();
    }

    // GUI usage
    public Propose(String ministryName) {
        this.ministryName = ministryName;
        createDirectories();
    }

    /* =========================
       LEGACY / CLI FLOW (edits-based)
       ========================= */

    /**
     * Legacy method: creates a proposal for a given ministry by collecting edits
     * (proposal mode), saving them to a file and storing reasoning.
     */
    public void editProposal(String ministryname) {
        createDirectories();

        // Backup the application's real balance
        double appBalanceBackup = Edit.balance;

        // For proposal mode, work on sharedBalance
        Edit.balance = sharedBalance;

        String safe = safeName(ministryname);
        File file = new File(BASE_DIR + "MinisterOf" + safe + ".txt");

        try (FileWriter fw = new FileWriter(file, false);
             PrintWriter pw = new PrintWriter(fw)) {

            System.out.println("Editing budget...");
            Edit proposeEdit = new Edit();

            // IMPORTANT: true => proposal mode
            proposeEdit.collectData(true);

            // store the proposal's remaining balance
            sharedBalance = Edit.balance;

            // write edits
            for (Edit e : Edit.history.getEditList()) {
                pw.println(e.toString());
            }

            System.out.println("Would you like to add a reasoning for the changes you made?");
            String reason = s.nextLine();
            pw.println("Reasoning for changes made: " + reason);

        } catch (IOException e) {
            System.err.println("Failed to write proposal file: " + e.getMessage());
        } finally {
            // restore real app balance
            Edit.balance = appBalanceBackup;

            // clear history so proposal edits don't mix with normal edits
            Edit.history.clear();
        }
    }

    /* =========================
       GUI-FRIENDLY API (text-based)
       ========================= */

    public void setMinistryName(String ministryName) {
        this.ministryName = ministryName;
    }

    public String getMinistryName() {
        return ministryName;
    }

    /**
     * Backward-compat method name (used by some GUI code):
     * This version accepts proposal text (not ministry name).
     */
    public void editProposal(String proposalText, boolean unused) {
        // NOTE: kept only if you need an overload; safe to remove if unused.
        submitProposal(proposalText);
    }

    public void submitProposal(String proposalText) {
        if (proposalText == null || proposalText.trim().isEmpty()) {
            throw new IllegalArgumentException("Proposal text cannot be empty.");
        }
        createDirectories();
        saveProposalToFile(proposalText.trim());
    }

    public File getProposalFile() {
        if (ministryName == null || ministryName.isBlank()) {
            throw new IllegalStateException("Ministry name not set.");
        }
        // Use safe file name consistently
        String safe = safeName(ministryName);
        return new File(BASE_DIR + "MinisterFor" + safe + ".txt");
    }

    public List<String> getAllProposals() {
        File file = getProposalFile();
        List<String> proposals = new ArrayList<>();

        if (!file.exists()) return proposals;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isBlank()) proposals.add(line);
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
        if (!dir.exists()) dir.mkdirs();
    }

    private void saveProposalToFile(String proposal) {
        File file = getProposalFile();
        try (PrintWriter pw = new PrintWriter(new FileWriter(file, true))) {
            pw.println(proposal);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save proposal.", e);
        }
    }

    private static String safeName(String name) {
        if (name == null) return "";
        // keep alphanumerics only to avoid weird filenames across OS
        return name.replaceAll("[^a-zA-Z0-9]", "");
    }
}
