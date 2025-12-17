package guiFolder;

import UserManagement.UserManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class StartMenuScreen {

    private final UserManager userManager;

    public StartMenuScreen(UserManager userManager) {
        this.userManager = userManager;
    }

    public void show(Stage stage) {

        // --- LOGO ---
        ImageView logo = new ImageView();
        var logoStream = getClass().getResourceAsStream("logo.png");
        if (logoStream != null) {
            logo.setImage(new Image(logoStream));
        }
        logo.setFitWidth(150);
        logo.setPreserveRatio(true);
        logo.setId("app-logo");

        // --- TITLE ---
        Label title = new Label("PRIME MINISTER FOR A DAY");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // --- BUTTONS ---
        Button registerButton = new Button("Register");
        Button loginButton = new Button("Login");
        Button exitButton = new Button("Exit");

        registerButton.setMinWidth(200);
        loginButton.setMinWidth(200);
        exitButton.setMinWidth(200);

        registerButton.setOnAction(e -> new RegisterScreen(userManager).show(stage));
        loginButton.setOnAction(e -> new LoginScreen(userManager).show(stage));
        exitButton.setOnAction(e -> stage.close());

        // Layout
        VBox layout = new VBox(20, logo, title, registerButton, loginButton, exitButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(25));

        Scene scene = new Scene(layout, 500, 420);

        // --- CSS (safe load) ---//
        var cssUrl = getClass().getResource("screen1.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        stage.setScene(scene);
        stage.setTitle("Welcome");
        stage.show();
    }
}
