package UserFeatures;

import UserManagement.User;
import UserManagement.User.Role;

import java.nio.file.Path;

/**
 * Central controller class for budget-related features.
 *
 * <p>
 * This class is the GUI-compatible equivalent of the CLI ViewEditBudget menu.
 * It is responsible for:
 * </p>
 *
 * <ul>
 *   <li>Loading all required data files (budgets, ministries)</li>
 *   <li>Deciding which actions are available based on the user's role</li>
 *   <li>Exposing methods that GUI screens can safely call</li>
 * </ul>
 *
 * <p>
 * IMPORTANT:
 * This class contains NO user interaction code (no Scanner, no prints).
 * All user interaction is handled by GUI classes.
 * </p>
 */
public class ViewEditBudget {

    /** The currently logged-in user */
    private final User user;

    /**
     * Constructs the ViewEditBudget controller and initializes
     * all required data for budget operations.
     *
     * @param user the currently logged-in user
     */
    public ViewEditBudget(User user) {
        this.user = user;
        initializeData();
    }

    /**
     * Loads all required files and initializes ministries and budgets.
     *
     * <p>
     * This method replicates the initialization logic of the CLI version,
     * ensuring that all budget-related files are available before
     * any feature is used.
     * </p>
     */
    private void initializeData() {
        Ministries ministries = new Ministries();
        MinistriesBudgets budgets = new MinistriesBudgets();

        // Create yearly budget files (2020–2026)
        for (int year = 2020; year <= 2026; year++) {
            budgets.budget(Path.of("NecessaryFilesAndData/BudgetReview" + year + ".txt"));
        }

        // Initialize ministry list
        ministries.minlist();

        // Create ministry CSV files for each year
        for (int year = 2020; year <= 2026; year++) {
            CreatingMinistries.ministryCreation(
                    Path.of("NecessaryFilesAndData/MinistriesBudgets" + year + ".csv")
            );
        }
    }

    /* =====================================================
       =============== ROLE-BASED PERMISSIONS ===============
       ===================================================== */

    /**
     * Checks if the user is allowed to view government budgets.
     *
     * @return true for ALL roles
     */
    public boolean canView() {
        return true;
    }

    /**
     * Checks if the user is allowed to perform direct edits.
     *
     * @return true only for GOVERNOR
     */
    public boolean canEdit() {
        return user.getRole() == Role.GOVERNOR;
    }

    /**
     * Checks if the user is allowed to submit edit proposals.
     *
     * @return true only for MINISTRYMEMBER
     */
    public boolean canProposeEdit() {
        return user.getRole() == Role.MINISTRYMEMBER;
    }

    /**
     * Checks if the user is allowed to perform virtual edits.
     *
     * @return true only for CITIZEN
     */
    public boolean canVirtualEdit() {
        return user.getRole() == Role.CITIZEN;
    }

    /**
     * Checks if the user can view edit history.
     *
     * @return true for GOVERNOR and MINISTRYMEMBER
     */
    public boolean canViewEditHistory() {
        return user.getRole() != Role.CITIZEN;
    }

    /**
     * Checks if the user can compare ministries.
     *
     * @return true for all roles
     */
    public boolean canCompare() {
        return true;
    }

    /**
     * Checks if the user can view tax receipt visualizations.
     *
     * @return true only for CITIZEN
     */
    public boolean canViewTaxReceipt() {
        return user.getRole() == Role.CITIZEN;
    }

    /* =====================================================
       ================= FEATURE EXECUTION =================
       ===================================================== */

    /**
     * Executes the View feature for a selected year.
     *
     * @param year the year to view
     */
    public void viewBudget(int year) {
        View.viewGovBudget(year);
    }

    /**
     * Executes the Edit feature (Governor only).
     */
    public void editBudget() {
        if (!canEdit()) return;

        Edit edit = new Edit();
        edit.collectData();
    }

    /**
     * Executes the Proposal feature (Ministry Member only).
     */
    public void proposeEdit() {
        if (!canProposeEdit()) return;

        Propose propose = new Propose();
        propose.editProposal(user.getUsername());
    }

    /**
     * Executes the Virtual Edit feature (Citizen only).
     */
    public void virtualEdit() {
        if (!canVirtualEdit()) return;

        Edit edit = new Edit();
        edit.collectData();
    }

    /**
     * Displays the edit history and allows undo if applicable.
     */
    public void viewEditHistory() {
        if (!canViewEditHistory()) return;

        EditHistory.showHistory();
    }

    /**
     * Executes the Compare Ministries feature.
     */
    public void compareMinistries() {
        Compare.comparingMinistries();
    }

    /**
     * Displays the tax receipt visualization for the citizen.
     */
    public void showTaxReceipt() {
        if (!canViewTaxReceipt()) return;

        TaxReceiptVisualizer.show();
    }
}
