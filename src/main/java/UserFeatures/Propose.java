package UserFeatures;
/** 
This is a class that creates the proposals the ministers make.
Each minister has a .txt proposal that are saved in a specific
folder where the governor will have access and take action.*/
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
public class Propose {
    Scanner s = new Scanner(System.in);
    public static double sharedBalance;
    public void editProposal(String ministryname) {// taking the user's username as a parameter to create the unique file 
            FileWriter fw = null;
            PrintWriter pw = null;
            try {
                fw = new FileWriter("NecessaryFilesAndData/ProposalsFromMinisters/MinisterOf" + ministryname + ".txt", true);
                pw = new PrintWriter(fw);
                System.out.println("Editing budget...");
                Edit proposeEdit = new Edit();
                proposeEdit.collectData(true); // calling the collectData method enables editing
                //System.out.println("Number of edits in history: " + Edit.history.editList.size());
                for (Edit e : Edit.history.editList) {
                    //System.out.println("this works");
                    pw.println(e.toString()); // calling the edit objects toString
                }
                System.out.println("Would you like to add a reasoning for the changes you made?");
                String reason = s.nextLine();
                pw.println("Reasoning for changes made:" + reason);
                pw.close();
                fw.close();

            } catch(IOException e) {}
    }
}
