package Main;

import UserFeatures.*;
import UserManagement.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BudgetReviewMain {
    public static void main(String[] args) {
        Ministries min = new Ministries();
        MinistriesBudgets budg = new MinistriesBudgets();
        for (int i = 2020; i <= 2026; i++) {
            budg.budget(Path.of("NecessaryFilesAndData/BudgetReview" + i + ".txt"));
        }
        min.minlist();
        for (int i = 2020; i <= 2026; i++) {
            CreatingMinistries.ministryCreation(Path.of("NecessaryFilesAndData/MinistriesBudgets" + i + ".csv"));
        }  
        UserManager userManager = new UserManager();
         AuthUI authUI = new AuthUI(userManager);
        authUI.start();
        ClearHistory.clearFile(Path.of("NecessaryFilesAndData/edithistory.txt"));
        for (int i = 2020; i <= 2026; i++) {
            ClearHistory.clearFile(Path.of("NecessaryFilesAndData/MinistriesBudgets" + i + ".csv"));
            ClearHistory.clearFile(Path.of("NecessaryFilesAndData/view" + i + ".txt"));
            for (int j = 2020; j <= 2026; j++) {
                try {
                    Files.deleteIfExists(Paths.get("NecessaryFilesAndData/compare" + i + "with" + j + ".txt"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
