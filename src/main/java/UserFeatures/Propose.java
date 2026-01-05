package UserFeatures;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class Propose {
    private final Scanner s = new Scanner(System.in);

    // proposal-only balance (δεν επηρεάζει το global app balance)
    public static double sharedBalance = 0;

    public void editProposal(String ministryname) {

        // Backup του κανονικού balance του app
        double appBalanceBackup = Edit.balance;

        // Για το proposal, δουλεύουμε με sharedBalance
        Edit.balance = sharedBalance;

        // Καλύτερο file name (χωρίς κενά/περίεργους χαρακτήρες)
        String safeName = ministryname.replaceAll("[^a-zA-Z0-9]", "");
        String path = "src/main/resources/NecessaryFilesAndData/ProposalsFromMinisters/MinisterOf" + safeName + ".txt";

        try (FileWriter fw = new FileWriter(path, false);
             PrintWriter pw = new PrintWriter(fw)) {

            System.out.println("Editing budget...");
            Edit proposeEdit = new Edit();

            // ΣΗΜΑΝΤΙΚΟ: true => proposal mode (δεν εφαρμόζει budgets)
            proposeEdit.collectData(true);

            // αποθήκευση του proposal balance (ό,τι έμεινε διαθέσιμο)
            sharedBalance = Edit.balance;

            // γράψιμο edits
            for (Edit e : Edit.history.getEditList()) {
                pw.println(e.toString());
            }

            System.out.println("Would you like to add a reasoning for the changes you made?");
            String reason = s.nextLine();
            pw.println("Reasoning for changes made: " + reason);

        } catch (IOException e) {
            System.err.println("Failed to write proposal file: " + e.getMessage());
        } finally {
            // Επαναφέρουμε το app balance όπως ήταν
            Edit.balance = appBalanceBackup;

            // Καθαρίζουμε το history για να μην ανακατεύονται proposals
            Edit.history.clear();
        }
    }
}
