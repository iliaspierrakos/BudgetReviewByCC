package guiFolder;

import UserFeatures.RecommendationSystem;
import UserManagement.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

/**
 * SubmitRecommendationScreen (Citizen)
 *
 * GUI screen that allows a Citizen to submit one recommendation vote
 * for a selected ministry.
 */
public class SubmitRecommendationScreen {

    private final User user;
    private final RecommendationSystem recSystem;

    public SubmitRecommendationScreen(User user) {
        this.user = user;
        this.recSystem = new RecommendationSystem();
    }

    public void show(Stage stage) {

        // ===== Title =====
        Label title = new Label("Submit Recommendation");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // ===== Ministry selector =====
        ComboBox<String> ministryBox = new ComboBox<>();
        List<String> ministries = recSystem.getAvailableMinistries();
        ministryBox.getItems().addAll(ministries);
        ministryBox.setPromptText("Select a Ministry");

        // ===== Options (RadioButtons) =====
        ToggleGroup optionsGroup = new ToggleGroup();
        VBox optionsBox = new VBox(8);
        optionsBox.setPadding(new Insets(10));
        optionsBox.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 6; -fx-background-radius: 6;");

        Label optionsHint = new Label("Select one investment category:");
        optionsHint.setStyle("-fx-font-weight: bold;");

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: #cc0000;");

        Button submitButton = new Button("Submit");
        submitButton.setDisable(true);

        Button backButton = new Button("Back");

        // Helper: rebuild options list when ministry changes
        Runnable rebuildOptions = () -> {
            optionsBox.getChildren().clear();
            optionsBox.getChildren().add(optionsHint);

            String selectedMinistry = ministryBox.getValue();
            if (selectedMinistry == null) {
                submitButton.setDisable(true);
                return;
            }

            String[] opts = recSystem.getOptionsForMinistry(selectedMinistry);
            if (opts.length == 0) {
                submitButton.setDisable(true);
                Label noOptions = new Label("No options available for this ministry.");
                optionsBox.getChildren().add(noOptions);
                return;
            }

            for (int i = 0; i < opts.length; i++) {
                RadioButton rb = new RadioButton((i + 1) + ". " + opts[i]);
                rb.setToggleGroup(optionsGroup);
                rb.setUserData(i); // store optionIndex (0..4)
                optionsBox.getChildren().add(rb);
            }

            submitButton.setDisable(true);
            statusLabel.setText("");
        };

        ministryBox.valueProperty().addListener((obs, oldV, newV) -> {
            optionsGroup.selectToggle(null);
            rebuildOptions.run();
        });

        optionsGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            statusLabel.setText("");
            submitButton.setDisable(newT == null || ministryBox.getValue() == null);
        });

        submitButton.setOnAction(e -> {
            String ministry = ministryBox.getValue();
            Toggle selected = optionsGroup.getSelectedToggle();

            if (ministry == null) {
                statusLabel.setText("Please select a ministry.");
                return;
            }
            if (selected == null) {
                statusLabel.setText("Please select one category.");
                return;
            }

            int optionIndex = (int) selected.getUserData();

            try {
                recSystem.submitRecommendation(ministry, optionIndex);

                Alert ok = new Alert(Alert.AlertType.INFORMATION);
                ok.setTitle("Submitted");
                ok.setHeaderText("Thank you!");
                ok.setContentText("Your recommendation was submitted successfully.");
                ok.showAndWait();

                // Reset UI after submission
                optionsGroup.selectToggle(null);
                submitButton.setDisable(true);
                statusLabel.setText("");

            } catch (RuntimeException ex) {
                statusLabel.setText("Error saving recommendation. Check data folder permissions.");
            }
        });

        // IMPORTANT: back goes to your main menu screen after login
        // Replace this with your actual menu screen class if different.
        backButton.setOnAction(e -> new ViewEditBudgetScreen(user, null).show(stage));

        // ===== Layout =====
        HBox buttons = new HBox(10, submitButton, backButton);
        buttons.setAlignment(Pos.CENTER);

        VBox center = new VBox(12, title, ministryBox, optionsBox, statusLabel, buttons);
        center.setAlignment(Pos.TOP_CENTER);
        center.setPadding(new Insets(16));
        center.setMaxWidth(520);

        BorderPane root = new BorderPane(center);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 640, 520);
        stage.setTitle("Submit Recommendation");
        stage.setScene(scene);
        stage.show();

        // initial
        rebuildOptions.run();
    }
}
