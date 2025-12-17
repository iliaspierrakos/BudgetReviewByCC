package guiFolder;

import UserManagement.User;
import UserManagement.UserManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RegisterScreen {
    private final UserManager userManager;

    public RegisterScreen(UserManager userManager) {
        this.userManager = userManager;
    }

    public void show(Stage stage) {
        Label title = new Label("Register");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        ComboBox<User.Role> roleBox = new ComboBox<>();
        roleBox.getItems().addAll(
                User.Role.CITIZEN,
                User.Role.MINISTRYMEMBER,
                User.Role.GOVERNOR
        );
        roleBox.setPromptText("Select Role");

        TextField ministryField = new TextField();
        ministryField.setPromptText("Ministry Name");
        ministryField.setVisible(false);
        ministryField.setManaged(false);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        Button registerButton = new Button("Register");
        registerButton.setMinWidth(120);

        Button backButton = new Button("Back");
        backButton.setMinWidth(120);


        usernameField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> {
            User.Role r = roleBox.getValue();
            if (r == User.Role.MINISTRYMEMBER) {
                ministryField.requestFocus();
            } else {
                registerButton.fire();
            }
        });
        ministryField.setOnAction(e -> registerButton.fire());

        roleBox.valueProperty().addListener((obs, oldV, newV) -> {
            boolean show = newV == User.Role.MINISTRYMEMBER;
            ministryField.setVisible(show);
            ministryField.setManaged(show);
            errorLabel.setText("");
            updateDisable(registerButton, usernameField, passwordField, roleBox, ministryField);
        });

        registerButton.setDisable(true);

        usernameField.textProperty().addListener((obs, o, n) -> {
            errorLabel.setText("");
            updateDisable(registerButton, usernameField, passwordField, roleBox, ministryField);
        });

        passwordField.textProperty().addListener((obs, o, n) -> {
            errorLabel.setText("");
            updateDisable(registerButton, usernameField, passwordField, roleBox, ministryField);
        });

        ministryField.textProperty().addListener((obs, o, n) -> {
            errorLabel.setText("");
            updateDisable(registerButton, usernameField, passwordField, roleBox, ministryField);
        });

        registerButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText(); // NO trim
            User.Role role = roleBox.getValue();
            String ministry = ministryField.getText().trim();

            if (username.isEmpty() || password.isEmpty() || role == null) {
                errorLabel.setText("Please fill all fields and select a role.");
                return;
            }

            if (role == User.Role.MINISTRYMEMBER && ministry.isEmpty()) {
                errorLabel.setText("Ministry name is required for Ministry Member.");
                return;
            }

            boolean success = (role == User.Role.MINISTRYMEMBER)
                    ? userManager.registerUser(username, password, role, ministry)
                    : userManager.registerUser(username, password, role);

            if (success) {
                new LoginScreen(userManager).show(stage);
            } else {
                errorLabel.setText("Registration failed. Username exists or role limit reached.");
            }
        });

        backButton.setOnAction(e -> new StartMenuScreen(userManager).show(stage));

        VBox layout = new VBox(
                12,
                title,
                usernameField,
                passwordField,
                roleBox,
                ministryField,
                registerButton,
                backButton,
                errorLabel
        );
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 420, 420);
        stage.setScene(scene);
        stage.setTitle("Register");
        stage.show();

        usernameField.requestFocus();
        updateDisable(registerButton, usernameField, passwordField, roleBox, ministryField);
    }

    private static void updateDisable(
            Button registerButton,
            TextField usernameField,
            PasswordField passwordField,
            ComboBox<User.Role> roleBox,
            TextField ministryField
    ) {
        String u = usernameField.getText().trim();
        String p = passwordField.getText();
        User.Role r = roleBox.getValue();
        boolean needsMinistry = r == User.Role.MINISTRYMEMBER;
        String m = ministryField.getText().trim();

        boolean ok = !u.isEmpty() && !p.isEmpty() && r != null && (!needsMinistry || !m.isEmpty());
        registerButton.setDisable(!ok);
    }
}
