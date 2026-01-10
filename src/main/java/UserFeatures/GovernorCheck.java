package UserFeatures;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

/**
 * The {@code GovernorCheck} class represents the functionality available to the
 * Governor for reviewing budget proposals submitted by ministers.
 *
 * <p>The Governor can:
 * <ul>
 *   <li>View the names of submitted proposal files</li>
 *   <li>Select and read a specific proposal</li>
 *   <li>Accept or reject the proposed budget changes</li>
 * </ul>
 *
 * <p>If a proposal is accepted, the budget changes are applied and recorded in
 * the edit history. If rejected, the corresponding proposal file is deleted
 * from the system.</p>
 */
public class GovernorCheck {

    /** Scanner used for user input through the console. */
    private final Scanner scanner = new Scanner(System.in);

    /**
     * Displays the names of all proposal files submitted by ministers.
     *
     * <p>If the folder is not empty, the Governor is prompted to select
     * a proposal to view.</p>
     */
    public void viewProposalsNames() {
        File folder = new File(
            "src/main/resources/NecessaryFilesAndData/ProposalsFromMinisters"
        );

        File[] files = folder.listFiles();

        if (files != null) {
            int counter = 0;
            for (File file : files) {
                if (file.isFile()) {
                    System.out.println(file.getName());
                    counter++;
                }
            }

            if (counter != 0) {
                System.out.println(
                    "Please select which minister's budget changes you would like to see:"
                );
                String ans = scanner.nextLine();
                viewProposal(ans);
            } else {
                System.out.println("Folder empty.");
            }
        } else {
            System.out.println("Folder not found or empty.");
        }
    }

    /**
     * Displays the contents of a selected proposal file.
     *
     * <p>After displaying the proposal, the Governor is asked whether
     * to accept or reject the changes.</p>
     *
     * @param fileName the name of the proposal file (without extension)
     */
    public void viewProposal(String fileName) {
        try {
            List<String> lines = Files.readAllLines(
                Paths.get(
                    "src/main/resources/NecessaryFilesAndData/ProposalsFromMinisters/"
                        + fileName + ".txt"
                )
            );

            for (String line : lines) {
                System.out.println(line);
            }
        } catch (Exception e) {
            // File reading failure is silently ignored
        }

        fileManagement(budgetChecking(), fileName);
    }

    /**
     * Prompts the Governor to decide whether the proposed budget changes
     * should be accepted or rejected.
     *
     * @return {@code true} if the proposal is accepted, {@code false} otherwise
     */
    public boolean budgetChecking() {
        System.out.println("Would you like to accept the changes?");
        String ans = scanner.nextLine();

        while (!(ans.equalsIgnoreCase("yes") || ans.equalsIgnoreCase("no"))) {
            System.out.println("Please type yes or no:");
            ans = scanner.nextLine();
        }

        return ans.equalsIgnoreCase("yes");
    }

    /**
     * Handles proposal file management based on the Governor's decision.
     *
     * <p>If accepted, the edits are applied and saved in the edit history.
     * If rejected, the proposal file is deleted.</p>
     *
     * @param accepted {@code true} if the proposal was accepted
     * @param fileName the name of the proposal file (without extension)
     */
    public void fileManagement(boolean accepted, String fileName) {
        if (accepted) {
            EditHistoryList history = new EditHistoryList();
            history.applyingEdits();
        } else {
            Path filePath = Paths.get(
                "src/main/resources/NecessaryFilesAndData/ProposalsFromMinisters/"
                    + fileName + ".txt"
            );

            try {
                Files.deleteIfExists(filePath);
                System.out.println("File deleted successfully.");
            } catch (Exception e) {
                System.out.println("Could not delete file.");
            }
        }
    }
}
