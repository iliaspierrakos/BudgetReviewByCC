package UserFeatures;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import UserManagement.User;
/**
 * Utility class responsible for resolving the file path
 * of a user's budget CSV file for a specific year.
 * <p>
 * Governors use a shared budget file, while other users
 * have individual budget files based on their username.
 * </p>
 */
public class UserBudgetFileUtil {

    /**
     * Returns the path to the user's budget CSV file for a given year.
     *
     * @param user the user whose budget file is requested
     * @param year the budget year
     * @return the {@link Path} to the corresponding CSV file
     */
    public static Path getUserBudgetFile(User user, int year) {
        String username = user.getUsername();
        if (User.Role.GOVERNOR == user.getRole()) {
            username = "Governor";
        }
        return Paths.get(
            "src/main/java/NecessaryFilesAndData/UserBudgets/" +
            username + "_" + year + ".csv"
        );
    }

    /**
     * Saves current in-memory budgets (CreatingMinistries.ministriesYYYY) AND Edit.balance
     * to the user's budget CSV.
     *
     * Format:
     * BALANCE,<value>
     * Ministry Name,<budget>
     * ...
     */
    public static void saveUserBudget(User user, int year) throws IOException {

        // Ensure folder exists
        Path folder = Paths.get("src/main/resources/NecessaryFilesAndData/UserBudgets");
        Files.createDirectories(folder);

        Path file = getUserBudgetFile(user, year);

        StringBuilder sb = new StringBuilder();

        // 1) balance first
        sb.append("BALANCE,").append(Edit.balance).append("\n");

        // 2) budgets (we use ministries2026 because your app edits 2026 draft)
        // If later you support other years, you can switch by year.
        Ministry[] arr = CreatingMinistries.ministries2026;

        for (Ministry m : arr) {
            if (m == null) continue;
            sb.append(m.getMinistryName()).append(",");
            sb.append(m.getBudget()).append("\n");
        }

        Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);
    }

    /** Convenience helper: deletes user's saved virtual budget file (if exists). */
    public static void deleteUserBudget(User user, int year) {
        try {
            Files.deleteIfExists(getUserBudgetFile(user, year));
        } catch (Exception ignored) {}
    }
}
