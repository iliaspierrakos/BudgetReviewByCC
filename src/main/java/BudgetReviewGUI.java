import UserFeatures.ViewEditBudget;
import UserManagement.UserManager;
import guiFolder.StartMenuScreen;
import javafx.application.Application;
import javafx.stage.Stage;

public class BudgetReviewGUI extends Application {

  @Override
  public void start(Stage stage) {

    // 1) Create UserManager (shared everywhere)
    UserManager userManager = new UserManager();

    // 2) Initialize all data (budgets, ministries etc)
    // Called once, before user enters the menu
    ViewEditBudget.ensureInitialized();

    // 3) (Register / Login / Exit)
    StartMenuScreen startMenu = new StartMenuScreen(userManager);
    startMenu.show(stage);
  }

  public static void main(String[] args) {
    launch(args);
  }
}
