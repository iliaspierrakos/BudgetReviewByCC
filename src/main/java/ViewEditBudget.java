import java.util.Scanner;
public class ViewEditBudget {
    public static void budgetMenu() {
        Scanner scanner = new Scanner(System.in);
        Ministries min = new Ministries();
        MinistriesBudgets budg = new MinistriesBudgets();
        budg.budget();
        min.minlist();
        do {
            System.out.println("Do you want to :");
            System.out.println("1.View");
            System.out.println("2.Edit");
            System.out.println("3.Exit");
            int number = scanner.nextInt();
            String answer = "no";
            switch (number) {
            case 1:
                View.view(true); //Run View
                System.out.println("Available=" + Edit.balance);
                break;
            case 2:
                do {
                    View.view(false);
                    Edit obj = new Edit();
                    obj.collectData();
                    answer = scanner.nextLine();
                }while (answer.equalsIgnoreCase("yes"));
                break;
            case 3:
                System.exit(0);
                break;
            default:
                System.out.println("Invalid");
                break;
            }
        } while (true);
    }
}