package guiFolder;

import UserManagement.User;
import UserManagement.UserManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;



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

        usernameField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> loginButton.fire());

        loginButton.setDisable(true);
        Runnable updateDisable = () -> loginButton.setDisable(
                usernameField.getText().trim().isEmpty() || passwordField.getText().isEmpty()
        );

        usernameField.textProperty().addListener((obs, oldV, newV) -> {
            errorLabel.setText("");
            updateDisable.run();
        });
        passwordField.textProperty().addListener((obs, oldV, newV) -> {
            errorLabel.setText("");
            updateDisable.run();
        });

        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText(); // 4) NO trim on password

            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Fields cannot be empty!");
                return;
            }

            User loggedIn = userManager.loginUser(username, password);
            if (loggedIn == null) {
                errorLabel.setText("Invalid username or password.");
            } else {
                new MenuScreen(loggedIn, userManager).show(stage);
            }
        });

        backButton.setOnAction(e -> new StartMenuScreen(userManager).show(stage));

        VBox layout = new VBox(12, title, usernameField, passwordField, loginButton, backButton, errorLabel);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        stage.setScene(new Scene(layout, 360, 280));
        stage.setTitle("Login");
        stage.show();
        usernameField.requestFocus();
    }
}
