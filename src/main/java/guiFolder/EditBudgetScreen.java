package guiFolder;

import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import java.nio.file.Files;
import java.nio.file.Path;

import UserFeatures.CreatingMinistries;
import UserFeatures.Edit;
import UserFeatures.Ministry;
import UserFeatures.UserBudgetFileUtil;
import UserManagement.User;
import UserManagement.UserManager;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class EditBudgetScreen {

    private final User user;
    private final UserManager userManager;

    public EditBudgetScreen(User user, UserManager userManager) {
        this.user = user;
        this.userManager = userManager;
    }

    public void show(Stage stage) {
        reloadUserBudgets();

        // TOP APP BAR
        Label appLogo = new Label("GovBudget");
        appLogo.getStyleClass().add("app-logo");

        Label bell = new Label("🔔");
        bell.getStyleClass().add("top-icon");

        Label settings = new Label("⚙");
        settings.getStyleClass().add("top-icon");

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        HBox topBar = new HBox(14, appLogo, topSpacer, bell, settings);
        topBar.getStyleClass().add("topbar");
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(14, 18, 14, 18));

        // HERO
        Label title = new Label("Budget Editing");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Select an editing mode.");
        subtitle.getStyleClass().add("subtitle");

        Label chip1 = new Label("Edits • 2026");
        chip1.getStyleClass().add("chip");

        Label chip2 = new Label("Role: " + user.getRole().name());
        chip2.getStyleClass().add("chip");

        Label chip3 = new Label("Balance: " + Ministry.getFormattedBudget(Edit.balance));
        chip3.getStyleClass().add("chip");

        HBox chips = new HBox(10, chip1, chip2, chip3);
        chips.setAlignment(Pos.CENTER_LEFT);

        VBox heroCard = new VBox(10, title, subtitle, chips);
        heroCard.getStyleClass().addAll("card", "toolbar-card", "hero-card");

        // CARDS
        GridPane cards = new GridPane();
        cards.setHgap(18);
        cards.setVgap(18);
        cards.setAlignment(Pos.TOP_CENTER);
        cards.getStyleClass().add("action-grid");

        ColumnConstraints col = new ColumnConstraints();
        col.setHgrow(Priority.ALWAYS);
        col.setFillWidth(true);
        cards.getColumnConstraints().addAll(col, col);

        VBox simpleCard = actionCard(
                "Simple Edit",
                "Edit one ministry with a fixed amount.",
                "/icons/edit.png",
                () -> showSimpleEditDialog(stage)
        );

        VBox bulkCard = actionCard(
                "Bulk Edit",
                "Apply percentage or fixed changes to many ministries.",
                "/icons/wand.png",
                () -> new BulkEditScreen(user, userManager).show(stage)
        );

        VBox historyCard = actionCard(
                "View Edit History",
                "See all changes and audit trail.",
                "/icons/inbox.png",
                () -> new EditHistoryScreen(user, userManager).show(stage)
        );

        VBox backCard = actionCard(
                "Back to Main Menu",
                "Return to main menu.",
                "/icons/compare.png",
                () -> new ViewEditBudgetScreen(user, userManager).show(stage)
        );
        backCard.getStyleClass().add("danger-card");

        cards.add(simpleCard, 0, 0);
        cards.add(bulkCard,   1, 0);
        cards.add(historyCard,0, 1);
        cards.add(backCard,   1, 1);

        VBox leftContent = new VBox(14, heroCard, new Separator(), cards);
        leftContent.setFillWidth(true);
        leftContent.setMaxWidth(760);
        HBox.setHgrow(leftContent, Priority.ALWAYS);

        VBox sidePanel = buildSidePanel();
        sidePanel.setMinWidth(280);
        sidePanel.setMaxWidth(280);

        HBox mainRow = new HBox(18, leftContent, sidePanel);
        mainRow.setAlignment(Pos.TOP_CENTER);
        mainRow.setPadding(new Insets(18));

        Button footerBack = new Button("⟵ Back");
        footerBack.getStyleClass().addAll("button", "subtle");
        footerBack.setOnAction(e -> new ViewEditBudgetScreen(user, userManager).show(stage));

        HBox footer = new HBox(footerBack);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(12, 18, 12, 18));
        footer.getStyleClass().add("footer-bar");

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(mainRow);
        root.setBottom(footer);

        Scene scene = new Scene(root, 1120, 720);
        scene.getStylesheets().add(getClass().getResource("/css/DarkTheme.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Edit Budget");
        stage.show();

        FadeTransition ft = new FadeTransition(Duration.millis(200), root);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    private VBox actionCard(String title, String desc, String iconPath, Runnable onClick) {

        ImageView icon = new ImageView(new javafx.scene.image.Image(
                EditBudgetScreen.class.getResourceAsStream(iconPath)
        ));
        icon.setFitWidth(34);
        icon.setFitHeight(34);
        icon.getStyleClass().add("action-icon");

        VBox iconBadge = new VBox(icon);
        iconBadge.setAlignment(Pos.CENTER);
        iconBadge.getStyleClass().add("icon-badge");

        Label t = new Label(title);
        t.getStyleClass().add("action-title");

        Label d = new Label(desc);
        d.getStyleClass().add("action-desc");
        d.setWrapText(true);

        VBox text = new VBox(5, t, d);
        text.setAlignment(Pos.CENTER_LEFT);

        Label chevron = new Label("›");
        chevron.getStyleClass().add("chevron");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(14, iconBadge, text, spacer, chevron);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(row);
        card.getStyleClass().addAll("card", "action-card");

        card.setMinHeight(118);
        card.setMaxWidth(Double.MAX_VALUE);

        card.setOnMouseEntered(e -> {
            card.setScaleX(1.02);
            card.setScaleY(1.02);
            card.setTranslateY(-2);
        });
        card.setOnMouseExited(e -> {
            card.setScaleX(1.00);
            card.setScaleY(1.00);
            card.setTranslateY(0);
        });

        card.setOnMouseClicked(e -> onClick.run());
        return card;
    }

    private VBox buildSidePanel() {

        Label t1 = new Label("Editing modes");
        t1.getStyleClass().add("side-title");

        Label l1 = new Label("• Simple Edit: change one ministry");
        Label l2 = new Label("• Bulk Edit: apply changes to many");
        Label l3 = new Label("• History: review audit trail");

        l1.getStyleClass().add("side-text");
        l2.getStyleClass().add("side-text");
        l3.getStyleClass().add("side-text");

        VBox card1 = new VBox(10, t1, l1, l2, l3);
        card1.getStyleClass().addAll("card", "side-card");

        Label t2 = new Label("Balance rules");
        t2.getStyleClass().add("side-title");

        Label r1 = new Label("• Increase uses available balance");
        Label r2 = new Label("• Decrease returns balance back");
        Label r3 = new Label("• You can’t decrease below 0");

        r1.getStyleClass().add("side-text");
        r2.getStyleClass().add("side-text");
        r3.getStyleClass().add("side-text");

        VBox card2 = new VBox(10, t2, r1, r2, r3);
        card2.getStyleClass().addAll("card", "side-card");

        VBox side = new VBox(14, card1, card2);
        side.setAlignment(Pos.TOP_LEFT);
        return side;
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

        Label title = new Label("Edit Single Ministry");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Select ministry, choose change type and enter amount.");
        subtitle.getStyleClass().add("subtitle");

        VBox header = new VBox(6, title, subtitle);

        ComboBox<String> ministryBox = new ComboBox<>();
        for (Ministry m : CreatingMinistries.ministries2026) {
            if (m != null) ministryBox.getItems().add(m.getMinistryName());
        }
        ministryBox.setPromptText("Select Ministry");
        ministryBox.setMaxWidth(Double.MAX_VALUE);

        Label currentBudgetLabel = new Label("Current Budget: —");
        currentBudgetLabel.getStyleClass().add("muted");

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

        Button applyBtn = new Button("Apply");
        applyBtn.getStyleClass().addAll("button", "primary");
        applyBtn.setDisable(true);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().addAll("button", "subtle");

        HBox buttons = new HBox(10, cancelBtn, applyBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

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

            Edit editObj = new Edit(ministry, changeType, amount, "fixed");
            Edit.history.addEdit(editObj);
            editObj.editingbudget(editObj, false, false);

            if (isIncrease) Edit.balance -= amount;
            else Edit.balance += amount;

            // ✅ PERSIST budgets + balance so screen changes don't lose state
            try {
                UserBudgetFileUtil.saveUserBudget(user, 2026);
            } catch (Exception ex) {
                System.err.println("Failed to save budgets: " + ex.getMessage());
            }

            double newBudget = Ministry.budgetSearchByName(ministry, CreatingMinistries.ministries2026);

            showThemedAlert(
                    Alert.AlertType.INFORMATION,
                    "Success",
                    "Budget Updated Successfully",
                    "Ministry: " + ministry + "\n" +
                            "Action: " + changeType + " by " + Ministry.getFormattedBudget(amount) + "\n" +
                            "New Budget: " + Ministry.getFormattedBudget(newBudget) + "\n" +
                            "Available Balance: " + Ministry.getFormattedBudget(Edit.balance)
            );

            dialog.close();
            show(parentStage);
        });

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
