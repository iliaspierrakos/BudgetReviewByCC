package guiFolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import UserFeatures.CreatingMinistries;
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
 * <p>Draft proposal hub for {@link MinistryMember} users. This screen is the single
 * entry point for creating, reviewing and submitting minister draft edits.</p>
 *
 * <h2>Key rules</h2>
 * <ul>
 *   <li><b>Draft-only workflow:</b> edits are applied to {@link DraftEditSession} sandbox budgets.</li>
 *   <li><b>No persistence:</b> budgets are not written to official CSVs or user budget files.</li>
 *   <li><b>Single submission point:</b> proposal export/submission happens <b>only</b> from this screen.</li>
 *   <li>Other draft screens (bulk/history/simple dialogs) must never export proposals.</li>
 * </ul>
 *
 * <h2>Draft initialization</h2>
 * <p>On first entry, this screen initializes the draft session by loading the current baseline
 * (typically Governor 2026 CSV) and then calling {@link DraftEditSession#resetFromCurrent(double)}.</p>
 */
public class ProposeScreen {

    private static final int YEAR = 2026;

    private final User user;
    private final UserManager userManager;

    /**
     * Cached starting balance for this propose session.
     *
     * <p>Minister drafts are not constrained by balance, however the UI displays an informational
     * draft balance. This value is used as the baseline when the draft session is initialized.</p>
     */
    private Double startingBalance = null;

    /**
     * Constructs the ProposeScreen.
     *
     * @param user the current user (must be a {@link MinistryMember})
     * @param userManager application user manager instance
     */
    public ProposeScreen(User user, UserManager userManager) {
        this.user = user;
        this.userManager = userManager;
    }

    /**
     * Builds and shows the screen on the provided stage.
     *
     * <p>If the user is not a {@link MinistryMember}, access is denied.</p>
     *
     * @param stage primary window stage
     */
    public void show(Stage stage) {

        if (!(user instanceof MinistryMember)) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Access denied: Only Ministry Members can create proposals.");
            applyTheme(a);
            a.showAndWait();
            return;
        }

        CurrentSession.setUser(user);

        // Initialize draft session once and keep it alive across navigation.
        if (!DraftEditSession.isInitialized()) {
            initDraftSessionFromOfficial();
        }

        // ---------- Top bar ----------
        Label appLogo = new Label("GovBudget");
        appLogo.getStyleClass().add("app-logo");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(14, appLogo, spacer);
        topBar.getStyleClass().add("topbar");
        topBar.setPadding(new Insets(14));

        // ---------- Hero ----------
        Label title = new Label("Propose (Draft Edit)");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Draft edits only. Submit to Prime Minister for approval.");
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

        // ---------- Actions grid ----------
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

        VBox reset = actionCard(
                "Reset Draft",
                "Discard all draft edits and restore official baseline.",
                "/icons/reset.png",
                () -> resetDraft(stage)
        );
        reset.getStyleClass().addAll("danger-action");

        grid.add(simpleEdit, 0, 0);
        grid.add(bulkEdit, 1, 0);
        grid.add(history, 0, 1);
        grid.add(reset, 1, 1);

        // ---------- Submit (ONLY HERE) ----------
        Button sendBtn = new Button("Send Proposal to Prime Minister");
        sendBtn.getStyleClass().addAll("button", "primary");
        sendBtn.setMaxWidth(Double.MAX_VALUE);

        // Single source of truth for exporting proposals
        sendBtn.setOnAction(e -> {
            DraftProposalExporter.exportAndNotify(stage, user);
            // After sending, refresh the screen so chips reflect cleared state (if exporter resets session)
            show(stage);
        });

        // ---------- Footer ----------
        Button backBtn = new Button("Back");
        backBtn.getStyleClass().addAll("button", "subtle");
        backBtn.setOnAction(e -> new EditBudgetScreen(user, userManager).show(stage));

        HBox footer = new HBox(12, backBtn);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(12, 18, 18, 18));

        // ---------- Layout ----------
        VBox content = new VBox(16, heroCard, new Separator(), grid, sendBtn);
        content.setPadding(new Insets(18));
        content.getStyleClass().add("virtual-content");
        content.setMaxWidth(900);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("virtual-edit-root");
        root.setTop(topBar);
        root.setCenter(new VBox(content));
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

    /* =========================================================
       Draft session initialization and reset
       ========================================================= */

    /**
     * Initializes the minister draft session using the current official baseline.
     *
     * <p>Typical flow:
     * <ol>
     *   <li>Load official baseline (Governor draft for the year, if it exists).</li>
     *   <li>Reset {@link DraftEditSession} from current budgets.</li>
     *   <li>Point runtime arrays to sandbox so budget lookups match draft.</li>
     * </ol></p>
     *
     * <p>If the baseline file is missing, the current in-memory budgets are used.</p>
     */
    private void initDraftSessionFromOfficial() {
        try {
            if (startingBalance == null) startingBalance = 0.0;

            Path gov = Path.of("src/main/resources/NecessaryFilesAndData/Governor_" + YEAR + ".csv");
            if (Files.exists(gov)) {
                CreatingMinistries.loadUserBudgets(gov, YEAR);
            }

            DraftEditSession.resetFromCurrent(startingBalance);
            CreatingMinistries.ministries2026 = DraftEditSession.getSandbox();

        } catch (Exception e) {
            System.err.println("Failed to init minister draft session: " + e.getMessage());
        }
    }

    /**
     * Resets the draft session after user confirmation.
     *
     * <p>This discards all draft edits and restores the official baseline.</p>
     *
     * @param stage owner window
     */
    private void resetDraft(Stage stage) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.initOwner(stage);
        confirm.initModality(Modality.WINDOW_MODAL);
        confirm.setTitle("Reset Draft");
        confirm.setHeaderText("Discard draft edits?");
        confirm.setContentText("This will restore the official baseline and clear draft history.");

        applyTheme(confirm);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK) return;

            initDraftSessionFromOfficial();
            show(stage);
        });
    }

    /* =========================================================
       Simple Draft Edit dialog
       ========================================================= */

    /**
     * Opens a modal dialog allowing the minister to apply a single fixed edit
     * into the {@link DraftEditSession} sandbox.
     *
     * @param parentStage owner stage
     * @param balanceChip UI chip to refresh after apply
     * @param countChip UI chip to refresh after apply
     */
    private void openSimpleDraftDialog(Stage parentStage, Label balanceChip, Label countChip) {

        Stage dialog = new Stage();
        dialog.initOwner(parentStage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Simple Draft Edit");

        Label title = new Label("Edit Single Ministry (Draft)");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Select ministry, choose change type and enter amount.");
        subtitle.getStyleClass().add("subtitle");

        VBox header = new VBox(6, title, subtitle);

        ComboBox<String> ministryBox = new ComboBox<>();
        Arrays.stream(CreatingMinistries.ministries2026)
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
                double budget = Ministry.budgetSearchByName(n, CreatingMinistries.ministries2026);
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

            String err = DraftEditSession.applyFixed(ministry, isIncrease, amount);
            if (err != null) {
                errorLabel.setText(err);
                return;
            }

            // Refresh chips/labels
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

    /* =========================================================
       UI helpers
       ========================================================= */

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
        card.getStyleClass().addAll("card", "action-card", "image-card", "virtual-action");
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
