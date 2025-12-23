import UserFeatures.ViewEditBudget;
import UserManagement.UserManager;
import guiFolder.StartMenuScreen;
import javafx.application.Application;
import javafx.stage.Stage;

public class BudgetReviewGUI extends Application {

    @Override
    public void start(Stage stage) {

        // 1) Δημιουργία UserManager (shared σε όλη την εφαρμογή)
        UserManager userManager = new UserManager();

        // 2) Αρχικοποίηση ΟΛΩΝ των δεδομένων (budgets, ministries κτλ)
        // Καλείται ΜΙΑ ΦΟΡΑ, πριν μπει ο χρήστης στο menu
        ViewEditBudget.ensureInitialized();

        // 3) Πρώτη οθόνη (Register / Login / Exit)
        StartMenuScreen startMenu = new StartMenuScreen(userManager);
        startMenu.show(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
