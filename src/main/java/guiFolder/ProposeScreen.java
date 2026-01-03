package guiFolder;

import UserFeatures.Propose;
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
 * ProposeScreen
 *
 * GUI screen for Ministry Members to submit proposals
 * for their assigned ministry.
 */
public class ProposeScreen {

    private final User user;
    private final UserManager userManager;

    public ProposeScreen(User user, UserManager userManager) {
        this.user = user;
        this.userManager = userManager;
    }

    public void show(Stage stage) {

        /* ================= ACCESS CONTROL ================= */
        if (!(user instanceof MinistryMember)) {
            Alert a = new Alert(Alert.AlertType.ERROR,
                    "Access denied: Only Ministry Members can submit proposals.");

            a.getDialogPane().getStylesheets().add(
                    getClass().getResource("/css/DarkTheme.css").toExternalForm()
            );
            a.getDialogPane().getStyleClass().add("dark-dialog");

            a.showAndWait();
            return;
        }

        MinistryMember mm = (MinistryMember) user;
        String ministryName = mm.getMinistryName();

        Propose propose = new Propose(ministryName);

        /* ================= TITLE ================= */
        Label title = new Label("SUBMIT PROPOSAL");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Ministry: " + ministryName);
        subtitle.getStyleClass().add("subtitle");

        VBox headerCard = new VBox(6, title, subtitle);
        headerCard.getStyleClass().add("card");
        headerCard.setPadding(new Insets(18));

        /* ================= TEXT AREA ================= */
        Label inputLabel = new Label("Proposal text");
        inputLabel.setStyle("-fx-font-weight: bold;");

        TextArea proposalArea = new TextArea();
        proposalArea.setPromptText(
                "Describe your proposal clearly and concisely...\n\n" +
                "Example:\n" +
                "- Objective\n" +
                "- Estimated impact\n" +
                "- Implementation details"
        );
        proposalArea.setWrapText(true);
        VBox.setVgrow(proposalArea, Priority.ALWAYS);

        VBox inputCard = new VBox(10, inputLabel, proposalArea);
        inputCard.getStyleClass().add("card");
        inputCard.setPadding(new Insets(16));
        VBox.setVgrow(inputCard, Priority.ALWAYS);

        /* ================= STATUS ================= */
        Label statusLabel = new Label();
        statusLabel.getStyleClass().add("error");

        /* ================= BUTTONS ================= */
        Button submitButton = new Button("Submit Proposal");
        submitButton.getStyleClass().addAll("button", "primary");

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("button");

        submitButton.setOnAction(e -> {
            String text = proposalArea.getText();

            try {
                propose.submitProposal(text);

                Alert ok = new Alert(Alert.AlertType.INFORMATION);
                ok.setHeaderText("Proposal Submitted");
                ok.setContentText(
                        "Your proposal has been saved successfully\n" +
                        "for the Ministry of " + ministryName + "."
                );

                ok.getDialogPane().getStylesheets().add(
                        getClass().getResource("/css/DarkTheme.css").toExternalForm()
                );
                ok.getDialogPane().getStyleClass().add("dark-dialog");

                ok.showAndWait();

                proposalArea.clear();
                statusLabel.setText("");

            } catch (IllegalArgumentException ex) {
                statusLabel.setText(ex.getMessage());
            } catch (RuntimeException ex) {
                statusLabel.setText("Error saving proposal.");
            }
        });

        backButton.setOnAction(e ->
                new ViewEditBudgetScreen(user, userManager).show(stage)
        );

        HBox actions = new HBox(12, submitButton, backButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        /* ================= ROOT ================= */
        VBox content = new VBox(
                20,
                headerCard,
                inputCard,
                statusLabel,
                actions
        );
        content.setPadding(new Insets(26));
        VBox.setVgrow(inputCard, Priority.ALWAYS);

        BorderPane root = new BorderPane(content);

        Scene scene = new Scene(root, 1100, 720);
        scene.getStylesheets().add(
                getClass().getResource("/css/DarkTheme.css").toExternalForm()
        );

        stage.setTitle("Submit Proposal");
        stage.setScene(scene);
        stage.show();
    }
}
