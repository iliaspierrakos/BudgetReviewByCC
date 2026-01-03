package guiFolder;

import UserFeatures.*;
import UserManagement.User;
import UserManagement.UserManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class BulkEditScreen {

    private final User user;
    private final UserManager userManager;

    public BulkEditScreen(User user, UserManager userManager) {
        this.user = user;
        this.userManager = userManager;
    }

    // ===== Row model για preview =====
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

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(24));

        VBox card = new VBox(18);
        card.getStyleClass().add("card");
        card.setMaxWidth(520);

        Label title = new Label("Bulk Edit");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Apply changes to multiple ministries at once");
        subtitle.getStyleClass().add("subtitle");

        Button percentAllBtn = new Button("Percentage Change (All)");
        percentAllBtn.getStyleClass().addAll("button", "primary");
        percentAllBtn.setMaxWidth(Double.MAX_VALUE);
        percentAllBtn.setOnAction(e -> showPercentageAllDialog(stage));

        Button fixedAllBtn = new Button("Fixed Amount Change (All)");
        fixedAllBtn.getStyleClass().addAll("button", "subtle");
        fixedAllBtn.setMaxWidth(Double.MAX_VALUE);
        fixedAllBtn.setOnAction(e -> showFixedAllDialog(stage));

        Button selectedBtn = new Button("Change Selected Ministries");
        selectedBtn.getStyleClass().addAll("button", "subtle");
        selectedBtn.setMaxWidth(Double.MAX_VALUE);
        selectedBtn.setOnAction(e -> showSelectedMinistriesDialog());

        Separator sep = new Separator();

        Button backBtn = new Button("Back");
        backBtn.getStyleClass().addAll("button", "subtle");
        backBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.setOnAction(e -> {
            if (user.getRole() == User.Role.CITIZEN) new VirtualEditScreen(user, userManager).show(stage);
            else new EditBudgetScreen(user, userManager).show(stage);
        });

        VBox buttons = new VBox(10, percentAllBtn, fixedAllBtn, selectedBtn, sep, backBtn);

        card.getChildren().addAll(title, subtitle, buttons);
        root.setCenter(card);
        BorderPane.setAlignment(card, Pos.CENTER);

        Scene scene = new Scene(root, 650, 430);
        var css = getClass().getResource("/css/DarkTheme.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Bulk Edit");
        stage.show();
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

        applyBtn.setOnAction(e -> {
            error.setText("");
            String txt = percentField.getText().trim();
            double pct;

            try {
                pct = Double.parseDouble(txt);
            } catch (Exception ex) {
                error.setText("Invalid percentage.");
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

        applyBtn.setOnAction(e -> {
            error.setText("");
            String txt = amountField.getText().trim();
            double amount;

            try { amount = Double.parseDouble(txt); }
            catch (Exception ex) { error.setText("Invalid amount."); return; }

            for (Ministry m : CreatingMinistries.ministries2026) {
                if (m == null) continue;

                double oldBudget = m.getBudget();
                double newBudget = oldBudget + amount;
                if (newBudget < 0) {
                    error.setText("Negative budget for: " + m.getMinistryName());
                    return;
                }
                m.setBudget(newBudget);

                
                EditHistory.historyOfEdit(m.getMinistryName(), oldBudget, newBudget, 0);

                Edit editObj = new Edit(
                        m.getMinistryName(),
                        amount >= 0 ? "Increase" : "Decrease",
                        Math.abs(amount),
                        "Fixed"
                );
                Edit.history.addEdit(editObj);
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

        TableColumn<PreviewRow, String> colDelta = new TableColumn<>(" Change");
        colDelta.setCellValueFactory(new PropertyValueFactory<>("delta"));
        colDelta.setPrefWidth(140);

        // χρώμα στο delta
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

        // row highlight
        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(PreviewRow row, boolean empty) {
                super.updateItem(row, empty);
                if (row == null || empty) { setStyle(""); return; }
                String d = row.getDelta();
                if (d.startsWith("+")) setStyle("-fx-background-color: rgba(46,204,113,0.12);");
                else if (d.startsWith("-")) setStyle("-fx-background-color: rgba(255,107,107,0.12);");
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
