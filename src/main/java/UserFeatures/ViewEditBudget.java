/**
* The ViewEditBudget class creates the necessary files for the application.
* It also creates the ministry objects and saves them in a static array,
* which is essential for the operations this application will support.
* It also prints the menu that allows users to view, edit and manage
* ministry budgets.
*/
package UserFeatures;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
public class ViewEditBudget {
    public static void budgetMenu() {
        Scanner scanner = new Scanner(System.in);
        Ministries min = new Ministries();
        MinistriesBudgets budg = new MinistriesBudgets();        
        budg.budget(Path.of("NecessaryFilesAndData/BudgetReview2026.txt"));
        budg.budget(Path.of("NecessaryFilesAndData/BudgetReview2025.txt"));
        budg.budget(Path.of("NecessaryFilesAndData/BudgetReview2024.txt"));
        budg.budget(Path.of("NecessaryFilesAndData/BudgetReview2023.txt"));
        budg.budget(Path.of("NecessaryFilesAndData/BudgetReview2022.txt"));
        budg.budget(Path.of("NecessaryFilesAndData/BudgetReview2021.txt"));
        budg.budget(Path.of("NecessaryFilesAndData/BudgetReview2020.txt"));
        min.minlist();
        CreatingMinistries.ministryCreation(Path.of("NecessaryFilesAndData/MinistriesBudgets2026.csv")); // Initializing Ministry objects
        CreatingMinistries.ministryCreation(Path.of("NecessaryFilesAndData/MinistriesBudgets2025.csv"));
        CreatingMinistries.ministryCreation(Path.of("NecessaryFilesAndData/MinistriesBudgets2024.csv"));
        CreatingMinistries.ministryCreation(Path.of("NecessaryFilesAndData/MinistriesBudgets2023.csv"));
        CreatingMinistries.ministryCreation(Path.of("NecessaryFilesAndData/MinistriesBudgets2022.csv"));
        CreatingMinistries.ministryCreation(Path.of("NecessaryFilesAndData/MinistriesBudgets2021.csv"));
        CreatingMinistries.ministryCreation(Path.of("NecessaryFilesAndData/MinistriesBudgets2020.csv"));
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
                int selectedYear = 0;
                boolean validYear = false;
                while (!validYear) {
                    System.out.println("Please select a year (2020-2026):");
                    try {
                        selectedYear = scanner.nextInt();
                        scanner.nextLine();
                        if (selectedYear >= 2020 && selectedYear <= 2026) {
                            validYear = true;
                        } else {
                            System.out.println("Invalid year. Please enter a year between 2020 and 2026.");
                        }
                    } catch (Exception e) {
                        System.out.println("Invalid input. Please enter a valid year.");
                        scanner.nextLine();
                    }
                }
                View.viewGovBudget(selectedYear);
                if (selectedYear == 2026) {
                    System.out.println("Available=" + Ministry.getFormattedBudget(Edit.balance));
                }
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
                    System.out.println("Changes made:" + EditHistoryList.editList.size());
                    System.out.println("Do you want to undo?");
                    scanner.nextLine();
                    answer = scanner.nextLine();
                    if (answer.equalsIgnoreCase("yes")) {
                    Edit.history.undo();
                    }
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