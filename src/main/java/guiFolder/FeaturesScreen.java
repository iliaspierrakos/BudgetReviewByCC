package guiFolder;

/* Imports */
import UserManagement.User;
import UserManagement.UserManager;

/* graphics imports */
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FeaturesScreen {
    private final User loggedInUser;
    private final UserManager userManager;
    public FeaturesScreen(User loggedInUser, UserManager userManager) {
        this.loggedInUser = loggedInUser;
        this.userManager = userManager;
    }

    public void show(Stage stage) {
        Label title = new Label("FEATURES");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Button viewButton = new Button("VIEW BUDGET");
        viewButton.setMinWidth(200);

        Button returnButton = new Button("RETURN");
        returnButton.setMinWidth(200);

        viewButton.setOnAction(e -> {
            new ViewFeature(loggedInUser).show(stage);
        });
        returnButton.setOnAction(e -> {
            new StartMenuScreen(userManager).show(stage);
        });

        VBox layout = new VBox(20, title, viewButton, returnButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        Scene scene = new Scene(layout, 400, 260);
        stage.setScene(scene);
        stage.setTitle("FEATURES");
        stage.show();
    }
}

