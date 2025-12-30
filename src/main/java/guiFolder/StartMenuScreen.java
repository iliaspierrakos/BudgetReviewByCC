package guiFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import UserFeatures.ClearHistory;
import UserManagement.UserManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
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
        logo.setFitWidth(140);
        logo.setPreserveRatio(true);

        Label title = new Label("Prime Minister for a Day");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Budget Review Simulator");
        subtitle.getStyleClass().add("subtitle");

        Button loginButton = new Button("Login");
        loginButton.getStyleClass().addAll("button", "primary");

        Button registerButton = new Button("Create account");
        registerButton.getStyleClass().addAll("button");

        Button exitButton = new Button("Exit");
        exitButton.getStyleClass().addAll("button", "danger");

        // full width buttons
        loginButton.setMaxWidth(Double.MAX_VALUE);
        registerButton.setMaxWidth(Double.MAX_VALUE);
        exitButton.setMaxWidth(Double.MAX_VALUE);

        loginButton.setOnAction(e -> new LoginScreen(userManager).show(stage));
        registerButton.setOnAction(e -> new RegisterScreen(userManager).show(stage));
        exitButton.setOnAction(e -> {
            cleanupOnExit();
            stage.close();
            System.exit(0);
        });

        // ---- Card ----
        VBox card = new VBox(
                14,
                logo,
                title,
                subtitle,
                new Separator(),
                loginButton,
                registerButton,
                exitButton
        );
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("card");
        card.setMaxWidth(380);

        VBox root = new VBox(card);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(24));

        Scene scene = new Scene(root, 560, 520);

        // Use ONE theme everywhere (dark)
        scene.getStylesheets().add(
                getClass().getResource("/css/DarkTheme.css").toExternalForm()
        );

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
