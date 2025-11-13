import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
public class Ministry {    //Ministry class
    private String ministryName; //Ministry name
    private double budget; //Ministry's general budget
    private static int counter; //
    public Ministry (String name, double number) { //Ministry object constructor
        this.ministryName = name;
        this.budget = number;
        counter++;
    }
    public static int getCounter() { //getCounter method used for making sure the array is made
        return counter;
    }
    @Override
    public String toString() {
        return ministryName + "Regular Budget:" + budget;
    }

    public static double budgetSearchByName(String searchingMinistry) { //method for searching the regular budget of a ministry with its name
        for (Ministry m : CreatingMinistries.ministries) {
            if (m.ministryName.equalsIgnoreCase(searchingMinistry)) {
                return m.budget;
            }
        }
        return -1 ;
    }
    
    public String getMinistryName(){
        return ministryName;
    }
    public void setMinistryName(String name){
        this.ministryName = name;
    }
    public void setBudget(double budget) {
        this.budget = budget;
    }
    public double getBudget() {
        return budget;
    }
    public static String getFormattedBudget(double budget) {
    DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.GERMAN);
    symbols.setGroupingSeparator('.');
    DecimalFormat df = new DecimalFormat("#,###", symbols);
    return df.format(budget);
    }
}
