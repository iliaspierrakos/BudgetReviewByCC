package guiFolder;

import javafx.scene.control.Label;

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
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class EditBudgetScreen {

    private final User user;
    private final UserManager userManager;

    public EditBudgetScreen(User user, UserManager userManager) {
        this.user = user;
        this.userManager = userManager;
    }

    public void show(Stage stage) {
        reloadUserBudgets();

        // ===== Header =====
        Label title = new Label("Budget Editing");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Select an editing mode.");
        subtitle.getStyleClass().add("subtitle");

        VBox headerCard = new VBox(8, title, subtitle);
        headerCard.getStyleClass().addAll("card", "toolbar-card");
        headerCard.setMaxWidth(860);

        // ===== Cards (2x2) =====
        GridPane cards = new GridPane();
        cards.setHgap(14);
        cards.setVgap(14);
        cards.setAlignment(Pos.TOP_CENTER);
        cards.getStyleClass().add("action-grid");

        VBox simpleCard = actionCard(
                "Simple Edit",
                "Edit one ministry with a fixed amount.",
                () -> showSimpleEditDialog(stage)
        );

        VBox bulkCard = actionCard(
                "Bulk Edit",
                "Apply percentage or fixed changes to many ministries.",
                () -> new BulkEditScreen(user, userManager).show(stage)
        );

        VBox historyCard = actionCard(
                "View Edit History",
                "See all changes and audit trail.",
                () -> new EditHistoryScreen(user, userManager).show(stage)
        );

        VBox backCard = actionCard(
                "Back",
                "Return to main menu.",
                () -> new ViewEditBudgetScreen(user, userManager).show(stage)
        );
        backCard.getStyleClass().add("danger-card"); // subtle red border

        // place cards
        cards.add(simpleCard, 0, 0);
        cards.add(bulkCard,   1, 0);
        cards.add(historyCard,0, 1);
        cards.add(backCard,   1, 1);

        // allow stretching evenly
        ColumnConstraints col = new ColumnConstraints();
        col.setHgrow(Priority.ALWAYS);
        col.setFillWidth(true);
        cards.getColumnConstraints().addAll(col, col);

        // ===== Root =====
        VBox center = new VBox(14, headerCard, cards);
        center.setPadding(new Insets(18));
        center.setAlignment(Pos.TOP_CENTER);

        BorderPane root = new BorderPane(center);

        Scene scene = new Scene(root, 980, 650);
        scene.getStylesheets().add(getClass().getResource("/css/DarkTheme.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Edit Budget");
        stage.show();
    }

    private VBox actionCard(String title, String desc, Runnable onClick) {
        Label t = new Label(title);
        t.getStyleClass().add("action-title");

        Label d = new Label(desc);
        d.getStyleClass().add("action-desc");
        d.setWrapText(true);

        VBox box = new VBox(6, t, d);
        box.getStyleClass().addAll("card", "glass-card", "image-card");

        // consistent sizing
        box.setMinWidth(380);
        box.setPrefWidth(400);
        box.setMaxWidth(Double.MAX_VALUE);
        box.setMinHeight(118);

        box.setOnMouseClicked(e -> onClick.run());
        return box;
    }

    private void reloadUserBudgets() {
        Path userFile = UserBudgetFileUtil.getUserBudgetFile(user, 2026);

        try {
            if (user.getRole() == User.Role.GOVERNOR) {
                CreatingMinistries.loadUserBudgets(userFile, 2026);
            } else if (user.getRole() == User.Role.CITIZEN) {
                if (Files.exists(userFile)) {
                    CreatingMinistries.loadUserBudgets(userFile, 2026);
                } else {
                    Path govPath = Path.of("src/main/resources/NecessaryFilesAndData/Governor_2026.csv");
                    CreatingMinistries.loadUserBudgets(govPath, 2026);
                }
            } else {
                Path govPath = Path.of("src/main/resources/NecessaryFilesAndData/Governor_2026.csv");
                CreatingMinistries.loadUserBudgets(govPath, 2026);
            }
        } catch (Exception e) {
            System.err.println("Failed to load budgets: " + e.getMessage());
        }
    }

    private void showSimpleEditDialog(Stage parentStage) {

        Stage dialog = new Stage();
        dialog.initOwner(parentStage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Simple Edit");

        // ===== Header =====
        Label title = new Label("Edit Single Ministry");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Select ministry, choose change type and enter amount.");
        subtitle.getStyleClass().add("subtitle");

        VBox header = new VBox(6, title, subtitle);

        // ===== Form controls =====
        ComboBox<String> ministryBox = new ComboBox<>();
        for (Ministry m : CreatingMinistries.ministries2026) {
            if (m != null) ministryBox.getItems().add(m.getMinistryName());
        }
        ministryBox.setPromptText("Select Ministry");
        ministryBox.setMaxWidth(Double.MAX_VALUE);

        Label currentBudgetLabel = new Label("Current Budget: —");
        currentBudgetLabel.getStyleClass().add("muted");

        // segmented change type
        ToggleGroup changeGroup = new ToggleGroup();
        ToggleButton increaseBtn = new ToggleButton("Increase");
        ToggleButton decreaseBtn = new ToggleButton("Decrease");
        increaseBtn.setToggleGroup(changeGroup);
        decreaseBtn.setToggleGroup(changeGroup);
        increaseBtn.setSelected(true);

        increaseBtn.getStyleClass().add("role-toggle");
        decreaseBtn.getStyleClass().add("role-toggle");

        HBox changeTypeBox = new HBox(10, increaseBtn, decreaseBtn);
        changeTypeBox.setAlignment(Pos.CENTER_LEFT);

        TextField amountField = new TextField();
        amountField.setPromptText("Amount (e.g., 1000000)");
        amountField.setMaxWidth(Double.MAX_VALUE);

        Label balanceLabel = new Label("Available Balance: " + Ministry.getFormattedBudget(Edit.balance));
        balanceLabel.getStyleClass().add("success");

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error");

        // ===== Buttons =====
        Button applyBtn = new Button("Apply");
        applyBtn.getStyleClass().addAll("button", "primary");
        applyBtn.setDisable(true);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().addAll("button", "subtle");

        HBox buttons = new HBox(10, cancelBtn, applyBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        // ===== Layout grid =====
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setMinWidth(130);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(c1, c2);

        form.addRow(0, new Label("Ministry:"), ministryBox);
        form.addRow(1, new Label(""), currentBudgetLabel);
        form.addRow(2, new Label("Change:"), changeTypeBox);
        form.addRow(3, new Label("Amount:"), amountField);
        form.addRow(4, new Label(""), balanceLabel);

        // ===== Validation helpers =====
        Runnable updateApplyEnabled = () -> {
            boolean hasMin = ministryBox.getValue() != null;
            boolean hasAmt = !amountField.getText().trim().isEmpty();
            applyBtn.setDisable(!(hasMin && hasAmt));
        };

        ministryBox.valueProperty().addListener((obs, o, n) -> {
            errorLabel.setText("");
            updateApplyEnabled.run();
            if (n != null) {
                double budget = Ministry.budgetSearchByName(n, CreatingMinistries.ministries2026);
                currentBudgetLabel.setText("Current Budget: " + Ministry.getFormattedBudget(budget));
            } else {
                currentBudgetLabel.setText("Current Budget: —");
            }
        });

        amountField.textProperty().addListener((obs, o, n) -> {
            errorLabel.setText("");
            updateApplyEnabled.run();
        });

        cancelBtn.setOnAction(e -> dialog.close());

        applyBtn.setOnAction(e -> {
            errorLabel.setText("");

            String ministry = ministryBox.getValue();
            String amountStr = amountField.getText().trim();

            if (ministry == null) {
                errorLabel.setText("Please select a ministry.");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    errorLabel.setText("Amount must be positive.");
                    return;
                }
            } catch (NumberFormatException ex) {
                errorLabel.setText("Invalid amount.");
                return;
            }

            boolean isIncrease = changeGroup.getSelectedToggle() == increaseBtn;
            String changeType = isIncrease ? "Increase" : "Decrease";

            double currentBudget = Ministry.budgetSearchByName(ministry, CreatingMinistries.ministries2026);

            if (!isIncrease && amount > currentBudget) {
                errorLabel.setText("Cannot decrease more than current budget.");
                return;
            }
            if (isIncrease && amount > Edit.balance) {
                errorLabel.setText("Insufficient balance.");
                return;
            }

            // Apply edit (your existing logic)
            Edit editObj = new Edit(ministry, changeType, amount, "fixed");
            Edit.history.addEdit(editObj);
            editObj.editingbudget(editObj, false, false);

            // Update balance
            if (isIncrease) Edit.balance -= amount;
            else Edit.balance += amount;

            // Reload from file
            Path userFile = UserBudgetFileUtil.getUserBudgetFile(user, 2026);
            try {
                CreatingMinistries.loadUserBudgets(userFile, 2026);
            } catch (Exception ex) {
                System.err.println("Failed to reload budgets: " + ex.getMessage());
            }

            double newBudget = Ministry.budgetSearchByName(ministry, CreatingMinistries.ministries2026);

            // Nice themed success
            showThemedAlert(
                    Alert.AlertType.INFORMATION,
                    "Success",
                    "Budget Updated Successfully",
                    "Ministry: " + ministry + "\n" +
                            "Action: " + changeType + " by " + Ministry.getFormattedBudget(amount) + "\n" +
                            "New Budget: " + Ministry.getFormattedBudget(newBudget) + "\n" +
                            "Available Balance: " + Ministry.getFormattedBudget(Edit.balance)
            );

            // close + refresh screen
            dialog.close();
            show(parentStage);
        });

        // ===== Card wrapper =====
        VBox card = new VBox(12, header, new Separator(), form, errorLabel, buttons);
        card.getStyleClass().addAll("card", "toolbar-card");
        card.setPadding(new Insets(18));
        card.setMaxWidth(620);

        VBox root = new VBox(card);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(18));

        Scene scene = new Scene(root, 740, 540);
        scene.getStylesheets().add(getClass().getResource("/css/DarkTheme.css").toExternalForm());

        dialog.setScene(scene);
        dialog.show();
    }

    private void showThemedAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/DarkTheme.css").toExternalForm()
        );
        alert.showAndWait();
    }
}
