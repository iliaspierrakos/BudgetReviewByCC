package UserFeatures;

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