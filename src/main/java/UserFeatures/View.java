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
        NumberFormat df = NumberFormat.getNumberInstance(Locale.US);//make number readable
        df.setMaximumFractionDigits(2);
        String readable;
        for (Ministry m : CreatingMinistries.ministries) {
            System.out.println(m.getMinistryName() + ": " + Ministry.getFormattedBudget(m.getBudget()));
        }
    }
}
