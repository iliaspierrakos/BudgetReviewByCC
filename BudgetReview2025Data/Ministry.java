public class Ministry {    //Ministry class
    private String ministryName; //Ministry name 
    private String budget; //Ministry's general budget
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
    public String toString() {
        return ministryName + "Regular Budget:" + budget;
    }
}