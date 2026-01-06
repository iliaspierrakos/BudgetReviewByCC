package UserFeatures;

import UserManagement.CurrentSession;
import UserManagement.User;
import java.util.Scanner;

/**
 * Edit - core logic for budget edits (CLI + GUI).
 *
 * FIXES:
 * - Single source of truth: applyEdit(...) updates BOTH balance and budgets.
 * - editingbudget(...) kept for backward compatibility and delegates to applyEdit(...).
 * - CLI flow no longer updates balance directly (prevents double counting).
 * - Proposal mode: does NOT change real budgets/balance.
 */
public class Edit {

    // instance fields (one edit)
    private String name;
    private String change;       // "Increase" / "Decrease"
    private double amount;
    private String changeType;   // "fixed" / "percentage" (or null)

    private final Scanner scanner = new Scanner(System.in);

    public static double balance = 0;
    public static EditHistoryList history = new EditHistoryList();

    /* ===============================
       Constructors
       =============================== */
    public Edit() {}

    public Edit(String name, String change, double amount) {
        this.name = name;
        this.change = change;
        this.amount = amount;
    }

    public Edit(String name, String change, double amount, String changeType) {
        this.name = name;
        this.change = change;
        this.amount = amount;
        this.changeType = changeType;
    }

    /* ===============================
       ✅ SINGLE SOURCE OF TRUTH
       =============================== */

    /**
     * Applies (or undoes) an edit.
     * - Updates balance
     * - Updates ministry budget
     * - Logs history
     * - Autosaves
     *
     * @param edit the edit to apply/undo
     * @param undo if true, reverses the edit's effect
     * @param isProposal if true, does NOTHING to real budgets/balance
     */
    public static void applyEdit(Edit edit, boolean undo, boolean isProposal) {
        if (edit == null) return;

        // Proposal mode: do not touch real data
        if (isProposal) return;

        boolean increase = "Increase".equalsIgnoreCase(edit.getChange());
        double amt = edit.getAmount();

        // ---------- BALANCE ----------
        if (!undo) {
            if (increase) balance -= amt;
            else balance += amt;
        } else {
            if (increase) balance += amt;
            else balance -= amt;
        }

        // ---------- BUDGET ----------
        Ministry m = Ministry.findByName(edit.getName(), CreatingMinistries.ministries2026);
        if (m == null) throw new IllegalArgumentException("Ministry not found: " + edit.getName());

        double oldBudget = m.getBudget();
        double newBudget;

        if (!undo) {
            newBudget = increase ? oldBudget + amt : oldBudget - amt;
        } else {
            newBudget = increase ? oldBudget - amt : oldBudget + amt;
        }

        m.setBudget(newBudget);

        // log
        EditHistory.historyOfEdit(m.getMinistryName(), oldBudget, newBudget, undo ? 1 : 0);

        // autosave (only if CurrentSession has user)
        User user = CurrentSession.getUser();
        if (user != null) {
            try {
                UserBudgetPersistence.saveUserBudgets(user, CreatingMinistries.ministries2026, 2026);
            } catch (Exception ignored) {}
        }
    }

    /* ===============================
       Main entry (CLI)
       =============================== */
    public void collectData() {
        collectData(false);
    }

    public void collectData(boolean isProposal) {
        System.out.println("*** Ministry Budget Transfer ***");

        // Safer check than balance == 0 with doubles
        if (Math.abs(balance) < 1e-9) {
            zerobalance(isProposal);
        } else {
            nonzerobalance(isProposal);
        }
    }

    /* ===============================
       BACKWARD COMPATIBILITY (GUI/old CLI)
       =============================== */

    /**
     * Old API. Kept so existing GUI/CLI calls don't break.
     * Now delegates to applyEdit(...) to keep balance & budgets consistent.
     */
    public void editingbudget(Edit object, boolean undo, boolean isProposal) {
        applyEdit(object, undo, isProposal);

        // Optional: keep your old console messages (only for CLI)
        if (!isProposal) {
            if (!undo) {
                System.out.println("Budget updated successfully!");
            } else {
                System.out.println("Undo completed.");
            }
        }
    }

    /* ===============================
       VALIDATIONS (CLI)
       =============================== */
    public String validityCheck(String name) {
        boolean minfound = false;

        do {
            for (int i = 0; i < CreatingMinistries.ministries2026.length; i++) {
                if (CreatingMinistries.ministries2026[i] != null &&
                        CreatingMinistries.ministries2026[i].getMinistryName().equalsIgnoreCase(name)) {
                    minfound = true;
                    break;
                }
            }

            if (!minfound) {
                System.out.println("Invalid name of Ministry. Please type again!");
                name = "Ministry of " + scanner.nextLine();
            }
        } while (!minfound);

        return name;
    }

    public String validityChange(String validChange) {
        while (true) {
            if (!validChange.equalsIgnoreCase("Increase") && !validChange.equalsIgnoreCase("Decrease")) {
                System.out.println("Invalid input, please type Increase or Decrease");
                validChange = scanner.nextLine();
            } else {
                break;
            }
        }
        return validChange;
    }

    public String validityAnswer(String validAnswer) {
        boolean valid = false;
        do {
            if (validAnswer.equalsIgnoreCase("yes") || validAnswer.equalsIgnoreCase("no")) {
                valid = true;
            } else {
                System.out.println("Invalid input. Your answer must be either Yes or No.");
                validAnswer = scanner.nextLine();
            }
        } while (!valid);
        return validAnswer;
    }

    public double validityAmount(double budgetOrBalance) {
        double validAmount;

        while (true) {
            if (!scanner.hasNextDouble()) {
                System.out.println("Invalid input! Please enter a numeric value.");
                scanner.nextLine();
                continue;
            }

            validAmount = scanner.nextDouble();
            scanner.nextLine();

            if (validAmount <= 0) {
                System.out.println("The amount must be a positive number. Please enter a new amount");
                continue;
            }

            if (validAmount > budgetOrBalance) {
                System.out.println("The amount " + Ministry.getFormattedBudget(validAmount)
                        + " exceeds the maximum limit of " + Ministry.getFormattedBudget(budgetOrBalance)
                        + ". Please enter a smaller amount");
                continue;
            }

            return validAmount;
        }
    }

    /* ===============================
       CLI FLOW (balance rules)
       IMPORTANT: balance is NOT manually changed here anymore.
       applyEdit(...) handles balance updates.
       =============================== */
    public void zerobalance(boolean isProposal) {

        System.out.println("You have to decrease first a ministry's budget because you do not have available money ");
        System.out.println("Which ministry's budgets do you want to decrease?");
        String fromName = "Ministry of " + scanner.nextLine();
        fromName = validityCheck(fromName);

        System.out.println("The budget of the " + fromName + " is "
                + Ministry.getFormattedBudget(Ministry.budgetSearchByName(fromName, CreatingMinistries.ministries2026)));

        System.out.println("Enter amount to decrease:");
        double amt = validityAmount(Ministry.budgetSearchByName(fromName, CreatingMinistries.ministries2026));

        System.out.println(fromName + " previous budget: "
                + Ministry.getFormattedBudget(Ministry.budgetSearchByName(fromName, CreatingMinistries.ministries2026)));

        Edit obj1 = new Edit(fromName, "Decrease", amt, "fixed");
        history.addEdit(obj1);
        // This will UPDATE balance + budgets (unless proposal)
        Edit.applyEdit(obj1, false, isProposal);

        System.out.println("Available money for Investment : " + Ministry.getFormattedBudget(balance));

        System.out.println("Would you like to edit the budget of another ministry? ");
        String answer = validityAnswer(scanner.nextLine());

        if (answer.equalsIgnoreCase("yes")) {
            nonzerobalance(isProposal);
        }
    }

    public void nonzerobalance(boolean isProposal) {

        String again;
        do {
            System.out.println("Which ministry's budgets do you want to edit?");
            String toName = "Ministry of " + scanner.nextLine();
            toName = validityCheck(toName);

            System.out.println("Available money for Investment : " + Ministry.getFormattedBudget(balance));
            System.out.println("Do you want to Increase or Decrease the budget of " + toName + "?");
            String ch = validityChange(scanner.nextLine());

            System.out.println("By how much?");
            double amt;

            if (ch.equalsIgnoreCase("Decrease")) {
                amt = validityAmount(Ministry.budgetSearchByName(toName, CreatingMinistries.ministries2026));
            } else {
                amt = validityAmount(balance);
            }

            System.out.println(toName + " previous budget: "
                    + Ministry.getFormattedBudget(Ministry.budgetSearchByName(toName, CreatingMinistries.ministries2026)));

            Edit obj2 = new Edit(toName, ch, amt, "fixed");
            history.addEdit(obj2);
            // This will UPDATE balance + budgets (unless proposal)
            Edit.applyEdit(obj2, false, isProposal);

            System.out.println("Available money for Investment : " + Ministry.getFormattedBudget(balance));

            System.out.println("Would you like to edit the budget of another ministry? ");
            again = validityAnswer(scanner.nextLine());

            if (Math.abs(balance) < 1e-9 && again.equalsIgnoreCase("yes")) {
                zerobalance(isProposal);
                return;
            }

        } while (again.equalsIgnoreCase("yes"));
    }

    /* ===============================
       Getters / toString
       =============================== */
    public String getName() { return name; }
    public String getChange() { return change; }
    public double getAmount() { return amount; }
    public String getChangeType() { return changeType; }

    @Override
    public String toString() {
        return name + " " + change + "d by " + Ministry.getFormattedBudget(amount) + " " + changeType;
    }
}
