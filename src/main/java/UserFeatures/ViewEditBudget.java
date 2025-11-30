/**
* The ViewEditBudget class creates the necessary files for the application.
* It also creates the ministry objects and saves them in a static array,
* which is essential for the operations this application will support.
* It also prints the menu that allows users to view, edit and manage
* ministry budgets.
*/
package UserFeatures;

import java.util.Scanner;
import java.util.prefs.BackingStoreException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
public class ViewEditBudget {
    public static void budgetMenu() {
        Scanner scanner = new Scanner(System.in);
        Ministries min = new Ministries();
        MinistriesBudgets budg = new MinistriesBudgets();
        budg.budget(Path.of("NecessaryFilesAndData/BudgetReview2026.txt"));
        min.minlist();
        CreatingMinistries.ministryCreation(Path.of("NecessaryFilesAndData/MinistriesBudgets2026.csv")); // Initializing Ministry objects
        do {
            System.out.println("Do you want to :");
            System.out.println("1.View");
            System.out.println("2.Edit");
            System.out.println("3.Edit History");
            System.out.println("4.Return");
            int number = scanner.nextInt();
            String answer = "no";
            switch (number) {
            case 1:
                View.viewGovBudget();
                System.out.println("Available=" + Edit.balance);
                break;
            case 2:
                do {
                    Edit obj = new Edit();
                    obj.collectData();
                    answer = scanner.nextLine();
                }while (answer.equalsIgnoreCase("yes"));
                break;
            case 3:
                try {
                    System.out.println(Files.readString(Paths.get("NecessaryFilesAndData/edithistory.txt")));
                } catch (IOException e) {}
                break;
            case 4:
                return;
            default:
                System.out.println("Invalid");
                break;
            }
        } while (true);
    }
}