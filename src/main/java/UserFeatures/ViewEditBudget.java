package UserFeatures;

import UserManagement.User;
import UserManagement.MinistryMember;

/**
 * ViewEditBudget
 * ----------------
 * Central logic class for budget-related features.
 * NO Scanner
 * NO menu
 * NO loops
 *
 * Called ONLY from JavaFX screens (ViewEditBudgetScreen etc.)
 */
public class ViewEditBudget {

    private static boolean initialized = false;

    /* ===============================
       INITIALIZATION (called once)
       =============================== */
    public static void ensureInitialized() {
        if (initialized) return;

        for (int year = 2020; year <= 2026; year++) {
            MinistriesBudgets.loadFromResources(year);
            CreatingMinistries.ministryCreationFromLoadedBudgets(year);
        }

        initialized = true;
    }

    /* ===============================
       VIEW (CLI case 1)
       =============================== */
    public static Ministry[] viewBudget(int year) {
        return switch (year) {
            case 2020 -> CreatingMinistries.ministries2020;
            case 2021 -> CreatingMinistries.ministries2021;
            case 2022 -> CreatingMinistries.ministries2022;
            case 2023 -> CreatingMinistries.ministries2023;
            case 2024 -> CreatingMinistries.ministries2024;
            case 2025 -> CreatingMinistries.ministries2025;
            case 2026 -> CreatingMinistries.ministries2026;
            default -> throw new IllegalArgumentException("Invalid year: " + year);
        };
    }

    /* ===============================
       EDIT / PROPOSE (CLI case 2)
       =============================== */
    public static void edit(User user, int editType) {

        // editType:
        // 1 = Simple Edit
        // 2 = Bulk Edit
        // 3 = Edit History

        if (editType < 1 || editType > 3) {
            throw new IllegalArgumentException("Invalid edit type");
        }

        if (editType == 3) {
            EditHistoryList history = new EditHistoryList();
            history.reverseChanges();
            return;
        }

        if (user.getRole() == User.Role.MINISTRYMEMBER) {
            Edit.balance = 0;

            if (editType == 1) {
                MinistryMember mm = (MinistryMember) user;
                Propose p = new Propose();
                p.editProposal(mm.getMinistryName());
            } else if (editType == 2) {
                throw new UnsupportedOperationException("Bulk Edit not supported yet for MinistryMember");
            }
        }

        else if (user.getRole() == User.Role.GOVERNOR) {
            if (editType == 1) {
                Edit e = new Edit();
                e.collectData();
            } else if (editType == 2) {
                BulkEdit b = new BulkEdit();
                b.bulkEditMenu();
            }
        }

        else if (user.getRole() == User.Role.CITIZEN) {
            if (editType == 1) {
                Edit e = new Edit();
                e.collectData();
            } else if (editType == 2) {
                BulkEdit b = new BulkEdit();
                b.bulkEditMenu();
            }
        }
    }

    /* ===============================
       COMPARE (CLI case 3)
       =============================== */
    public static void compare() {
        Compare.comparingMinistries();
    }

    /* ===============================
       RECOMMENDATIONS / PROPOSALS
       (CLI case 4)
       =============================== */
    public static void recommendations(User user) {

        if (user.getRole() == User.Role.GOVERNOR) {
            GovernorCheck g = new GovernorCheck();
            g.viewProposalsNames();
        }
        
        else if (user.getRole() == User.Role.MINISTRYMEMBER) {
            // προσωρινό – θα γίνει GUI screen
            throw new UnsupportedOperationException(
                "Viewing citizen proposals for ministry member will be GUI-based"
            );
        }
    }

    /* ===============================
       TAX RECEIPT (CLI case 5)
       =============================== */
    public static void taxReceipt(User user) {
        if (user.getRole() != User.Role.CITIZEN) {
            throw new IllegalStateException("Only citizens can view tax receipts");
        }
        TaxReceiptVisualizer receipt = new TaxReceiptVisualizer();
        receipt.generateReceipt();
    }
}
