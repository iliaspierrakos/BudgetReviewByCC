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

public class ViewBudgetScreen {

    private final User loggedInUser;
    private final UserManager userManager;

    public ViewBudgetScreen(User loggedInUser, UserManager userManager) {
        this.loggedInUser = loggedInUser;
        this.userManager = userManager;
    }

    public void show(Stage stage) {
        Label title = new Label("VIEW BUDGET");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Label info = new Label("Budget view coming next (List / Table).");

        Button backButton = new Button("Back");
        backButton.setMinWidth(200);
        backButton.setOnAction(e ->
                new FeaturesScreen(loggedInUser, userManager).show(stage)
        );

        VBox layout = new VBox(20, title, info, backButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        stage.setScene(new Scene(layout, 500, 320));
        stage.setTitle("VIEW BUDGET");
        stage.show();
    }
}
