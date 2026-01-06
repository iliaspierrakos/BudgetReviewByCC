package guiFolder;

import UserManagement.User;
import UserManagement.UserManager;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginScreen {
    private final UserManager userManager;

    public LoginScreen(UserManager userManager) {
    if (userManager == null) {
        throw new IllegalArgumentException("UserManager cannot be null");
    }
    this.userManager = userManager;
}


    public void show(Stage stage) {

        // Window state snapshot (so it doesn't jump/resize)
        final boolean wasMaximized = stage.isMaximized();
        final boolean wasFullScreen = stage.isFullScreen();
        final double prevW = stage.getWidth();
        final double prevH = stage.getHeight();
        final double prevX = stage.getX();
        final double prevY = stage.getY();

        // ---- Logo plaque (gold) ----
        ImageView logo = new ImageView();
        var logoStream = getClass().getResourceAsStream("/guiFolder/logo1.png");
        if (logoStream != null) logo.setImage(new Image(logoStream));
        logo.setFitWidth(320);
        logo.setPreserveRatio(true);
        logo.getStyleClass().add("auth-logo");

        StackPane logoFrame = new StackPane(logo);
        logoFrame.getStyleClass().add("auth-logo-frame");

        // ---- Titles ----
        Label title = new Label("Welcome back");
        title.getStyleClass().addAll("title", "auth-title");

        Label subtitle = new Label("Sign in to continue.");
        subtitle.getStyleClass().addAll("subtitle", "auth-subtitle");

        // ---- Fields ----
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.getStyleClass().add("auth-input");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("auth-input");

        TextField passwordVisibleField = new TextField();
        passwordVisibleField.setPromptText("Password");
        passwordVisibleField.getStyleClass().add("auth-input");
        passwordVisibleField.setVisible(false);
        passwordVisibleField.setManaged(false);

        // keep values synced
        passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());

        ToggleButton showPassBtn = new ToggleButton("👁");
        showPassBtn.getStyleClass().add("icon-toggle");
        showPassBtn.setFocusTraversable(false);

        HBox passwordRow = new HBox(8, passwordField, passwordVisibleField, showPassBtn);
        passwordRow.setAlignment(Pos.CENTER);
        HBox.setHgrow(passwordField, Priority.ALWAYS);
        HBox.setHgrow(passwordVisibleField, Priority.ALWAYS);

        showPassBtn.selectedProperty().addListener((obs, was, isNow) -> {
            passwordVisibleField.setVisible(isNow);
            passwordVisibleField.setManaged(isNow);

            passwordField.setVisible(!isNow);
            passwordField.setManaged(!isNow);

            if (isNow) {
                passwordVisibleField.requestFocus();
                passwordVisibleField.positionCaret(passwordVisibleField.getText().length());
            } else {
                passwordField.requestFocus();
                passwordField.positionCaret(passwordField.getText().length());
            }
        });

        // ---- Error label ----
        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error");
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(380);
        errorLabel.setMinHeight(36);

        // ---- Buttons ----
        Button loginButton = new Button("Login");
        loginButton.getStyleClass().addAll("button", "primary", "auth-primary");
        loginButton.setMaxWidth(Double.MAX_VALUE);

        Button backButton = new Button("Back");
        backButton.getStyleClass().addAll("button", "subtle");
        backButton.setMaxWidth(Double.MAX_VALUE);

        loginButton.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> usernameField.getText().trim().isEmpty()
                                || passwordField.getText().isEmpty(),
                        usernameField.textProperty(),
                        passwordField.textProperty()
                )
        );

        Runnable clearError = () -> errorLabel.setText("");
        usernameField.textProperty().addListener((obs, o, n) -> clearError.run());
        passwordField.textProperty().addListener((obs, o, n) -> clearError.run());

        usernameField.setOnAction(e -> {
            if (showPassBtn.isSelected()) passwordVisibleField.requestFocus();
            else passwordField.requestFocus();
        });

        Runnable doLogin = () -> attemptLogin(stage, usernameField, passwordField, errorLabel);
        passwordField.setOnAction(e -> doLogin.run());
        passwordVisibleField.setOnAction(e -> doLogin.run());
        loginButton.setOnAction(e -> doLogin.run());

        backButton.setOnAction(e -> new StartMenuScreen(userManager).show(stage));

        VBox card = new VBox(
                12,
                logoFrame,
                title,
                subtitle,
                new Separator(),
                usernameField,
                passwordRow,
                errorLabel,
                loginButton,
                backButton
        );
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().addAll("card", "auth-card");
        card.setMaxWidth(400);

        VBox root = new VBox(card);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(24));
        root.getStyleClass().add("auth-root");

        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/css/DarkTheme.css").toExternalForm()
            );
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
            String css = getClass().getResource("/css/DarkTheme.css").toExternalForm();
            if (!scene.getStylesheets().contains(css)) {
                scene.getStylesheets().add(css);
            }
        }

        stage.setTitle("Login");
        stage.show();

        if (wasFullScreen) {
            stage.setFullScreen(true);
        } else if (wasMaximized) {
            stage.setMaximized(true);
        } else {
            if (prevW > 0 && prevH > 0) {
                stage.setWidth(prevW);
                stage.setHeight(prevH);
                stage.setX(prevX);
                stage.setY(prevY);
            }
        }

        usernameField.requestFocus();
    }

    private void attemptLogin(
            Stage stage,
            TextField usernameField,
            PasswordField passwordField,
            Label errorLabel
    ) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

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

        User loggedIn = userManager.loginUser(username, password);
        if (loggedIn == null) {
            errorLabel.setText("Invalid username or password.");
        } else {
            new ViewEditBudgetScreen(loggedIn, userManager).show(stage);
        }
    }
}
