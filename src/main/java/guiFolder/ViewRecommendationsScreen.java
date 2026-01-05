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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * ViewRecommendationsScreen
 *
 * Ministry Member screen for viewing citizen recommendations.
 * (UI improved – logic unchanged)
 */
public class ViewRecommendationsScreen {

    private final User user;
    private final UserManager userManager;

    private static final Path DATA_DIR =
            Path.of("src/main/resources/NecessaryFilesAndData/ProposalsFromCitizens");

    public ViewRecommendationsScreen(User user, UserManager userManager) {
        this.user = user;
        this.userManager = userManager;
    }

    public void show(Stage stage) {

        /* ================= ACCESS CONTROL ================= */
        if (!(user instanceof MinistryMember)) {
            Alert a = new Alert(Alert.AlertType.ERROR,
                    "Access denied: Only Ministry Members can view recommendations.");

            a.getDialogPane().getStylesheets().add(
                    getClass().getResource("/css/DarkTheme.css").toExternalForm()
            );
            a.getDialogPane().getStyleClass().add("dark-dialog");

            a.showAndWait();
            return;
        }

        MinistryMember mm = (MinistryMember) user;
        String ministryName = mm.getMinistryName();

        /* ================= TITLE ================= */
        Label title = new Label("CITIZEN RECOMMENDATIONS");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Ministry: " + ministryName);
        subtitle.getStyleClass().add("subtitle");

        VBox headerCard = new VBox(6, title, subtitle);
        headerCard.getStyleClass().add("card");
        headerCard.setPadding(new Insets(18));

        /* ================= TEXT AREA ================= */
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setFocusTraversable(false);
        textArea.getStyleClass().add("dark-textarea");

        VBox.setVgrow(textArea, Priority.ALWAYS);

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

        VBox contentCard = new VBox(textArea);
        contentCard.getStyleClass().add("card");
        contentCard.setPadding(new Insets(16));
        VBox.setVgrow(contentCard, Priority.ALWAYS);

        /* ================= BUTTONS ================= */
        Button backButton = new Button("Back");
        backButton.getStyleClass().add("button");

        backButton.setOnAction(e ->
                new ViewEditBudgetScreen(user, userManager).show(stage)
        );

        HBox actions = new HBox(backButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        /* ================= ROOT ================= */
        VBox content = new VBox(20, headerCard, contentCard, actions);
        content.setPadding(new Insets(26));
        VBox.setVgrow(contentCard, Priority.ALWAYS);

        BorderPane root = new BorderPane(content);

        Scene scene = new Scene(root, 1100, 720);
        scene.getStylesheets().add(
                getClass().getResource("/css/DarkTheme.css").toExternalForm()
        );

        stage.setTitle("View Recommendations");
        stage.setScene(scene);
        stage.show();
        stage.centerOnScreen();

    }
}
