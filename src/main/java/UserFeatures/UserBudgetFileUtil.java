package UserFeatures;

import UserManagement.User;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UserBudgetFileUtil {

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
     * Saves current ministries2026 budgets + Edit.balance for this user/year.
     * File format:
     * BALANCE,<value>
     * Ministry Name,<budget>
     */
    public static void saveUserBudget(User user, int year) throws IOException {
        Path file = getUserBudgetFile(user, year);
        Files.createDirectories(file.getParent());

        StringBuilder sb = new StringBuilder();
        sb.append("BALANCE,").append(Edit.balance).append("\n");

        for (Ministry m : CreatingMinistries.ministries2026) {
            if (m == null) continue;
            sb.append(m.getMinistryName()).append(",").append(m.getBudget()).append("\n");
        }

        Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);
    }
}
