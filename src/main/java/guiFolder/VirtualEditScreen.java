package guiFolder;

import java.nio.file.Files;
import java.nio.file.Path;

import UserFeatures.CreatingMinistries;
import UserFeatures.Edit;
import UserFeatures.EditHistoryList;
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

public class VirtualEditScreen {

    private final User user;
    private final UserManager userManager;

    public VirtualEditScreen(User user, UserManager userManager) {
        this.user = user;
        this.userManager = userManager;
    }

    public void show(Stage stage) {

        Path userBudgetFile = UserBudgetFileUtil.getUserBudgetFile(user, 2026);
        Path governorPath = Path.of("src/main/resources/NecessaryFilesAndData/Governor_2026.csv");

        // ===== LOAD LOGIC (Saved vs Fresh) =====
        if (Files.exists(userBudgetFile)) {

            Alert loadAlert = new Alert(Alert.AlertType.CONFIRMATION);
            loadAlert.initOwner(stage);
            loadAlert.initModality(Modality.WINDOW_MODAL);
            loadAlert.setTitle("Load Saved Budget");
            loadAlert.setHeaderText("You have a saved virtual budget.");
            loadAlert.setContentText("Do you want to load it or start fresh?");

            ButtonType loadBtn = new ButtonType("Load Saved");
            ButtonType freshBtn = new ButtonType("Start Fresh");
            ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            loadAlert.getButtonTypes().setAll(loadBtn, freshBtn, cancelBtn);
            loadAlert.getDialogPane().getStylesheets().add(
                    getClass().getResource("/css/DarkTheme.css").toExternalForm()
            );

            loadAlert.showAndWait().ifPresent(response -> {
                if (response == loadBtn) {
                    CreatingMinistries.loadUserBudgets(userBudgetFile, 2026);
                } else if (response == freshBtn) {
                    CreatingMinistries.loadUserBudgets(governorPath, 2026);
                    Edit.balance = 0;
                    Edit.history = new EditHistoryList();
                    UserBudgetFileUtil.deleteUserBudget(user, 2026);
                } else {
                    new ViewEditBudgetScreen(user, userManager).show(stage);
                }
            });

        } else {
            CreatingMinistries.loadUserBudgets(governorPath, 2026);
            Edit.balance = 0;
            Edit.history = new EditHistoryList();
        }

        /* =========================
           TOP APP BAR
           ========================= */
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

        /* =========================
           HERO HEADER
           ========================= */
        Label title = new Label("Virtual Budget Editing");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Simulate budget changes (saved separately per user)");
        subtitle.getStyleClass().add("subtitle");

        Label chip1 = new Label("Virtual • 2026");
        chip1.getStyleClass().add("chip");

        Label chip2 = new Label("Role: " + user.getRole().name());
        chip2.getStyleClass().add("chip");

        Label chip3 = new Label("Balance: " + Ministry.getFormattedBudget(Edit.balance));
        chip3.getStyleClass().add("chip");

        HBox chips = new HBox(10, chip1, chip2, chip3);
        chips.setAlignment(Pos.CENTER_LEFT);

        VBox heroCard = new VBox(10, title, subtitle, chips);
        heroCard.getStyleClass().addAll("card", "toolbar-card", "hero-card");

        /* =========================
           ACTION CARDS (2x2)
           ========================= */
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
                () -> showSimpleEditDialog(stage)
        );

        VBox bulkCard = actionCard(
                "Bulk Edit",
                "Apply percentage or fixed changes to all ministries.",
                () -> new BulkEditScreen(user, userManager).show(stage)
        );

        VBox historyCard = actionCard(
                "View Edit History",
                "Review changes and undo operations.",
                () -> new EditHistoryScreen(user, userManager).show(stage)
        );

        VBox resetCard = actionCard(
                "Reset to Original",
                "Discard virtual changes and reset baseline.",
                () -> confirmAndReset(stage, governorPath)
        );
        resetCard.getStyleClass().add("danger-card");

        cards.add(simpleCard, 0, 0);
        cards.add(bulkCard,   1, 0);
        cards.add(historyCard,0, 1);
        cards.add(resetCard,  1, 1);

        VBox leftContent = new VBox(14, heroCard, new Separator(), cards);
        leftContent.setFillWidth(true);
        leftContent.setMaxWidth(760);
        HBox.setHgrow(leftContent, Priority.ALWAYS);

        /* =========================
           RIGHT SIDE PANEL
           ========================= */
        VBox sidePanel = buildSidePanel();
        sidePanel.setMinWidth(280);
        sidePanel.setMaxWidth(280);

        HBox mainRow = new HBox(18, leftContent, sidePanel);
        mainRow.setAlignment(Pos.TOP_CENTER);
        mainRow.setPadding(new Insets(18));

        // ✅ ScrollPane ώστε να μη “κόβεται” τίποτα σε μικρά ύψη
        ScrollPane scroll = new ScrollPane(mainRow);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        /* =========================
           FOOTER BAR (Back ΠΑΝΤΑ ορατό)
           ========================= */
        Button footerBack = new Button("⟵ Back");
        footerBack.getStyleClass().addAll("button", "subtle");
        footerBack.setOnAction(e -> new ViewEditBudgetScreen(user, userManager).show(stage));

        HBox footer = new HBox(footerBack);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(12, 18, 12, 18));
        footer.getStyleClass().add("footer-bar");

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(scroll);
        root.setBottom(footer);

        Scene scene = new Scene(root, 1120, 720);
        var css = getClass().getResource("/css/DarkTheme.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Virtual Edit");
        stage.show();

        FadeTransition ft = new FadeTransition(Duration.millis(200), root);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    private VBox actionCard(String title, String desc, Runnable onClick) {

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

        HBox row = new HBox(14, text, spacer, chevron);
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

        Label t1 = new Label("Virtual mode");
        t1.getStyleClass().add("side-title");

        Label l1 = new Label("• Changes are saved per user");
        Label l2 = new Label("• Undo works from History screen");
        Label l3 = new Label("• Reset restores governor baseline");

        l1.getStyleClass().add("side-text");
        l2.getStyleClass().add("side-text");
        l3.getStyleClass().add("side-text");

        VBox card1 = new VBox(10, t1, l1, l2, l3);
        card1.getStyleClass().addAll("card", "side-card");

        Label t2 = new Label("Balance rules");
        t2.getStyleClass().add("side-title");

        Label r1 = new Label("• Increase uses available balance");
        Label r2 = new Label("• Decrease refunds balance back");
        Label r3 = new Label("• You can’t increase beyond balance");

        r1.getStyleClass().add("side-text");
        r2.getStyleClass().add("side-text");
        r3.getStyleClass().add("side-text");

        VBox card2 = new VBox(10, t2, r1, r2, r3);
        card2.getStyleClass().addAll("card", "side-card");

        VBox side = new VBox(14, card1, card2);
        side.setAlignment(Pos.TOP_LEFT);
        return side;
    }

    private void confirmAndReset(Stage stage, Path governorPath) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.initOwner(stage);
        confirm.initModality(Modality.WINDOW_MODAL);
        confirm.setTitle("Reset Budget");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("This will discard ALL your virtual changes and reset balance.");
        confirm.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/DarkTheme.css").toExternalForm()
        );

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {

                CreatingMinistries.loadUserBudgets(governorPath, 2026);
                Edit.balance = 0;
                Edit.history = new EditHistoryList();
                UserBudgetFileUtil.deleteUserBudget(user, 2026);

                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.initOwner(stage);
                success.setTitle("Reset Complete");
                success.setHeaderText("Done");
                success.setContentText("Budget + Balance reset to original.");
                success.getDialogPane().getStylesheets().add(
                        getClass().getResource("/css/DarkTheme.css").toExternalForm()
                );
                success.showAndWait();

                show(stage);
            }
        });
    }

    // ===== Simple Edit Dialog (embedded) =====
    private void showSimpleEditDialog(Stage parentStage) {

        Stage dialog = new Stage();
        dialog.initOwner(parentStage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Virtual Simple Edit");

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

            // ✅ persist virtual state
            try {
                UserBudgetFileUtil.saveUserBudget(user, 2026);
            } catch (Exception ex) {
                System.err.println("Failed to save virtual budget: " + ex.getMessage());
            }

            double newBudget = Ministry.budgetSearchByName(ministry, CreatingMinistries.ministries2026);

            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.initOwner(dialog);
            ok.setTitle("Success");
            ok.setHeaderText("Virtual Budget Updated Successfully");
            ok.setContentText(
                    "Ministry: " + ministry + "\n" +
                            "Action: " + changeType + " by " + Ministry.getFormattedBudget(amount) + "\n" +
                            "New Budget: " + Ministry.getFormattedBudget(newBudget) + "\n" +
                            "Available Balance: " + Ministry.getFormattedBudget(Edit.balance)
            );
            ok.getDialogPane().getStylesheets().add(
                    getClass().getResource("/css/DarkTheme.css").toExternalForm()
            );
            ok.showAndWait();

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
}
