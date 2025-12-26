package UserFeatures;
/**
* The ViewEditBudget class creates the necessary files for the application.
* It also creates the ministry objects and saves them in a static array,
* which is essential for the operations this application will support.
* It also prints the menu that allows users to view, edit and manage
* ministry budgets.
*/
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import UserManagement.CurrentSession;
import UserManagement.MinistryMember;
import UserManagement.User;

public class ViewEditBudget {
    public static void budgetMenu(User u) {
        CurrentSession.setUser(u);
        Path governorPath = Paths.get("NecessaryFilesAndData/Governor_2026.csv"); 
        Path userBudgetFile = UserBudgetFileUtil.getUserBudgetFile(u, 2026);
        if (u.getRole() == User.Role.GOVERNOR) {
            governorPath = userBudgetFile;
        }
        Scanner scanner = new Scanner(System.in);
        do {
            if (u.getRole() == User.Role.MINISTRYMEMBER) {
                System.out.println("Do you want to :");
                System.out.println("1.View");
                System.out.println("2.Propose Edit");
                System.out.println("3.Compare");
                System.out.println("4.See Recommendations");
                System.out.println("5.Return");
            } else if (u.getRole() == User.Role.CITIZEN) {
                System.out.println("Do you want to :");
                System.out.println("1.View");
                System.out.println("2.Virtual Edit");
                System.out.println("3.Compare");
                System.out.println("4.Submit Recommendation");
                System.out.println("5.Tax Receipt");
                System.out.println("6.Return");
            } else if (u.getRole() == User.Role.GOVERNOR) {
                System.out.println("Do you want to :");
                System.out.println("1.View");
                System.out.println("2.Edit");
                System.out.println("3.Compare");
                System.out.println("4.View proposals");
                System.out.println("5.View Statistics");
                System.out.println("6.Restart all");
                System.out.println("7.Publish draft as official budget");
                System.out.println("8.Return");
            }
            int number = scanner.nextInt();
            scanner.nextLine();
            String answer = "no";
            switch (number) {
            case 1:
                int selectedYear = Compare.validityYear(0);
                ViewGovernmentBudget v = new ViewGovernmentBudget();
                if (u.getRole() == User.Role.CITIZEN) {

                    System.out.println("Which budget would you like to view?");
                    System.out.println("1. Original government budget");
                    System.out.println("2. My edited budget");

                    int viewChoice = scanner.nextInt();
                    scanner.nextLine();

                    if (viewChoice == 2 && Files.exists(userBudgetFile)) {
                        CreatingMinistries.loadUserBudgets(userBudgetFile, selectedYear);
                        System.out.println("Showing your edited budget...");
                    } else {
                        CreatingMinistries.loadUserBudgets(governorPath, 2026);
                        System.out.println("Showing original government budget...");
                    }
                }
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
                        if (Files.exists(Paths.get("NecessaryFilesAndData/edithistory.txt"))) {
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
                        } else {
                            System.out.println("No edit history found.");
                            return;
                        }
                    } catch (IOException e) {}
                } else if (choice > 3 || choice <1) {
                    System.out.println("Invalid");
                    break;
                }
                if (u.getRole() == User.Role.MINISTRYMEMBER) {
                    if (u.getRole() == User.Role.GOVERNOR) {
                        Edit.balance = 0; // this is necessary so the balance's of the other roles are correct
                    } 
                    if (choice == 1) {
                        Propose p = new Propose();
                        MinistryMember mm = (MinistryMember) u;
                        String ministryName = mm.getMinistryName();
                        p.editProposal(ministryName);
                    } else if (choice == 2) {
                        return;// to be changed!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    }
                } else if (u.getRole() == User.Role.GOVERNOR) {

                    if (choice==1) {
                        Edit obj = new Edit();
                        obj.collectData();
                        
                    } else if (choice==2) {
                        BulkEdit bulkEdit = new BulkEdit();
                        bulkEdit.bulkEditMenu();
                    }
                } else if (u.getRole() == User.Role.CITIZEN) {
                    if (Files.exists(userBudgetFile)) {
                        System.out.println("Do you want to load your saved budget edits?");
                        System.out.println("Type 'yes' to load or 'no' to use original budgets:");
                        String loadChoice = scanner.nextLine();
                        loadChoice = Ministry.yesOrNo(loadChoice);
                        if (loadChoice.equalsIgnoreCase("yes")) {
                            CreatingMinistries.loadUserBudgets(userBudgetFile, 2026);
                            System.out.println("Your saved budget has been loaded.");
                        } else {
                            System.out.println("Using original government budgets.");
                            // Ensure original budgets are restored
                            CreatingMinistries.loadUserBudgets(governorPath, 2026);
                        }
                    } else {
                        System.out.println("No saved edits found. Using original government budgets.");
                        CreatingMinistries.loadUserBudgets(governorPath, 2026);
                    }
                    if (choice == 1) {
                        Edit obj = new Edit();
                        obj.collectData();
                    } else if (choice == 2 ) {
                        BulkEdit bulkEdit = new BulkEdit();
                        bulkEdit.bulkEditMenu();
                    }
                
                }
                break;
            case 3:
                Compare.comparingMinistries();
                break;
            case 4:
                if (u.getRole() == User.Role.GOVERNOR) {
                    GovernorCheck g = new GovernorCheck();
                    g.viewProposalsNames();
                    break;
                } else if (u.getRole() == User.Role.CITIZEN) {
                    RecommendationSystem rs = new RecommendationSystem();
                    rs.castRecommendation();
                    break;
                } else if (u.getRole() == User.Role.MINISTRYMEMBER){
                    try {
                        MinistryMember mm = (MinistryMember) u;
                        String ministry_Name = mm.getMinistryName();
                        System.out.println(Files.readString(Paths.get("NecessaryFilesAndData/ProposalsFromCitizens/CitizenForMinistry of " + ministry_Name + ".txt")));//To be changed
                    } catch (IOException e) {}
                    break;
                }

            case 5:
                if (u.getRole() == User.Role.CITIZEN) {
                    TaxReceiptVisualizer receipt = new TaxReceiptVisualizer();
                    receipt.generateReceipt();
                    break;
                } else if (u.getRole() == User.Role.GOVERNOR){
                    try {
                       System.out.println(Files.readString(Paths.get("NecessaryFilesAndData/ProposalsFromCitizens/MinistryVotes.txt")));
                    } catch (IOException e) {
                    }
                    break;
                } else {
                    return;
                }
            case 6:
                if (u.getRole() == User.Role.CITIZEN ) {
                    return;
                } else if (u.getRole() == User.Role.GOVERNOR) {
                    ClearHistory.clearFile(Paths.get("NecessaryFilesAndData/ProposalsFromCitizens/VotesData.csv"));
                    ClearHistory.clearFile(Paths.get("NecessaryFilesAndData/ProposalsFromMinisters"));
                } else {
                    System.out.println("Invalid");
                    break;
                }                
                if (u.getRole() == User.Role.GOVERNOR) {

                    System.out.println("WARNING:");
                    System.out.println("This will restore the ORIGINAL 2026 government budget.");
                    System.out.println("All governor drafts and future edits will start from it.");
                    System.out.println("Type 'yes' to confirm:");

                    String confirm = scanner.nextLine();
                    confirm = Ministry.yesOrNo(confirm);

                    if (confirm.equalsIgnoreCase("yes")) {

                        Path original = Path.of("NecessaryFilesAndData/OriginalBudget/MinistriesBudgets2026_original.csv"
                        );

                        Path official = Path.of("NecessaryFilesAndData/Governor_2026.csv");

                        try {
                            Files.copy(original, official, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                            CreatingMinistries.resetGovernorToOriginal(2026);
                            System.out.println("Original 2026 budget successfully restored.");
                        } catch (IOException e) {
                            System.out.println("Failed to restore original budget.");
                        }

                    } else {
                        System.out.println("Restart cancelled.");
                    }
                    break;
                } else {
                    System.out.println("Invalid");
                    break;
                }


            case 7:                
            if (u.getRole() == User.Role.GOVERNOR) {

                System.out.println("Are you sure you want to publish your draft?");
                System.out.println("This will replace the official government budget.");
                System.out.println("Type 'yes' to confirm:");

                String confirm = scanner.nextLine();
                confirm = Ministry.yesOrNo(confirm);

                if (confirm.equalsIgnoreCase("yes")) {
                    Path officialFile =
                        Path.of("NecessaryFilesAndData/Governor_2026.csv");

                    CreatingMinistries.saveCurrentBudgetsAsOfficial(officialFile, 2026);

                    System.out.println("Draft successfully published as official budget.");
                } else {
                    System.out.println("Operation cancelled.");
                }
                break;
            } else {
                System.out.println("Invalid");
                break;
            }
            default:
                return;
            }
        } while (true);
    }
}
