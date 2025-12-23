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

            if (null != u.getRole()) switch (u.getRole()) {
                case MINISTRYMEMBER -> {
                    System.out.println("Do you want to :");
                    System.out.println("1.View");
                    System.out.println("2.Propose Edit");
                    System.out.println("3.Compare");
                    System.out.println("4.See Recommendations");
                    System.out.println("5.Return");
                }
                case CITIZEN -> {
                    System.out.println("Do you want to :");
                    System.out.println("1.View");
                    System.out.println("2.Virtual Edit");
                    System.out.println("3.Compare");
                    System.out.println("4.Submit Recommendation");
                    System.out.println("5.Tax Receipt");
                    System.out.println("6.Return");
                }
                case GOVERNOR -> {
                    System.out.println("Do you want to :");
                    System.out.println("1.View");
                    System.out.println("2.Edit");
                    System.out.println("3.Compare");
                    System.out.println("4.View proposals");
                    System.out.println("5.View Statistics");
                    System.out.println("6.Restart all");
                    System.out.println("7.Return");
                }
                default -> {
                }
            }
            int number = scanner.nextInt();
            scanner.nextLine();
            OUTER:
            OUTER_1:
            OUTER_2:
            switch (number) {
                case 1:
                    int selectedYear = Compare.validityYear(0);
                    ViewGovernmentBudget v = new ViewGovernmentBudget();
                    System.out.println("Would you like to see the budget sorted?");
                    String a = scanner.nextLine();
                    if (a.equalsIgnoreCase("yes")) {
                        v.viewGovBudget(selectedYear, true);
                    } else {
                        v.viewGovBudget(selectedYear, false);
                    }
                    break;
                case 2:
                    if (u.getRole() == User.Role.MINISTRYMEMBER) {
                        System.out.println("Starting proposal...");
                    } else if (u.getRole() == User.Role.CITIZEN) {
                        System.out.println("Starting virtual editing...");
                    }
                    System.out.println("Which type of Edit do you want to make:");
                    System.out.println("1.Simple Edit");
                    System.out.println("2.Bulk Edit");
                    System.out.println("3.Edit History");
                    int choice = scanner.nextInt();
                    scanner.nextLine();
                    if (choice == 3 ) {
                        try {
                            if (Edit.history.editList.isEmpty()) {
                                System.out.println("No changes have been made!");
                            } else {
                                System.out.println(Files.readString(Paths.get("NecessaryFilesAndData/edithistory.txt")));
                                System.out.println("Do you want to undo your last changes?");
                                String ans = scanner.nextLine();
                                ans = Ministry.yesOrNo(ans);
                                if (ans.equalsIgnoreCase("yes")) {
                                    EditHistoryList undo = new EditHistoryList();
                                    undo.reverseChanges();
                                } else {
                                    return;
                                }
                            }
                        } catch (IOException e) {}
                    } else if (choice > 3 || choice <1) {
                        System.out.println("Invalid");
                        break;
                    }
                    if (null != u.getRole()) switch (u.getRole()) {
                        case MINISTRYMEMBER -> {
                            Edit.balance = 0; // this is necessary so the balance's of the other roles are correct
                            if (choice == 1) {
                                Propose p = new Propose();
                                MinistryMember mm = (MinistryMember) u;
                                String ministryName = mm.getMinistryName();
                                p.editProposal(ministryName);
                            } else if (choice == 2) {
                                return;// to be changed!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                            }
                        }
                        case GOVERNOR -> {
                            if (choice==1) {
                                Edit obj = new Edit();
                                obj.collectData();
                            } else if (choice==2) {
                                BulkEdit bulkEdit = new BulkEdit();
                                bulkEdit.bulkEditMenu();
                            }
                        }
                        case CITIZEN -> {
                            if (choice == 1) {
                                Edit obj = new Edit();
                                obj.collectData();
                            } else if (choice == 2 ) {
                                BulkEdit bulkEdit = new BulkEdit();
                                bulkEdit.bulkEditMenu();
                            }
                        }
                        default -> {
                        }
                    }
                    break;
                case 3:
                    Compare.comparingMinistries();
                    break;
                case 4:
                    if (null != u.getRole()) {
                        switch (u.getRole()) {
                            case GOVERNOR -> {
                                GovernorCheck g = new GovernorCheck();
                                g.viewProposalsNames();
                                break OUTER;
                            }
                            case CITIZEN -> {
                                RecommendationSystem rs = new RecommendationSystem();
                                rs.castRecommendation();
                                break OUTER;
                            }
                            case MINISTRYMEMBER -> {
                                try {
                                    MinistryMember mm = (MinistryMember) u;
                                    String ministry_Name = mm.getMinistryName();
                                    System.out.println(Files.readString(Paths.get("NecessaryFilesAndData/ProposalsFromCitizens/CitizenForMinistry of " + ministry_Name + ".txt")));//To be changed
                                } catch (IOException e) {}
                                break OUTER;
                            }
                            default -> {
                            }
                        }
                    }
                case 5:
                    if (null == u.getRole()) {
                        return;
                    } else {
                        switch (u.getRole()) {
                            case CITIZEN -> {
                                TaxReceiptVisualizer receipt = new TaxReceiptVisualizer();
                                receipt.generateReceipt();
                                break OUTER_1;
                            }
                            case GOVERNOR -> {
                                try {
                                    System.out.println(Files.readString(Paths.get("NecessaryFilesAndData/ProposalsFromCitizens/MinistryVotes.txt")));
                                } catch (IOException e) { }
                                break OUTER_1;
                            }
                            default -> {
                                return;
                            }
                        }
                    }
                case 6:
                    if (null == u.getRole()) {
                        System.out.println("Invalid");
                        break OUTER_2;
                    } else {
                        switch (u.getRole()) {
                            case CITIZEN -> {
                                return;
                        }
                            case GOVERNOR -> {
                                ClearHistory.clearFile(Paths.get("NecessaryFilesAndData/ProposalsFromCitizens/VotesData.csv"));
                                ClearHistory.clearFile(Paths.get("NecessaryFilesAndData/ProposalsFromMinisters"));
                                System.out.println("Successful");
                            }
                            default -> System.out.println("Invalid");
                        }
                    }
                case 7:
                    if (u.getRole() == User.Role.GOVERNOR) {
                        return;
                    } else {
                        System.out.println("Invalid");
                    }
                default:
                    System.out.println("Invalid");
                    break;
            }
        } while (true);
    }
}