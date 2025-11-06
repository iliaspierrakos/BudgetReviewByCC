
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
<<<<<<< HEAD

=======
<<<<<<< HEAD
    
>>>>>>> 851c8d2a69ec93ff6a05bb929f01208e3ee39617
    public static double budgetSearchByName(String searchingMinistry) { //method for searching the regular budget of a ministry with its name
        for (Ministry m : View.ministries) {
            if (m.ministryName.equalsIgnoreCase(searchingMinistry)) {
                return m.budget;
            }
        }
        return -1 ;
    }
=======
>>>>>>> main
    public String getMinistryName(){
        return ministryName;
    }
    public void setMinistryName(String name){
        this.ministryName = name;
    }
    public void setBudget(double budget) {
        this.budget = budget;
    }
<<<<<<< HEAD
    public double getBudget() {
        return budget;
    }
}
=======
}
>>>>>>> main
