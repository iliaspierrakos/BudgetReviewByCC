package guiFolder;

import UserManagement.User;
import UserManagement.UserManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginScreen {
    private final UserManager userManager;

    public LoginScreen(UserManager userManager) {
        this.userManager = userManager;
    }

    public void show(Stage stage) {
        Label title = new Label("Welcome back");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Sign in to continue.");
        subtitle.getStyleClass().add("subtitle");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error");

        Button loginButton = new Button("Login");
        loginButton.getStyleClass().addAll("button", "primary");
        loginButton.setDisable(true);

        Button backButton = new Button("Back");
        backButton.getStyleClass().addAll("button", "subtle");

        // full width buttons (looks like real app)
        loginButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setMaxWidth(Double.MAX_VALUE);

        // keyboard flow
        usernameField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> loginButton.fire());

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
            String password = passwordField.getText(); // NO trim on password

            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Fields cannot be empty.");
                return;
            }

            User loggedIn = userManager.loginUser(username, password);
            if (loggedIn == null) {
                errorLabel.setText("Invalid username or password.");
            } else {
                new ViewEditBudgetScreen(loggedIn, userManager).show(stage);
            }
        });

        backButton.setOnAction(e -> new StartMenuScreen(userManager).show(stage));

        // ---- Card ----
        VBox card = new VBox(
                12,
                title,
                subtitle,
                new Separator(),
                usernameField,
                passwordField,
                loginButton,
                backButton,
                errorLabel
        );
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("card");
        card.setMaxWidth(380);

        VBox root = new VBox(card);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(24));

        Scene scene = new Scene(root, 560, 520);
        scene.getStylesheets().add(
                getClass().getResource("/css/DarkTheme.css").toExternalForm()
        );

        stage.setScene(scene);
        stage.setTitle("Login");
        stage.show();

        usernameField.requestFocus();
        updateDisable.run();
    }
}
