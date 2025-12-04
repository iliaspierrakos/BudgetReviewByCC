import UserManagement.UserManager;
import guiFolder.StartMenuScreen;
import javafx.application.Application;
import javafx.stage.Stage;

public class BudgetReviewGUI extends Application {

    @Override
    public void start(Stage stage) {
        UserManager userManager = new UserManager();
        StartMenuScreen startMenu = new StartMenuScreen(userManager);
        startMenu.show(stage);
    }

    public static void main(String[] args) {
        launch();
    }
}

