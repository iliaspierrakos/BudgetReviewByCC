package guiFolder;

import UserManagement.User;
import UserManagement.UserManager;
import UserFeatures.ViewEditBudget;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
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

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        Button loginButton = new Button("Login");
        Button backButton = new Button("Back");

        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Please fill all fields.");
                return;
            }

            User loggedInUser = userManager.loginUser(username, password);

            if (loggedInUser == null) {
                errorLabel.setText("Invalid username or password.");
            } else {
                // 🔑 ΕΔΩ ΤΕΛΕΙΩΝΕΙ ΤΟ LOGIN
                ViewEditBudget controller = new ViewEditBudget(loggedInUser);
                new ViewEditBudgetScreen(loggedInUser, controller, userManager)
                        .show(stage);
            }
        });

        backButton.setOnAction(e ->
                new StartMenuScreen(userManager).show(stage)
        );

        VBox layout = new VBox(
                15, title, usernameField, passwordField,
                loginButton, backButton, errorLabel
        );
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        stage.setScene(new Scene(layout, 350, 260));
        stage.setTitle("Login");
        stage.show();
    }
}
