package UserFeatures;

import java.text.NumberFormat;
import java.util.Locale;

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
        double mbudg;
        String readable;
        String readablePercent;
        double inUseBudget = 0;
        for (Ministry m : CreatingMinistries.ministries) {
<<<<<<< HEAD
            System.out.println(m.getMinistryName() + ": " + Ministry.getFormattedBudget(m.getBudget()));
=======
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
            System.out.println(m.getMinistryName() + ": " + readable + "$" + " , " + readablePercent + " % of total budget" );
>>>>>>> d20a546222961f7863d0a820a8ac1bbc34b3ad4f
        }
    }
}
