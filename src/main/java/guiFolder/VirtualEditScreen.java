package guiFolder;

import java.nio.file.Files;
import java.nio.file.Path;

import UserFeatures.CreatingMinistries;
import UserFeatures.Edit;
import UserFeatures.Ministry;
import UserFeatures.UserBudgetFileUtil;
import UserFeatures.UserBudgetPersistence;
import UserManagement.CurrentSession;
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
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
        // Set current user in session for auto-save
        CurrentSession.setUser(user);

        // Don't load anything here - the alert in edit modes will handle loading
        // But define governorPath for Reset button
        Path governorPath = Path.of("src/main/resources/NecessaryFilesAndData/Governor_2026.csv");

        // ===== UI (Î¼Îµ DarkTheme classes) =====
        Label title = new Label("Virtual Budget Editing");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Simulate budget changes (your changes are saved separately)");
        subtitle.getStyleClass().add("subtitle");

        Button simpleEditBtn = new Button("Simple Edit");
        simpleEditBtn.getStyleClass().addAll("button", "primary");
        simpleEditBtn.setMaxWidth(Double.MAX_VALUE);
        simpleEditBtn.setOnAction(e -> showLoadAlertThenSimpleEdit(stage));

        Button bulkEditBtn = new Button("Bulk Edit");
        bulkEditBtn.getStyleClass().addAll("button", "subtle");
        bulkEditBtn.setMaxWidth(Double.MAX_VALUE);
        bulkEditBtn.setOnAction(e -> showLoadAlertThenBulkEdit(stage));

        Button historyBtn = new Button("View Edit History");
        historyBtn.getStyleClass().addAll("button", "subtle");
        historyBtn.setMaxWidth(Double.MAX_VALUE);
        historyBtn.setOnAction(e -> new EditHistoryScreen(user, userManager).show(stage));

        Button resetBtn = new Button("Reset to Original");
        resetBtn.getStyleClass().addAll("button", "subtle");
        resetBtn.setMaxWidth(Double.MAX_VALUE);
        resetBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Reset Budget");
            confirm.setHeaderText("Are you sure?");
            confirm.setContentText("This will discard all your virtual changes.");

            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    Edit.balance = 0;
                    CreatingMinistries.loadUserBudgets(governorPath, 2026);
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Reset Complete");
                    success.setContentText("Budget has been reset to original.");
                    success.showAndWait();
                }
            });
        });

        Button backBtn = new Button("Back");
        backBtn.getStyleClass().addAll("button", "subtle");
        backBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.setOnAction(e -> new ViewEditBudgetScreen(user, userManager).show(stage));

        VBox buttonsBox = new VBox(10,
                simpleEditBtn,
                bulkEditBtn,
                historyBtn,
                resetBtn,
                new Separator(),
                backBtn
        );
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setFillWidth(true);

        VBox card = new VBox(12, title, subtitle, buttonsBox);
        card.getStyleClass().addAll("card");
        card.setMaxWidth(520);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(24));
        root.setCenter(card);
        BorderPane.setAlignment(card, Pos.CENTER);

        Scene scene = new Scene(root, 720, 520);
        scene.getStylesheets().add(getClass().getResource("/css/DarkTheme.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Virtual Edit");
        stage.show();
    }

    // ===== Load Alert Helper Methods =====
    private void showLoadAlertThenSimpleEdit(Stage parentStage) {
        if (checkAndShowLoadAlert(parentStage)) {
            showSimpleEditDialog(parentStage);
        }
    }

    private void showLoadAlertThenBulkEdit(Stage parentStage) {
        if (checkAndShowLoadAlert(parentStage)) {
            new BulkEditScreen(user, userManager).show(parentStage);
        }
    }

    /**
     * Checks if user has saved budget and shows load alert.
     * Returns true if user should proceed (loaded or fresh start).
     * Returns false if user cancelled.
     */
    private boolean checkAndShowLoadAlert(Stage parentStage) {
        Path userBudgetFile = UserBudgetFileUtil.getUserBudgetFile(user, 2026);
        Path governorPath = Path.of("src/main/resources/NecessaryFilesAndData/Governor_2026.csv");

        if (Files.exists(userBudgetFile)) {
            Alert loadAlert = new Alert(Alert.AlertType.CONFIRMATION);
            loadAlert.setTitle("Load Saved Budget");
            loadAlert.setHeaderText("You have a saved virtual budget.");
            loadAlert.setContentText("Do you want to load it or start fresh?");

            ButtonType loadBtn = new ButtonType("Load Saved");
            ButtonType freshBtn = new ButtonType("Start Fresh");
            ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            loadAlert.getButtonTypes().setAll(loadBtn, freshBtn, cancelBtn);

            var result = loadAlert.showAndWait();
            if (result.isPresent()) {
                if (result.get() == loadBtn) {
                    // Load saved budget (balance is loaded from file)
                    CreatingMinistries.loadUserBudgets(userBudgetFile, 2026);
                    return true;
                } else if (result.get() == freshBtn) {
                    // Start fresh: reset balance and load governor budget
                    Edit.balance = 0;
                    CreatingMinistries.loadUserBudgets(governorPath, 2026);
                    return true;
                }
            }
            return false; // User cancelled
        } else {
            // First time: reset balance, load governor budget and create user file
            Edit.balance = 0;
            CreatingMinistries.loadUserBudgets(governorPath, 2026);
            UserBudgetPersistence.saveUserBudgets(user, CreatingMinistries.ministries2026, 2026);
            return true;
        }
    }

    // ===== Simple Edit Dialog (Î¼Îµ Î­Î½Ï„Î¿Î½Î± Increase/Decrease + Select Ministry) =====
    private void showSimpleEditDialog(Stage parentStage) {
        Stage dialog = new Stage();
        dialog.initOwner(parentStage);
        dialog.setTitle("Virtual Simple Edit");

        VBox card = new VBox(14);
        card.getStyleClass().add("card");

        Label title = new Label("Edit Single Ministry Budget");
        title.getStyleClass().add("title");

        Label hint = new Label("Choose ministry, select Increase/Decrease, and apply amount.");
        hint.getStyleClass().add("subtitle");

        // ComboBox (Î­Î½Ï„Î¿Î½Î¿)
        ComboBox<String> ministryBox = new ComboBox<>();
        ministryBox.getStyleClass().add("combo-box");
        ministryBox.setPromptText("Select Ministry");
        ministryBox.setMaxWidth(Double.MAX_VALUE);

        for (Ministry m : CreatingMinistries.ministries2026) {
            if (m != null) ministryBox.getItems().add(m.getMinistryName());
        }

        Label currentBudgetLabel = new Label("");
        currentBudgetLabel.getStyleClass().add("subtitle");

        // ===== Segmented ToggleButtons (Increase/Decrease) =====
        ToggleGroup changeGroup = new ToggleGroup();

        ToggleButton increaseBtn = new ToggleButton("Increase");
        increaseBtn.getStyleClass().addAll("segment-btn", "increase");
        increaseBtn.setToggleGroup(changeGroup);
        increaseBtn.setSelected(true);
        increaseBtn.setMinWidth(140);

        ToggleButton decreaseBtn = new ToggleButton("Decrease");
        decreaseBtn.getStyleClass().addAll("segment-btn", "decrease");
        decreaseBtn.setToggleGroup(changeGroup);
        decreaseBtn.setMinWidth(140);

        HBox segmented = new HBox(increaseBtn, decreaseBtn);
        segmented.getStyleClass().add("segmented-box");
        segmented.setAlignment(Pos.CENTER_LEFT);

        TextField amountField = new TextField();
        amountField.setPromptText("Enter amount");
        amountField.setMaxWidth(Double.MAX_VALUE);

        Label balanceLabel = new Label("Available Balance: " + Ministry.getFormattedBudget(Edit.balance));
        balanceLabel.getStyleClass().add("subtitle");

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error");

        Button applyBtn = new Button("Apply");
        applyBtn.getStyleClass().addAll("button", "primary");

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().addAll("button", "subtle");
        cancelBtn.setOnAction(e -> dialog.close());

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

            String changeType = increaseBtn.isSelected() ? "Increase" : "Decrease";
            double currentBudget = Ministry.budgetSearchByName(ministry, CreatingMinistries.ministries2026);

            if (changeType.equals("Decrease") && amount > currentBudget) {
                errorLabel.setText("Cannot decrease more than current budget");
                return;
            }

            if (changeType.equals("Increase") && amount > Edit.balance) {
                errorLabel.setText("Insufficient balance");
                return;
            }

            // Apply the edit (backend logic)
            Edit editObj = new Edit(ministry, changeType, amount, "fixed");
            Edit.history.addEdit(editObj);
            editObj.editingbudget(editObj, false, false);

            // Update balance
            if (changeType.equals("Increase")) Edit.balance -= amount;
            else Edit.balance += amount;

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
        });

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(6));

        Label ministryLbl = new Label("Ministry:");
        ministryLbl.getStyleClass().add("subtitle");
        Label changeLbl = new Label("Change Type:");
        changeLbl.getStyleClass().add("subtitle");
        Label amountLbl = new Label("Amount:");
        amountLbl.getStyleClass().add("subtitle");

        form.addRow(0, ministryLbl, ministryBox);
        form.addRow(1, new Label(""), currentBudgetLabel);
        form.addRow(2, changeLbl, segmented);
        form.addRow(3, amountLbl, amountField);
        form.addRow(4, new Label(""), balanceLabel);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setMinWidth(110);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().setAll(c1, c2);

        HBox buttons = new HBox(10, applyBtn, cancelBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(title, hint, form, errorLabel, buttons);

        BorderPane root = new BorderPane(card);
        root.setPadding(new Insets(18));

        Scene s = new Scene(root, 620, 440);
        s.getStylesheets().add(getClass().getResource("/css/DarkTheme.css").toExternalForm());

        dialog.setScene(s);
        dialog.show();
    }
}