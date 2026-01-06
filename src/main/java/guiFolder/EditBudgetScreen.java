package guiFolder;

import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

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

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * EditBudgetScreen (Governor / MinistryMember)
 * FIXES:
 * - No manual balance updates. Uses Edit.applyEdit(...)
 * - Persist via UserBudgetPersistence (balance+budgets)
 * - Preserve stage bounds on scene changes
 *
 * EXTRA:
 * - Gold accents (inline only, no CSS edits)
 * - No window jumping (reuse Scene root + preserve state)
 */
public class EditBudgetScreen {

    private final User user;
    private final UserManager userManager;

    public EditBudgetScreen(User user, UserManager userManager) {
        this.user = user;
        this.userManager = userManager;
    }

    public void show(Stage stage) {
        CurrentSession.setUser(user);
        reloadUserBudgets();

        /* =========================
           WINDOW STATE SNAPSHOT (NO JUMP)
           ========================= */
        final boolean wasMaximized = stage.isMaximized();
        final boolean wasFullScreen = stage.isFullScreen();
        final double prevW = stage.getWidth();
        final double prevH = stage.getHeight();
        final double prevX = stage.getX();
        final double prevY = stage.getY();

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
        // GOLD: make balance chip subtly special
        chip3.setStyle(
                "-fx-border-color: rgba(212,175,55,0.28);" +
                "-fx-background-color: rgba(212,175,55,0.08);"
        );

        HBox chips = new HBox(10, chip1, chip2, chip3);
        chips.setAlignment(Pos.CENTER_LEFT);

        VBox heroCard = new VBox(10, title, subtitle, chips);
        heroCard.getStyleClass().addAll("card", "toolbar-card", "hero-card");
        // GOLD accent halo for this screen header
        heroCard.setStyle(
                "-fx-border-color: rgba(212,175,55,0.22);" +
                "-fx-effect: dropshadow(gaussian, rgba(212,175,55,0.12), 26, 0.22, 0, 10);"
        );

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
                () -> showSimpleEditDialog(stage),
                true // GOLD accent (primary action)
        );

        VBox bulkCard = actionCard(
                "Bulk Edit",
                "Apply percentage or fixed changes to many ministries.",
                "/icons/wand.png",
                () -> new BulkEditScreen(user, userManager).show(stage),
                false
        );

        VBox historyCard = actionCard(
                "View Edit History",
                "See all changes and audit trail.",
                "/icons/inbox.png",
                () -> new EditHistoryScreen(user, userManager).show(stage),
                false
        );

        VBox backCard = actionCard(
                "Back to Main Menu",
                "Return to main menu.",
                "/icons/compare.png",
                () -> new ViewEditBudgetScreen(user, userManager).show(stage),
                false
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
        // GOLD: tiny side accent only (subtle)
        sidePanel.setStyle("-fx-border-color: rgba(212,175,55,0.10);");

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

        /* =========================
           SCENE (REUSE + NO JUMP)
           ========================= */
        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root);
            var css = getClass().getResource("/css/DarkTheme.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
            var css = getClass().getResource("/css/DarkTheme.css");
            if (css != null) {
                String cssUrl = css.toExternalForm();
                if (!scene.getStylesheets().contains(cssUrl)) scene.getStylesheets().add(cssUrl);
            }
        }

        stage.setTitle("Edit Budget");
        stage.show();

        // Restore window state (fullscreen/max/normal) exactly
        if (wasFullScreen) {
            stage.setFullScreen(true);
        } else if (wasMaximized) {
            stage.setMaximized(true);
        } else {
            if (prevW > 0 && prevH > 0) {
                stage.setWidth(prevW);
                stage.setHeight(prevH);
                stage.setX(prevX);
                stage.setY(prevY);
            }
        }

        FadeTransition ft = new FadeTransition(Duration.millis(200), root);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    private VBox actionCard(String title, String desc, String iconPath, Runnable onClick, boolean goldPrimary) {
        ImageView icon = new ImageView(new javafx.scene.image.Image(
                EditBudgetScreen.class.getResourceAsStream(iconPath)
        ));
        icon.setFitWidth(34);
        icon.setFitHeight(34);
        icon.getStyleClass().add("action-icon");

        VBox iconBadge = new VBox(icon);
        iconBadge.setAlignment(Pos.CENTER);
        iconBadge.getStyleClass().add("icon-badge");

        // GOLD: primary action gets gold-tinted icon badge
        if (goldPrimary) {
            iconBadge.setStyle(
                    "-fx-background-color: rgba(212,175,55,0.14);" +
                    "-fx-border-color: rgba(212,175,55,0.22);"
            );
        }

        Label t = new Label(title);
        t.getStyleClass().add("action-title");

        Label d = new Label(desc);
        d.getStyleClass().add("action-desc");
        d.setWrapText(true);

        VBox text = new VBox(5, t, d);
        text.setAlignment(Pos.CENTER_LEFT);

        Label chevron = new Label("›");
        chevron.getStyleClass().add("chevron");
        // GOLD: chevron accent (subtle)
        chevron.setStyle("-fx-text-fill: rgba(212,175,55,0.62);");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(14, iconBadge, text, spacer, chevron);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(row);
        card.getStyleClass().addAll("card", "action-card");
        card.setMinHeight(118);
        card.setMaxWidth(Double.MAX_VALUE);

        // GOLD: hover glow + your original scale hover
        card.setOnMouseEntered(e -> {
            card.setScaleX(1.02); card.setScaleY(1.02); card.setTranslateY(-2);
            card.setStyle(
                    "-fx-border-color: rgba(212,175,55,0.30);" +
                    "-fx-effect: dropshadow(gaussian, rgba(212,175,55,0.12), 22, 0.22, 0, 10);"
            );
        });

        card.setOnMouseExited(e -> {
            card.setScaleX(1.00); card.setScaleY(1.00); card.setTranslateY(0);
            card.setStyle("");
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
            } else {
                Path govPath = Path.of("src/main/resources/NecessaryFilesAndData/Governor_2026.csv");
                if (Files.exists(userFile)) CreatingMinistries.loadUserBudgets(userFile, 2026);
                else CreatingMinistries.loadUserBudgets(govPath, 2026);
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
            } else currentBudgetLabel.setText("Current Budget: —");
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
            if (ministry == null) { errorLabel.setText("Please select a ministry."); return; }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
                if (amount <= 0) { errorLabel.setText("Amount must be positive."); return; }
            } catch (NumberFormatException ex) {
                errorLabel.setText("Invalid amount.");
                return;
            }

            boolean isIncrease = changeGroup.getSelectedToggle() == increaseBtn;
            String changeType = isIncrease ? "Increase" : "Decrease";

            double currentBudget = Ministry.budgetSearchByName(ministry, CreatingMinistries.ministries2026);

            if (!isIncrease && amount > currentBudget) { errorLabel.setText("Cannot decrease more than current budget."); return; }
            if (isIncrease && amount > Edit.balance) { errorLabel.setText("Insufficient balance."); return; }

            Edit editObj = new Edit(ministry, changeType, amount, "fixed");
            Edit.history.addEdit(editObj);
            Edit.applyEdit(editObj, false, false);

            // persist
            UserBudgetPersistence.saveUserBudgets(user, CreatingMinistries.ministries2026, 2026);

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

        // GOLD: small edge + halo on dialog card
        card.setStyle(
                "-fx-border-color: rgba(212,175,55,0.22);" +
                "-fx-effect: dropshadow(gaussian, rgba(212,175,55,0.10), 22, 0.20, 0, 10);"
        );

        VBox root = new VBox(card);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(18));

        Scene scene = new Scene(root, 740, 540);
        var css = getClass().getResource("/css/DarkTheme.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());

        dialog.setScene(scene);

        // Keep dialog near parent; no screen-jump feel
        dialog.setX(parentStage.getX() + 60);
        dialog.setY(parentStage.getY() + 60);

        dialog.show();
    }

    private void showThemedAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        var css = getClass().getResource("/css/DarkTheme.css");
        if (css != null) alert.getDialogPane().getStylesheets().add(css.toExternalForm());

        // GOLD: tiny border on alerts too (no CSS edit)
        alert.getDialogPane().setStyle(
                "-fx-border-color: rgba(212,175,55,0.22);" +
                "-fx-background-radius: 16;" +
                "-fx-border-radius: 16;"
        );

        alert.showAndWait();
    }

    private static void applyScenePreserveWindow(Stage stage, Scene scene, String title) {
        boolean wasShowing = stage.isShowing();
        double x = stage.getX();
        double y = stage.getY();
        double w = stage.getWidth();
        double h = stage.getHeight();
        boolean max = stage.isMaximized();
        boolean fs = stage.isFullScreen();

        stage.setScene(scene);
        stage.setTitle(title);

        if (!wasShowing) {
            stage.show();
            stage.centerOnScreen();
            return;
        }

        stage.setMaximized(max);
        stage.setFullScreen(fs);

        if (!max && !fs && w > 0 && h > 0) {
            stage.setWidth(w);
            stage.setHeight(h);
            stage.setX(x);
            stage.setY(y);
        }
    }
}
