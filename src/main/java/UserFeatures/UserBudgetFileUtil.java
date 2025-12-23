package UserFeatures;
/** This is a class that has only one objective
 * returning the path for the user's .csv file
  */

import UserManagement.User;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UserBudgetFileUtil {

    public static Path getUserBudgetFile(User user, int year) {
        String username = user.getUsername();
        return Paths.get(
            "NecessaryFilesAndData/UserBudgets/" +
            username + "_" + year + ".csv"
        );
    }
}
