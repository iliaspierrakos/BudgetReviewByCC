
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
    public String toString() { // toString method
        return ministryName + "Regular Budget:" + budget;
    }
    
    public static double budgetSearchByName(String searchingMinistry) { //method for searching the regular budget of a ministry with its name
        for (Ministry m : View.ministries) {
            if (m.ministryName.equalsIgnoreCase(searchingMinistry)) {
                return m.budget;
            }
        }
        return -1 ;
    }
    public String getMinistryName(){
        return ministryName;
    }
     public double getMinistryBudget(){
        return budget;
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
}
