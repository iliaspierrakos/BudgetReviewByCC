package UserFeatures;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import UserManagement.User;

/**
 * Utility class responsible for handling user-specific budget files.
 *
 * <p>Each user has a separate budget file per year, stored in CSV format.
 * The file contains the user's remaining balance and the budgets of all
 * ministries for that year.</p>
 */
public class UserBudgetFileUtil {

    /**
     * Constructs the file path for a user's budget file for a given year.
     *
     * <p>If the user has the {@code GOVERNOR} role, the username is replaced
     * with a fixed identifier to ensure a single shared governor file.</p>
     *
     * @param user the user whose budget file is requested
     * @param year the year of the budget
     * @return the {@link Path} to the user's budget file
     */
    public static Path getUserBudgetFile(User user, int year) {
        String username = user.getUsername();
        if (User.Role.GOVERNOR == user.getRole()) {
            username = "Governor";
        }

        return Paths.get(
            "src/main/resources/NecessaryFilesAndData/UserBudgets/" +
            username + "_" + year + ".csv"
        );
    }

    /**
     * Saves the current ministry budgets and user balance to a CSV file.
     *
     * <p>File format:</p>
     * <pre>
     * BALANCE,&lt;value&gt;
     * Ministry Name,&lt;budget&gt;
     * </pre>
     *
     * @param user the user whose budget is being saved
     * @param year the year of the budget
     * @throws IOException if an I/O error occurs while writing the file
     */
    public static void saveUserBudget(User user, int year) throws IOException {
        Path file = getUserBudgetFile(user, year);
        Files.createDirectories(file.getParent());

        StringBuilder sb = new StringBuilder();
        sb.append("BALANCE,").append(Edit.balance).append("\n");

        for (Ministry m : CreatingMinistries.ministries2026) {
            if (m == null) continue;
            sb.append(m.getMinistryName())
              .append(",")
              .append(m.getBudget())
              .append("\n");
        }

        Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);
    }
}
