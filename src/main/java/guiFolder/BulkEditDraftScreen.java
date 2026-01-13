package guiFolder;

import UserFeatures.DraftEditSession;
import UserFeatures.Ministry;
import UserManagement.CurrentSession;
import UserManagement.MinistryMember;
import UserManagement.User;
import UserManagement.UserManager;
import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
 * BulkEditDraftScreen
 *
 * Minister-only screen that performs draft bulk edits.
 *
 * Rules:
 *  - Applies edits ONLY to DraftEditSession sandbox.
 *  - DraftEditSession is the single source of truth for:
 *      - sandbox budgets
 *      - draft history
 *      - draft balance
 */
public class BulkEditDraftScreen {

    private final User user;
    private final UserManager userManager;

    public BulkEditDraftScreen(User user, UserManager userManager) {
        this.user = user;
        this.userManager = userManager;
    }

    /** Table model for preview rows. */
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

    /** Table model for selecting ministries. */
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

    public void show(Stage stage) {

        if (!(user instanceof MinistryMember)) {
            Alert a = new Alert(Alert.AlertType.ERROR,
                    "Access denied: Only Ministry Members can use Draft Bulk Edit.");
            applyTheme(a);
            a.showAndWait();
            return;
        }

        CurrentSession.setUser(user);

        // Ensure draft session exists
        if (!DraftEditSession.isInitialized()) {
            DraftEditSession.resetFromCurrent(0);
        }

        // ---------- Top bar ----------
        Label appLogo = new Label("BudgetReviewByCC");
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

        // ---------- Hero ----------
        Label title = new Label("Bulk Draft Edit");
        title.getStyleClass().add("title");
        title.setStyle("-fx-effect: dropshadow(gaussian, rgba(212,175,55,0.14), 16, 0.20, 0, 0);");

        Label subtitle = new Label("Create draft edits (NOT persisted). Submit only from Propose screen.");
        subtitle.getStyleClass().add("subtitle");

        Label chip1 = new Label("Draft • 2026");
        chip1.getStyleClass().add("chip");

        Label chip2 = new Label("Role: " + user.getRole().name());
        chip2.getStyleClass().add("chip");

        Label chip3 = new Label("Draft Balance: " + Ministry.getFormattedBudget(DraftEditSession.getDraftBalance()));
        chip3.getStyleClass().add("chip");

        HBox chips = new HBox(10, chip1, chip2, chip3);
        chips.setAlignment(Pos.CENTER_LEFT);

        VBox heroCard = new VBox(10, title, subtitle, chips);
        heroCard.getStyleClass().addAll("card", "toolbar-card", "hero-card");
        heroCard.setMaxWidth(Double.MAX_VALUE);
        heroCard.setStyle("-fx-border-color: rgba(212,175,55,0.14);");

        // ---------- Action grid ----------
        GridPane grid = new GridPane();
        grid.getStyleClass().add("action-grid");
        grid.setHgap(18);
        grid.setVgap(18);
        grid.setAlignment(Pos.TOP_CENTER);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(50);
        c1.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c1);

        VBox percentAllCard = actionCard(
                "Percentage Draft (All)",
                "Apply a percentage to all ministries with preview (draft).",
                "/icons/percent.png",
                () -> showPercentageAllDialog(stage, chip3)
        );

        VBox fixedAllCard = actionCard(
                "Fixed Amount Draft (All)",
                "Apply a fixed amount to all ministries with preview (draft).",
                "/icons/exposure.png",
                () -> showFixedAllDialog(stage, chip3)
        );

        VBox selectedCard = actionCard(
                "Draft Selected Ministries",
                "Select ministries, preview changes, then apply (draft).",
                "/icons/selected.png",
                () -> showSelectedMinistriesDialog(stage, chip3)
        );
        selectedCard.setStyle("-fx-border-color: rgba(212,175,55,0.18);");

        VBox backCard = actionCard(
                "Back",
                "Return to Propose screen (send from there).",
                "/icons/back.png",
                () -> new ProposeScreen(user, userManager).show(stage)
        );
        backCard.getStyleClass().add("danger-card");

        grid.add(percentAllCard, 0, 0);
        grid.add(fixedAllCard,   1, 0);
        grid.add(selectedCard,   0, 1);
        grid.add(backCard,       1, 1);

        VBox leftContent = new VBox(14, heroCard, new Separator(), grid);
        leftContent.setFillWidth(true);
        leftContent.setMaxWidth(760);
        HBox.setHgrow(leftContent, Priority.ALWAYS);

        VBox sidePanel = buildSidePanel();
        sidePanel.setMinWidth(280);
        sidePanel.setPrefWidth(320);
        sidePanel.setMaxWidth(360);

        HBox mainRow = new HBox(18, leftContent, sidePanel);
        mainRow.setAlignment(Pos.TOP_CENTER);
        mainRow.setPadding(new Insets(18));

        Button footerBack = new Button("⟵ Back");
        footerBack.getStyleClass().addAll("button", "subtle");
        footerBack.setStyle("-fx-border-color: rgba(212,175,55,0.18);");
        footerBack.setOnAction(e -> new ProposeScreen(user, userManager).show(stage));

        HBox footer = new HBox(footerBack);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(12, 18, 12, 18));
        footer.getStyleClass().add("footer-bar");

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(mainRow);
        root.setBottom(footer);

        Scene scene = new Scene(root,
                stage.getWidth() > 0 ? stage.getWidth() : 1120,
                stage.getHeight() > 0 ? stage.getHeight() : 720
        );

        var css = getClass().getResource("/css/DarkTheme.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());

        applyScenePreserveWindow(stage, scene, "Bulk Draft Edit");

        FadeTransition ft = new FadeTransition(Duration.millis(200), root);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    /** Right-side info panel (same style as BulkEditScreen). */
    private VBox buildSidePanel() {
        Label t1 = new Label("How Draft Bulk Edit works");
        t1.getStyleClass().add("side-title");

        Label l1 = new Label("• Preview changes before applying");
        Label l2 = new Label("• Percentage affects each ministry proportionally");
        Label l3 = new Label("• Fixed amount adds/subtracts a constant value");
        Label l4 = new Label("• Selected Ministries targets only a subset");
        Label l5 = new Label("• Draft mode does NOT save changes to file (send from Propose screen)");

        for (Label l : new Label[]{ l1, l2, l3, l4, l5 }) {
            l.getStyleClass().add("side-text");
            l.setWrapText(true);
            l.setMaxWidth(Double.MAX_VALUE);
        }

        VBox card1 = new VBox(10, t1, l1, l2, l3, l4, l5);
        card1.getStyleClass().addAll("card", "side-card");
        card1.setStyle("-fx-border-color: rgba(212,175,55,0.12);");
        card1.setFillWidth(true);
        card1.setPadding(new Insets(14));

        Label t2 = new Label("Tips");
        t2.getStyleClass().add("side-title");

        Label t21 = new Label("• Use Percentage for overall policy change");
        Label t22 = new Label("• Use Fixed for targeted reallocation");
        Label t23 = new Label("• Use Selected to avoid unwanted global edits");

        for (Label l : new Label[]{ t21, t22, t23 }) {
            l.getStyleClass().add("side-text");
            l.setWrapText(true);
            l.setMaxWidth(Double.MAX_VALUE);
        }

        VBox card2 = new VBox(10, t2, t21, t22, t23);
        card2.getStyleClass().addAll("card", "side-card");
        card2.setStyle("-fx-border-color: rgba(212,175,55,0.10);");
        card2.setFillWidth(true);
        card2.setPadding(new Insets(14));

        VBox side = new VBox(14, card1, card2);
        side.setAlignment(Pos.TOP_LEFT);
        side.setFillWidth(true);
        side.setMaxWidth(Double.MAX_VALUE);
        return side;
    }

    /** Clickable action card */
    private VBox actionCard(String title, String desc, String iconPath, Runnable onClick) {
        Node iconNode = safeIcon(iconPath, 34);

        VBox iconBadge = new VBox(iconNode);
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
        card.setFocusTraversable(true);

        card.setOnMouseEntered(e -> { card.setScaleX(1.02); card.setScaleY(1.02); card.setTranslateY(-2); });
        card.setOnMouseExited(e -> { card.setScaleX(1.00); card.setScaleY(1.00); card.setTranslateY(0); });

        card.setOnMouseClicked(e -> onClick.run());
        card.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) onClick.run();
        });

        return card;
    }

    /** Loads icon safely. */
    private Node safeIcon(String iconPath, double size) {
        try {
            var stream = BulkEditDraftScreen.class.getResourceAsStream(iconPath);
            if (stream == null) throw new IllegalStateException("Missing icon: " + iconPath);
            ImageView icon = new ImageView(new Image(stream));
            icon.setFitWidth(size);
            icon.setFitHeight(size);
            icon.getStyleClass().add("action-icon");
            return icon;
        } catch (Exception ex) {
            Label fallback = new Label("⬤");
            fallback.getStyleClass().add("icon-fallback");
            return fallback;
        }
    }

    /* =========================================================
       DIALOGS (Draft)
       ========================================================= */

    /** Percentage Draft (All) - uses DraftEditSession.applyPercent so history keeps mode=percent. */
    private void showPercentageAllDialog(Stage parent, Label balanceChip) {
        Stage dialog = baseDialog(parent, "Percentage Draft (All)");

        VBox card = new VBox(14);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(18));
        card.setStyle("-fx-border-color: rgba(212,175,55,0.14);");

        Label title = new Label("Apply Percentage Draft to ALL Ministries");
        title.getStyleClass().add("title");

        Label hint = new Label("Enter a positive percentage and choose Increase/Decrease (e.g., 5).");
        hint.getStyleClass().add("subtitle");

        ToggleGroup typeGroup = new ToggleGroup();
        ToggleButton incBtn = new ToggleButton("Increase");
        ToggleButton decBtn = new ToggleButton("Decrease");
        incBtn.setToggleGroup(typeGroup);
        decBtn.setToggleGroup(typeGroup);
        incBtn.setSelected(true);
        incBtn.getStyleClass().add("role-toggle");
        decBtn.getStyleClass().add("role-toggle");

        HBox typeRow = new HBox(10, incBtn, decBtn);
        typeRow.setAlignment(Pos.CENTER_LEFT);

        TextField percentField = new TextField();
        percentField.setPromptText("Percentage (e.g., 5)");
        percentField.setTextFormatter(positiveNumberFormatter());

        Label balance = new Label("Available Draft Balance: "
                + Ministry.getFormattedBudget(DraftEditSession.getDraftBalance()));
        balance.getStyleClass().add("subtitle");

        Label info = new Label("Preview first, then Apply.");
        info.getStyleClass().add("subtitle");

        Label error = new Label();
        error.getStyleClass().add("error");
        error.setWrapText(true);

        TableView<PreviewRow> table = buildPreviewTable();
        table.setVisible(false);
        table.setManaged(false);

        Button previewBtn = new Button("Preview");
        previewBtn.getStyleClass().addAll("button", "subtle");
        previewBtn.setStyle("-fx-border-color: rgba(212,175,55,0.16);");

        Button applyBtn = new Button("Apply Draft");
        applyBtn.getStyleClass().addAll("button", "primary");
        applyBtn.setDisable(true);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().addAll("button", "subtle");
        cancelBtn.setOnAction(e -> dialog.close());

        previewBtn.setOnAction(e -> {
            error.setText("");

            String txt = percentField.getText().trim();
            if (!isValidPositiveNumber(txt)) { error.setText("Please enter a valid percentage."); return; }

            double pct = Double.parseDouble(txt);
            if (pct <= 0) { error.setText("Percentage must be positive."); return; }

            boolean isIncrease = (typeGroup.getSelectedToggle() == incBtn);
            double signed = isIncrease ? pct : -pct;
            if (signed <= -100) { error.setText("Cannot decrease by 100% or more."); return; }

            Ministry[] sandbox = DraftEditSession.getSandbox();
            ObservableList<PreviewRow> rows = FXCollections.observableArrayList();

            double totalPositive = 0;
            for (Ministry m : sandbox) {
                if (m == null) continue;

                double oldB = m.getBudget();
                double newB = oldB * (1 + signed / 100.0);

                if (newB < 0) { error.setText("Would create negative budget for: " + m.getMinistryName()); return; }

                double delta = newB - oldB;
                if (delta > 0) totalPositive += delta;

                rows.add(new PreviewRow(
                        m.getMinistryName(),
                        Ministry.getFormattedBudget(oldB),
                        Ministry.getFormattedBudget(newB),
                        formatDelta(delta)
                ));
            }

            double available = DraftEditSession.getDraftBalance();
            if (totalPositive > available + 1e-9) {
                error.setText("Insufficient draft balance for this increase.");
                return;
            }

            table.setItems(rows);
            table.setVisible(true);
            table.setManaged(true);
            applyBtn.setDisable(false);
            table.requestFocus();
        });

        applyBtn.setOnAction(e -> {
            error.setText("");

            String txt = percentField.getText().trim();
            if (!isValidPositiveNumber(txt)) { error.setText("Please enter a valid percentage."); return; }

            double pct = Double.parseDouble(txt);
            if (pct <= 0) { error.setText("Percentage must be positive."); return; }

            boolean isIncrease = (typeGroup.getSelectedToggle() == incBtn);
            double signed = isIncrease ? pct : -pct;
            if (signed <= -100) { error.setText("Cannot decrease by 100% or more."); return; }

            // Re-check affordability using current sandbox values
            Ministry[] sandbox = DraftEditSession.getSandbox();
            double totalPositive = 0;
            for (Ministry m : sandbox) {
                if (m == null) continue;
                double oldB = m.getBudget();
                double newB = oldB * (1 + signed / 100.0);
                if (newB < 0) { error.setText("Would create negative budget for: " + m.getMinistryName()); return; }
                double delta = newB - oldB;
                if (delta > 0) totalPositive += delta;
            }

            double available = DraftEditSession.getDraftBalance();
            if (totalPositive > available + 1e-9) {
                error.setText("Insufficient draft balance for this increase.");
                return;
            }

            double absPct = Math.abs(pct);
            for (Ministry m : sandbox) {
                if (m == null) continue;

                String err = DraftEditSession.applyPercent(m.getMinistryName(), isIncrease, absPct);
                if (err != null) { error.setText(err); return; }
            }

            balance.setText("Available Draft Balance: "
                    + Ministry.getFormattedBudget(DraftEditSession.getDraftBalance()));
            balanceChip.setText("Draft Balance: " + Ministry.getFormattedBudget(DraftEditSession.getDraftBalance()));
            dialog.close();
        });

        HBox actions = new HBox(10, previewBtn, applyBtn, cancelBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(title, hint, typeRow, percentField, balance, info, error, actions, table);

        Scene s = new Scene(card, 980, 620);
        var css = getClass().getResource("/css/DarkTheme.css");
        if (css != null) s.getStylesheets().add(css.toExternalForm());

        dialog.setScene(s);
        dialog.centerOnScreen();
        dialog.show();
    }

    /** Fixed Amount Draft (All) - uses DraftEditSession.applyFixed (mode=fixed). */
    private void showFixedAllDialog(Stage parent, Label balanceChip) {
        Stage dialog = baseDialog(parent, "Fixed Amount Draft (All)");

        VBox card = new VBox(14);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(18));
        card.setStyle("-fx-border-color: rgba(212,175,55,0.14);");

        Label title = new Label("Apply Fixed Amount Draft to ALL Ministries");
        title.getStyleClass().add("title");

        Label hint = new Label("Enter a positive amount and choose Increase/Decrease (e.g., 50000).");
        hint.getStyleClass().add("subtitle");

        ToggleGroup typeGroup = new ToggleGroup();
        ToggleButton incBtn = new ToggleButton("Increase");
        ToggleButton decBtn = new ToggleButton("Decrease");
        incBtn.setToggleGroup(typeGroup);
        decBtn.setToggleGroup(typeGroup);
        incBtn.setSelected(true);
        incBtn.getStyleClass().add("role-toggle");
        decBtn.getStyleClass().add("role-toggle");

        HBox typeRow = new HBox(10, incBtn, decBtn);
        typeRow.setAlignment(Pos.CENTER_LEFT);

        TextField amountField = new TextField();
        amountField.setPromptText("Amount (e.g., 50000)");
        amountField.setTextFormatter(positiveNumberFormatter());

        Label balance = new Label("Available Draft Balance: "
                + Ministry.getFormattedBudget(DraftEditSession.getDraftBalance()));
        balance.getStyleClass().add("subtitle");

        Label info = new Label("Preview first, then Apply.");
        info.getStyleClass().add("subtitle");

        Label error = new Label();
        error.getStyleClass().add("error");
        error.setWrapText(true);

        TableView<PreviewRow> table = buildPreviewTable();
        table.setVisible(false);
        table.setManaged(false);

        Button previewBtn = new Button("Preview");
        previewBtn.getStyleClass().addAll("button", "subtle");
        previewBtn.setStyle("-fx-border-color: rgba(212,175,55,0.16);");

        Button applyBtn = new Button("Apply Draft");
        applyBtn.getStyleClass().addAll("button", "primary");
        applyBtn.setDisable(true);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().addAll("button", "subtle");
        cancelBtn.setOnAction(e -> dialog.close());

        final int[] cachedCount = { 0 };

        previewBtn.setOnAction(e -> {
            error.setText("");

            String txt = amountField.getText().trim();
            if (!isValidPositiveNumber(txt)) { error.setText("Please enter a valid amount."); return; }

            double amount = Double.parseDouble(txt);
            if (amount <= 0) { error.setText("Amount must be positive."); return; }

            boolean isIncrease = (typeGroup.getSelectedToggle() == incBtn);
            double signed = isIncrease ? amount : -amount;

            Ministry[] sandbox = DraftEditSession.getSandbox();

            for (Ministry m : sandbox) {
                if (m == null) continue;
                if (m.getBudget() + signed < 0) {
                    error.setText("Would create negative budget for: " + m.getMinistryName());
                    return;
                }
            }

            int count = 0;
            for (Ministry m : sandbox) if (m != null) count++;
            cachedCount[0] = count;

            if (signed > 0) {
                double totalCost = signed * count;
                double available = DraftEditSession.getDraftBalance();
                if (totalCost > available + 1e-9) { error.setText("Insufficient draft balance."); return; }
            }

            ObservableList<PreviewRow> rows = FXCollections.observableArrayList();
            for (Ministry m : sandbox) {
                if (m == null) continue;
                double oldB = m.getBudget();
                double newB = oldB + signed;

                rows.add(new PreviewRow(
                        m.getMinistryName(),
                        Ministry.getFormattedBudget(oldB),
                        Ministry.getFormattedBudget(newB),
                        formatDelta(newB - oldB)
                ));
            }

            table.setItems(rows);
            table.setVisible(true);
            table.setManaged(true);
            applyBtn.setDisable(false);
            table.requestFocus();
        });

        applyBtn.setOnAction(e -> {
            error.setText("");

            String txt = amountField.getText().trim();
            if (!isValidPositiveNumber(txt)) { error.setText("Please enter a valid amount."); return; }

            double amount = Double.parseDouble(txt);
            if (amount <= 0) { error.setText("Amount must be positive."); return; }

            boolean isIncrease = (typeGroup.getSelectedToggle() == incBtn);
            double signed = isIncrease ? amount : -amount;

            Ministry[] sandbox = DraftEditSession.getSandbox();

            for (Ministry m : sandbox) {
                if (m == null) continue;
                if (m.getBudget() + signed < 0) {
                    error.setText("Would create negative budget for: " + m.getMinistryName());
                    return;
                }
            }

            if (signed > 0) {
                double totalCost = signed * cachedCount[0];
                double available = DraftEditSession.getDraftBalance();
                if (totalCost > available + 1e-9) { error.setText("Insufficient draft balance."); return; }
            }

            for (Ministry m : sandbox) {
                if (m == null) continue;
                if (Math.abs(signed) < 1e-9) continue;

                boolean inc = signed > 0;
                double abs = Math.abs(signed);

                String err = DraftEditSession.applyFixed(m.getMinistryName(), inc, abs);
                if (err != null) { error.setText(err); return; }
            }

            balance.setText("Available Draft Balance: "
                    + Ministry.getFormattedBudget(DraftEditSession.getDraftBalance()));
            balanceChip.setText("Draft Balance: " + Ministry.getFormattedBudget(DraftEditSession.getDraftBalance()));
            dialog.close();
        });

        HBox actions = new HBox(10, previewBtn, applyBtn, cancelBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(title, hint, typeRow, amountField, balance, info, error, actions, table);

        Scene s = new Scene(card, 980, 620);
        var css = getClass().getResource("/css/DarkTheme.css");
        if (css != null) s.getStylesheets().add(css.toExternalForm());

        dialog.setScene(s);
        dialog.centerOnScreen();
        dialog.show();
    }

    /** Selected Ministries Draft - uses applyPercent for percent mode, applyFixed for fixed mode. */
    private void showSelectedMinistriesDialog(Stage parent, Label balanceChip) {
        Stage dialog = baseDialog(parent, "Draft Selected Ministries");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(18));

        Label title = new Label("Draft Selected Ministries");
        title.getStyleClass().add("title");
        title.setStyle("-fx-effect: dropshadow(gaussian, rgba(212,175,55,0.16), 14, 0.22, 0, 0);");

        Label subtitle = new Label("Select ministries, choose mode, preview, then apply (draft).");
        subtitle.getStyleClass().add("subtitle");

        Label balance = new Label("Available Draft Balance: "
                + Ministry.getFormattedBudget(DraftEditSession.getDraftBalance()));
        balance.getStyleClass().add("subtitle");
        balance.setStyle("-fx-text-fill: rgba(212,175,55,0.85);");

        VBox header = new VBox(6, title, subtitle, balance);
        header.getStyleClass().addAll("card", "toolbar-card");
        header.setPadding(new Insets(14));
        header.setStyle("-fx-border-color: rgba(212,175,55,0.14);");
        root.setTop(header);

        TableView<MinistryPickRow> pickTable = new TableView<>();
        pickTable.getStyleClass().addAll("budget-table");
        pickTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        pickTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        pickTable.setPrefHeight(420);

        TableColumn<MinistryPickRow, String> cMin = new TableColumn<>("Ministry");
        cMin.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getMinistry()));

        TableColumn<MinistryPickRow, String> cCur = new TableColumn<>("Current Budget");
        cCur.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getCurrent()));
        cCur.setStyle("-fx-alignment: CENTER-RIGHT;");

        pickTable.getColumns().addAll(cMin, cCur);

        ObservableList<MinistryPickRow> pickRows = FXCollections.observableArrayList();
        Ministry[] sandbox = DraftEditSession.getSandbox();
        for (int i = 0; i < sandbox.length; i++) {
            Ministry m = sandbox[i];
            if (m == null) continue;
            pickRows.add(new MinistryPickRow(i, m.getMinistryName(), Ministry.getFormattedBudget(m.getBudget())));
        }
        pickTable.setItems(pickRows);

        Label pickHint = new Label("Tip: Ctrl / Shift for multi-select.");
        pickHint.getStyleClass().add("subtitle");
        pickHint.setStyle("-fx-opacity: 0.80;");

        VBox pickCard = new VBox(10, new Label("Pick ministries"), pickHint, pickTable);
        ((Label) pickCard.getChildren().get(0)).getStyleClass().add("section-title");
        pickCard.getStyleClass().addAll("card", "table-card");
        pickCard.setPadding(new Insets(14));
        pickCard.setStyle("-fx-border-color: rgba(212,175,55,0.10);");

        ToggleGroup modeGroup = new ToggleGroup();
        ToggleButton percentBtn = new ToggleButton("Percentage");
        ToggleButton fixedBtn = new ToggleButton("Fixed Amount");
        percentBtn.setToggleGroup(modeGroup);
        fixedBtn.setToggleGroup(modeGroup);
        percentBtn.setSelected(true);
        percentBtn.getStyleClass().addAll("role-toggle");
        fixedBtn.getStyleClass().addAll("role-toggle");

        HBox modeRow = new HBox(10, percentBtn, fixedBtn);
        modeRow.setAlignment(Pos.CENTER_LEFT);

        ToggleGroup typeGroup = new ToggleGroup();
        ToggleButton incBtn = new ToggleButton("Increase");
        ToggleButton decBtn = new ToggleButton("Decrease");
        incBtn.setToggleGroup(typeGroup);
        decBtn.setToggleGroup(typeGroup);
        incBtn.setSelected(true);
        incBtn.getStyleClass().addAll("role-toggle");
        decBtn.getStyleClass().addAll("role-toggle");

        HBox typeRow = new HBox(10, incBtn, decBtn);
        typeRow.setAlignment(Pos.CENTER_LEFT);

        TextField valueField = new TextField();
        valueField.setPromptText("Value (e.g. 5 or 100000)");
        valueField.setTextFormatter(positiveNumberFormatter());

        Label valueHint = new Label("Choose Increase/Decrease via toggle (value stays positive).");
        valueHint.getStyleClass().add("subtitle");
        valueHint.setStyle("-fx-opacity: 0.82;");

        Label error = new Label();
        error.getStyleClass().add("error");
        error.setWrapText(true);

        TableView<PreviewRow> previewTable = buildPreviewTable();
        previewTable.setPrefHeight(320);

        VBox previewCard = new VBox(10, new Label("Preview (selected)"), previewTable);
        ((Label) previewCard.getChildren().get(0)).getStyleClass().add("section-title");
        previewCard.getStyleClass().addAll("card", "table-card");
        previewCard.setPadding(new Insets(14));
        previewCard.setStyle("-fx-border-color: rgba(212,175,55,0.10);");
        VBox.setVgrow(previewTable, Priority.ALWAYS);

        Button previewBtn = new Button("Preview");
        previewBtn.getStyleClass().addAll("button", "subtle");
        previewBtn.setStyle("-fx-border-color: rgba(212,175,55,0.18);");

        Button applyBtn = new Button("Apply Draft");
        applyBtn.getStyleClass().addAll("button", "primary");
        applyBtn.setDisable(true);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().addAll("button", "subtle");
        cancelBtn.setOnAction(e -> dialog.close());

        final boolean[] previewOk = { false };

        Runnable doPreview = () -> {
            error.setText("");
            previewOk[0] = false;
            applyBtn.setDisable(true);

            var selected = pickTable.getSelectionModel().getSelectedItems();
            if (selected == null || selected.isEmpty()) {
                error.setText("Please select at least one ministry.");
                return;
            }

            String txt = valueField.getText().trim();
            if (!isValidPositiveNumber(txt)) { error.setText("Please enter a valid value."); return; }

            double value = Double.parseDouble(txt);
            if (value <= 0) { error.setText("Value must be positive."); return; }

            boolean isPercent = (modeGroup.getSelectedToggle() == percentBtn);
            boolean isIncrease = (typeGroup.getSelectedToggle() == incBtn);

            double signed = isIncrease ? value : -value;
            if (isPercent && signed <= -100) {
                error.setText("Cannot decrease by 100% or more.");
                return;
            }

            ObservableList<PreviewRow> rows = FXCollections.observableArrayList();
            double totalPositive = 0;

            for (MinistryPickRow r : selected) {
                Ministry m = DraftEditSession.getSandbox()[r.getIndex()];
                if (m == null) continue;

                double oldB = m.getBudget();
                double newB = isPercent ? oldB * (1 + signed / 100.0) : oldB + signed;

                if (newB < 0) {
                    error.setText("Would create negative budget for: " + m.getMinistryName());
                    return;
                }

                double delta = newB - oldB;
                if (delta > 0) totalPositive += delta;

                rows.add(new PreviewRow(
                        m.getMinistryName(),
                        Ministry.getFormattedBudget(oldB),
                        Ministry.getFormattedBudget(newB),
                        formatDelta(delta)
                ));
            }

            double available = DraftEditSession.getDraftBalance();
            if (totalPositive > available + 1e-9) {
                error.setText("Insufficient draft balance for this increase.");
                return;
            }

            previewTable.setItems(rows);
            previewOk[0] = true;
            applyBtn.setDisable(false);
        };

        previewBtn.setOnAction(e -> doPreview.run());

        applyBtn.setOnAction(e -> {
            error.setText("");
            if (!previewOk[0]) { error.setText("Please preview first."); return; }

            var selected = pickTable.getSelectionModel().getSelectedItems();
            if (selected == null || selected.isEmpty()) {
                error.setText("Please select at least one ministry.");
                return;
            }

            String txt = valueField.getText().trim();
            if (!isValidPositiveNumber(txt)) { error.setText("Please enter a valid value."); return; }

            double value = Double.parseDouble(txt);
            if (value <= 0) { error.setText("Value must be positive."); return; }

            boolean isPercent = (modeGroup.getSelectedToggle() == percentBtn);
            boolean isIncrease = (typeGroup.getSelectedToggle() == incBtn);

            double signed = isIncrease ? value : -value;
            if (isPercent && signed <= -100) { error.setText("Cannot decrease by 100% or more."); return; }

            // Safety re-check against current draft balance
            double totalPositive = 0;
            for (MinistryPickRow r : selected) {
                Ministry m = DraftEditSession.getSandbox()[r.getIndex()];
                if (m == null) continue;

                double oldB = m.getBudget();
                double newB = isPercent ? oldB * (1 + signed / 100.0) : oldB + signed;

                if (newB < 0) { error.setText("Would create negative budget for: " + m.getMinistryName()); return; }

                double delta = newB - oldB;
                if (delta > 0) totalPositive += delta;
            }

            double available = DraftEditSession.getDraftBalance();
            if (totalPositive > available + 1e-9) { error.setText("Insufficient draft balance for this increase."); return; }

            for (MinistryPickRow r : selected) {
                Ministry m = DraftEditSession.getSandbox()[r.getIndex()];
                if (m == null) continue;

                if (isPercent) {
                    String err = DraftEditSession.applyPercent(m.getMinistryName(), isIncrease, value);
                    if (err != null) { error.setText(err); return; }
                } else {
                    String err = DraftEditSession.applyFixed(m.getMinistryName(), isIncrease, value);
                    if (err != null) { error.setText(err); return; }
                }
            }

            balance.setText("Available Draft Balance: "
                    + Ministry.getFormattedBudget(DraftEditSession.getDraftBalance()));
            balanceChip.setText("Draft Balance: " + Ministry.getFormattedBudget(DraftEditSession.getDraftBalance()));
            dialog.close();
        });

        HBox actions = new HBox(10, previewBtn, applyBtn, cancelBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox controlsCard = new VBox(
                12,
                new Label("Controls"),
                new Separator(),
                new Label("Mode"), modeRow,
                new Label("Type"), typeRow,
                new Label("Value"), valueField,
                valueHint,
                error,
                actions
        );
        ((Label) controlsCard.getChildren().get(0)).getStyleClass().add("section-title");
        controlsCard.getStyleClass().addAll("card", "toolbar-card");
        controlsCard.setPadding(new Insets(14));
        controlsCard.setStyle("-fx-border-color: rgba(212,175,55,0.12);");

        for (Node n : controlsCard.getChildren()) {
            if (n instanceof Label l && !l.getStyleClass().contains("title")
                    && !l.getStyleClass().contains("section-title")) {
                l.getStyleClass().add("subtitle");
            }
        }

        VBox right = new VBox(14, controlsCard, previewCard);
        VBox.setVgrow(previewCard, Priority.ALWAYS);

        HBox center = new HBox(16, pickCard, right);
        HBox.setHgrow(pickCard, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);

        root.setCenter(center);

        Scene s = new Scene(root, 1180, 760);
        var css = getClass().getResource("/css/DarkTheme.css");
        if (css != null) s.getStylesheets().add(css.toExternalForm());

        dialog.setScene(s);
        dialog.centerOnScreen();

        FadeTransition ft = new FadeTransition(Duration.millis(180), root);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        dialog.show();
    }

    /* =========================================================
       TABLE HELPERS
       ========================================================= */

    private TableView<PreviewRow> buildPreviewTable() {
        TableView<PreviewRow> table = new TableView<>();
        table.getStyleClass().addAll("table-view", "budget-table");
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(46);
        table.setPrefHeight(380);

        TableColumn<PreviewRow, String> colMin = new TableColumn<>("Ministry");
        colMin.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getMinistry()));
        colMin.setPrefWidth(420);

        TableColumn<PreviewRow, String> colPrev = new TableColumn<>("Previous");
        colPrev.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getPrevious()));
        colPrev.setPrefWidth(170);

        TableColumn<PreviewRow, String> colNew = new TableColumn<>("New");
        colNew.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getNow()));
        colNew.setPrefWidth(170);

        TableColumn<PreviewRow, String> colDelta = new TableColumn<>("Change");
        colDelta.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getDelta()));
        colDelta.setPrefWidth(140);

        colDelta.setStyle("-fx-alignment: CENTER-RIGHT;");
        colPrev.setStyle("-fx-alignment: CENTER-RIGHT;");
        colNew.setStyle("-fx-alignment: CENTER-RIGHT;");

        table.getColumns().addAll(colMin, colPrev, colNew, colDelta);
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

    /* =========================================================
       DIALOG / THEME HELPERS
       ========================================================= */

    private Stage baseDialog(Stage parent, String title) {
        Stage d = new Stage();
        d.initOwner(parent);
        d.initModality(Modality.WINDOW_MODAL);
        d.setTitle(title);
        return d;
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
