package UserFeatures;

import java.text.NumberFormat;
import java.util.Locale;
// Class for operating the View option of the menu
public class View {
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
