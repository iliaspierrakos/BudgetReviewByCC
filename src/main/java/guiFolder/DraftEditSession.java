package guiFolder;

import UserFeatures.CreatingMinistries;
import UserFeatures.Ministry;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <h1>DraftEditSession</h1>
 *
 * <p>
 * Centralized, shared in-memory session that manages <b>minister draft budget edits</b>.
 * This class is the single source of truth for all draft-related operations
 * (Simple Draft Edit, Bulk Draft Edit, Draft History, Submit Proposal).
 * </p>
 *
 * <h2>Key Properties</h2>
 * <ul>
 *   <li><b>Sandbox budgets:</b> a deep copy of the current runtime budgets.</li>
 *   <li><b>Shared draft history:</b> edits are accumulated across all draft screens.</li>
 *   <li><b>Balance constraints:</b> increases are allowed only if sufficient balance exists.</li>
 *   <li><b>No persistence:</b> this class never writes to files or official budgets.</li>
 * </ul>
 */
public final class DraftEditSession {

    /**
     * Represents one draft edit entry stored in draft history.
     *
     * <p>
     * NOTE: Some UI classes reference the field name {@code at}.
     * For compatibility, we expose both {@code at} and {@code timestamp}.
     * </p>
     */
    public static final class DraftEdit {

        /** Target ministry name (must exist in sandbox). */
        public final String ministry;

        /** Change direction: "Increase" or "Decrease". */
        public final String changeType;

        /** Absolute monetary impact (always positive). */
        public final double amount;

        /** Edit mode: "fixed" or "percent". */
        public final String mode;

        /** Timestamp when the edit was created. */
        public final LocalDateTime timestamp;

        /**
         * Compatibility alias for UI code that references {@code e.at}.
         * Same value as {@link #timestamp}.
         */
        public final LocalDateTime at;

        /**
         * Creates a draft edit record.
         *
         * @param ministry target ministry name
         * @param changeType "Increase" or "Decrease"
         * @param amount positive absolute amount
         * @param mode "fixed" or "percent"
         */
        public DraftEdit(String ministry, String changeType, double amount, String mode) {
            this.ministry = ministry;
            this.changeType = changeType;
            this.amount = amount;
            this.mode = mode;
            this.timestamp = LocalDateTime.now();
            this.at = this.timestamp; // alias for compatibility
        }
    }

    /** Indicates whether the draft session is initialized. */
    private static boolean initialized = false;

    /** Sandbox copy of ministries used during draft editing. */
    private static Ministry[] sandbox;

    /** Combined draft history shared across all draft screens. */
    private static final List<DraftEdit> history = new ArrayList<>();

    /**
     * Draft balance = available funds for increases.
     *
     * <p>
     * Semantics:
     * <ul>
     *   <li>Decrease → increases balance (creates funds)</li>
     *   <li>Increase → consumes balance</li>
     * </ul>
     * </p>
     */
    private static double draftBalance = 0;

    /** Utility class; no instances. */
    private DraftEditSession() {}

    /**
     * Checks whether the draft session is ready for use.
     *
     * @return true if sandbox exists and session is active
     */
    public static boolean isInitialized() {
        return initialized && sandbox != null;
    }

    /**
     * Returns the sandbox ministries array (draft budgets).
     *
     * @return sandbox ministries
     */
    public static Ministry[] getSandbox() {
        return sandbox;
    }

    /**
     * Returns an unmodifiable view of the draft edit history.
     *
     * @return draft history list
     */
    public static List<DraftEdit> getHistory() {
        return Collections.unmodifiableList(history);
    }

    /**
     * Returns the current available draft balance.
     *
     * @return draft balance
     */
    public static double getDraftBalance() {
        return draftBalance;
    }

    /**
     * Resets the draft session by deep-copying current budgets into sandbox and clearing history.
     *
     * <p>
     * Also redirects {@code CreatingMinistries.ministries2026} to the sandbox so that
     * existing code using {@code Ministry.budgetSearchByName(..., CreatingMinistries.ministries2026)}
     * automatically reads draft values.
     * </p>
     *
     * @param startingBalance initial available balance for increases (usually 0)
     */
    public static void resetFromCurrent(double startingBalance) {
        Ministry[] base = CreatingMinistries.ministries2026;
        sandbox = new Ministry[base.length];

        for (int i = 0; i < base.length; i++) {
            Ministry m = base[i];
            if (m != null) {
                sandbox[i] = new Ministry(m.getMinistryName(), m.getBudget());
            }
        }

        // Redirect runtime view to draft sandbox
        CreatingMinistries.ministries2026 = sandbox;

        history.clear();
        draftBalance = Math.max(0, startingBalance);
        initialized = true;
    }

    /**
     * Convenience reset using starting balance = 0.
     */
    public static void resetFromCurrentBudgets() {
        resetFromCurrent(0);
    }

    /**
     * Finds the index of a ministry inside the sandbox by name (case-insensitive).
     *
     * <p>
     * This method exists for compatibility with UI screens that expect it.
     * </p>
     *
     * @param ministryName ministry name to search
     * @return index in sandbox array, or -1 if not found / not initialized
     */
    public static int findIndexByName(String ministryName) {
        if (!isInitialized() || ministryName == null) return -1;

        for (int i = 0; i < sandbox.length; i++) {
            Ministry m = sandbox[i];
            if (m == null) continue;
            if (m.getMinistryName() != null && m.getMinistryName().equalsIgnoreCase(ministryName)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Applies a fixed draft edit to a single ministry.
     *
     * <p>Constraints enforced:</p>
     * <ul>
     *   <li>Increase requires sufficient {@link #draftBalance}.</li>
     *   <li>Decrease creates balance.</li>
     *   <li>Budget cannot become negative.</li>
     * </ul>
     *
     * @param ministryName target ministry
     * @param increase true for Increase, false for Decrease
     * @param amount positive amount
     * @return null if successful; otherwise UI-friendly error text
     */
    public static String applyFixed(String ministryName, boolean increase, double amount) {
        if (!isInitialized()) return "Draft session is not initialized.";
        if (ministryName == null || ministryName.isBlank()) return "Please select a ministry.";
        if (amount <= 0) return "Amount must be positive.";

        Ministry m = find(ministryName);
        if (m == null) return "Ministry not found in draft session.";

        //  BALANCE RULE: cannot increase unless you have enough available funds
        if (increase && draftBalance + 1e-9 < amount) {
            return "Insufficient draft balance. Decrease another ministry first.";
        }

        double oldBudget = m.getBudget();
        double newBudget = increase ? oldBudget + amount : oldBudget - amount;

        if (newBudget < 0) return "Budget cannot become negative.";

        m.setBudget(newBudget);

        history.add(new DraftEdit(
                m.getMinistryName(),
                increase ? "Increase" : "Decrease",
                amount,
                "fixed"
        ));

        // Decrease creates funds; Increase consumes funds
        draftBalance += increase ? -amount : +amount;

        return null;
    }

    /**
     * Applies a percent-based draft edit to a single ministry.
     *
     * <p>
     * The percentage is applied on the current sandbox budget. The computed delta is used
     * to update draft balance. Increases require sufficient balance.
     * </p>
     *
     * @param ministryName target ministry
     * @param increase true for Increase, false for Decrease
     * @param percent positive percentage value
     * @return null if successful; otherwise UI-friendly error message
     */
    public static String applyPercent(String ministryName, boolean increase, double percent) {
        if (!isInitialized()) return "Draft session is not initialized.";
        if (ministryName == null || ministryName.isBlank()) return "Please select a ministry.";
        if (percent <= 0) return "Percentage must be positive.";
        if (!increase && percent >= 100) return "Cannot decrease by 100% or more.";

        Ministry m = find(ministryName);
        if (m == null) return "Ministry not found in draft session.";

        double oldBudget = m.getBudget();
        double signed = increase ? percent : -percent;
        double newBudget = oldBudget * (1 + signed / 100.0);

        if (newBudget < 0) return "Budget cannot become negative.";

        double delta = newBudget - oldBudget; // positive for increase, negative for decrease
        if (Math.abs(delta) < 1e-9) return null;

        //  BALANCE RULE: increase requires enough funds for the computed delta
        if (delta > 0 && draftBalance + 1e-9 < delta) {
            return "Insufficient draft balance for this increase. Decrease another ministry first.";
        }

        m.setBudget(newBudget);

        history.add(new DraftEdit(
                m.getMinistryName(),
                delta >= 0 ? "Increase" : "Decrease",
                Math.abs(delta),
                "percent"
        ));

        // delta > 0 -> consumes balance; delta < 0 -> creates balance
        draftBalance += (delta >= 0) ? -Math.abs(delta) : +Math.abs(delta);

        return null;
    }

    /**
     * Undoes the last draft edit.
     *
     * <p>
     * The inverse operation is applied using the stored absolute amount.
     * This method assumes the sandbox has not been externally modified.
     * </p>
     *
     * @return null if successful; otherwise error message
     */
    public static String undoLast() {
        if (!isInitialized()) return "Draft session is not initialized.";
        if (history.isEmpty()) return "No draft edits to undo.";

        DraftEdit last = history.remove(history.size() - 1);
        Ministry m = find(last.ministry);
        if (m == null) return "Ministry not found during undo.";

        double current = m.getBudget();
        boolean wasIncrease = "Increase".equalsIgnoreCase(last.changeType);

        double newBudget = wasIncrease ? current - last.amount : current + last.amount;
        if (newBudget < 0) return "Undo would result in negative budget.";

        m.setBudget(newBudget);

        // Revert balance effect (inverse of apply)
        draftBalance += wasIncrease ? +last.amount : -last.amount;

        return null;
    }

    /**
     * Compatibility overload:
     * Some UI code calls {@code undoLast(double)}.
     * The parameter is ignored; the undo operation is always applied to the last edit.
     *
     * @param ignored unused legacy parameter
     * @return null if successful; otherwise error message
     */
    public static String undoLast(double ignored) {
        return undoLast();
    }

    /**
     * Finds a ministry within the sandbox by name (case-insensitive).
     *
     * @param ministryName ministry name
     * @return ministry instance or null if not found
     */
    private static Ministry find(String ministryName) {
        if (sandbox == null || ministryName == null) return null;

        for (Ministry m : sandbox) {
            if (m == null) continue;
            if (m.getMinistryName() != null && m.getMinistryName().equalsIgnoreCase(ministryName)) {
                return m;
            }
        }
        return null;
    }
}
