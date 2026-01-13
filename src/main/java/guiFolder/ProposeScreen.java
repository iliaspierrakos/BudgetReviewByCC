package guiFolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import UserFeatures.CreatingMinistries;
import UserFeatures.DraftEditSession;
import UserFeatures.DraftProposalExporter;
import UserFeatures.Ministry;
import UserManagement.CurrentSession;
import UserManagement.MinistryMember;
import UserManagement.User;
import UserManagement.UserManager;
import javafx.animation.FadeTransition;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * ProposeScreen
 *
 * Draft proposal workspace for authorized users.
 *
 * Adds:
 *  - Reload Original Budget Review (loads Governor CSV baseline, then resets draft)
 *  - Reset Draft (resets draft from currently loaded budgets, no file reload)
 */
public class ProposeScreen {

    private static final int YEAR = 2026;

    private final User user;
    private final UserManager userManager;

    private Double startingBalance = null;

    public ProposeScreen(User user, UserManager userManager) {
        this.user = user;
        this.userManager = userManager;
    }

    public void show(Stage stage) {

        if (!(user instanceof MinistryMember)) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Access denied: Only authorized users can create proposals.");
            applyTheme(a);
            a.showAndWait();
            return;
        }

        CurrentSession.setUser(user);

        if (!DraftEditSession.isInitialized()) {
            // first time: load baseline if possible (same as "Reload Original"), then reset
            reloadOriginalBudgetReviewInternal(false);
        }

        Label appLogo = new Label("BudgetReviewByCC");
        appLogo.getStyleClass().add("app-logo");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(14, appLogo, spacer);
        topBar.getStyleClass().add("topbar");
        topBar.setPadding(new Insets(14));

        Label title = new Label("Propose (Draft Edit)");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Draft edits only. Submit for review and approval.");
        subtitle.getStyleClass().add("subtitle");

        Label balanceChip = new Label("Draft Balance: " + Ministry.getFormattedBudget(DraftEditSession.getDraftBalance()));
        balanceChip.getStyleClass().add("chip");

        Label countChip = new Label("Draft edits: " + DraftEditSession.getHistory().size());
        countChip.getStyleClass().add("chip");

        HBox chips = new HBox(10, balanceChip, countChip);
        chips.setAlignment(Pos.CENTER_LEFT);

        VBox heroCard = new VBox(10, title, subtitle, chips);
        heroCard.getStyleClass().addAll("card", "toolbar-card", "hero-card", "virtual-hero");
        heroCard.setMaxWidth(860);

        GridPane grid = new GridPane();
        grid.getStyleClass().add("action-grid");
        grid.setHgap(14);
        grid.setVgap(14);
        grid.setAlignment(Pos.TOP_CENTER);

        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(50);
        col.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col, col);

        VBox simpleEdit = actionCard(
                "Simple Draft Edit",
                "Edit one ministry using a fixed amount (draft).",
                "/icons/wand.png",
                () -> openSimpleDraftDialog(stage, balanceChip, countChip)
        );
        simpleEdit.getStyleClass().add("primary-action");

        VBox bulkEdit = actionCard(
                "Bulk Draft Edit",
                "Apply changes to multiple ministries (draft).",
                "/icons/bulk.png",
                () -> new BulkEditDraftScreen(user, userManager).show(stage)
        );
        bulkEdit.getStyleClass().add("primary-action");

        VBox history = actionCard(
                "Draft History",
                "Review and undo your draft edits.",
                "/icons/history.png",
                () -> new DraftHistoryScreen(user, userManager).show(stage)
        );
        history.getStyleClass().add("primary-action");

        // NEW: Reload original baseline from Governor CSV
        VBox reloadOriginal = actionCard(
                "Reload Original Budget Review",
                "Reload baseline (Governor CSV) and clear draft edits.",
                "/icons/reset.png",
                () -> reloadOriginalBudgetReview(stage, balanceChip, countChip)
        );
        reloadOriginal.getStyleClass().addAll("danger-action");

        // NEW: Reset draft only from currently loaded budgets (no file reload)
        VBox resetDraft = actionCard(
                "Reset Draft",
                "Clear draft edits and reset from currently loaded budgets (no reload).",
                "/icons/reset.png",
                () -> resetDraftFromCurrentLoadedBudgets(stage, balanceChip, countChip)
        );
        resetDraft.getStyleClass().addAll("danger-action");

        grid.add(simpleEdit,      0, 0);
        grid.add(bulkEdit,        1, 0);
        grid.add(history,         0, 1);
        grid.add(resetDraft,      1, 1);
        grid.add(reloadOriginal,  0, 2);
        GridPane.setColumnSpan(reloadOriginal, 2);

        Button sendBtn = new Button("Send Proposal");
        sendBtn.getStyleClass().addAll("button", "send-proposal-btn");
        sendBtn.setMaxWidth(Double.MAX_VALUE);
        sendBtn.setFocusTraversable(true);

        sendBtn.setOnAction(e -> {
            if (sendBtn.isDisabled()) return;

            final String originalText = sendBtn.getText();
            sendBtn.setDisable(true);
            sendBtn.setText("Sending…");

            try {
                DraftProposalExporter.exportAndNotify(stage, user);

                // update chips (no full show(stage) rebuild)
                balanceChip.setText("Draft Balance: " + Ministry.getFormattedBudget(DraftEditSession.getDraftBalance()));
                countChip.setText("Draft edits: " + DraftEditSession.getHistory().size());

                sendBtn.setText("Proposal sent ✓");

            } catch (Exception ex) {
                sendBtn.setDisable(false);
                sendBtn.setText(originalText);

                Alert a = new Alert(Alert.AlertType.ERROR, "Failed to send proposal:\n" + ex.getMessage());
                applyTheme(a);
                a.showAndWait();
            }
        });

        VBox content = new VBox(16, heroCard, new Separator(), grid, sendBtn);
        content.setPadding(new Insets(18));
        content.getStyleClass().add("virtual-content");
        content.setMaxWidth(900);

        Button backBtn = new Button("Back");
        backBtn.getStyleClass().addAll("button", "subtle");
        backBtn.setOnAction(e -> new ViewEditBudgetScreen(user, userManager).show(stage));

        HBox footer = new HBox(backBtn);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(12, 18, 18, 18));

        BorderPane root = new BorderPane();
        root.getStyleClass().add("virtual-edit-root");
        root.setTop(topBar);
        root.setCenter(content);
        root.setBottom(footer);

        Scene scene = new Scene(root,
                stage.getWidth() > 0 ? stage.getWidth() : 1100,
                stage.getHeight() > 0 ? stage.getHeight() : 720
        );

        var css = getClass().getResource("/css/DarkTheme.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());

        applyScenePreserveWindow(stage, scene, "Propose (Draft Edit)");

        FadeTransition ft = new FadeTransition(Duration.millis(200), root);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    /** Reload baseline from Governor CSV (original budget review), then reset draft. */
    private void reloadOriginalBudgetReview(Stage stage, Label balanceChip, Label countChip) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.initOwner(stage);
        confirm.initModality(Modality.WINDOW_MODAL);
        confirm.setTitle("Reload Original Budget Review");
        confirm.setHeaderText("Reload baseline budgets?");
        confirm.setContentText("This will discard all draft edits and reload the original baseline (Governor CSV).");

        applyTheme(confirm);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK) return;

            reloadOriginalBudgetReviewInternal(true);

            balanceChip.setText("Draft Balance: " + Ministry.getFormattedBudget(DraftEditSession.getDraftBalance()));
            countChip.setText("Draft edits: " + DraftEditSession.getHistory().size());

            show(stage);
        });
    }

    /** Reset draft from current already-loaded budgets (no file load). */
    private void resetDraftFromCurrentLoadedBudgets(Stage stage, Label balanceChip, Label countChip) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.initOwner(stage);
        confirm.initModality(Modality.WINDOW_MODAL);
        confirm.setTitle("Reset Draft");
        confirm.setHeaderText("Reset draft edits?");
        confirm.setContentText("This will clear draft edits and reset from currently loaded budgets (no reload).");

        applyTheme(confirm);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK) return;

            DraftEditSession.resetFromCurrent(0);

            balanceChip.setText("Draft Balance: " + Ministry.getFormattedBudget(DraftEditSession.getDraftBalance()));
            countChip.setText("Draft edits: " + DraftEditSession.getHistory().size());

            show(stage);
        });
    }

    /**
     * Internal helper: tries to load Governor CSV baseline (if exists), then resets session.
     * If showWarning=true, it will warn if baseline file missing.
     */
    private void reloadOriginalBudgetReviewInternal(boolean showWarning) {
        try {
            if (startingBalance == null) startingBalance = 0.0;

            Path gov = Path.of("src/main/resources/NecessaryFilesAndData/Governor_" + YEAR + ".csv");
            if (Files.exists(gov)) {
                CreatingMinistries.loadUserBudgets(gov, YEAR);
            } else if (showWarning) {
                System.err.println("Baseline not found: " + gov.toAbsolutePath());
            }

            DraftEditSession.resetFromCurrent(startingBalance);

        } catch (Exception e) {
            System.err.println("Failed to reload baseline: " + e.getMessage());
        }
    }

    private void openSimpleDraftDialog(Stage parentStage, Label balanceChip, Label countChip) {

        Stage dialog = new Stage();
        dialog.initOwner(parentStage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Simple Draft Edit");

        Label title = new Label("Edit Single Ministry (Draft)");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Select a ministry, choose change type and enter amount.");
        subtitle.getStyleClass().add("subtitle");

        VBox header = new VBox(6, title, subtitle);

        ComboBox<String> ministryBox = new ComboBox<>();
        Arrays.stream(DraftEditSession.getSandbox())
                .filter(m -> m != null && m.getMinistryName() != null)
                .map(Ministry::getMinistryName)
                .sorted(String::compareToIgnoreCase)
                .forEach(ministryBox.getItems()::add);

        ministryBox.setPromptText("Select Ministry");
        ministryBox.setMaxWidth(Double.MAX_VALUE);

        Label currentBudgetLabel = new Label("Current Budget: —");
        currentBudgetLabel.getStyleClass().add("subtitle");

        ToggleGroup changeGroup = new ToggleGroup();
        ToggleButton increaseBtn = new ToggleButton("Increase");
        ToggleButton decreaseBtn = new ToggleButton("Decrease");
        increaseBtn.setToggleGroup(changeGroup);
        decreaseBtn.setToggleGroup(changeGroup);
        increaseBtn.setSelected(true);

        increaseBtn.getStyleClass().addAll("segment-btn", "increase");
        decreaseBtn.getStyleClass().addAll("segment-btn", "decrease");

        HBox changeTypeBox = new HBox(10, increaseBtn, decreaseBtn);
        changeTypeBox.setAlignment(Pos.CENTER_LEFT);
        changeTypeBox.getStyleClass().add("segmented-box");

        TextField amountField = new TextField();
        amountField.setPromptText("Amount (e.g., 1000000)");
        amountField.setMaxWidth(Double.MAX_VALUE);
        amountField.setTextFormatter(new TextFormatter<>(change -> {
            String t = change.getControlNewText().trim();
            if (t.isEmpty()) return change;
            if (t.matches("\\d+(\\.\\d{0,2})?")) return change;
            return null;
        }));

        Label balanceLabel = new Label("Draft Balance: " + Ministry.getFormattedBudget(DraftEditSession.getDraftBalance()));
        balanceLabel.getStyleClass().add("subtitle");

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error");
        errorLabel.setWrapText(true);

        Button applyBtn = new Button("Apply (Draft)");
        applyBtn.getStyleClass().addAll("button", "primary");
        applyBtn.setDisable(true);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().addAll("button", "subtle");
        cancelBtn.setOnAction(e -> dialog.close());

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

        applyBtn.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> ministryBox.getValue() == null || amountField.getText().trim().isEmpty(),
                        ministryBox.valueProperty(),
                        amountField.textProperty()
                )
        );

        ministryBox.valueProperty().addListener((obs, o, n) -> {
            errorLabel.setText("");
            if (n != null) {
                double budget = Ministry.budgetSearchByName(n, DraftEditSession.getSandbox());
                currentBudgetLabel.setText("Current Budget: " + Ministry.getFormattedBudget(budget));
            } else {
                currentBudgetLabel.setText("Current Budget: —");
            }
        });

        amountField.textProperty().addListener((obs, o, n) -> errorLabel.setText(""));

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

            String err = DraftEditSession.applyFixed(ministry, isIncrease, amount);
            if (err != null) {
                errorLabel.setText(err);
                return;
            }

            balanceChip.setText("Draft Balance: " + Ministry.getFormattedBudget(DraftEditSession.getDraftBalance()));
            countChip.setText("Draft edits: " + DraftEditSession.getHistory().size());
            balanceLabel.setText("Draft Balance: " + Ministry.getFormattedBudget(DraftEditSession.getDraftBalance()));

            dialog.close();
        });

        VBox card = new VBox(12, header, new Separator(), form, errorLabel, buttons);
        card.getStyleClass().addAll("card", "toolbar-card", "virtual-dialog-card");
        card.setPadding(new Insets(18));
        card.setMaxWidth(680);

        VBox root = new VBox(card);
        root.getStyleClass().add("virtual-dialog-root");
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(18));

        Scene scene = new Scene(root, 820, 560);
        var css = getClass().getResource("/css/DarkTheme.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());

        dialog.setScene(scene);
        dialog.centerOnScreen();
        dialog.show();
    }

    private VBox actionCard(String title, String desc, String iconPath, Runnable onClick) {
        Node iconNode = safeIcon(iconPath, 34);
        iconNode.getStyleClass().add("action-icon");

        Label t = new Label(title);
        t.getStyleClass().add("action-title");

        Label d = new Label(desc);
        d.getStyleClass().add("action-desc");
        d.setWrapText(true);

        VBox text = new VBox(5, t, d);

        Label chevron = new Label("›");
        chevron.getStyleClass().add("chevron");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(14, iconNode, text, spacer, chevron);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(row);
        card.getStyleClass().addAll("card", "action-card", "image-card");
        card.getStyleClass().add("virtual-action");
        card.setMinHeight(118);

        card.setFocusTraversable(true);
        card.setOnMouseClicked(e -> onClick.run());
        card.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) onClick.run();
        });

        return card;
    }

    private Node safeIcon(String iconPath, double size) {
        try {
            var stream = ProposeScreen.class.getResourceAsStream(iconPath);
            if (stream == null) throw new IllegalStateException("Missing icon: " + iconPath);
            ImageView icon = new ImageView(new Image(stream));
            icon.setFitWidth(size);
            icon.setFitHeight(size);
            return icon;
        } catch (Exception ex) {
            Label fallback = new Label("⬤");
            fallback.getStyleClass().add("icon-fallback");
            return fallback;
        }
    }

    private void applyTheme(Dialog<?> dialog) {
        var css = getClass().getResource("/css/DarkTheme.css");
        if (css != null) dialog.getDialogPane().getStylesheets().add(css.toExternalForm());
    }

    private void applyTheme(Alert a) {
        var css = getClass().getResource("/css/DarkTheme.css");
        if (css != null) a.getDialogPane().getStylesheets().add(css.toExternalForm());
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
