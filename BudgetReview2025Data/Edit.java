import java.util.Scanner;

public class Edit {
private String name;
private String change;
private double amount;
private Scanner scanner = new Scanner(System.in);

public void collectData() {
        System.out.println("The budget of which ministry would you like to change?");
        System.out.print("Ministry of: ");
        String tempname = scanner.nextLine();
        String tempfullname = "Ministry of " + tempname; 

        double budget = Ministry.budgetSearchByName(tempfullname);
        System.out.println("The budget for ministry of " + tempname + " is " + Ministry.getFormattedBudget(budget));

        //System.out.println("The budget for ministry of " + tempname + " is " + Ministry.budgetSearchByName(tempfullname));

        System.out.println("How would you like to change this budget? Increase or Decrease?");
        String tempchange = scanner.nextLine();

        System.out.print("By how much? ");
        Double tempamount = scanner.nextDouble();
        Edit obj2 = new Edit(tempfullname,tempchange,tempamount);
        obj2.editingbudget(obj2);
}
public Edit(String name, String change, double amount) {
        this.name = name;
        this.amount = amount;
        this.change = change;
}
public Edit() {};

public void editingbudget( Edit object) {
        for (int i = 0; i < View.ministries.length; i++) {
            if (View.ministries[i] != null && View.ministries[i].getMinistryName().equalsIgnoreCase(object.name)) {
                // Βρήκαμε το σωστό υπουργείο
                double newBudget;
                if (object.change.equalsIgnoreCase("Increase")) {
                    newBudget = View.ministries[i].getBudget() + object.amount;
                } else {
                    newBudget = View.ministries[i].getBudget() - object.amount ;
                }
                View.ministries[i].setBudget(newBudget);
                System.out.println("Budget updated successfully!");
                System.out.println("New budget: " + Ministry.getFormattedBudget(newBudget)); // ← ΕΔΩ
                break;
                }
        }
    }
}