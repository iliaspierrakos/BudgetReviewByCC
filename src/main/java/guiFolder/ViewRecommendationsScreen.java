package guiFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import UserManagement.MinistryMember;
import UserManagement.User;
import UserManagement.UserManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * ViewRecommendationsScreen
 *
 * Screen for Ministry Members to view citizen recommendations
 * submitted for their assigned ministry.
 */
public class ViewRecommendationsScreen {

    private final User user;
    private final UserManager userManager;

    // BASE directory for runtime data
    private static final Path DATA_DIR =
            Path.of("NecessaryFilesAndData/ProposalsFromCitizens");

    public ViewRecommendationsScreen(User user, UserManager userManager) {
        this.user = user;
        this.userManager = userManager;
    }

    public void show(Stage stage) {

        if (!(user instanceof MinistryMember)) {
            Alert a = new Alert(Alert.AlertType.ERROR,
                    "Access denied: Only Ministry Members can view recommendations.");
            a.showAndWait();
            return;
        }

        MinistryMember mm = (MinistryMember) user;
        String ministryName = mm.getMinistryName();

        Label title = new Label("Citizen Recommendations");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label subtitle = new Label("Ministry: " + ministryName);
        subtitle.setStyle("-fx-font-size: 14px;");

        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);

        Path filePath = DATA_DIR.resolve(
                "CitizenForMinistry of " + ministryName + ".txt"
        );

        if (Files.exists(filePath)) {
            try {
                textArea.setText(Files.readString(filePath));
            } catch (IOException e) {
                textArea.setText("Error reading recommendations file.");
            }
        } else {
            textArea.setText(
                "No citizen recommendations have been submitted yet\n" +
                "for this ministry."
            );
        }

        Button backButton = new Button("Back");
        backButton.setOnAction(e ->
                new ViewEditBudgetScreen(user, userManager).show(stage)
        );

        VBox top = new VBox(6, title, subtitle);
        top.setAlignment(Pos.CENTER);
        top.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(textArea);
        root.setBottom(backButton);

        BorderPane.setMargin(textArea, new Insets(10));
        BorderPane.setMargin(backButton, new Insets(10));
        BorderPane.setAlignment(backButton, Pos.CENTER);

        Scene scene = new Scene(root, 650, 500);
        stage.setTitle("View Recommendations");
        stage.setScene(scene);
        stage.show();
    }
}