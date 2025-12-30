package guiFolder;

import java.util.function.Supplier;
import org.kordamp.ikonli.javafx.FontIcon;

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
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RegisterScreen {
    private final UserManager userManager;

    public RegisterScreen(UserManager userManager) {
        this.userManager = userManager;
    }

    public void show(Stage stage) {
        Label title = new Label("Create Account");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Choose a role and set your credentials.");
        subtitle.getStyleClass().add("subtitle");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        // ---- Pretty role picker (segmented buttons) ----
        Label roleLabel = new Label("Role");
        roleLabel.getStyleClass().add("subtitle");

        ToggleGroup roleGroup = new ToggleGroup();

        FontIcon userIcon = new FontIcon("fas-user");
        FontIcon govIcon  = new FontIcon("fas-landmark");
        FontIcon kingIcon = new FontIcon("fas-crown");


        ToggleButton citizenBtn = new ToggleButton("Citizen", new FontIcon("fas-user"));
        ToggleButton ministryBtn = new ToggleButton("Ministry", new FontIcon("fas-landmark"));
        ToggleButton governorBtn = new ToggleButton("Governor", new FontIcon("fas-crown"));


        citizenBtn.setToggleGroup(roleGroup);
        ministryBtn.setToggleGroup(roleGroup);
        governorBtn.setToggleGroup(roleGroup);

        citizenBtn.getStyleClass().add("role-toggle");
        ministryBtn.getStyleClass().add("role-toggle");
        governorBtn.getStyleClass().add("role-toggle");

        // default role (optional): Citizen selected
        citizenBtn.setSelected(true);

        HBox rolePicker = new HBox(10, citizenBtn, ministryBtn, governorBtn);
        rolePicker.getStyleClass().add("role-picker");
        rolePicker.setAlignment(Pos.CENTER);
        rolePicker.setMaxWidth(Double.MAX_VALUE);

        Supplier<User.Role> getRole = () -> {
            var sel = roleGroup.getSelectedToggle();
            if (sel == citizenBtn) return User.Role.CITIZEN;
            if (sel == ministryBtn) return User.Role.MINISTRYMEMBER;
            if (sel == governorBtn) return User.Role.GOVERNOR;
            return null;
        };

        TextField ministryField = new TextField();
        ministryField.setPromptText("Ministry Name");
        ministryField.setVisible(false);
        ministryField.setManaged(false);

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error");

        Button registerButton = new Button("Register");
        registerButton.getStyleClass().addAll("button", "primary");
        registerButton.setDisable(true);

        Button backButton = new Button("Back");
        backButton.getStyleClass().addAll("button", "subtle");

        registerButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setMaxWidth(Double.MAX_VALUE);

        // keyboard flow
        usernameField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> {
            User.Role r = getRole.get();
            if (r == User.Role.MINISTRYMEMBER) {
                ministryField.requestFocus();
            } else {
                registerButton.fire();
            }
        });
        ministryField.setOnAction(e -> registerButton.fire());

        // role change -> show/hide ministry field
        roleGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            User.Role r = getRole.get();
            boolean show = r == User.Role.MINISTRYMEMBER;

            ministryField.setVisible(show);
            ministryField.setManaged(show);

            errorLabel.setText("");
            updateDisable(registerButton, usernameField, passwordField, getRole, ministryField);
        });

        // live validation
        usernameField.textProperty().addListener((obs, o, n) -> {
            errorLabel.setText("");
            updateDisable(registerButton, usernameField, passwordField, getRole, ministryField);
        });

        passwordField.textProperty().addListener((obs, o, n) -> {
            errorLabel.setText("");
            updateDisable(registerButton, usernameField, passwordField, getRole, ministryField);
        });

        ministryField.textProperty().addListener((obs, o, n) -> {
            errorLabel.setText("");
            updateDisable(registerButton, usernameField, passwordField, getRole, ministryField);
        });

        registerButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText(); // NO trim
            User.Role role = getRole.get();
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

        // ---- Card UI ----
        VBox card = new VBox(
                12,
                title,
                subtitle,
                new Separator(),
                usernameField,
                passwordField,
                roleLabel,
                rolePicker,
                ministryField,
                registerButton,
                backButton,
                errorLabel
        );
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("card");
        card.setMaxWidth(420);

        VBox root = new VBox(card);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(24));

        Scene scene = new Scene(root, 600, 560);
        scene.getStylesheets().add(
                getClass().getResource("/css/DarkTheme.css").toExternalForm()
        );

        stage.setScene(scene);
        stage.setTitle("Register");
        stage.show();

        usernameField.requestFocus();
        updateDisable(registerButton, usernameField, passwordField, getRole, ministryField);
    }

    private static void updateDisable(
            Button registerButton,
            TextField usernameField,
            PasswordField passwordField,
            Supplier<User.Role> getRole,
            TextField ministryField
    ) {
        String u = usernameField.getText().trim();
        String p = passwordField.getText();
        User.Role r = getRole.get();
        boolean needsMinistry = r == User.Role.MINISTRYMEMBER;
        String m = ministryField.getText().trim();

        boolean ok = !u.isEmpty() && !p.isEmpty() && r != null && (!needsMinistry || !m.isEmpty());
        registerButton.setDisable(!ok);
    }
}
