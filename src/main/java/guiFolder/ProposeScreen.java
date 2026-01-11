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
import javafx.scene.Scene;
import javafx.scene.Node;
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
 * <p>Draft proposal workspace for authorized users.</p>
 *
 * <p>This screen provides an isolated drafting environment where budget changes can be created
 * and managed as <b>draft edits</b>. Draft edits are applied to an internal draft state and are
 * not persisted to any official budget sources. The screen exposes a controlled set of actions:
 * creating draft edits, reviewing draft history, resetting the draft state, and submitting a proposal.</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Validate user eligibility for accessing the draft workflow.</li>
 *   <li>Initialize a draft editing state from an authoritative baseline for the configured year.</li>
 *   <li>Provide navigation to draft editing actions (simple edit, bulk edit, history, reset).</li>
 *   <li>Display draft metadata (draft balance and number of draft edits).</li>
 *   <li>Submit a draft proposal through an explicit user action.</li>
 * </ul>
 *
 * <h2>Safety Guarantees</h2>
 * <ul>
 *   <li>Draft operations do not write changes to official budget files.</li>
 *   <li>Reset is irreversible and requires confirmation.</li>
 *   <li>Submission is explicit and user-triggered.</li>
 * </ul>
 *
 * <h2>User Experience</h2>
 * <p>The layout uses a card-based action grid and modal dialogs with validation,
 * preserving window size/position state across navigation and applying subtle transitions.</p>
 */
public class ProposeScreen {

    /** The working year for the draft workflow. */
    private static final int YEAR = 2026;

    private final User user;
    private final UserManager userManager;

    /**
     * Cached starting balance used when initializing the draft environment.
     * This value is only used for initialization and does not represent persisted state.
     */
    private Double startingBalance = null;

    /**
     * Creates a new propose screen.
     *
     * @param user the active user
     * @param userManager application user manager
     */
    public ProposeScreen(User user, UserManager userManager) {
        this.user = user;
        this.userManager = userManager;
    }

    /**
     * Builds and displays the draft proposal interface on the given stage.
     *
     * <p>This method validates access, ensures the draft state is initialized,
     * constructs the full UI graph, applies styling, and preserves window state.</p>
     *
     * @param stage the primary application window
     */
    public void show(Stage stage) {

        if (!(user instanceof MinistryMember)) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Access denied: Only authorized users can create proposals.");
            applyTheme(a);
            a.showAndWait();
            return;
        }

        CurrentSession.setUser(user);

        if (!DraftEditSession.isInitialized()) {
            initDraftSessionFromOfficial();
        }

        // =========================
        // TOP BAR (Virtual-style)
        // =========================
        Label appLogo = new Label("BudgetReviewByCC");
        appLogo.getStyleClass().add("app-logo");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(14, appLogo, spacer);
        topBar.getStyleClass().add("topbar");
        topBar.setPadding(new Insets(14));

        // =========================
        // HERO (Virtual-style)
        // =========================
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

        // =========================
        // ACTION GRID (Virtual-style)
        // =========================
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
                "Discard all draft edits and restore the baseline.",
                "/icons/reset.png",
                () -> resetDraft(stage, balanceChip, countChip)
        );
        reset.getStyleClass().addAll("danger-action");

        grid.add(simpleEdit, 0, 0);
        grid.add(bulkEdit,  1, 0);
        grid.add(history,   0, 1);
        grid.add(reset,     1, 1);

        Button sendBtn = new Button("Send Proposal");
sendBtn.getStyleClass().addAll("button", "send-proposal-btn");
sendBtn.setMaxWidth(Double.MAX_VALUE);
sendBtn.setFocusTraversable(true);

// No hover effects. Click feedback is handled by CSS :pressed.
// Add sending state + safety.
sendBtn.setOnAction(e -> {
    if (sendBtn.isDisabled()) return;

    final String originalText = sendBtn.getText();
    sendBtn.setDisable(true);
    sendBtn.setText("Sending…");

    try {
        DraftProposalExporter.exportAndNotify(stage, user);

        sendBtn.setText("Proposal sent ✓");

        // Optional: refresh screen so chips update if exporter clears/changes state
        show(stage);

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

        // =========================
        // FOOTER (Virtual-style)
        // =========================
        Button backBtn = new Button("Back");
        backBtn.getStyleClass().addAll("button", "subtle");
        backBtn.setOnAction(e -> new EditBudgetScreen(user, userManager).show(stage));

        HBox footer = new HBox(backBtn);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(12, 18, 18, 18));

        // =========================
        // ROOT
        // =========================
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

    /**
     * Initializes the draft state from the current authoritative baseline.
     *
     * <p>The baseline is loaded for the configured year when available. If the baseline file
     * is missing, the method falls back to the currently loaded in-memory budgets.</p>
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
            System.err.println("Failed to initialize draft session: " + e.getMessage());
        }
    }

    /**
     * Resets the draft state after explicit confirmation.
     *
     * @param stage owner window for the confirmation dialog
     * @param balanceChip UI label that displays the draft balance
     * @param countChip UI label that displays the number of draft edits
     */
    private void resetDraft(Stage stage, Label balanceChip, Label countChip) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.initOwner(stage);
        confirm.initModality(Modality.WINDOW_MODAL);
        confirm.setTitle("Reset Draft");
        confirm.setHeaderText("Discard draft edits?");
        confirm.setContentText("This will restore the baseline and clear draft history.");

        applyTheme(confirm);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK) return;

            initDraftSessionFromOfficial();
            balanceChip.setText("Draft Balance: " + Ministry.getFormattedBudget(DraftEditSession.getDraftBalance()));
            countChip.setText("Draft edits: " + DraftEditSession.getHistory().size());
            show(stage);
        });
    }

    /**
     * Opens a modal dialog to apply a single fixed draft edit.
     *
     * @param parentStage owner window for the modal dialog
     * @param balanceChip UI label that displays the draft balance
     * @param countChip UI label that displays the number of draft edits
     */
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

    /**
     * Creates a keyboard-accessible action card used as the primary interaction surface.
     *
     * @param title primary title text
     * @param desc secondary descriptive text
     * @param iconPath icon resource path
     * @param onClick callback invoked when activated
     * @return configured action card container
     */
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

    /**
     * Loads an icon resource safely and returns a suitable node for UI composition.
     *
     * @param iconPath classpath icon resource path
     * @param size requested icon size in pixels
     * @return an ImageView when available; otherwise a styled fallback Label
     */
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

    /**
     * Applies the application theme stylesheet to the provided dialog.
     *
     * @param dialog the dialog to style
     */
    private void applyTheme(Dialog<?> dialog) {
        var css = getClass().getResource("/css/DarkTheme.css");
        if (css != null) dialog.getDialogPane().getStylesheets().add(css.toExternalForm());
    }

    /**
     * Applies the application theme stylesheet to the provided alert.
     *
     * @param a the alert to style
     */
    private void applyTheme(Alert a) {
        var css = getClass().getResource("/css/DarkTheme.css");
        if (css != null) a.getDialogPane().getStylesheets().add(css.toExternalForm());
    }

    /**
     * Replaces the stage scene while preserving the window's position, size, and
     * fullscreen/maximized state, preventing visual "jumps" during navigation.
     *
     * @param stage application window
     * @param scene new scene to set
     * @param title window title
     */
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
