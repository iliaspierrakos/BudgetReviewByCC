package gui;

import UserManagement.UserManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class LoginTestMain extends Application {

    @Override
    public void start(Stage stage) {
        UserManager um = new UserManager();
        new LoginScreen(um).show(stage);
    }

    public static void main(String[] args) {
        launch();
    }
}
