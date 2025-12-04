/**
* The ViewEditBudget class creates the necessary files for the application.
* It also creates the ministry objects and saves them in a static array,
* which is essential for the operations this application will support.
* It also prints the menu that allows users to view, edit and manage
* ministry budgets.
*/
package UserFeatures;
import UserManagement.User;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
public class ViewEditBudget {
    public static void budgetMenu(User u) {
        Scanner scanner = new Scanner(System.in);
        Ministries min = new Ministries();
        MinistriesBudgets budg = new MinistriesBudgets();
        for (int i = 2020; i <= 2026; i++) {
            budg.budget(Path.of("NecessaryFilesAndData/BudgetReview" + i + ".txt"));
        }
        min.minlist();
        for (int i = 2020; i <= 2026; i++) {
            CreatingMinistries.ministryCreation(Path.of("NecessaryFilesAndData/MinistriesBudgets" + i + ".csv"));
        }
        do {
            System.out.println("Do you want to :");
            System.out.println("1.View");
            if (u.getRole() == User.Role.MINISTRYMEMBER) {
                System.out.println("2.Propose Edit");
            } else if (u.getRole() == User.Role.CITIZEN) {
                System.out.println("2.Virtual Edit");
            } else if (u.getRole() == User.Role.GOVERNOR) {
                System.out.println("2.Edit");
            }
            System.out.println("3.Edit History");
            System.out.println("4.Compare");
            System.out.println("5.Bulk Edit");
            System.out.println("6.Return");
            int number = scanner.nextInt();
            String answer = "no";
            switch (number) {
            case 1:
                int selectedYear = Compare.validityYear();
                View.viewGovBudget(selectedYear);
                if (selectedYear == 2026) {
                    System.out.println("Available=" + Ministry.getFormattedBudget(Edit.balance));
                }
                break;
            case 2:
                if (u.getRole() == User.Role.MINISTRYMEMBER) {
                    System.out.println("Starting proposal...");
                    Propose p = new Propose();
                    p.editProposal(u.getUsername());
                } else {
                    Edit obj = new Edit();
                    obj.collectData();
                }
                break;
            case 3:
                try {
                    System.out.println(Files.readString(Paths.get("NecessaryFilesAndData/edithistory.txt")));
                    System.out.println("Changes made:" + Edit.history.editList.size());
                    System.out.println("Do you want to undo?");
                    scanner.nextLine();
                    answer = scanner.nextLine();
                    if (answer.equalsIgnoreCase("yes")) {
                    Edit.history.undo();
                    }
                } catch (IOException e) {}
                break;
            case 4:
                Compare.comparingMinistries();
                break;
            case 5:
                BulkEdit bulkEdit = new BulkEdit();
                bulkEdit.bulkEditMenu();
                break;
            case 6:
                return;
            default:
                System.out.println("Invalid");
                break;
            }
        } while (true);
    }
}