package guiFolder;

import UserFeatures.ViewEditBudget;
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

        // Ensure data are loaded (safe to call multiple times)
        ViewEditBudget.ensureInitialized();

        Label title = new Label("Welcome, " + user.getUsername());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        VBox buttonsBox = new VBox(12);
        buttonsBox.setAlignment(Pos.CENTER);

        Button viewButton = new Button("View Budget");
        viewButton.setMinWidth(220);
        viewButton.setOnAction(e -> {
        new ViewBudgetScreen(user).show(stage);

        });

        buttonsBox.getChildren().add(viewButton);

        if (user.getRole() != User.Role.CITIZEN) {
            Button editButton = new Button(
                user.getRole() == User.Role.MINISTRYMEMBER
                    ? "Propose Edit"
                    : "Edit Budget"
            );
            editButton.setMinWidth(220);
            editButton.setOnAction(e -> {
                showInfo("Edit", "Edit feature will be GUI-based.");
            });
            buttonsBox.getChildren().add(editButton);
        }

        if (user.getRole() == User.Role.CITIZEN) {
            Button virtualEditButton = new Button("Virtual Edit");
            virtualEditButton.setMinWidth(220);
            virtualEditButton.setOnAction(e -> {
                showInfo("Virtual Edit", "Virtual Edit feature will be GUI-based.");
            });
            buttonsBox.getChildren().add(virtualEditButton);
        }

        Button compareButton = new Button("Compare Budgets");
        compareButton.setMinWidth(220);
        compareButton.setOnAction(e -> 
          new CompareScreen(user).show(stage)
        );
        buttonsBox.getChildren().add(compareButton);

        Button recButton = new Button();
        recButton.setMinWidth(220);

        switch (user.getRole()) {
            case GOVERNOR -> recButton.setText("View Proposals");
            case CITIZEN -> recButton.setText("Submit Recommendation");
            case MINISTRYMEMBER -> recButton.setText("View Citizen Proposals");
        }

        recButton.setOnAction(e -> {
            ViewEditBudget.recommendations(user);
            showInfo("Recommendations", "Action executed.");
        });

        buttonsBox.getChildren().add(recButton);

        if (user.getRole() == User.Role.CITIZEN) {
            Button taxButton = new Button("Tax Receipt");
            taxButton.setMinWidth(220);
            taxButton.setOnAction(e -> {
                ViewEditBudget.taxReceipt(user);
                showInfo("Tax Receipt", "Tax receipt generated.");
            });
            buttonsBox.getChildren().add(taxButton);
        }

        Button logoutButton = new Button("Logout");
        logoutButton.setMinWidth(220);
        logoutButton.setOnAction(e -> {
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

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
