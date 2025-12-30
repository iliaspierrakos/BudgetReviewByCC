package Main;

import UserManagement.UserManager;
import guiFolder.StartMenuScreen;
import javafx.application.Application;
import javafx.stage.Stage;
import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory;

public class BudgetReviewFxMain extends Application {

    private static UserManager userManager;

    public static void main(String[] args) {
        userManager = new UserManager();
        launch(args);   // 🚀 JavaFX launch
    }

    @Override
    public void start(Stage stage) {

        SvgImageLoaderFactory.install();

       
        new StartMenuScreen(userManager).show(stage);
    }
}
