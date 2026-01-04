package UserFeatures;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import UserManagement.User;
/**
 * Utility class responsible for persisting a user's budget data.
 * <p>
 * This class stores the following information:
 * </p>
 * <ul>
 *   <li>The user's remaining balance</li>
 *   <li>The budget assigned to each ministry</li>
 * </ul>
 * <p>
 * The data is saved to a CSV file so that it can be reloaded
 * when the user signs in again.
 * </p>
 */
public class UserBudgetPersistence {

    /**
     * Saves the user's budget data for a specific year.
     *
     * <p>
     * The generated CSV file contains:
     * </p>
     * <ul>
     *   <li>A balance entry</li>
     *   <li>One entry per ministry containing its budget</li>
     * </ul>
     *
     * @param user the user whose budget data is being saved
     * @param ministries an array of ministries containing budget information
     * @param year the year for which the budget data applies
     */
    public static void saveUserBudgets(User user, Ministry[] ministries, int year) {
        Path file = UserBudgetFileUtil.getUserBudgetFile(user, year);

        try {
            Files.createDirectories(file.getParent());

            try (BufferedWriter writer = Files.newBufferedWriter(file)) {
                writer.write("BALANCE," + Edit.balance);
                writer.newLine();

                for (Ministry m : ministries) {
                    if (m != null) {
                        writer.write(m.getMinistryName() + "," + m.getBudget());
                        writer.newLine();
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to save user budgets: " + e.getMessage());
        }
    }
}
