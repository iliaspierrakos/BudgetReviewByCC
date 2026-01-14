package UserFeatures;

import UserManagement.MinistryMember;
import UserManagement.User;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

/**
 * ViewEditBudget - Main entry point for budget viewing and editing features.
 *
 * This class has been refactored for GUI compatibility. All initialization is now handled by
 * ViewEditBudgetInitializer.
 */
public class ViewEditBudget {

  private static boolean initialized = false;

  /*
   * =============================== INITIALIZATION (called once) ===============================
   */
  public static void ensureInitialized() {
    if (initialized)
      return;

    // Delegate to ViewEditBudgetInitializer
    ViewEditBudgetInitializer.ensureInitialized();

    initialized = true;
  }

  /*
   * =============================== VIEW (CLI case 1) ===============================
   */
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

  /*
   * =============================== EDIT / PROPOSE (CLI case 2) ===============================
   */
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

  /*
   * =============================== COMPARE (CLI case 3) ===============================
   */
  public static void compare() {
    Compare.comparingMinistries();
  }

  /*
   * =============================== RECOMMENDATIONS / PROPOSALS (CLI case 4)
   * ===============================
   */
  public static void recommendations(User user) {

    if (user.getRole() == User.Role.GOVERNOR) {
      GovernorCheck g = new GovernorCheck();
      g.viewProposalsNames();
    }

    else if (user.getRole() == User.Role.MINISTRYMEMBER) {
      // GUI screen handles this
      throw new UnsupportedOperationException(
          "Viewing citizen proposals for ministry member is GUI-based");
    }
  }

  /*
   * =============================== TAX RECEIPT (CLI case 5) ===============================
   */
  public static void taxReceipt(User user) {
    if (user.getRole() != User.Role.CITIZEN) {
      throw new IllegalStateException("Only citizens can view tax receipts");
    }
    throw new UnsupportedOperationException(
        "Tax receipt is GUI-based. Open TaxReceiptScreen from the menu.");
  }

  public static void resetAll() throws IOException {
    Path folder1 = Paths.get("src/main/java/NecessaryFilesAndData");
    if (Files.exists(folder1)) {
      Files.walk(folder1).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    }

    Path folder2 = Paths.get("Data");
    if (Files.exists(folder2)) {
      Files.walk(folder2).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    }

    // Reset initialization flag
    initialized = false;
  }
}
