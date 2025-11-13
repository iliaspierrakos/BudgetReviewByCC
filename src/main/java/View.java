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
            mbudg=m.getBudget();
            readable = df.format(mbudg); //Caution readble variable is String it is used only for readable print in View
            System.out.println(m.getMinistryName() + ": " + readable);
        }
    }
}
