package guiFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;      // ✅ ΠΡΟΣΘΗΚΗ
import java.nio.file.Paths;

import UserFeatures.ClearHistory;
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
        
        ImageView logo = new ImageView();
        var logoStream = getClass().getResourceAsStream("logo.png");
        if (logoStream != null) {
            logo.setImage(new Image(logoStream));
        }
        logo.setFitWidth(150);
        logo.setPreserveRatio(true);
        logo.setId("app-logo");
        
        Label title = new Label("PRIME MINISTER FOR A DAY");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Button registerButton = new Button("Register");
        Button loginButton = new Button("Login");
        Button exitButton = new Button("Exit");

        registerButton.setMinWidth(200);
        loginButton.setMinWidth(200);
        exitButton.setMinWidth(200);

        registerButton.setOnAction(e -> new RegisterScreen(userManager).show(stage));
        loginButton.setOnAction(e -> new LoginScreen(userManager).show(stage));
        exitButton.setOnAction(e -> {
            cleanupOnExit();
            stage.close();
            System.exit(0);
        });
        
        // Layout
        VBox layout = new VBox(20, logo, title, registerButton, loginButton, exitButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(25));

        Scene scene = new Scene(layout, 500, 420);

        // --- CSS ---
        var cssUrl = getClass().getResource("screen1.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        stage.setScene(scene);
        stage.setTitle("Welcome");
        stage.show();
    }

    /**
     * Cleanup on application exit - removes temporary files.
     * Keeps user budgets and governor drafts.
     */
    private void cleanupOnExit() {
        try {
            // 1. Clear edit history
            ClearHistory.clearFile(Path.of("src/main/resources/NecessaryFilesAndData/edithistory.txt"));
            
            // 2. Clear ALL CSV files (except UserBudgets and Governor)
            for (int year = 2020; year <= 2026; year++) {
                ClearHistory.clearFile(
                    Path.of("src/main/resources/NecessaryFilesAndData/MinistriesBudgets" + year + ".csv")
                );
                ClearHistory.clearFile(
                    Path.of("src/main/resources/NecessaryFilesAndData/view" + year + ".txt")
                );
            }
            
            // 3. Clear comparison files
            for (int year1 = 2020; year1 <= 2026; year1++) {
                for (int year2 = 2020; year2 <= 2026; year2++) {
                    Files.deleteIfExists(
                        Paths.get("src/main/resources/NecessaryFilesAndData/compare" + year1 + "with" + year2 + ".txt")
                    );
                }
            }
            
        } catch (IOException ex) {
            System.err.println("Exit cleanup failed: " + ex.getMessage());
        }
    }
}