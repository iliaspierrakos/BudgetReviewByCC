package guiFolder;

import UserFeatures.CreatingMinistries;
import UserFeatures.Ministry;
import UserManagement.CurrentSession;
import UserManagement.User;
import UserManagement.UserManager;

import javafx.animation.FadeTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * MinisterDraftEditScreen
 *
 * Virtual/Draft editing for Ministers:
 * - All edits apply ONLY to a sandbox copy (in-memory)
 * - No persistence to official CSVs or UserBudgets/
 * - Can export proposal into:
 *   src/main/resources/NecessaryFilesAndData/ProposalsFromMinisters/
 */
public class MinisterDraftEditScreen {

    private static final int YEAR = 2026;

    private static final Path PROPOSALS_DIR = Paths.get(
            "src/main/resources/NecessaryFilesAndData/ProposalsFromMinisters"
    );

    private final User user;
    private final UserManager userManager;

    /** Sandbox ministries (draft copy) */
    private final Ministry[] sandbox = new Ministry[CreatingMinistries.ministries2026.length];

    /** Draft “balance” (local) so increases can be constrained if you want */
    private double draftBalance;

    /** Draft history (local, not Edit.history) */
    private final ObservableList<DraftEditRow> draftHistory = FXCollections.observableArrayList();

    public MinisterDraftEditScreen(User user, UserManager userManager) {
        this.user = user;
        this.userManager = userManager;
        CurrentSession.setUser(user);

        // Start draft from the CURRENT in-memory “official” budgets already loaded in the app
        initSandboxFromCurrent();

        // If your project uses Edit.balance globally, you can start from that value.
        // Here we just start from 0 by default. If you want, set it from a known source.
        // draftBalance = Edit.balance;
        draftBalance = 0;
    }

    // ==========================
    // Models
    // ==========================

    private static class DraftEdit {
        final String ministry;
        final String changeType; // Increase/Decrease
        final double amount;     // positive
        final String mode;       // fixed/percent
        final LocalDateTime at;

        DraftEdit(String ministry, String changeType, double amount, String mode) {
            this.ministry = ministry;
            this.changeType = changeType;
            this.amount = amount;
            this.mode = mode;
            this.at = LocalDateTime.now();
        }
    }

    public static class DraftEditRow {
        private final SimpleStringProperty ministry = new SimpleStringProperty("");
        private final SimpleStringProperty action = new SimpleStringProperty("");
        private final SimpleStringProperty amount = new SimpleStringProperty("");
        private final SimpleStringProperty mode = new SimpleStringProperty("");
        private final SimpleStringProperty at = new SimpleStringProperty("");

        DraftEditRow(DraftEdit e) {
            ministry.set(e.ministry);
            action.set(e.changeType);
            amount.set(Ministry.getFormattedBudget(e.amount));
            mode.set(e.mode);
            at.set(e.at.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        public String getMinistry() { return ministry.get(); }
        public String getAction() { return action.get(); }
        public String getAmount() { return amount.get(); }
        public String getMode() { return mode.get(); }
        public String getAt() { return at.get(); }
    }

    public static class PreviewRow {
        private final SimpleStringProperty ministry = new SimpleStringProperty("");
        private final SimpleStringProperty previous = new SimpleStringProperty("");
        private final SimpleStringProperty now = new SimpleStringProperty("");
        private final SimpleStringProperty delta = new SimpleStringProperty("");

        public PreviewRow(String ministry, String previous, String now, String delta) {
            this.ministry.set(ministry);
            this.previous.set(previous);
            this.now.set(now);
            this.delta.set(delta);
        }

        public String getMinistry() { return ministry.get(); }
        public String getPrevious() { return previous.get(); }
        public String getNow() { return now.get(); }
        public String getDelta() { return delta.get(); }
    }

    // ==========================
    // Screen
    // ==========================

    public void show(Stage stage) {

        Label appLogo = new Label("GovBudget");
        appLogo.getStyleClass().add("app-logo");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(14, appLogo, spacer);
        topBar.getStyleClass().add("topbar");
        topBar.setPadding(new Insets(14));

        Label title = new Label("Minister Draft Edit");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Create draft edits (no official changes) and send proposal to Prime Minister.");
        subtitle.getStyleClass().add("subtitle");

        Label balanceChip = new Label("Draft Balance: " + Ministry.getFormattedBudget(draftBalance));
        balanceChip.getStyleClass().add("chip");

        Label editsChip = new Label("Draft edits: " + draftHistory.size());
        editsChip.getStyleClass().add("chip");

        VBox hero = new VBox(10, title, subtitle, new HBox(10, balanceChip, editsChip));
        hero.getStyleClass().addAll("card", "toolbar-card", "hero-card", "virtual-hero");
        hero.setMaxWidth(860);

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
                "Edit one ministry using a fixed amount (draft-only).",
                "/icons/wand.png",
                () -> openSimpleDraftDialog(stage, balanceChip, editsChip)
        );
        simpleEdit.getStyleClass().add("primary-action");

        VBox bulkEdit = actionCard(
                "Bulk Draft Edit",
                "Apply changes to multiple ministries (draft-only).",
                "/icons/bulk.png",
                () -> openBulkDraftMenu(stage, balanceChip, editsChip)
        );
        bulkEdit.getStyleClass().add("primary-action");

        VBox history = actionCard(
                "Draft History",
                "Review your draft edits.",
                "/icons/history.png",
                () -> openDraftHistory(stage)
        );
        history.getStyleClass().add("primary-action");

        VBox reset = actionCard(
                "Reset Draft",
                "Discard all draft edits and restore sandbox to current official budgets.",
                "/icons/reset.png",
                () -> resetDraft(stage, balanceChip, editsChip)
        );
        reset.getStyleClass().addAll("danger-action");

        VBox send = actionCard(
                "Send to Prime Minister",
                "Export proposal file into ProposalsFromMinisters/ for review.",
                "/icons/selected.png",
                () -> exportProposal(stage)
        );
        send.getStyleClass().add("primary-action");

        grid.add(simpleEdit, 0, 0);
        grid.add(bulkEdit,   1, 0);
        grid.add(history,    0, 1);
        grid.add(send,       1, 1);
        grid.add(reset,      0, 2, 2, 1);

        // Sandbox budgets table (live view)
        TableView<PreviewRow> sandboxTable = buildSandboxTable();
        sandboxTable.setItems(buildSandboxRows());
        VBox tableCard = new VBox(10, sectionTitle("Sandbox Budgets (Draft View)"), sandboxTable);
        tableCard.getStyleClass().addAll("card", "table-card");
        tableCard.setPadding(new Insets(14));

        VBox content = new VBox(16, hero, new Separator(), grid, tableCard);
        content.setPadding(new Insets(18));
        content.getStyleClass().add("virtual-content");

        Button backBtn = new Button("Back");
        backBtn.getStyleClass().addAll("button", "subtle");
        backBtn.setOnAction(e -> new EditBudgetScreen(user, userManager).show(stage));

        Button refreshTableBtn = new Button("Refresh View");
        refreshTableBtn.getStyleClass().addAll("button", "subtle");
        refreshTableBtn.setOnAction(e -> sandboxTable.setItems(buildSandboxRows()));

        HBox footer = new HBox(10, backBtn, refreshTableBtn);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(12, 18, 18, 18));

        BorderPane root = new BorderPane();
        root.getStyleClass().add("virtual-edit-root");
        root.setTop(topBar);
        root.setCenter(content);
        root.setBottom(footer);

        Scene scene = new Scene(root,
                stage.getWidth() > 0 ? stage.getWidth() : 1100,
                stage.getHeight() > 0 ? stage.getHeight() : 820
        );

        var css = getClass().getResource("/css/DarkTheme.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());

        applyScenePreserveWindow(stage, scene, "Minister Draft Edit");

        FadeTransition ft = new FadeTransition(Duration.millis(200), root);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    // ==========================
    // Simple Draft Edit (one ministry)
    // ==========================

    private void openSimpleDraftDialog(Stage parentStage, Label balanceChip, Label editsChip) {

        Stage dialog = new Stage();
        dialog.initOwner(parentStage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Simple Draft Edit");

        Label title = new Label("Edit Single Ministry (Draft)");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Select ministry, choose change type and enter amount (draft-only).");
        subtitle.getStyleClass().add("subtitle");

        VBox header = new VBox(6, title, subtitle);

        ComboBox<String> ministryBox = new ComboBox<>();
        Arrays.stream(sandbox)
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

        HBox changeTypeBox = new HBox(10, increaseBtn, decreaseBtn);
        changeTypeBox.setAlignment(Pos.CENTER_LEFT);

        TextField amountField = new TextField();
        amountField.setPromptText("Amount (e.g., 1000000)");
        amountField.setMaxWidth(Double.MAX_VALUE);
        amountField.setTextFormatter(positiveNumberFormatter());

        Label balanceLabel = new Label("Draft Balance: " + Ministry.getFormattedBudget(draftBalance));
        balanceLabel.getStyleClass().add("subtitle");

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error");
        errorLabel.setWrapText(true);

        Button applyBtn = new Button("Apply Draft");
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
                double budget = budgetByNameSandbox(n);
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
            String changeType = isIncrease ? "Increase" : "Decrease";

            Ministry m = ministryObjByNameSandbox(ministry);
            if (m == null) { errorLabel.setText("Ministry not found."); return; }

            double currentBudget = m.getBudget();

            if (!isIncrease && amount > currentBudget) {
                errorLabel.setText("Cannot decrease more than current budget.");
                return;
            }

            // If you want to enforce draft balance for increases, uncomment:
            // if (isIncrease && amount > draftBalance) { errorLabel.setText("Insufficient draft balance."); return; }

            // Apply to sandbox
            double newBudget = isIncrease ? (currentBudget + amount) : (currentBudget - amount);
            if (newBudget < 0) { errorLabel.setText("Would create negative budget."); return; }

            m.setBudget(newBudget);

            // Update local draft balance if you want:
            // if (isIncrease) draftBalance -= amount; else draftBalance += amount;

            addHistory(new DraftEdit(ministry, changeType, amount, "fixed"));

            balanceChip.setText("Draft Balance: " + Ministry.getFormattedBudget(draftBalance));
            balanceLabel.setText("Draft Balance: " + Ministry.getFormattedBudget(draftBalance));
            editsChip.setText("Draft edits: " + draftHistory.size());

            showThemedAlert(
                    Alert.AlertType.INFORMATION,
                    "Success",
                    "Draft Updated Successfully",
                    "Ministry: " + ministry + "\n" +
                            "Action: " + changeType + " by " + Ministry.getFormattedBudget(amount) + "\n" +
                            "New Draft Budget: " + Ministry.getFormattedBudget(newBudget)
            );

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
        applyTheme(scene);

        dialog.setScene(scene);
        dialog.centerOnScreen();
        dialog.show();
    }

    // ==========================
    // Bulk Draft (menu -> dialogs)
    // ==========================

    private void openBulkDraftMenu(Stage parentStage, Label balanceChip, Label editsChip) {
        Stage dialog = new Stage();
        dialog.initOwner(parentStage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Bulk Draft Options");

        VBox card = new VBox(12);
        card.getStyleClass().addAll("card", "toolbar-card");
        card.setPadding(new Insets(18));

        Label title = new Label("Bulk Draft Options");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Choose how you want to apply draft edits.");
        subtitle.getStyleClass().add("subtitle");

        Button percentAll = new Button("Percentage (All)");
        Button fixedAll = new Button("Fixed Amount (All)");
        Button selected = new Button("Selected Ministries");
        Button cancel = new Button("Cancel");

        percentAll.getStyleClass().addAll("button", "primary");
        fixedAll.getStyleClass().addAll("button", "primary");
        selected.getStyleClass().addAll("button", "primary");
        cancel.getStyleClass().addAll("button", "subtle");

        percentAll.setOnAction(e -> { dialog.close(); bulkPercentAll(parentStage, balanceChip, editsChip); });
        fixedAll.setOnAction(e -> { dialog.close(); bulkFixedAll(parentStage, balanceChip, editsChip); });
        selected.setOnAction(e -> { dialog.close(); bulkSelected(parentStage, balanceChip, editsChip); });
        cancel.setOnAction(e -> dialog.close());

        VBox buttons = new VBox(10, percentAll, fixedAll, selected, cancel);
        buttons.setFillWidth(true);

        card.getChildren().addAll(title, subtitle, new Separator(), buttons);

        Scene s = new Scene(new VBox(card), 520, 420);
        applyTheme(s);
        dialog.setScene(s);
        dialog.centerOnScreen();
        dialog.show();
    }

    private void bulkPercentAll(Stage parent, Label balanceChip, Label editsChip) {
        Stage dialog = baseDialog(parent, "Percentage Draft (All)");

        VBox card = new VBox(14);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(18));

        Label title = new Label("Apply Percentage Draft to ALL Ministries");
        title.getStyleClass().add("title");

        Label hint = new Label("Enter positive percentage (e.g., 5), choose Increase/Decrease, preview then apply.");
        hint.getStyleClass().add("subtitle");

        ToggleGroup typeGroup = new ToggleGroup();
        ToggleButton incBtn = new ToggleButton("Increase");
        ToggleButton decBtn = new ToggleButton("Decrease");
        incBtn.setToggleGroup(typeGroup);
        decBtn.setToggleGroup(typeGroup);
        incBtn.setSelected(true);

        TextField percentField = new TextField();
        percentField.setPromptText("Percentage (e.g., 5)");
        percentField.setTextFormatter(positiveNumberFormatter());

        Label error = new Label();
        error.getStyleClass().add("error");
        error.setWrapText(true);

        TableView<PreviewRow> preview = buildPreviewTable();
        preview.setVisible(false);
        preview.setManaged(false);

        Button previewBtn = new Button("Preview");
        Button applyBtn = new Button("Apply Draft");
        Button cancelBtn = new Button("Cancel");
        previewBtn.getStyleClass().addAll("button", "subtle");
        applyBtn.getStyleClass().addAll("button", "primary");
        cancelBtn.getStyleClass().addAll("button", "subtle");
        applyBtn.setDisable(true);

        cancelBtn.setOnAction(e -> dialog.close());

        previewBtn.setOnAction(e -> {
            error.setText("");

            String txt = percentField.getText().trim();
            if (!isValidPositiveNumber(txt)) { error.setText("Please enter a valid percentage."); return; }

            double pct = Double.parseDouble(txt);
            if (pct <= 0) { error.setText("Percentage must be positive."); return; }

            boolean isIncrease = typeGroup.getSelectedToggle() == incBtn;
            double signed = isIncrease ? pct : -pct;
            if (signed <= -100) { error.setText("Cannot decrease by 100% or more."); return; }

            ObservableList<PreviewRow> rows = FXCollections.observableArrayList();
            for (Ministry m : sandbox) {
                if (m == null) continue;

                double oldB = m.getBudget();
                double newB = oldB * (1 + signed / 100.0);
                if (newB < 0) { error.setText("Would create negative budget for: " + m.getMinistryName()); return; }

                rows.add(new PreviewRow(
                        m.getMinistryName(),
                        Ministry.getFormattedBudget(oldB),
                        Ministry.getFormattedBudget(newB),
                        formatDelta(newB - oldB)
                ));
            }

            preview.setItems(rows);
            preview.setVisible(true);
            preview.setManaged(true);
            applyBtn.setDisable(false);
        });

        applyBtn.setOnAction(e -> {
            error.setText("");

            String txt = percentField.getText().trim();
            if (!isValidPositiveNumber(txt)) { error.setText("Please enter a valid percentage."); return; }

            double pct = Double.parseDouble(txt);
            if (pct <= 0) { error.setText("Percentage must be positive."); return; }

            boolean isIncrease = typeGroup.getSelectedToggle() == incBtn;
            double signed = isIncrease ? pct : -pct;
            if (signed <= -100) { error.setText("Cannot decrease by 100% or more."); return; }

            for (Ministry m : sandbox) {
                if (m == null) continue;

                double oldB = m.getBudget();
                double newB = oldB * (1 + signed / 100.0);
                if (newB < 0) { error.setText("Would create negative budget for: " + m.getMinistryName()); return; }

                double delta = newB - oldB;
                if (Math.abs(delta) < 1e-9) continue;

                m.setBudget(newB);

                String change = delta > 0 ? "Increase" : "Decrease";
                addHistory(new DraftEdit(m.getMinistryName(), change, Math.abs(delta), "percent"));
            }

            balanceChip.setText("Draft Balance: " + Ministry.getFormattedBudget(draftBalance));
            editsChip.setText("Draft edits: " + draftHistory.size());

            dialog.close();
        });

        HBox actions = new HBox(10, previewBtn, applyBtn, cancelBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(title, hint, new HBox(10, incBtn, decBtn), percentField, error, actions, preview);

        Scene s = new Scene(card, 980, 640);
        applyTheme(s);
        dialog.setScene(s);
        dialog.centerOnScreen();
        dialog.show();
    }

    private void bulkFixedAll(Stage parent, Label balanceChip, Label editsChip) {
        Stage dialog = baseDialog(parent, "Fixed Amount Draft (All)");

        VBox card = new VBox(14);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(18));

        Label title = new Label("Apply Fixed Amount Draft to ALL Ministries");
        title.getStyleClass().add("title");

        Label hint = new Label("Enter positive amount, choose Increase/Decrease, preview then apply.");
        hint.getStyleClass().add("subtitle");

        ToggleGroup typeGroup = new ToggleGroup();
        ToggleButton incBtn = new ToggleButton("Increase");
        ToggleButton decBtn = new ToggleButton("Decrease");
        incBtn.setToggleGroup(typeGroup);
        decBtn.setToggleGroup(typeGroup);
        incBtn.setSelected(true);

        TextField amountField = new TextField();
        amountField.setPromptText("Amount (e.g., 50000)");
        amountField.setTextFormatter(positiveNumberFormatter());

        Label error = new Label();
        error.getStyleClass().add("error");
        error.setWrapText(true);

        TableView<PreviewRow> preview = buildPreviewTable();
        preview.setVisible(false);
        preview.setManaged(false);

        Button previewBtn = new Button("Preview");
        Button applyBtn = new Button("Apply Draft");
        Button cancelBtn = new Button("Cancel");
        previewBtn.getStyleClass().addAll("button", "subtle");
        applyBtn.getStyleClass().addAll("button", "primary");
        cancelBtn.getStyleClass().addAll("button", "subtle");
        applyBtn.setDisable(true);

        cancelBtn.setOnAction(e -> dialog.close());

        previewBtn.setOnAction(e -> {
            error.setText("");

            String txt = amountField.getText().trim();
            if (!isValidPositiveNumber(txt)) { error.setText("Please enter a valid amount."); return; }

            double amount = Double.parseDouble(txt);
            if (amount <= 0) { error.setText("Amount must be positive."); return; }

            boolean isIncrease = typeGroup.getSelectedToggle() == incBtn;
            double signed = isIncrease ? amount : -amount;

            ObservableList<PreviewRow> rows = FXCollections.observableArrayList();
            for (Ministry m : sandbox) {
                if (m == null) continue;

                double oldB = m.getBudget();
                double newB = oldB + signed;
                if (newB < 0) { error.setText("Would create negative budget for: " + m.getMinistryName()); return; }

                rows.add(new PreviewRow(
                        m.getMinistryName(),
                        Ministry.getFormattedBudget(oldB),
                        Ministry.getFormattedBudget(newB),
                        formatDelta(newB - oldB)
                ));
            }

            preview.setItems(rows);
            preview.setVisible(true);
            preview.setManaged(true);
            applyBtn.setDisable(false);
        });

        applyBtn.setOnAction(e -> {
            error.setText("");

            String txt = amountField.getText().trim();
            if (!isValidPositiveNumber(txt)) { error.setText("Please enter a valid amount."); return; }

            double amount = Double.parseDouble(txt);
            if (amount <= 0) { error.setText("Amount must be positive."); return; }

            boolean isIncrease = typeGroup.getSelectedToggle() == incBtn;
            double signed = isIncrease ? amount : -amount;

            for (Ministry m : sandbox) {
                if (m == null) continue;

                double oldB = m.getBudget();
                double newB = oldB + signed;
                if (newB < 0) { error.setText("Would create negative budget for: " + m.getMinistryName()); return; }

                double delta = newB - oldB;
                if (Math.abs(delta) < 1e-9) continue;

                m.setBudget(newB);

                String change = delta > 0 ? "Increase" : "Decrease";
                addHistory(new DraftEdit(m.getMinistryName(), change, Math.abs(delta), "fixed"));
            }

            balanceChip.setText("Draft Balance: " + Ministry.getFormattedBudget(draftBalance));
            editsChip.setText("Draft edits: " + draftHistory.size());

            dialog.close();
        });

        HBox actions = new HBox(10, previewBtn, applyBtn, cancelBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(title, hint, new HBox(10, incBtn, decBtn), amountField, error, actions, preview);

        Scene s = new Scene(card, 980, 640);
        applyTheme(s);
        dialog.setScene(s);
        dialog.centerOnScreen();
        dialog.show();
    }

    private void bulkSelected(Stage parent, Label balanceChip, Label editsChip) {
        Stage dialog = baseDialog(parent, "Draft Selected Ministries");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(18));

        Label title = new Label("Draft Selected Ministries");
        title.getStyleClass().add("title");

        Label error = new Label();
        error.getStyleClass().add("error");
        error.setWrapText(true);

        // pick table
        TableView<MinistryPickRow> pick = new TableView<>();
        pick.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        pick.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<MinistryPickRow, String> cMin = new TableColumn<>("Ministry");
        cMin.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getMinistry()));
        TableColumn<MinistryPickRow, String> cCur = new TableColumn<>("Current (Sandbox)");
        cCur.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getCurrent()));
        cCur.setStyle("-fx-alignment: CENTER-RIGHT;");
        pick.getColumns().addAll(cMin, cCur);

        ObservableList<MinistryPickRow> rows = FXCollections.observableArrayList();
        for (int i = 0; i < sandbox.length; i++) {
            if (sandbox[i] == null) continue;
            rows.add(new MinistryPickRow(i, sandbox[i].getMinistryName(), Ministry.getFormattedBudget(sandbox[i].getBudget())));
        }
        pick.setItems(rows);

        ToggleGroup modeGroup = new ToggleGroup();
        ToggleButton percentBtn = new ToggleButton("Percentage");
        ToggleButton fixedBtn = new ToggleButton("Fixed Amount");
        percentBtn.setToggleGroup(modeGroup);
        fixedBtn.setToggleGroup(modeGroup);
        percentBtn.setSelected(true);

        ToggleGroup typeGroup = new ToggleGroup();
        ToggleButton incBtn = new ToggleButton("Increase");
        ToggleButton decBtn = new ToggleButton("Decrease");
        incBtn.setToggleGroup(typeGroup);
        decBtn.setToggleGroup(typeGroup);
        incBtn.setSelected(true);

        TextField valueField = new TextField();
        valueField.setPromptText("Value (e.g. 5 or 100000)");
        valueField.setTextFormatter(positiveNumberFormatter());

        TableView<PreviewRow> preview = buildPreviewTable();

        Button previewBtn = new Button("Preview");
        Button applyBtn = new Button("Apply Draft");
        Button cancelBtn = new Button("Cancel");
        previewBtn.getStyleClass().addAll("button", "subtle");
        applyBtn.getStyleClass().addAll("button", "primary");
        cancelBtn.getStyleClass().addAll("button", "subtle");
        cancelBtn.setOnAction(e -> dialog.close());

        previewBtn.setOnAction(e -> {
            error.setText("");
            var sel = pick.getSelectionModel().getSelectedItems();
            if (sel == null || sel.isEmpty()) { error.setText("Select at least one ministry."); return; }

            String txt = valueField.getText().trim();
            if (!isValidPositiveNumber(txt)) { error.setText("Enter a valid value."); return; }
            double value = Double.parseDouble(txt);
            if (value <= 0) { error.setText("Value must be positive."); return; }

            boolean isPercent = modeGroup.getSelectedToggle() == percentBtn;
            boolean isIncrease = typeGroup.getSelectedToggle() == incBtn;

            double signed = isIncrease ? value : -value;
            if (isPercent && signed <= -100) { error.setText("Cannot decrease by 100% or more."); return; }

            ObservableList<PreviewRow> pr = FXCollections.observableArrayList();
            for (MinistryPickRow r : sel) {
                Ministry m = sandbox[r.getIndex()];
                if (m == null) continue;

                double oldB = m.getBudget();
                double newB = isPercent ? oldB * (1 + signed / 100.0) : oldB + signed;
                if (newB < 0) { error.setText("Would create negative budget for: " + m.getMinistryName()); return; }

                pr.add(new PreviewRow(
                        m.getMinistryName(),
                        Ministry.getFormattedBudget(oldB),
                        Ministry.getFormattedBudget(newB),
                        formatDelta(newB - oldB)
                ));
            }
            preview.setItems(pr);
        });

        applyBtn.setOnAction(e -> {
            error.setText("");
            var sel = pick.getSelectionModel().getSelectedItems();
            if (sel == null || sel.isEmpty()) { error.setText("Select at least one ministry."); return; }

            String txt = valueField.getText().trim();
            if (!isValidPositiveNumber(txt)) { error.setText("Enter a valid value."); return; }
            double value = Double.parseDouble(txt);
            if (value <= 0) { error.setText("Value must be positive."); return; }

            boolean isPercent = modeGroup.getSelectedToggle() == percentBtn;
            boolean isIncrease = typeGroup.getSelectedToggle() == incBtn;

            double signed = isIncrease ? value : -value;
            if (isPercent && signed <= -100) { error.setText("Cannot decrease by 100% or more."); return; }

            for (MinistryPickRow r : sel) {
                Ministry m = sandbox[r.getIndex()];
                if (m == null) continue;

                double oldB = m.getBudget();
                double newB = isPercent ? oldB * (1 + signed / 100.0) : oldB + signed;
                if (newB < 0) { error.setText("Would create negative budget for: " + m.getMinistryName()); return; }

                double delta = newB - oldB;
                if (Math.abs(delta) < 1e-9) continue;

                m.setBudget(newB);
                String change = delta > 0 ? "Increase" : "Decrease";
                addHistory(new DraftEdit(m.getMinistryName(), change, Math.abs(delta), isPercent ? "percent" : "fixed"));
            }

            balanceChip.setText("Draft Balance: " + Ministry.getFormattedBudget(draftBalance));
            editsChip.setText("Draft edits: " + draftHistory.size());
            dialog.close();
        });

        VBox left = new VBox(10, sectionTitle("Pick ministries"), pick);
        left.getStyleClass().addAll("card", "table-card");
        left.setPadding(new Insets(14));
        VBox.setVgrow(pick, Priority.ALWAYS);

        VBox controls = new VBox(
                10,
                sectionTitle("Controls"),
                new Label("Mode"), new HBox(10, percentBtn, fixedBtn),
                new Label("Type"), new HBox(10, incBtn, decBtn),
                new Label("Value"), valueField,
                error,
                new HBox(10, previewBtn, applyBtn, cancelBtn)
        );
        controls.getStyleClass().addAll("card", "toolbar-card");
        controls.setPadding(new Insets(14));

        VBox right = new VBox(10, controls, sectionTitle("Preview"), preview);
        VBox.setVgrow(preview, Priority.ALWAYS);

        HBox center = new HBox(14, left, right);
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);

        root.setTop(new VBox(8, title));
        root.setCenter(center);

        Scene s = new Scene(root, 1180, 760);
        applyTheme(s);
        dialog.setScene(s);
        dialog.centerOnScreen();
        dialog.show();
    }

    // ==========================
    // History
    // ==========================

    private void openDraftHistory(Stage owner) {
        Stage dialog = baseDialog(owner, "Draft History");

        VBox card = new VBox(12);
        card.getStyleClass().addAll("card", "toolbar-card");
        card.setPadding(new Insets(18));

        Label title = new Label("Draft History");
        title.getStyleClass().add("title");

        TableView<DraftEditRow> table = new TableView<>();
        table.setItems(draftHistory);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<DraftEditRow, String> c1 = new TableColumn<>("Ministry");
        c1.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getMinistry()));
        TableColumn<DraftEditRow, String> c2 = new TableColumn<>("Action");
        c2.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getAction()));
        TableColumn<DraftEditRow, String> c3 = new TableColumn<>("Amount");
        c3.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getAmount()));
        c3.setStyle("-fx-alignment: CENTER-RIGHT;");
        TableColumn<DraftEditRow, String> c4 = new TableColumn<>("Mode");
        c4.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getMode()));
        TableColumn<DraftEditRow, String> c5 = new TableColumn<>("At");
        c5.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getAt()));

        table.getColumns().addAll(c1, c2, c3, c4, c5);

        Button close = new Button("Close");
        close.getStyleClass().addAll("button", "subtle");
        close.setOnAction(e -> dialog.close());

        card.getChildren().addAll(title, new Separator(), table, close);

        Scene s = new Scene(new VBox(card), 980, 620);
        applyTheme(s);
        dialog.setScene(s);
        dialog.centerOnScreen();
        dialog.show();
    }

    // ==========================
    // Reset + Export
    // ==========================

    private void resetDraft(Stage stage, Label balanceChip, Label editsChip) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.initOwner(stage);
        confirm.initModality(Modality.WINDOW_MODAL);
        confirm.setTitle("Reset Draft");
        confirm.setHeaderText("Discard draft edits?");
        confirm.setContentText("This will restore the sandbox from current official budgets and clear draft history.");

        applyTheme(confirm);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK) return;

            draftHistory.clear();
            initSandboxFromCurrent();

            // optional: reset draftBalance
            draftBalance = 0;

            balanceChip.setText("Draft Balance: " + Ministry.getFormattedBudget(draftBalance));
            editsChip.setText("Draft edits: " + draftHistory.size());
        });
    }

    private void exportProposal(Stage owner) {
        if (draftHistory.isEmpty()) {
            showThemedAlert(Alert.AlertType.WARNING, "Nothing to send", "No draft edits", "Create some draft edits first.");
            return;
        }

        try {
            Files.createDirectories(PROPOSALS_DIR);

            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "MinisterDraft_" + user.getUsername() + "_" + YEAR + "_" + stamp + ".txt";
            Path out = PROPOSALS_DIR.resolve(filename);

            String content = buildProposalText();
            Files.writeString(out, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);

            showThemedAlert(
                    Alert.AlertType.INFORMATION,
                    "Sent",
                    "Draft exported",
                    "Saved to:\n" + out.normalize() + "\n\nPrime Minister can now review it."
            );

        } catch (IOException ex) {
            showThemedAlert(Alert.AlertType.ERROR, "Error", "Export failed", ex.getMessage());
        }
    }

    private String buildProposalText() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== MINISTER DRAFT PROPOSAL ===\n");
        sb.append("Year: ").append(YEAR).append("\n");
        sb.append("From: ").append(user.getUsername()).append(" (").append(user.getRole().name()).append(")\n");
        sb.append("Created at: ").append(LocalDateTime.now()).append("\n\n");

        sb.append("---- Draft Edits ----\n");
        int i = 1;
        for (DraftEditRow r : draftHistory) {
            sb.append(i++).append(") ")
              .append(r.getMinistry()).append(" | ")
              .append(r.getAction()).append(" ")
              .append(r.getAmount()).append(" | ")
              .append("mode=").append(r.getMode()).append(" | ")
              .append("at=").append(r.getAt()).append("\n");
        }

        sb.append("\n---- Resulting Draft Budgets (Sandbox) ----\n");
        for (Ministry m : sandbox) {
            if (m == null) continue;
            sb.append(m.getMinistryName()).append(": ")
              .append(Ministry.getFormattedBudget(m.getBudget())).append("\n");
        }

        sb.append("\n=== END ===\n");
        return sb.toString();
    }

    // ==========================
    // Helpers (sandbox + UI)
    // ==========================

    private void initSandboxFromCurrent() {
        for (int i = 0; i < CreatingMinistries.ministries2026.length; i++) {
            Ministry m = CreatingMinistries.ministries2026[i];
            if (m == null) { sandbox[i] = null; continue; }

            // Assumes Ministry(String name, double budget) exists in your project.
            sandbox[i] = new Ministry(m.getMinistryName(), m.getBudget());
        }
    }

    private double budgetByNameSandbox(String name) {
        Ministry m = ministryObjByNameSandbox(name);
        return (m == null) ? 0 : m.getBudget();
    }

    private Ministry ministryObjByNameSandbox(String name) {
        for (Ministry m : sandbox) {
            if (m == null) continue;
            if (m.getMinistryName() != null && m.getMinistryName().equals(name)) return m;
        }
        return null;
    }

    private void addHistory(DraftEdit e) {
        draftHistory.add(new DraftEditRow(e));
    }

    private VBox actionCard(String title, String desc, String iconPath, Runnable onClick) {
        Node iconNode = safeIcon(iconPath, 34);

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
            var stream = MinisterDraftEditScreen.class.getResourceAsStream(iconPath);
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

    private Label sectionTitle(String t) {
        Label l = new Label(t);
        l.getStyleClass().add("section-title");
        return l;
    }

    private TableView<PreviewRow> buildSandboxTable() {
        TableView<PreviewRow> t = new TableView<>();
        t.getStyleClass().addAll("table-view", "budget-table");
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<PreviewRow, String> colMin = new TableColumn<>("Ministry");
        colMin.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getMinistry()));

        TableColumn<PreviewRow, String> colNow = new TableColumn<>("Draft Budget");
        colNow.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getNow()));
        colNow.setStyle("-fx-alignment: CENTER-RIGHT;");

        t.getColumns().addAll(colMin, colNow);
        return t;
    }

    private ObservableList<PreviewRow> buildSandboxRows() {
        ObservableList<PreviewRow> rows = FXCollections.observableArrayList();
        for (Ministry m : sandbox) {
            if (m == null) continue;
            rows.add(new PreviewRow(m.getMinistryName(), "", Ministry.getFormattedBudget(m.getBudget()), ""));
        }
        return rows;
    }

    private TableView<PreviewRow> buildPreviewTable() {
        TableView<PreviewRow> table = new TableView<>();
        table.getStyleClass().addAll("table-view", "budget-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(42);

        TableColumn<PreviewRow, String> colMin = new TableColumn<>("Ministry");
        colMin.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getMinistry()));

        TableColumn<PreviewRow, String> colPrev = new TableColumn<>("Previous");
        colPrev.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getPrevious()));
        colPrev.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<PreviewRow, String> colNow = new TableColumn<>("New");
        colNow.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getNow()));
        colNow.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<PreviewRow, String> colDelta = new TableColumn<>("Change");
        colDelta.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getDelta()));
        colDelta.setStyle("-fx-alignment: CENTER-RIGHT;");

        table.getColumns().addAll(colMin, colPrev, colNow, colDelta);
        return table;
    }

    private String formatDelta(double delta) {
        String s = Ministry.getFormattedBudget(Math.abs(delta));
        if (delta > 0) return "+" + s;
        if (delta < 0) return "-" + s;
        return "0";
    }

    private TextFormatter<String> positiveNumberFormatter() {
        return new TextFormatter<>(change -> {
            String t = change.getControlNewText().trim();
            if (t.isEmpty()) return change;
            if (t.matches("\\d*([\\.]\\d{0,2})?")) return change;
            return null;
        });
    }

    private boolean isValidPositiveNumber(String txt) {
        if (txt == null) return false;
        txt = txt.trim();
        if (txt.isEmpty()) return false;
        if (txt.equals(".")) return false;
        return txt.matches("\\d+(\\.\\d{0,2})?");
    }

    private Stage baseDialog(Stage parent, String title) {
        Stage d = new Stage();
        d.initOwner(parent);
        d.initModality(Modality.WINDOW_MODAL);
        d.setTitle(title);
        return d;
    }

    private void applyTheme(Scene scene) {
        var css = getClass().getResource("/css/DarkTheme.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
    }

    private void applyTheme(Alert alert) {
        var css = getClass().getResource("/css/DarkTheme.css");
        if (css != null) alert.getDialogPane().getStylesheets().add(css.toExternalForm());
    }

    private void showThemedAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        applyTheme(alert);
        alert.showAndWait();
    }

    public static class MinistryPickRow {
        private final SimpleStringProperty ministry = new SimpleStringProperty("");
        private final SimpleStringProperty current = new SimpleStringProperty("");
        private final int index;

        public MinistryPickRow(int index, String ministry, String current) {
            this.index = index;
            this.ministry.set(ministry);
            this.current.set(current);
        }

        public int getIndex() { return index; }
        public String getMinistry() { return ministry.get(); }
        public String getCurrent() { return current.get(); }
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
