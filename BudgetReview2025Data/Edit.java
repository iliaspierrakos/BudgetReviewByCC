import java.util.Scanner;

public class Edit {
private String name; //instance variables: ministryname , the type of change, the amount of the change
private String change;
private double amount;
private Scanner scanner = new Scanner(System.in);

public void collectData() { // method for collecting user input
        System.out.println("The budget of which ministry would you like to change?");
        System.out.print("Ministry of: ");
        String tempname = scanner.nextLine();
        String tempfullname = "Ministry of " + tempname;

        System.out.println("The budget for ministry of " + tempname + " is " + Ministry.budgetSearchByName(tempfullname));// printing the fullname of the ministry and its current budget 
        System.out.println("How would you like to change this budget? Increase or Decrease?");
        String tempchange = scanner.nextLine();


        System.out.print("By how much? ");
        Double tempamount = scanner.nextDouble();
        Edit obj2 = new Edit(tempfullname,tempchange,tempamount);// creating the Edit object with the user input 
        obj2.editingbudget(obj2);
}
public Edit(String name, String change, double amount) {// edit constructor 
        this.name = name;
        this.amount = amount;
        this.change = change;
}
public Edit() {}; //default constructor useless

public void editingbudget( Edit object) {// editing budget method
        for (int i = 0; i < View.ministries.length; i++) {// loop used for searching the ministry's name 
            if (View.ministries[i] != null && View.ministries[i].getMinistryName().equalsIgnoreCase(object.name)) {
                // found the correct ministry
                double newBudget;
                if (object.change.equalsIgnoreCase("Increase")) {// checking the type of change and making the proper move to the ministry's budget
                    newBudget = View.ministries[i].getBudget() + object.amount;
                } else {
                    newBudget = View.ministries[i].getBudget() - object.amount ;
                }
                View.ministries[i].setBudget(newBudget);
                System.out.println("Budget updated successfully!");
                System.out.println("New budget: " + newBudget);// printing the new result
                break;
            }
        }
    }
}