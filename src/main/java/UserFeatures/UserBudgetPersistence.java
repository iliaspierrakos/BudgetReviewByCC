package UserFeatures;
/**
 * This is a class that has only one method {@Code saveUserBudgets}.
 * Its only purpose is to store ministry budgets and the balance created
 * for each user, so he can reload after he signs in again.
 */

import UserManagement.User;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;

public class UserBudgetPersistence {

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