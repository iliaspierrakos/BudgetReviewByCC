package guiFolder;

import UserManagement.UserManager;

import java.lang.classfile.Label;

import UserManagement.User;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main.java.guiFolder.MenuScreen;

public class LoginScreen {
    private final UserManager userManager;
    public LoginScreen(UserManager userManager) {
        this.userManager = userManager;
    }
    public void show(Stage stage) {
        Label title = new Label("Login");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle("-fx-font-size: 10px;");
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");
        Button loginButton = new Button("Login");
        loginButton.setMinWidth(120);
        Button backButton = new Button("Back");
        backButton.setMinWidth(120);
        
        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Fields cannot be empty!");
                return;
            }
            User loggedIn = userManager.loginUser(username, password);
            if (loggedIn == null) {
                errorLabel.setText("Invalid username or password.");
            } else {
                errorLabel.setText("");
                MenuScreen menu = new MenuScreen(loggedIn, userManager);
                menu.show(stage);
            }
        });
        backButton.setOnAction(e -> {
            new StartMenuScreen(userManager).show(stage);
        });
        VBox layout = new VBox(
            15, title, usernameField, passwordField, loginButton, backButton, errorLabel);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        Scene scene = new Scene(layout, 350, 260);
        stage.setScene(scene);
        stage.setTitle("Login");
        stage.show();    
    }
}

