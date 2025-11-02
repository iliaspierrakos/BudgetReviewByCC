public class Ministry {    //Ministry class
    private String ministryName; //Ministry name 
    private String budget; //Ministry's regular budget
    private static int counter; //
    public Ministry (String name, String number) { //Ministry object constructor
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
    public void setministryName(String ministryName) { // getters and setters for instance variables
        this.ministryName = ministryName;
    }
    public String getministryName() {
        return ministryName;
    }
     public void setBudget(String budget) {
        this.budget = budget;
    }
    public String getBudget() {
        return budget;
    }
    public static String budgetSearchByName(String searchingMinistry) { //method for searching the regular budget of a ministry with its name 
        for (Ministry m : View.ministries) {
            if (m.ministryName.equalsIgnoreCase(searchingMinistry)) { 
                return m.budget;
            }               
        }  
        return "Ministry not found!" ;       
    }
}

