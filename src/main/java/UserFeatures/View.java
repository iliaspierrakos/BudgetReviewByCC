package UserFeatures;

import java.text.NumberFormat;
import java.util.Locale;
// Class for operating the View option of the menu
public class View {
    public static void viewGovBudget() {
        double mbudg;
        String readable;
        String readablePercent;
        double inUseBudget = 0;
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
            System.out.println(m.getMinistryName() + ": " + readable + "$" + " , " + readablePercent + " % of total budget" );
        }
    }
}
