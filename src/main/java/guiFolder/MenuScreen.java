package guiFolder;

import UserManagement.User;
import UserManagement.UserManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MenuScreen {
    private final User loggedInUser;
    private final UserManager userManager;

    public MenuScreen(User loggedInUser, UserManager userManager) {
        this.loggedInUser = loggedInUser;
        this.userManager = userManager;
    }
    public void show(Stage stage) {
        Label title = new Label("Welcome, " + loggedInUser.getUsername());
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label roleLabel = new Label("Role: " + loggedInUser.getRole());
        roleLabel.setStyle("-fx-font-size: 14px;");

        Button viewEditBudgetButton = new Button("View / Edit Budget");
        viewEditBudgetButton.setMinWidth(200);

        Button logoutButton = new Button("Logout");
        logoutButton.setMinWidth(200);
        viewEditBudgetButton.setOnAction(e -> {
            System.out.println("Features coming soon...");
        });

        logoutButton.setOnAction(e -> {
            new StartMenuScreen(userManager).show(stage);
        });

        VBox layout = new VBox(15, title, roleLabel, viewEditBudgetButton, logoutButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 400, 300);
        stage.setScene(scene);
        stage.setTitle("Main Menu");
        stage.show();
    }
}
