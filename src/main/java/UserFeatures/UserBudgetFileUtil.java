package UserFeatures;

import java.nio.file.Path;
import java.nio.file.Paths;

import UserManagement.User;

/**
 * Utility class responsible for resolving the file path
 * of a user's budget CSV file for a specific year.
 *
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
}
