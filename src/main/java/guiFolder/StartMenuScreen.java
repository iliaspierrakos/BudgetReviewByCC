package guiFolder;

import UserManagement.UserManager;
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
        ImageView logo = new ImageView(
                new Image(getClass().getResourceAsStream("logo.png"))
        );
        logo.setFitWidth(150);
        logo.setPreserveRatio(true);
        logo.setId("app-logo");

        // --- TITLE ---
        Label title = new Label("PRIME MINISTER FOR A DAY");
        title.setId("main-title");

        // --- BUTTONS ---
        Button registerButton = new Button("Register");
        Button loginButton = new Button("Login");
        Button exitButton = new Button("Exit");

        registerButton.getStyleClass().add("main-button");
        loginButton.getStyleClass().add("main-button");
        exitButton.getStyleClass().addAll("main-button", "exit-button");

        // Actions
        registerButton.setOnAction(e -> new RegisterScreen(userManager).show(stage));
        loginButton.setOnAction(e -> new LoginScreen(userManager).show(stage));
        exitButton.setOnAction(e -> stage.close());

        // Layout
        VBox layout = new VBox(20, logo, title, registerButton, loginButton, exitButton);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 500, 420);

        // LOAD CSS
        scene.getStylesheets().add(
                getClass().getResource("screen1.css").toExternalForm()
        );

        stage.setScene(scene);
        stage.setTitle("Welcome");
        stage.show();
    }
}
