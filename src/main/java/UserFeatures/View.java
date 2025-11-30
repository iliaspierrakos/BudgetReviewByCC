package UserFeatures;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * The {@code View} class handles the "View" option of the application's menu.
 * It provides methods for displaying data related to the budgets of the selected Ministries.
 */
public class View {

    /**
     * Displays the name and budget of every Ministry object currently stored
     * in the {@code CreatingMinistries.ministries} array.
     *
     * The budget is retrieved as a double and formatted into a readable string
     * using the static {@code getFormattedBudget} method from the {@code Ministry} class.
     *
     * Note: This method depends on the existence and accessibility of a static array
     * named {@code ministries} in a class named {@code CreatingMinistries}.
     */
    public static void viewGovBudget() {
        FileWriter fw = null;
        PrintWriter pw = null;
        double mbudg;
        String readable;
        String readablePercent;
        double inUseBudget = 0;
        try {
            fw = new FileWriter("NecessaryFilesAndData/View.txt", false);
            pw = new PrintWriter(fw);            
            for (Ministry m : CreatingMinistries.ministries) {
                mbudg=m.getBudget();
                inUseBudget += mbudg;
            }
            if (inUseBudget == 0) {
                System.out.println("Total budget is 0 — cannot calculate percentages.");
            return;
            }
            for (Ministry m : CreatingMinistries.ministries) {
                mbudg=m.getBudget();
                readable = Ministry.getFormattedBudget(mbudg); //Caution readble variable is String it is used only for readable print in View
                double percent = (mbudg / inUseBudget) * 100;
                readablePercent =  Ministry.getFormattedBudget(percent);
                pw.println(m.getMinistryName() + " : " + readable + "€" + " , " + readablePercent + " % of total budget" ) ;
                
            }
            pw.close();
            fw.close();
            System.out.println(Files.readString(Paths.get("NecessaryFilesAndData/View.txt")));
        } catch(IOException e) {}
    }

}
