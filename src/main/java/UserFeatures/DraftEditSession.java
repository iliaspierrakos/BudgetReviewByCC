package UserFeatures;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DraftEditSession
 *
 * Centralized, shared in-memory session that manages minister draft budget edits. Single source of
 * truth for: - sandbox budgets - draft history - draft balance
 *
 * IMPORTANT: Does NOT reassign CreatingMinistries.ministries2026 (fix for stale references).
 */
public final class DraftEditSession {

  public static final class DraftEdit {
    public final String ministry;
    public final String changeType; // "Increase" or "Decrease"
    public final double amount; // positive
    public final String mode; // "fixed" or "percent"
    public final LocalDateTime timestamp;

    /** Compatibility alias for UI code referencing e.at */
    public final LocalDateTime at;

    public DraftEdit(String ministry, String changeType, double amount, String mode) {
      this.ministry = ministry;
      this.changeType = changeType;
      this.amount = amount;
      this.mode = mode;
      this.timestamp = LocalDateTime.now();
      this.at = this.timestamp;
    }
  }

  private static boolean initialized = false;
  private static Ministry[] sandbox;
  private static final List<DraftEdit> history = new ArrayList<>();
  private static double draftBalance = 0;

  private DraftEditSession() {}

  public static boolean isInitialized() {
    return initialized && sandbox != null;
  }

  public static Ministry[] getSandbox() {
    return sandbox;
  }

  public static List<DraftEdit> getHistory() {
    return Collections.unmodifiableList(history);
  }

  public static double getDraftBalance() {
    return draftBalance;
  }

  /**
   * Reset draft state from currently loaded "official" in-memory budgets. startingBalance typically
   * 0 (reallocation-only rule) or another chosen rule.
   *
   * Fix: DOES NOT redirect CreatingMinistries.ministries2026.
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

    history.clear();
    draftBalance = Math.max(0, startingBalance);
    initialized = true;
  }

  public static void resetFromCurrentBudgets() {
    resetFromCurrent(0);
  }

  public static int findIndexByName(String ministryName) {
    if (!isInitialized() || ministryName == null) {
      return -1;
    }
    for (int i = 0; i < sandbox.length; i++) {
      Ministry m = sandbox[i];
      if (m == null) {
        continue;
      }
      if (m.getMinistryName() != null && m.getMinistryName().equalsIgnoreCase(ministryName)) {
        return i;
      }
    }
    return -1;
  }

  public static String applyFixed(String ministryName, boolean increase, double amount) {
    if (!isInitialized()) {
      return "Draft session is not initialized.";
    }
    if (ministryName == null || ministryName.isBlank()) {
      return "Please select a ministry.";
    }
    if (amount <= 0) {
      return "Amount must be positive.";
    }
    Ministry m = find(ministryName);
    if (m == null) {
      return "Ministry not found in draft session.";
    }
    if (increase && draftBalance + 1e-9 < amount) {
      return "Insufficient draft balance. Decrease another ministry first.";
    }

    double oldBudget = m.getBudget();
    double newBudget = increase ? oldBudget + amount : oldBudget - amount;
    if (newBudget < 0) {
      return "Budget cannot become negative.";
    }
    m.setBudget(newBudget);

    history.add(
        new DraftEdit(m.getMinistryName(), increase ? "Increase" : "Decrease", amount, "fixed"));

    draftBalance += increase ? -amount : +amount;
    return null;
  }

  public static String applyPercent(String ministryName, boolean increase, double percent) {
    if (!isInitialized()) {
      return "Draft session is not initialized.";
    }
    if (ministryName == null || ministryName.isBlank()) {
      return "Please select a ministry.";
    }
    if (percent <= 0) {
      return "Percentage must be positive.";
    }
    if (!increase && percent >= 100) {
      return "Cannot decrease by 100% or more.";
    }

    Ministry m = find(ministryName);
    if (m == null) {
      return "Ministry not found in draft session.";
    }
    double oldBudget = m.getBudget();
    double signed = increase ? percent : -percent;
    double newBudget = oldBudget * (1 + signed / 100.0);
    if (newBudget < 0) {
      return "Budget cannot become negative.";
    }
    double delta = newBudget - oldBudget; // >0 inc, <0 dec
    if (Math.abs(delta) < 1e-9) {
      return null;
    }

    if (delta > 0 && draftBalance + 1e-9 < delta) {
      return "Insufficient draft balance for this increase. Decrease another ministry first.";
    }

    m.setBudget(newBudget);

    history.add(new DraftEdit(m.getMinistryName(), delta >= 0 ? "Increase" : "Decrease",
        Math.abs(delta), "percent"));

    draftBalance += (delta >= 0) ? -Math.abs(delta) : +Math.abs(delta);
    return null;
  }

  public static String undoLast() {
    if (!isInitialized()) {
      return "Draft session is not initialized.";
    }
    if (history.isEmpty()) {
      return "No draft edits to undo.";
    }

    DraftEdit last = history.remove(history.size() - 1);
    Ministry m = find(last.ministry);
    if (m == null) {
      return "Ministry not found during undo.";
    }
    double current = m.getBudget();
    boolean wasIncrease = "Increase".equalsIgnoreCase(last.changeType);

    double newBudget = wasIncrease ? current - last.amount : current + last.amount;
    if (newBudget < 0) {
      return "Undo would result in negative budget.";
    }

    m.setBudget(newBudget);
    draftBalance += wasIncrease ? +last.amount : -last.amount;

    return null;
  }

  /** Compatibility overload */
  public static String undoLast(double ignored) {
    return undoLast();
  }

  private static Ministry find(String ministryName) {
    if (sandbox == null || ministryName == null)
      return null;

    for (Ministry m : sandbox) {
      if (m == null) {
        continue;
      }
      if (m.getMinistryName() != null && m.getMinistryName().equalsIgnoreCase(ministryName)) {
        return m;
      }
    }
    return null;
  }
}
