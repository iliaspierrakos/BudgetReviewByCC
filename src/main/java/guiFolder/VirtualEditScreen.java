package guiFolder;

import java.nio.file.Files;
import java.nio.file.Path;

import UserFeatures.CreatingMinistries;
import UserFeatures.Edit;
import UserFeatures.Ministry;
import UserFeatures.UserBudgetFileUtil;
import UserManagement.User;
import UserManagement.UserManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class VirtualEditScreen {

    private final User user;
    private final UserManager userManager;

    public VirtualEditScreen(User user, UserManager userManager) {
        this.user = user;
        this.userManager = userManager;
    }

    public void show(Stage stage) {

        // Load user's saved budget if exists
        Path userBudgetFile = UserBudgetFileUtil.getUserBudgetFile(user, 2026);
        Path governorPath = Path.of("NecessaryFilesAndData/Governor_2026.csv");

        if (Files.exists(userBudgetFile)) {
            Alert loadAlert = new Alert(Alert.AlertType.CONFIRMATION);
            loadAlert.setTitle("Load Saved Budget");
            loadAlert.setHeaderText("You have a saved virtual budget.");
            loadAlert.setContentText("Do you want to load it or start fresh?");
            
            ButtonType loadBtn = new ButtonType("Load Saved");
            ButtonType freshBtn = new ButtonType("Start Fresh");
            ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            
            loadAlert.getButtonTypes().setAll(loadBtn, freshBtn, cancelBtn);
            
            loadAlert.showAndWait().ifPresent(response -> {
                if (response == loadBtn) {
                    CreatingMinistries.loadUserBudgets(userBudgetFile, 2026);
                } else if (response == freshBtn) {
                    CreatingMinistries.loadUserBudgets(governorPath, 2026);
                }
            });
        } else {
            CreatingMinistries.loadUserBudgets(governorPath, 2026);
        }

        Label title = new Label("Virtual Budget Editing");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label subtitle = new Label("Simulate budget changes (your changes are saved separately)");
        subtitle.setStyle("-fx-font-size: 12px; -fx-font-style: italic;");

        Button simpleEditBtn = new Button("Simple Edit");
        simpleEditBtn.setMinWidth(200);
        simpleEditBtn.setOnAction(e -> showSimpleEditDialog(stage));

        Button bulkEditBtn = new Button("Bulk Edit");
        bulkEditBtn.setMinWidth(200);
        bulkEditBtn.setOnAction(e -> new BulkEditScreen(user, userManager).show(stage));

        Button historyBtn = new Button("View Edit History");
        historyBtn.setMinWidth(200);
        historyBtn.setOnAction(e -> new EditHistoryScreen(user, userManager).show(stage));

        Button resetBtn = new Button("Reset to Original");
        resetBtn.setMinWidth(200);
        resetBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Reset Budget");
            confirm.setHeaderText("Are you sure?");
            confirm.setContentText("This will discard all your virtual changes.");
            
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    CreatingMinistries.loadUserBudgets(governorPath, 2026);
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Reset Complete");
                    success.setContentText("Budget has been reset to original.");
                    success.showAndWait();
                }
            });
        });

        Button backBtn = new Button("Back");
        backBtn.setMinWidth(200);
        backBtn.setOnAction(e -> new ViewEditBudgetScreen(user, userManager).show(stage));

        VBox buttonsBox = new VBox(12, simpleEditBtn, bulkEditBtn, historyBtn, resetBtn, backBtn);
        buttonsBox.setAlignment(Pos.CENTER);

        VBox root = new VBox(15, title, subtitle, buttonsBox);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(25));

        stage.setScene(new Scene(root, 450, 450));
        stage.setTitle("Virtual Edit");
        stage.show();
    }

    private void showSimpleEditDialog(Stage parentStage) {
        Stage dialog = new Stage();
        dialog.setTitle("Virtual Simple Edit");

        Label title = new Label("Edit Single Ministry Budget");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        ComboBox<String> ministryBox = new ComboBox<>();
        for (Ministry m : CreatingMinistries.ministries2026) {
            if (m != null) {
                ministryBox.getItems().add(m.getMinistryName());
            }
        }
        ministryBox.setPromptText("Select Ministry");

        Label currentBudgetLabel = new Label();
        currentBudgetLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e3a5f;");

        ToggleGroup changeGroup = new ToggleGroup();
        RadioButton increaseRb = new RadioButton("Increase");
        increaseRb.setToggleGroup(changeGroup);
        increaseRb.setSelected(true);
        RadioButton decreaseRb = new RadioButton("Decrease");
        decreaseRb.setToggleGroup(changeGroup);

        HBox changeTypeBox = new HBox(15, increaseRb, decreaseRb);
        changeTypeBox.setAlignment(Pos.CENTER);

        TextField amountField = new TextField();
        amountField.setPromptText("Enter amount");

        Label balanceLabel = new Label("Available Balance: " + Ministry.getFormattedBudget(Edit.balance));
        balanceLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        Button applyBtn = new Button("Apply");
        Button cancelBtn = new Button("Cancel");

        ministryBox.setOnAction(e -> {
            String selected = ministryBox.getValue();
            if (selected != null) {
                double budget = Ministry.budgetSearchByName(selected, CreatingMinistries.ministries2026);
                currentBudgetLabel.setText("Current Budget: " + Ministry.getFormattedBudget(budget));
            }
        });

        applyBtn.setOnAction(e -> {
            errorLabel.setText("");
            
            String ministry = ministryBox.getValue();
            if (ministry == null) {
                errorLabel.setText("Please select a ministry");
                return;
            }

            String amountStr = amountField.getText().trim();
            if (amountStr.isEmpty()) {
                errorLabel.setText("Please enter an amount");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    errorLabel.setText("Amount must be positive");
                    return;
                }
            } catch (NumberFormatException ex) {
                errorLabel.setText("Invalid amount");
                return;
            }

            String changeType = increaseRb.isSelected() ? "Increase" : "Decrease";
            
            double currentBudget = Ministry.budgetSearchByName(ministry, CreatingMinistries.ministries2026);
            
            if (changeType.equals("Decrease") && amount > currentBudget) {
                errorLabel.setText("Cannot decrease more than current budget");
                return;
            }
            
            if (changeType.equals("Increase") && amount > Edit.balance) {
                errorLabel.setText("Insufficient balance");
                return;
            }

            // Apply the edit
            Edit editObj = new Edit(ministry, changeType, amount, "fixed");
            Edit.history.addEdit(editObj);
            editObj.editingbudget(editObj, false, false);

            // Update balance
            if (changeType.equals("Increase")) {
                Edit.balance -= amount;
            } else {
                Edit.balance += amount;
            }

            // Reload from file to ensure data consistency
            Path userFile = UserBudgetFileUtil.getUserBudgetFile(user, 2026);
            try {
                CreatingMinistries.loadUserBudgets(userFile, 2026);
            } catch (Exception ex) {
                System.err.println("Failed to reload budgets: " + ex.getMessage());
            }

            // Refresh labels with updated data
            balanceLabel.setText("Available Balance: " + Ministry.getFormattedBudget(Edit.balance));
            double newBudget = Ministry.budgetSearchByName(ministry, CreatingMinistries.ministries2026);
            currentBudgetLabel.setText("Current Budget: " + Ministry.getFormattedBudget(newBudget));

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Success");
            success.setHeaderText("Virtual Budget Updated");
            success.setContentText(
                String.format(
                    "Virtual budget for %s has been %s by %s",
                    ministry,
                    changeType.toLowerCase() + "d",
                    Ministry.getFormattedBudget(amount)
                )
            );
            success.showAndWait();

            amountField.clear();
            errorLabel.setText("");
            
        });

        cancelBtn.setOnAction(e -> dialog.close());

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(12);
        form.setPadding(new Insets(10));
        form.addRow(0, new Label("Ministry:"), ministryBox);
        form.addRow(1, new Label(""), currentBudgetLabel);
        form.addRow(2, new Label("Change Type:"), changeTypeBox);
        form.addRow(3, new Label("Amount:"), amountField);
        form.addRow(4, new Label(""), balanceLabel);

        HBox buttons = new HBox(10, applyBtn, cancelBtn);
        buttons.setAlignment(Pos.CENTER);

        VBox dialogRoot = new VBox(15, title, form, errorLabel, buttons);
        dialogRoot.setPadding(new Insets(20));
        dialogRoot.setAlignment(Pos.CENTER);

        dialog.setScene(new Scene(dialogRoot, 450, 400));
        dialog.show();
    }
}