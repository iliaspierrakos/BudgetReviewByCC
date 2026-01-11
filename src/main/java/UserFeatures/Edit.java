package UserFeatures;

import java.util.Scanner;

import UserManagement.CurrentSession;
import UserManagement.User;

/**
 * Represents a single budget edit operation and provides logic for applying edits.
 *
 * <p>Key behavior:
 * <ul>
 *   <li>{@link #applyEdit(Edit, boolean, boolean)} is the single source of truth for applying/undoing edits.</li>
 *   <li>Proposal mode updates only the temporary balance (sandbox) and does not mutate real budgets.</li>
 *   <li>Edits can be serialized/deserialized for persistence inside proposal files.</li>
 * </ul>
 */
public class Edit {

    /** Target ministry name (e.g., "Ministry of Health"). */
    private String name;

    /** Change direction: "Increase" or "Decrease". */
    private String change;

    /** Amount of the change in raw numeric form. */
    private double amount;

    /** Change type: e.g. "fixed" / "percentage" (currently treated as informational). */
    private String changeType;

    private final Scanner scanner = new Scanner(System.in);

    /**
     * Shared balance value used by the application workflow.
     *
     * <p>In proposal mode, this is treated as a sandbox balance used to validate
     * increases against the currently available funds.</p>
     */
    public static double balance = 0;

    /**
     * In-memory edit history used for collecting edits before persistence.
     *
     * <p>During proposal creation, edits are accumulated here and then written to a proposal file.</p>
     */
    public static EditHistoryList history = new EditHistoryList();

    /** Default constructor. */
    public Edit() {}

    /**
     * Constructs an edit.
     *
     * @param name ministry name
     * @param change "Increase" or "Decrease"
     * @param amount positive amount
     */
    public Edit(String name, String change, double amount) {
        this.name = name;
        this.change = change;
        this.amount = amount;
    }

    /**
     * Constructs an edit with an explicit change type.
     *
     * @param name ministry name
     * @param change "Increase" or "Decrease"
     * @param amount positive amount
     * @param changeType change type (e.g. "fixed")
     */
    public Edit(String name, String change, double amount, String changeType) {
        this.name = name;
        this.change = change;
        this.amount = amount;
        this.changeType = changeType;
    }

    /**
     * Applies or undoes an edit.
     *
     * <p>Behavior:
     * <ul>
     *   <li>Balance is always updated (including proposal mode), so constraints can be enforced.</li>
     *   <li>If {@code isProposal == true}, no real budgets are mutated and no persistence occurs.</li>
     *   <li>If {@code isProposal == false}, the target ministry budget is updated and autosave may occur.</li>
     * </ul>
     *
     * @param edit the edit instance
     * @param undo if true, reverses the effect of the edit
     * @param isProposal if true, applies only to temporary proposal state (balance only)
     */
    public static void applyEdit(Edit edit, boolean undo, boolean isProposal) {
        if (edit == null) return;

        boolean increase = "Increase".equalsIgnoreCase(edit.getChange());
        double amt = edit.getAmount();

        // ---------------- BALANCE (always updated) ----------------
        if (!undo) {
            if (increase) balance -= amt;
            else balance += amt;
        } else {
            if (increase) balance += amt;
            else balance -= amt;
        }

        // Proposal mode: do not mutate real budgets/history/autosave.
        if (isProposal) return;

        // ---------------- REAL BUDGET MUTATION ----------------
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

        // Audit/log
        EditHistory.historyOfEdit(m.getMinistryName(), oldBudget, newBudget, undo ? 1 : 0);

        // Autosave if a session user exists
        User user = CurrentSession.getUser();
        if (user != null) {
            try {
                UserBudgetPersistence.saveUserBudgets(user, CreatingMinistries.ministries2026, 2026);
            } catch (Exception ignored) {}
        }
    }

    /* ===============================
       CLI workflow (kept as-is)
       =============================== */

    /** Starts the CLI data collection in normal mode. */
    public void collectData() {
        collectData(false);
    }

    /**
     * Starts the CLI data collection.
     *
     * @param isProposal if true, runs in proposal mode (no real budget mutations)
     */
    public void collectData(boolean isProposal) {
        System.out.println("*** Ministry Budget Transfer ***");

        if (Math.abs(balance) < 1e-9) {
            zerobalance(isProposal);
        } else {
            nonzerobalance(isProposal);
        }
    }

    /* ===============================
       Validation helpers (CLI)
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
        while (true) {
            if (validAnswer.equalsIgnoreCase("yes") || validAnswer.equalsIgnoreCase("no")) return validAnswer;
            System.out.println("Invalid input. Your answer must be either Yes or No.");
            validAnswer = scanner.nextLine();
        }
    }

    public double validityAmount(double budgetOrBalance) {
        while (true) {
            if (!scanner.hasNextDouble()) {
                System.out.println("Invalid input! Please enter a numeric value.");
                scanner.nextLine();
                continue;
            }

            double validAmount = scanner.nextDouble();
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
       CLI flows (kept as-is)
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

        Edit obj1 = new Edit(fromName, "Decrease", amt, "fixed");
        history.addEdit(obj1);
        Edit.applyEdit(obj1, false, isProposal);

        System.out.println("Available money for Investment : " + Ministry.getFormattedBudget(balance));

        System.out.println("Would you like to edit the budget of another ministry? ");
        String answer = validityAnswer(scanner.nextLine());

        if (answer.equalsIgnoreCase("yes")) nonzerobalance(isProposal);
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
            double amt = ch.equalsIgnoreCase("Decrease")
                    ? validityAmount(Ministry.budgetSearchByName(toName, CreatingMinistries.ministries2026))
                    : validityAmount(balance);

            Edit obj2 = new Edit(toName, ch, amt, "fixed");
            history.addEdit(obj2);
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
       Persistence helpers for proposals
       =============================== */

    /**
     * Serializes an edit into a machine-readable single line for proposal files.
     *
     * @return a line in the form: EDIT|<name>|<change>|<amount>|<changeType>
     */
    public String serialize() {
        return "EDIT|" + (name == null ? "" : name) + "|"
                + (change == null ? "" : change) + "|"
                + amount + "|"
                + (changeType == null ? "" : changeType);
    }

    /**
     * Parses a serialized edit line from a proposal file.
     *
     * @param line a line starting with "EDIT|"
     * @return an {@link Edit} instance
     * @throws IllegalArgumentException if the line is not a valid serialized edit
     */
    public static Edit parse(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 5 || !"EDIT".equals(parts[0])) {
            throw new IllegalArgumentException("Invalid edit line: " + line);
        }
        String name = parts[1];
        String change = parts[2];
        double amount = Double.parseDouble(parts[3]);
        String changeType = parts[4].isBlank() ? null : parts[4];
        return new Edit(name, change, amount, changeType);
    }

    /* ===============================
       Getters
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
