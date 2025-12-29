package guiFolder;

import UserFeatures.*;
import UserManagement.User;
import UserManagement.UserManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * BulkEditScreen - For bulk budget operations
 * 
 * Allows editing multiple ministries at once:
 * - Apply percentage change to all ministries
 * - Apply fixed amount change to all ministries
 * - Apply change to selected ministries only
 */
public class BulkEditScreen {

    private final User user;
    private final UserManager userManager;

    public BulkEditScreen(User user, UserManager userManager) {
        this.user = user;
        this.userManager = userManager;
    }

    public void show(Stage stage) {

        Label title = new Label("Bulk Edit");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label subtitle = new Label("Apply changes to multiple ministries at once");

        Button percentAllBtn = new Button("Percentage Change (All)");
        percentAllBtn.setMinWidth(250);
        percentAllBtn.setOnAction(e -> showPercentageAllDialog(stage));

        Button fixedAllBtn = new Button("Fixed Amount Change (All)");
        fixedAllBtn.setMinWidth(250);
        fixedAllBtn.setOnAction(e -> showFixedAllDialog(stage));

        Button selectedBtn = new Button("Change Selected Ministries");
        selectedBtn.setMinWidth(250);
        selectedBtn.setOnAction(e -> showSelectedMinistriesDialog(stage));

        Button backBtn = new Button("Back");
        backBtn.setMinWidth(250);
        backBtn.setOnAction(e -> {
            if (user.getRole() == User.Role.CITIZEN) {
                new VirtualEditScreen(user, userManager).show(stage);
            } else {
                new EditBudgetScreen(user, userManager).show(stage);
            }
        });

        VBox buttonsBox = new VBox(15, percentAllBtn, fixedAllBtn, selectedBtn, backBtn);
        buttonsBox.setAlignment(Pos.CENTER);

        VBox root = new VBox(20, title, subtitle, buttonsBox);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));

        stage.setScene(new Scene(root, 450, 380));
        stage.setTitle("Bulk Edit");
        stage.show();
    }

    /**
     * Dialog for percentage change to all ministries
     */
    private void showPercentageAllDialog(Stage parentStage) {
        Stage dialog = new Stage();
        dialog.setTitle("Percentage Change - All Ministries");

        Label title = new Label("Apply Percentage Change to ALL Ministries");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        TextField percentField = new TextField();
        percentField.setPromptText("Enter percentage (e.g., 5 or -10)");

        Label balanceLabel = new Label("Available Balance: " + Ministry.getFormattedBudget(Edit.balance));
        balanceLabel.setStyle("-fx-text-fill: green;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        Button previewBtn = new Button("Preview");
        Button applyBtn = new Button("Apply");
        Button cancelBtn = new Button("Cancel");

        TextArea previewArea = new TextArea();
        previewArea.setEditable(false);
        previewArea.setPrefHeight(200);
        previewArea.setVisible(false);

        previewBtn.setOnAction(e -> {
            errorLabel.setText("");
            String percentStr = percentField.getText().trim();
            
            if (percentStr.isEmpty()) {
                errorLabel.setText("Please enter a percentage");
                return;
            }

            double percentage;
            try {
                percentage = Double.parseDouble(percentStr);
                if (percentage <= -100) {
                    errorLabel.setText("Cannot decrease by 100% or more");
                    return;
                }
            } catch (NumberFormatException ex) {
                errorLabel.setText("Invalid percentage");
                return;
            }

            // Calculate total change
            double totalChange = 0;
            StringBuilder preview = new StringBuilder();
            preview.append("PREVIEW - Percentage Change: ").append(percentage).append("%\n\n");
            preview.append(String.format("%-40s %15s %15s\n", "Ministry", "Current", "New"));
            preview.append("=".repeat(70)).append("\n");

            for (Ministry m : CreatingMinistries.ministries2026) {
                if (m != null) {
                    double oldBudget = m.getBudget();
                    double newBudget = oldBudget * (1 + percentage / 100.0);
                    double change = newBudget - oldBudget;
                    totalChange += change;

                    preview.append(String.format("%-40s %15s %15s\n",
                        m.getMinistryName(),
                        Ministry.getFormattedBudget(oldBudget),
                        Ministry.getFormattedBudget(newBudget)));
                }
            }

            preview.append("=".repeat(70)).append("\n");
            preview.append("Total Change: ").append(Ministry.getFormattedBudget(totalChange)).append("\n");
            preview.append("Available Balance: ").append(Ministry.getFormattedBudget(Edit.balance)).append("\n");

            if (totalChange > Edit.balance) {
                preview.append("\nWARNING: Insufficient balance!");
                applyBtn.setDisable(true);
            } else {
                applyBtn.setDisable(false);
            }

            previewArea.setText(preview.toString());
            previewArea.setVisible(true);
        });

        applyBtn.setOnAction(e -> {
            double percentage = Double.parseDouble(percentField.getText().trim());
            
            // Apply to all ministries
            for (Ministry m : CreatingMinistries.ministries2026) {
                if (m != null) {
                    double oldBudget = m.getBudget();
                    double newBudget = oldBudget * (1 + percentage / 100.0);
                    
                    String changeType = percentage >= 0 ? "Increase" : "Decrease";
                    Edit editObj = new Edit(m.getMinistryName(), changeType, Math.abs(percentage), "percentage");
                    Edit.history.addEdit(editObj);
                    EditHistory.historyOfEdit(m.getMinistryName(), oldBudget, newBudget, 0);
                    m.setBudget(newBudget);
                }
            }

            // Update balance
            double totalChange = 0;
            for (Ministry m : CreatingMinistries.ministries2026) {
                if (m != null) {
                    totalChange += m.getBudget() * (percentage / 100.0);
                }
            }

            if (percentage > 0) {
                Edit.balance -= totalChange;
            } else {
                Edit.balance += Math.abs(totalChange);
            }

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Success");
            success.setContentText("Bulk percentage change applied successfully!");
            success.showAndWait();

            dialog.close();
            show(parentStage);
        });

        cancelBtn.setOnAction(e -> dialog.close());

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(12);
        form.setPadding(new Insets(10));
        form.addRow(0, new Label("Percentage:"), percentField);
        form.addRow(1, new Label(""), balanceLabel);

        HBox buttons = new HBox(10, previewBtn, applyBtn, cancelBtn);
        buttons.setAlignment(Pos.CENTER);

        VBox dialogRoot = new VBox(15, title, form, errorLabel, buttons, previewArea);
        dialogRoot.setPadding(new Insets(20));

        dialog.setScene(new Scene(dialogRoot, 600, 500));
        dialog.show();
    }

    /**
     * Dialog for fixed amount change to all ministries
     */
    private void showFixedAllDialog(Stage parentStage) {
        Stage dialog = new Stage();
        dialog.setTitle("Fixed Amount Change - All Ministries");

        Label title = new Label("Apply Fixed Amount to ALL Ministries");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        TextField amountField = new TextField();
        amountField.setPromptText("Enter amount (positive or negative)");

        Label balanceLabel = new Label("Available Balance: " + Ministry.getFormattedBudget(Edit.balance));
        balanceLabel.setStyle("-fx-text-fill: green;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        Button previewBtn = new Button("Preview");
        Button applyBtn = new Button("Apply");
        Button cancelBtn = new Button("Cancel");

        TextArea previewArea = new TextArea();
        previewArea.setEditable(false);
        previewArea.setPrefHeight(200);
        previewArea.setVisible(false);

        previewBtn.setOnAction(e -> {
            errorLabel.setText("");
            String amountStr = amountField.getText().trim();
            
            if (amountStr.isEmpty()) {
                errorLabel.setText("Please enter an amount");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException ex) {
                errorLabel.setText("Invalid amount");
                return;
            }

            // Check for negative budgets
            boolean wouldCauseNegative = false;
            StringBuilder preview = new StringBuilder();
            preview.append("PREVIEW - Fixed Amount: ").append(Ministry.getFormattedBudget(amount)).append("\n\n");
            preview.append(String.format("%-40s %15s %15s\n", "Ministry", "Current", "New"));
            preview.append("=".repeat(70)).append("\n");

            int count = 0;
            for (Ministry m : CreatingMinistries.ministries2026) {
                if (m != null) {
                    count++;
                    double oldBudget = m.getBudget();
                    double newBudget = oldBudget + amount;
                    
                    if (newBudget < 0) {
                        wouldCauseNegative = true;
                    }

                    preview.append(String.format("%-40s %15s %15s\n",
                        m.getMinistryName(),
                        Ministry.getFormattedBudget(oldBudget),
                        Ministry.getFormattedBudget(newBudget)));
                }
            }

            double totalChange = amount * count;
            preview.append("=".repeat(70)).append("\n");
            preview.append("Total Change: ").append(Ministry.getFormattedBudget(totalChange)).append("\n");
            preview.append("Available Balance: ").append(Ministry.getFormattedBudget(Edit.balance)).append("\n");

            if (wouldCauseNegative) {
                preview.append("\nERROR: Would cause negative budgets!");
                applyBtn.setDisable(true);
            } else if (totalChange > Edit.balance) {
                preview.append("\nWARNING: Insufficient balance!");
                applyBtn.setDisable(true);
            } else {
                applyBtn.setDisable(false);
            }

            previewArea.setText(preview.toString());
            previewArea.setVisible(true);
        });

        applyBtn.setOnAction(e -> {
            double amount = Double.parseDouble(amountField.getText().trim());
            
            // Apply to all ministries
            for (Ministry m : CreatingMinistries.ministries2026) {
                if (m != null) {
                    double oldBudget = m.getBudget();
                    double newBudget = oldBudget + amount;
                    
                    String changeType = amount >= 0 ? "Increase" : "Decrease";
                    Edit editObj = new Edit(m.getMinistryName(), changeType, Math.abs(amount), "fixed");
                    Edit.history.addEdit(editObj);
                    EditHistory.historyOfEdit(m.getMinistryName(), oldBudget, newBudget, 0);
                    m.setBudget(newBudget);
                }
            }

            // Update balance
            int count = 0;
            for (Ministry m : CreatingMinistries.ministries2026) {
                if (m != null) count++;
            }
            double totalChange = amount * count;

            if (amount > 0) {
                Edit.balance -= totalChange;
            } else {
                Edit.balance += Math.abs(totalChange);
            }

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Success");
            success.setContentText("Bulk fixed amount change applied successfully!");
            success.showAndWait();

            dialog.close();
            show(parentStage);
        });

        cancelBtn.setOnAction(e -> dialog.close());

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(12);
        form.setPadding(new Insets(10));
        form.addRow(0, new Label("Amount:"), amountField);
        form.addRow(1, new Label(""), balanceLabel);

        HBox buttons = new HBox(10, previewBtn, applyBtn, cancelBtn);
        buttons.setAlignment(Pos.CENTER);

        VBox dialogRoot = new VBox(15, title, form, errorLabel, buttons, previewArea);
        dialogRoot.setPadding(new Insets(20));

        dialog.setScene(new Scene(dialogRoot, 600, 500));
        dialog.show();
    }

    /**
     * Dialog for editing selected ministries only
     */
    private void showSelectedMinistriesDialog(Stage parentStage) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Selected Ministries");
        alert.setHeaderText("Feature Coming Soon");
        alert.setContentText("Selected ministries editing will be implemented in the next version.");
        alert.showAndWait();
    }
}