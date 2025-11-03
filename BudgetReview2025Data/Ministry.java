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
}