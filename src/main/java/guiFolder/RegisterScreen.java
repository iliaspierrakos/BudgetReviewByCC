package guiFolder;

import java.util.function.Supplier;

import org.kordamp.ikonli.javafx.FontIcon;

import UserManagement.User;
import UserManagement.UserManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RegisterScreen {

    private final UserManager userManager;

    public RegisterScreen(UserManager userManager) {
        this.userManager = userManager;
    }

    public void show(Stage stage) {

        // =========================
        // Window state snapshot (so it doesn't jump/resize)
        // =========================
        final boolean wasMaximized = stage.isMaximized();
        final boolean wasFullScreen = stage.isFullScreen();
        final double prevW = stage.getWidth();
        final double prevH = stage.getHeight();
        final double prevX = stage.getX();
        final double prevY = stage.getY();

        // ---- Logo (gold) ----
        ImageView logo = new ImageView();
        var logoStream = getClass().getResourceAsStream("/guiFolder/logo1.png");
        if (logoStream != null) logo.setImage(new Image(logoStream));
        logo.setFitWidth(320);
        logo.setPreserveRatio(true);
        logo.getStyleClass().add("auth-logo");

        StackPane logoFrame = new StackPane(logo);
        logoFrame.getStyleClass().add("auth-logo-frame");

        // ---- Titles ----
        Label title = new Label("Create Account");
        title.getStyleClass().addAll("title", "auth-title");

        Label subtitle = new Label("Choose a role and set your credentials.");
        subtitle.getStyleClass().addAll("subtitle", "auth-subtitle");

        // ---- Fields ----
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        // NOTE: your CSS targets .text-field, not .auth-input. Keep if you use it elsewhere.
        usernameField.getStyleClass().add("auth-input");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("auth-input");

        // ---- Role Picker ----
        Label roleLabel = new Label("Role");
        roleLabel.getStyleClass().addAll("subtitle", "auth-subtitle");

        ToggleGroup roleGroup = new ToggleGroup();

        FontIcon userIcon = new FontIcon("fas-user");
        FontIcon govIcon  = new FontIcon("fas-landmark");
        FontIcon kingIcon = new FontIcon("fas-crown");
        userIcon.setIconSize(14);
        govIcon.setIconSize(14);
        kingIcon.setIconSize(14);

        ToggleButton citizenBtn  = new ToggleButton("Citizen",  userIcon);
        ToggleButton ministryBtn = new ToggleButton("Ministry",  govIcon);
        ToggleButton governorBtn = new ToggleButton("Governor",  kingIcon);

        citizenBtn.setToggleGroup(roleGroup);
        ministryBtn.setToggleGroup(roleGroup);
        governorBtn.setToggleGroup(roleGroup);

        citizenBtn.getStyleClass().addAll("role-toggle", "auth-role-toggle");
        ministryBtn.getStyleClass().addAll("role-toggle", "auth-role-toggle");
        governorBtn.getStyleClass().addAll("role-toggle", "auth-role-toggle");

        citizenBtn.setSelected(true);

        HBox rolePicker = new HBox(10, citizenBtn, ministryBtn, governorBtn);
        rolePicker.getStyleClass().addAll("role-picker", "auth-role-picker");
        rolePicker.setAlignment(Pos.CENTER);
        rolePicker.setMaxWidth(Double.MAX_VALUE);

        Supplier<User.Role> getRole = () -> {
            var sel = roleGroup.getSelectedToggle();
            if (sel == citizenBtn) return User.Role.CITIZEN;
            if (sel == ministryBtn) return User.Role.MINISTRYMEMBER;
            if (sel == governorBtn) return User.Role.GOVERNOR;
            return null;
        };

        // ---- Ministry dropdown (ONLY for Ministry role) ----
        ComboBox<String> ministryBox = new ComboBox<>();
        ministryBox.setPromptText("Select Ministry");
        ministryBox.setEditable(false);
        ministryBox.setMaxWidth(Double.MAX_VALUE);
        ministryBox.getStyleClass().addAll("combo", "auth-input");

        ministryBox.getItems().addAll(
                "Ministry of Interior",
                "Ministry of Foreign Affairs",
                "Ministry of National Defense",
                "Ministry of Health",
                "Ministry of Justice",
                "Ministry of Education, Religious Affairs, and Sports",
                "Ministry of Culture",
                "Ministry of National Economy and Finance",
                "Ministry of Rural Development and Food",
                "Ministry of Environment and Energy",
                "Ministry of Labor and Social Security",
                "Ministry of Social Cohesion and Family",
                "Ministry of Development",
                "Ministry of Infrastructure and Transport",
                "Ministry of Shipping and Island Policy",
                "Ministry of Tourism",
                "Ministry of Digital Governance",
                "Ministry of Migration and Asylum",
                "Ministry of Citizen Protection",
                "Ministry of Climate Crisis and Civil Protection"
        );

        setShown(ministryBox, false);

        // ---- Error label ----
        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error");
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(420);

        // ---- Buttons ----
        Button registerButton = new Button("Register");
        registerButton.getStyleClass().addAll("button", "primary", "auth-primary");
        registerButton.setDisable(true);
        registerButton.setMaxWidth(Double.MAX_VALUE);

        Button backButton = new Button("Back");
        backButton.getStyleClass().addAll("button", "subtle");
        backButton.setMaxWidth(Double.MAX_VALUE);

        // ---- UX: keyboard flow ----
        usernameField.setOnAction(e -> passwordField.requestFocus());

        passwordField.setOnAction(e -> {
            if (getRole.get() == User.Role.MINISTRYMEMBER) {
                ministryBox.requestFocus();
                ministryBox.show();
            } else {
                attemptRegister(stage, usernameField, passwordField, ministryBox, getRole, errorLabel);
            }
        });

        // ---- Live validation ----
        Runnable clearError = () -> errorLabel.setText("");

        usernameField.textProperty().addListener((obs, o, n) -> {
            clearError.run();
            updateDisable(registerButton, usernameField, passwordField, getRole, ministryBox);
        });

        passwordField.textProperty().addListener((obs, o, n) -> {
            clearError.run();
            updateDisable(registerButton, usernameField, passwordField, getRole, ministryBox);
        });

        ministryBox.valueProperty().addListener((obs, o, n) -> {
            clearError.run();
            updateDisable(registerButton, usernameField, passwordField, getRole, ministryBox);
        });

        roleGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            boolean show = getRole.get() == User.Role.MINISTRYMEMBER;
            setShown(ministryBox, show);

            if (!show) {
                ministryBox.setValue(null);
            } else {
                ministryBox.requestFocus();
                ministryBox.show();
            }

            clearError.run();
            updateDisable(registerButton, usernameField, passwordField, getRole, ministryBox);
        });

        // ---- Actions ----
        registerButton.setOnAction(e ->
                attemptRegister(stage, usernameField, passwordField, ministryBox, getRole, errorLabel)
        );

        backButton.setOnAction(e -> new StartMenuScreen(userManager).show(stage));

        // ---- Card layout ----
        VBox card = new VBox(
                12,
                logoFrame,
                title,
                subtitle,
                new Separator(),
                usernameField,
                passwordField,
                roleLabel,
                rolePicker,
                ministryBox,
                errorLabel,
                registerButton,
                backButton
        );
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().addAll("card", "auth-card");
        card.setMaxWidth(420);

        VBox root = new VBox(card);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(24));
        root.getStyleClass().add("auth-root");

        // =========================
        // Scene handling WITHOUT jumping
        // =========================
        Scene scene = stage.getScene();
        if (scene == null) {
            // First time: create scene (no fixed size; let stage keep its size)
            scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/css/DarkTheme.css").toExternalForm()
            );
            stage.setScene(scene);
        } else {
            // Reuse existing scene: just swap root (best for stable window)
            scene.setRoot(root);
            // Ensure stylesheet exists once (optional safety)
            String css = getClass().getResource("/css/DarkTheme.css").toExternalForm();
            if (!scene.getStylesheets().contains(css)) {
                scene.getStylesheets().add(css);
            }
        }

        stage.setTitle("Register");
        stage.show();

        // Restore window state (fullscreen/max/normal) exactly
        if (wasFullScreen) {
            stage.setFullScreen(true);
        } else if (wasMaximized) {
            stage.setMaximized(true);
        } else {
            // If previous size was valid, restore it; otherwise keep current
            if (prevW > 0 && prevH > 0) {
                stage.setWidth(prevW);
                stage.setHeight(prevH);
                stage.setX(prevX);
                stage.setY(prevY);
            }
        }

        usernameField.requestFocus();
        updateDisable(registerButton, usernameField, passwordField, getRole, ministryBox);
    }

    private void attemptRegister(
            Stage stage,
            TextField usernameField,
            PasswordField passwordField,
            ComboBox<String> ministryBox,
            Supplier<User.Role> getRole,
            Label errorLabel
    ) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        User.Role role = getRole.get();

        if (username.isEmpty()) {
            errorLabel.setText("Username is required.");
            usernameField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            errorLabel.setText("Password is required.");
            passwordField.requestFocus();
            return;
        }

        if (password.length() < 6) {
            errorLabel.setText("Password must be at least 6 characters.");
            passwordField.requestFocus();
            return;
        }

        if (role == null) {
            errorLabel.setText("Please select a role.");
            return;
        }

        String ministry = ministryBox.getValue();
        if (role == User.Role.MINISTRYMEMBER && (ministry == null || ministry.isBlank())) {
            errorLabel.setText("Please select a ministry.");
            ministryBox.requestFocus();
            ministryBox.show();
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
    }

    private static void updateDisable(
            Button registerButton,
            TextField usernameField,
            PasswordField passwordField,
            Supplier<User.Role> getRole,
            ComboBox<String> ministryBox
    ) {
        String u = usernameField.getText().trim();
        String p = passwordField.getText();
        User.Role r = getRole.get();

        boolean needsMinistry = r == User.Role.MINISTRYMEMBER;
        String selectedMinistry = ministryBox.getValue();

        boolean ok = !u.isEmpty()
                && !p.isEmpty()
                && r != null
                && (!needsMinistry || selectedMinistry != null);

        registerButton.setDisable(!ok);
    }

    private static void setShown(javafx.scene.Node node, boolean shown) {
        node.setVisible(shown);
        node.setManaged(shown);
    }
}
