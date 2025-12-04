package guiFolder;

import UserManagement.UserManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class StartMenuScreen {
    private final UserManager userManager;
    public StartMenuScreen(UserManager userManager) {
        this.userManager = userManager; 
    }
    public void show(Stage stage) {
        Label title = new Label("PRIME MINISTER FOR A DAY");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        Button loginButton = new Button("Login");
        loginButton.setMinWidth(150);
        Button registerButton = new Button("Register");
        registerButton.setMinWidth(150);
        Button exitButton = new Button("Exit");
        exitButton.setMinWidth(150);

        loginButton.setOnAction(e -> {
            new LoginScreen(userManager).show(stage);
        });
        
        registerButton.setOnAction(e -> {
            new RegisterScreen(userManager).show(stage);
        });
        exitButton.setOnAction(e -> {
            stage.close();
        });
        VBox layout = new VBox(20, title, loginButton, registerButton, exitButton);

        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 400, 350);
        stage.setScene(scene);
        stage.setTitle("Welcome");
        stage.show();
    }    
}
