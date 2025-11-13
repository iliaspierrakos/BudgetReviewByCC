// Class for operating the View option of the menu
public class View {
    public static void viewGovBudget() {
        for (Ministry m : CreatingMinistries.ministries) {
            System.out.println(m.getMinistryName() + ": " + m.getBudget());
        }
    }
}
