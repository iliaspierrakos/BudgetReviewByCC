import java.util.Scanner;

public class DataCollection {

    private Scanner scanner = new Scanner(System.in);

    public String ministrySelection() {
        System.out.println("The budget of which ministry would you like to change?");
        System.out.println("Ministry of: ");
        String name = scanner.nextLine();
        System.out.println("The budget for ministry of " + name + " is " + Ministry.budgetSearchByName(name));
        return name;

    }

    public String upOrDown() {
        System.out.println("How would you like to change this budget? Increase or Decrease?");
        String change = scanner.nextLine();
        return change;
    }

    public double amount() {
        System.out.println("By how much?");
        double amount = scanner.nextDouble();
        return amount;
    }

}
