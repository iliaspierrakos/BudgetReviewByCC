package guiFolder;

import UserManagement.UserManager;
import UserManagement.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
        
        roleBox.setOnAction(e -> {
            User.Role selected = roleBox.getValue();
            boolean show = selected == User.Role.MINISTRYMEMBER;
            ministryField.setVisible(show);
            ministryField.setManaged(show);
        });
        Button registerButton = new Button("Register");
        registerButton.setMinWidth(120);

        Button backButton = new Button("Back");
        backButton.setMinWidth(120);    

        registerButton.setOnAction(e -> {

            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
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

            boolean success;
            if (role == User.Role.MINISTRYMEMBER) {
                success = userManager.registerUser(username, password, role, ministry);
            } else {
                success = userManager.registerUser(username, password, role);
            }

            if (success) {
                errorLabel.setText("Registration successful!");
                usernameField.clear();
                passwordField.clear();
                ministryField.clear();
                roleBox.setValue(null);
            } else {
                errorLabel.setText("Registration failed. Username exists or role limit reached.");
            }
        });

        backButton.setOnAction(e -> {
            new StartMenuScreen(userManager).show(stage);
        });
        
        VBox layout = new VBox(
                15,
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

        Scene scene = new Scene(layout, 400, 420);
        stage.setScene(scene);
        stage.setTitle("Register");
        stage.show();    
    }
}
