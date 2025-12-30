package guiFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import UserFeatures.Edit;
import UserManagement.User;
import UserManagement.UserManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * EditHistoryScreen - View and manage edit history
 * 
 * Shows all recent budget changes and allows undo operations
 */
public class EditHistoryScreen {

    private final User user;
    private final UserManager userManager;

    public EditHistoryScreen(User user, UserManager userManager) {
        this.user = user;
        this.userManager = userManager;
    }

    public void show(Stage stage) {

        Label title = new Label("Edit History");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Text area to display history
        TextArea historyArea = new TextArea();
        historyArea.setEditable(false);
        historyArea.setWrapText(false);
        historyArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 11px;");

        // Load history from file
        try {
            if (Files.exists(Paths.get("src/main/resources/NecessaryFilesAndData/edithistory.txt"))) {
                String historyContent = Files.readString(Paths.get("src/main/resources/NecessaryFilesAndData/edithistory.txt"));
                if (historyContent.trim().isEmpty()) {
                    historyArea.setText("No edit history available.");
                } else {
                    historyArea.setText(historyContent);
                }
            } else {
                historyArea.setText("No edit history file found.");
            }
        } catch (IOException e) {
            historyArea.setText("Error reading edit history: " + e.getMessage());
        }

        // Undo controls
        Label undoLabel = new Label("Undo last changes:");
        undoLabel.setStyle("-fx-font-weight: bold;");

        Spinner<Integer> undoSpinner = new Spinner<>(0, Edit.history.getIndex() + 1, 0);
        undoSpinner.setEditable(true);
        undoSpinner.setPrefWidth(100);

        Button undoBtn = new Button("Undo");
        undoBtn.setDisable(Edit.history.getIndex() < 0);

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: green;");

        undoBtn.setOnAction(e -> {
            int numChanges = undoSpinner.getValue();
            
            if (numChanges <= 0) {
                statusLabel.setStyle("-fx-text-fill: red;");
                statusLabel.setText("Please select number of changes to undo");
                return;
            }

            if (numChanges > Edit.history.getIndex() + 1) {
                statusLabel.setStyle("-fx-text-fill: red;");
                statusLabel.setText("Cannot undo more than " + (Edit.history.getIndex() + 1) + " changes");
                return;
            }

            // Confirmation
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Undo");
            confirm.setHeaderText("Undo " + numChanges + " changes?");
            confirm.setContentText("This will reverse the last " + numChanges + " budget changes.");

            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // Perform undo
                    for (int i = 0; i < numChanges; i++) {
                        Edit.history.undo();
                    }

                    statusLabel.setStyle("-fx-text-fill: green;");
                    statusLabel.setText("Successfully undone " + numChanges + " changes!");

                    // Refresh the screen
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException ex) {}
                    show(stage);
                }
            });
        });

        Button clearBtn = new Button("Clear History");
        clearBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Clear History");
            confirm.setHeaderText("Are you sure?");
            confirm.setContentText("This will permanently delete the edit history file.");

            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        Files.deleteIfExists(Paths.get("src/main/resources/NecessaryFilesAndData/edithistory.txt"));
                        statusLabel.setStyle("-fx-text-fill: green;");
                        statusLabel.setText("History cleared!");
                        historyArea.setText("No edit history available.");
                    } catch (IOException ex) {
                        statusLabel.setStyle("-fx-text-fill: red;");
                        statusLabel.setText("Error clearing history: " + ex.getMessage());
                    }
                }
            });
        });

        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> {
            if (user.getRole() == User.Role.CITIZEN) {
                new VirtualEditScreen(user, userManager).show(stage);
            } else {
                new EditBudgetScreen(user, userManager).show(stage);
            }
        });

        // Layout
        HBox undoControls = new HBox(10, undoLabel, undoSpinner, undoBtn);
        undoControls.setAlignment(Pos.CENTER_LEFT);
        undoControls.setPadding(new Insets(10));

        HBox bottomButtons = new HBox(15, clearBtn, backBtn);
        bottomButtons.setAlignment(Pos.CENTER);
        bottomButtons.setPadding(new Insets(10));

        VBox controls = new VBox(10, undoControls, statusLabel);
        controls.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(title);
        BorderPane.setAlignment(title, Pos.CENTER);
        BorderPane.setMargin(title, new Insets(10));
        
        root.setCenter(historyArea);
        BorderPane.setMargin(historyArea, new Insets(10));

        VBox bottom = new VBox(10, controls, bottomButtons);
        root.setBottom(bottom);

        stage.setScene(new Scene(root, 700, 550));
        stage.setTitle("Edit History");
        stage.show();
    }
}