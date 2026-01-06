package guiFolder;

import UserFeatures.CreatingMinistries;
import UserFeatures.Edit;
import UserFeatures.EditHistory;
import UserFeatures.Ministry;
import UserFeatures.UserBudgetPersistence;
import UserManagement.User;
import UserManagement.UserManager;
import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class BulkEditScreen {

    private final User user;
    private final UserManager userManager;

    public BulkEditScreen(User user, UserManager userManager) {
        this.user = user;
        this.userManager = userManager;
    }

    // ===== Row model preview =====
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

    public void show(Stage stage) {

        // TOP BAR
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
        Label title = new Label("Bulk Edit");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Apply changes to multiple ministries at once.");
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
        heroCard.setMaxWidth(Double.MAX_VALUE);

        // ACTION GRID
        GridPane grid = new GridPane();
        grid.getStyleClass().add("action-grid");
        grid.setHgap(18);
        grid.setVgap(18);
        grid.setAlignment(Pos.TOP_CENTER);

        grid.getColumnConstraints().clear();
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(50);
        c1.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c1);

        VBox percentAllCard = actionCard(
                "Percentage Change (All)",
                "Apply a percentage (+/-) to all ministries with preview.",
                "/icons/wand.png",
                () -> showPercentageAllDialog(stage)
        );

        VBox fixedAllCard = actionCard(
                "Fixed Amount Change (All)",
                "Apply a fixed amount (+/-) to all ministries with preview.",
                "/icons/receipt.png",
                () -> showFixedAllDialog(stage)
        );

        VBox selectedCard = actionCard(
                "Change Selected Ministries",
                "Pick specific ministries and apply changes (coming soon).",
                "/icons/inbox.png",
                this::showSelectedMinistriesDialog
        );

        VBox backCard = actionCard(
                "Back",
                "Return to previous screen.",
                "/icons/compare.png",
                () -> {
                    if (user.getRole() == User.Role.CITIZEN) new VirtualEditScreen(user, userManager).show(stage);
                    else new EditBudgetScreen(user, userManager).show(stage);
                }
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
        sidePanel.setMaxWidth(280);

        HBox mainRow = new HBox(18, leftContent, sidePanel);
        mainRow.setAlignment(Pos.TOP_CENTER);
        mainRow.setPadding(new Insets(18));

        Button footerBack = new Button("⟵ Back");
        footerBack.getStyleClass().addAll("button", "subtle");
        footerBack.setOnAction(e -> {
            if (user.getRole() == User.Role.CITIZEN) new VirtualEditScreen(user, userManager).show(stage);
            else new EditBudgetScreen(user, userManager).show(stage);
        });

        HBox footer = new HBox(footerBack);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(12, 18, 12, 18));
        footer.getStyleClass().add("footer-bar");

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(mainRow);
        root.setBottom(footer);

        Scene scene = new Scene(root, 1120, 720);
        var css = getClass().getResource("/css/DarkTheme.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Bulk Edit");
        stage.show();

        FadeTransition ft = new FadeTransition(Duration.millis(200), root);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    private VBox buildSidePanel() {
        Label t1 = new Label("How Bulk Edit works");
        t1.getStyleClass().add("side-title");

        Label l1 = new Label("• Preview changes before applying");
        Label l2 = new Label("• Percentage affects each ministry proportionally");
        Label l3 = new Label("• Fixed amount adds/subtracts a constant value");

        l1.getStyleClass().add("side-text");
        l2.getStyleClass().add("side-text");
        l3.getStyleClass().add("side-text");

        VBox card1 = new VBox(10, t1, l1, l2, l3);
        card1.getStyleClass().addAll("card", "side-card");

        Label t2 = new Label("Tips");
        t2.getStyleClass().add("side-title");

        Label t21 = new Label("• Use Percentage for overall policy change");
        Label t22 = new Label("• Use Fixed for targeted reallocation");
        t21.getStyleClass().add("side-text");
        t22.getStyleClass().add("side-text");

        VBox card2 = new VBox(10, t2, t21, t22);
        card2.getStyleClass().addAll("card", "side-card");

        VBox side = new VBox(14, card1, card2);
        side.setAlignment(Pos.TOP_LEFT);
        return side;
    }

    private VBox actionCard(String title, String desc, String iconPath, Runnable onClick) {
        ImageView icon = new ImageView(new Image(
                BulkEditScreen.class.getResourceAsStream(iconPath)
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

    // ================== DIALOGS ==================

    private void showPercentageAllDialog(Stage parent) {

        Stage dialog = baseDialog(parent, "Percentage Change (All)");

        VBox card = new VBox(14);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(18));

        Label title = new Label("Apply Percentage Change to ALL Ministries");
        title.getStyleClass().add("title");

        Label hint = new Label("Example: 5 or -10");
        hint.getStyleClass().add("subtitle");

        TextField percentField = new TextField();
        percentField.setPromptText("Percentage");

        Label balance = new Label("Available Balance: " + Ministry.getFormattedBudget(Edit.balance));
        balance.getStyleClass().add("subtitle");

        Label info = new Label("Preview first, then Apply.");
        info.getStyleClass().add("subtitle");

        Label error = new Label();
        error.getStyleClass().add("error");

        TableView<PreviewRow> table = buildPreviewTable();
        table.setVisible(false);
        table.setManaged(false);

        Button previewBtn = new Button("Preview");
        previewBtn.getStyleClass().addAll("button", "subtle");

        Button applyBtn = new Button("Apply");
        applyBtn.getStyleClass().addAll("button", "primary");
        applyBtn.setDisable(true);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().addAll("button", "subtle");
        cancelBtn.setOnAction(e -> dialog.close());

        previewBtn.setOnAction(e -> {
            error.setText("");
            String txt = percentField.getText().trim();
            if (txt.isEmpty()) { error.setText("Please enter a percentage."); return; }

            double pct;
            try {
                pct = Double.parseDouble(txt);
                if (pct <= -100) { error.setText("Cannot decrease by 100% or more."); return; }
            } catch (NumberFormatException ex) {
                error.setText("Invalid percentage.");
                return;
            }

            ObservableList<PreviewRow> rows = FXCollections.observableArrayList();
            for (Ministry m : CreatingMinistries.ministries2026) {
                if (m == null) continue;
                double oldB = m.getBudget();
                double newB = oldB * (1 + pct / 100.0);
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
        });

        //  APPLY (percentage) + balance + save
        applyBtn.setOnAction(e -> {
            error.setText("");
            String txt = percentField.getText().trim();

            double pct;
            try {
                pct = Double.parseDouble(txt);
                if (pct <= -100) { error.setText("Cannot decrease by 100% or more."); return; }
            } catch (Exception ex) {
                error.setText("Invalid percentage.");
                return;
            }

            // total impact on balance
            double totalChange = 0;
            for (Ministry m : CreatingMinistries.ministries2026) {
                if (m == null) continue;
                double oldB = m.getBudget();
                double newB = oldB * (1 + pct / 100.0);
                totalChange += (newB - oldB);
            }

            if (totalChange > 0 && totalChange > Edit.balance) {
                error.setText("Insufficient balance.");
                return;
            }

            for (Ministry m : CreatingMinistries.ministries2026) {
                if (m == null) continue;

                double oldBudget = m.getBudget();
                double newBudget = oldBudget * (1 + pct / 100.0);
                m.setBudget(newBudget);

                EditHistory.historyOfEdit(m.getMinistryName(), oldBudget, newBudget, 0);

                Edit editObj = new Edit(
                    m.getMinistryName(),
                    pct >= 0 ? "Increase" : "Decrease",
                    Math.abs(pct),
                    "percentage"
                );
                Edit.history.addEdit(editObj);
            }

            // update balance: spend if positive, refund if negative
            Edit.balance -= totalChange;

            // persist
            try {
                UserBudgetFileUtil.saveUserBudget(user, 2026);
            } catch (Exception ex) {
                System.err.println("Failed to save budgets: " + ex.getMessage());
            }

            dialog.close();
            new EditHistoryScreen(user, userManager).show(parent);
        });

        HBox actions = new HBox(10, previewBtn, applyBtn, cancelBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(title, hint, percentField, balance, info, error, actions, table);

        Scene s = new Scene(card, 980, 620);
        var css = getClass().getResource("/css/DarkTheme.css");
        if (css != null) s.getStylesheets().add(css.toExternalForm());

        dialog.setScene(s);
        dialog.show();
    }

    private void showFixedAllDialog(Stage parent) {

        Stage dialog = baseDialog(parent, "Fixed Amount Change (All)");

        VBox card = new VBox(14);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(18));

        Label title = new Label("Apply Fixed Amount to ALL Ministries");
        title.getStyleClass().add("title");

        Label hint = new Label("Example: 100000 or -50000");
        hint.getStyleClass().add("subtitle");

        TextField amountField = new TextField();
        amountField.setPromptText("Amount");

        Label balance = new Label("Available Balance: " + Ministry.getFormattedBudget(Edit.balance));
        balance.getStyleClass().add("subtitle");

        Label info = new Label("Preview first, then Apply.");
        info.getStyleClass().add("subtitle");

        Label error = new Label();
        error.getStyleClass().add("error");

        TableView<PreviewRow> table = buildPreviewTable();
        table.setVisible(false);
        table.setManaged(false);

        Button previewBtn = new Button("Preview");
        previewBtn.getStyleClass().addAll("button", "subtle");

        Button applyBtn = new Button("Apply");
        applyBtn.getStyleClass().addAll("button", "primary");
        applyBtn.setDisable(true);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().addAll("button", "subtle");
        cancelBtn.setOnAction(e -> dialog.close());

        previewBtn.setOnAction(e -> {
            error.setText("");
            String txt = amountField.getText().trim();
            if (txt.isEmpty()) { error.setText("Please enter an amount."); return; }

            double amount;
            try { amount = Double.parseDouble(txt); }
            catch (NumberFormatException ex) { error.setText("Invalid amount."); return; }

            ObservableList<PreviewRow> rows = FXCollections.observableArrayList();

            for (Ministry m : CreatingMinistries.ministries2026) {
                if (m == null) continue;
                double oldB = m.getBudget();
                double newB = oldB + amount;

                if (newB < 0) {
                    error.setText("Would create negative budget for: " + m.getMinistryName());
                    return;
                }

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
        });

        //  APPLY (fixed amount) + balance + save
        applyBtn.setOnAction(e -> {
            error.setText("");
            String txt = amountField.getText().trim();

            double amount;
            try {
                amount = Double.parseDouble(txt);
            } catch (Exception ex) {
                error.setText("Invalid amount.");
                return;
            }

            // prevent negative budgets
            for (Ministry m : CreatingMinistries.ministries2026) {
                if (m == null) continue;
                if (m.getBudget() + amount < 0) {
                    error.setText("Would create negative budget for: " + m.getMinistryName());
                    return;
                }
            }

            // total impact on balance
            int count = 0;
            for (Ministry m : CreatingMinistries.ministries2026) if (m != null) count++;
            double totalChange = amount * count;

            if (totalChange > 0 && totalChange > Edit.balance) {
                error.setText("Insufficient balance.");
                return;
            }

            // Count ministries for total change
            int ministryCount = 0;
            for (Ministry m : CreatingMinistries.ministries2026) {
                if (m != null) ministryCount++;
            }
            double totalChange = amount * ministryCount;

            // Update balance BEFORE applying changes
            if (amount >= 0) {
                Edit.balance -= totalChange;
            } else {
                Edit.balance += Math.abs(totalChange);
            }

            // Apply changes to ministries
            for (Ministry m : CreatingMinistries.ministries2026) {
                if (m == null) continue;

                double oldBudget = m.getBudget();
                double newBudget = oldBudget + amount;
                m.setBudget(newBudget);

                EditHistory.historyOfEdit(m.getMinistryName(), oldBudget, newBudget, 0);

                Edit editObj = new Edit(
                        m.getMinistryName(),
                        amount >= 0 ? "Increase" : "Decrease",
                        Math.abs(amount),
                        "fixed"
                );
                Edit.history.addEdit(editObj);
            }

            Edit.balance -= totalChange;

            try {
                UserBudgetFileUtil.saveUserBudget(user, 2026);
            } catch (Exception ex) {
                System.err.println("Failed to save budgets: " + ex.getMessage());
            }

            dialog.close();
            new EditHistoryScreen(user, userManager).show(parent);
        });

        HBox actions = new HBox(10, previewBtn, applyBtn, cancelBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(title, hint, amountField, balance, info, error, actions, table);

        Scene s = new Scene(card, 980, 620);
        var css = getClass().getResource("/css/DarkTheme.css");
        if (css != null) s.getStylesheets().add(css.toExternalForm());

        dialog.setScene(s);
        dialog.show();
    }

    private void showSelectedMinistriesDialog() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Coming soon");
        a.setHeaderText("Selected ministries");
        a.setContentText("Will be implemented next.");
        a.showAndWait();
    }

    // ================== HELPERS ==================

    private Stage baseDialog(Stage parent, String title) {
        Stage d = new Stage();
        d.initOwner(parent);
        d.initModality(Modality.WINDOW_MODAL);
        d.setTitle(title);
        return d;
    }

    private TableView<PreviewRow> buildPreviewTable() {
        TableView<PreviewRow> table = new TableView<>();
        table.getStyleClass().addAll("table-view", "budget-table");
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(46);
        table.setPrefHeight(380);

        TableColumn<PreviewRow, String> colMin = new TableColumn<>("Ministry");
        colMin.setCellValueFactory(new PropertyValueFactory<>("ministry"));
        colMin.setPrefWidth(420);

        TableColumn<PreviewRow, String> colPrev = new TableColumn<>("Previous");
        colPrev.setCellValueFactory(new PropertyValueFactory<>("previous"));
        colPrev.setPrefWidth(170);

        TableColumn<PreviewRow, String> colNew = new TableColumn<>("New");
        colNew.setCellValueFactory(new PropertyValueFactory<>("now"));
        colNew.setPrefWidth(170);

        TableColumn<PreviewRow, String> colDelta = new TableColumn<>("Change");
        colDelta.setCellValueFactory(new PropertyValueFactory<>("delta"));
        colDelta.setPrefWidth(140);

        colDelta.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                if (item.startsWith("+")) setStyle("-fx-text-fill: #2ecc71; -fx-alignment: CENTER-RIGHT;");
                else if (item.startsWith("-")) setStyle("-fx-text-fill: #ff6b6b; -fx-alignment: CENTER-RIGHT;");
                else setStyle("-fx-text-fill: #e7eaf0; -fx-alignment: CENTER-RIGHT;");
            }
        });

        colPrev.setStyle("-fx-alignment: CENTER-RIGHT;");
        colNew.setStyle("-fx-alignment: CENTER-RIGHT;");
        colDelta.setStyle("-fx-alignment: CENTER-RIGHT;");

        table.getColumns().addAll(colMin, colPrev, colNew, colDelta);

        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(PreviewRow row, boolean empty) {
                super.updateItem(row, empty);
                if (row == null || empty) { setStyle(""); return; }
                String d = row.getDelta();
                if (d != null && d.startsWith("+")) setStyle("-fx-background-color: rgba(46,204,113,0.12);");
                else if (d != null && d.startsWith("-")) setStyle("-fx-background-color: rgba(255,107,107,0.12);");
                else setStyle("");
            }
        });

        return table;
    }

    private String formatDelta(double delta) {
        String s = Ministry.getFormattedBudget(Math.abs(delta));
        if (delta > 0) return "+" + s;
        if (delta < 0) return "-" + s;
        return "0";
    }
}