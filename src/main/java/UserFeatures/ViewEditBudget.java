package UserFeatures;
/**
* The ViewEditBudget class creates the necessary files for the application.
* It also creates the ministry objects and saves them in a static array,
* which is essential for the operations this application will support.
* It also prints the menu that allows users to view, edit and manage
* ministry budgets.
*/
import UserManagement.MinistryMember;
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
            if (u.getRole() == User.Role.MINISTRYMEMBER) {
                System.out.println("Do you want to :");
                System.out.println("1.View");
                System.out.println("2.Propose Edit");
                System.out.println("3.Compare");
                System.out.println("4.Return");
            } else if (u.getRole() == User.Role.CITIZEN) {
                System.out.println("Do you want to :");
                System.out.println("1.View");
                System.out.println("2.Virtual Edit");
                System.out.println("3.Compare");
                System.out.println("4.Return");
                System.out.println("5.Submit Recommendation");
            } else if (u.getRole() == User.Role.GOVERNOR) {
                System.out.println("Do you want to :");
                System.out.println("1.View");
                System.out.println("2.Edit");
                System.out.println("3.Compare");
                System.out.println("4.Return");
                System.out.println("5.View proposals");
            }
            int number = scanner.nextInt();
            String answer = "no";
            switch (number) {
            case 1:
                int selectedYear = Compare.validityYear(0);
                View.viewGovBudget(selectedYear);
                break;
            case 2:
                if (u.getRole() == User.Role.MINISTRYMEMBER) {
                    System.out.println("Starting proposal...");
                    Propose p = new Propose();
                    MinistryMember mm = (MinistryMember) u;
                    String ministryName = mm.getMinistryName();
                    p.editProposal(ministryName);
                    Edit.balance = 0; // this is necessary so the balance's of the other roles are correct
                } else if (u.getRole() == User.Role.GOVERNOR) {
                    System.out.println("Which type of Edit do you want to make:");
                    System.out.println("1.Simple Edit");
                    System.out.println("2.Bulk Edit");
                    int choice = scanner.nextInt();
                    scanner.nextLine();
                    if (choice==1) {
                        Edit obj = new Edit();
                        obj.collectData();
                        System.out.println("Would you like to see the changes you made?");
                        String ans = scanner.nextLine();
                        if (ans.equalsIgnoreCase("yes")){
                        try {
                            System.out.println(Files.readString(Paths.get("NecessaryFilesAndData/edithistory.txt")));
                            System.out.println("Changes made:" + Edit.history.editList.size());
                            System.out.println("Do you want to undo?");
                            scanner.nextLine();
                            answer = scanner.nextLine();
                            if (answer.equalsIgnoreCase("yes")) {
                                Edit.history.undo();
                            }
                            Edit.balance = 0;
                        } catch (IOException e) {}
                            break;
                        }
                    } else if (choice==2) {
                        BulkEdit bulkEdit = new BulkEdit();
                        bulkEdit.bulkEditMenu();
                    } else {
                        System.out.println("Invalid");
                    }
                } else if (u.getRole() == User.Role.CITIZEN) {
                    System.out.println("Starting virtual editing...");
                    Edit obj = new Edit();
                    obj.collectData();
                    System.out.println("Would you like to see the changes you made?");
                    String ans = scanner.nextLine();

                    while (Edit.history.getIndex() >= 0) {
                        //System.out.println("this works");
                        Edit.history.undo();
                    }
                    if (ans.equalsIgnoreCase("yes")){
                        try {
                            System.out.println(Files.readString(Paths.get("NecessaryFilesAndData/edithistory.txt")));
                            System.out.println("Changes made:" + Edit.history.editList.size());
                            System.out.println("Do you want to undo?");
                            scanner.nextLine();
                            answer = scanner.nextLine();
                            if (answer.equalsIgnoreCase("yes")) {
                                Edit.history.undo();
                            }
                            Edit.balance = 0;
                        } catch (IOException e) {}
                            break;
                    }
                }
                break;
            case 3:
                Compare.comparingMinistries();
                break;
            case 4:
                return;
            case 5:
                if (u.getRole() == User.Role.GOVERNOR) {
                    GovernorCheck g = new GovernorCheck();
                    g.viewProposalsNames();
                    break;
                } else if (u.getRole() == User.Role.CITIZEN) {
                    RecommendationSystem rs = new RecommendationSystem();
                    rs.castRecommendation();
                    break;
                } else {
                    System.out.println("Invalid");
                    break;
                }
            default:
                System.out.println("Invalid");
                break;
            }
        } while (true);
    }
}
