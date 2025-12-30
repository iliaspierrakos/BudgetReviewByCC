package guiFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import UserFeatures.ClearHistory;
import UserFeatures.Edit;
import UserFeatures.EditHistoryList;
import UserFeatures.ViewEditBudgetInitializer;
import UserManagement.CurrentSession;
import UserManagement.User;
import UserManagement.UserManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Main application menu displayed after successful login.
 *
 * <p>This JavaFX screen replaces the old CLI-based menu and provides
 * access to all available features of the application.</p>
 *
 * <p>The available options are displayed dynamically based on the
 * role of the logged-in user.</p>
 */

public class ViewEditBudgetScreen {

    private final User user;
    private final UserManager userManager;

    public ViewEditBudgetScreen(User user, UserManager userManager) {
        this.user = user;
        this.userManager = userManager;
    }

    public void show(Stage stage) {

        CurrentSession.setUser(user);
        // Ensure data are loaded (safe to call multiple times)
        ViewEditBudgetInitializer.ensureInitialized();

        Label title = new Label("Welcome, " + user.getUsername());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        VBox buttonsBox = new VBox(12);
        buttonsBox.setAlignment(Pos.CENTER);

        // ===== VIEW BUDGET BUTTON (all roles) =====
        Button viewButton = new Button("View Budget");
        viewButton.setMinWidth(220);
        viewButton.setOnAction(e -> {
            new ViewBudgetScreen(user, userManager).show(stage);
        });
        buttonsBox.getChildren().add(viewButton);

        // ===== EDIT/PROPOSE/VIRTUAL EDIT BUTTON =====
        if (user.getRole() != User.Role.CITIZEN) {
            Button editButton = new Button(
                user.getRole() == User.Role.MINISTRYMEMBER
                    ? "Propose Edit"
                    : "Edit Budget"
            );
            editButton.setMinWidth(220);
            editButton.setOnAction(e -> {
                new EditBudgetScreen(user, userManager).show(stage);
            });
            buttonsBox.getChildren().add(editButton);
        }

        if (user.getRole() == User.Role.CITIZEN) {
            Button virtualEditButton = new Button("Virtual Edit");
            virtualEditButton.setMinWidth(220);
            virtualEditButton.setOnAction(e -> {
                new VirtualEditScreen(user, userManager).show(stage);
            });
            buttonsBox.getChildren().add(virtualEditButton);
        }

        // ===== COMPARE BUDGETS BUTTON (all roles) =====
        Button compareButton = new Button("Compare Budgets");
        compareButton.setMinWidth(220);
        compareButton.setOnAction(e -> 
          new CompareScreen(user, userManager).show(stage)
        );
        buttonsBox.getChildren().add(compareButton);

        // ===== RECOMMENDATIONS BUTTON (role-specific) =====
        Button recButton = new Button();
        recButton.setMinWidth(220);

        switch (user.getRole()) {
            case GOVERNOR -> recButton.setText("View Statistics");
            case CITIZEN -> recButton.setText("Submit Recommendation");
            case MINISTRYMEMBER -> recButton.setText("View Citizen Proposals");
        }

        recButton.setOnAction(e -> {
            switch (user.getRole()) {
                case CITIZEN -> 
                    new SubmitRecommendationScreen(user).show(stage);

                case MINISTRYMEMBER -> 
                    new ViewRecommendationsScreen(user, userManager).show(stage);

                case GOVERNOR -> 
                    new ViewStatisticsScreen(user).show(stage);
            }
        });

        buttonsBox.getChildren().add(recButton);

        // ===== TAX RECEIPT BUTTON (Citizen only) =====
        if (user.getRole() == User.Role.CITIZEN) {
            Button taxButton = new Button("Tax Receipt");
            taxButton.setMinWidth(220);
            taxButton.setOnAction(e ->
                new TaxReceiptScreen(user, userManager).show(stage)
            );
            buttonsBox.getChildren().add(taxButton);
        }

        // ===== LOGOUT BUTTON (all roles) =====
        Button logoutButton = new Button("Logout");
        logoutButton.setMinWidth(220);
        logoutButton.setOnAction(e -> {
            cleanupOnLogout();
            new StartMenuScreen(userManager).show(stage);
        });

        buttonsBox.getChildren().add(logoutButton);

        VBox root = new VBox(20, title, buttonsBox);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(25));

        stage.setScene(new Scene(root, 420, 500));
        stage.setTitle("Main Menu");
        stage.show();
    }
    private void cleanupOnLogout() {
    try {
        // 1. Clear edit history
        ClearHistory.clearFile(Path.of("src/main/resources/NecessaryFilesAndData/edithistory.txt"));
        
        // 2. Clear view files (temporary)
        for (int year = 2020; year <= 2026; year++) {
            ClearHistory.clearFile(Path.of("src/main/resources/NecessaryFilesAndData/view" + year + ".txt"));
        }
        
        // 3. Clear comparison files (temporary)
        for (int year1 = 2020; year1 <= 2026; year1++) {
            for (int year2 = 2020; year2 <= 2026; year2++) {
                Files.deleteIfExists(
                    Paths.get("src/main/resources/NecessaryFilesAndData/compare" + year1 + "with" + year2 + ".txt")
                );
            }
        }
        
        // 4. Reset static state
        Edit.balance = 0;
        Edit.history = new EditHistoryList();
        
    } catch (IOException ex) {
        System.err.println("Cleanup failed: " + ex.getMessage());
    }
}

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}