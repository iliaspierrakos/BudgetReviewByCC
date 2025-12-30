package UserFeatures;
/** This is a class that has only one objective,
 * returning the path for the user's .csv file
*/

import java.nio.file.Path;
import java.nio.file.Paths;

import UserManagement.User;

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
}
