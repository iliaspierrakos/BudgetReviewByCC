// DraftHistoryScreen.java
package guiFolder;

import UserFeatures.Edit;
import UserFeatures.Ministry;
import UserManagement.User;
import UserManagement.UserManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class DraftHistoryScreen {

    private final User user;
    private final UserManager userManager;

    public DraftHistoryScreen(User user, UserManager userManager) {
        this.user = user;
        this.userManager = userManager;
    }

    private static class Row {
        final DraftEditSession.DraftEdit e;
        Row(DraftEditSession.DraftEdit e) { this.e = e; }
        public String getMinistry() { return e.ministry; }
        public String getType() { return e.changeType; }
        public String getAmount() { return Ministry.getFormattedBudget(e.amount); }
        public String getMode() { return e.mode; }
        public String getWhen() { return e.at.toString(); }
    }

    public void show(Stage stage) {
        if (!DraftEditSession.isInitialized()) DraftEditSession.resetFromCurrent(Edit.balance);

        Label title = new Label("Draft History");
        title.getStyleClass().add("title");

        Label subtitle = new Label("These are DRAFT edits only (not official). You can undo.");
        subtitle.getStyleClass().add("subtitle");

        Label balance = new Label("Draft Balance: " + Ministry.getFormattedBudget(DraftEditSession.getDraftBalance()));
        balance.getStyleClass().add("chip");

        TableView<Row> table = new TableView<>();
        table.getStyleClass().addAll("table-view", "budget-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Row, String> c1 = new TableColumn<>("Ministry");
        c1.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getMinistry()));

        TableColumn<Row, String> c2 = new TableColumn<>("Type");
        c2.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getType()));

        TableColumn<Row, String> c3 = new TableColumn<>("Amount");
        c3.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getAmount()));
        c3.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<Row, String> c4 = new TableColumn<>("Mode");
        c4.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getMode()));

        TableColumn<Row, String> c5 = new TableColumn<>("When");
        c5.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getWhen()));

        table.getColumns().addAll(c1, c2, c3, c4, c5);

        ObservableList<Row> rows = FXCollections.observableArrayList();
        for (var e : DraftEditSession.getHistory()) rows.add(new Row(e));
        table.setItems(rows);

        Button back = new Button("Back");
        back.getStyleClass().addAll("button", "subtle");
        // Keep navigation inside the new Minister Draft UI
        back.setOnAction(e -> new MinisterDraftEditScreen(user, userManager).show(stage));

        Button undoLast = new Button("Undo Last");
        undoLast.getStyleClass().addAll("button", "subtle");
        undoLast.setOnAction(e -> {
            String err = DraftEditSession.undoLast(Edit.balance);
            if (err != null) warn(stage, "Undo failed", err);

            balance.setText("Draft Balance: " + Ministry.getFormattedBudget(DraftEditSession.getDraftBalance()));
            refresh(table);
        });

        Button clear = new Button("Clear Draft");
        clear.getStyleClass().addAll("button", "subtle");
        clear.setOnAction(e -> {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Clear all draft edits?", ButtonType.OK, ButtonType.CANCEL);
            applyTheme(a);
            a.showAndWait().ifPresent(btn -> {
                if (btn != ButtonType.OK) return;
                DraftEditSession.resetFromCurrent(Edit.balance);
                balance.setText("Draft Balance: " + Ministry.getFormattedBudget(DraftEditSession.getDraftBalance()));
                refresh(table);
            });
        });

        HBox actions = new HBox(10, back, undoLast, clear);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox header = new VBox(6, title, subtitle, balance);
        header.getStyleClass().addAll("card", "toolbar-card", "hero-card", "virtual-hero");
        header.setPadding(new Insets(14));

        VBox content = new VBox(14, header, table, actions);
        content.setPadding(new Insets(18));
        content.getStyleClass().add("virtual-content");

        BorderPane root = new BorderPane();
        root.getStyleClass().add("virtual-edit-root");
        root.setCenter(content);

        Scene scene = new Scene(root,
                stage.getWidth() > 0 ? stage.getWidth() : 1180,
                stage.getHeight() > 0 ? stage.getHeight() : 720
        );
        var css = getClass().getResource("/css/DarkTheme.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Draft History");
        stage.show();
    }

    private void refresh(TableView<Row> table) {
        ObservableList<Row> rows = FXCollections.observableArrayList();
        for (var e : DraftEditSession.getHistory()) rows.add(new Row(e));
        table.setItems(rows);
    }

    private void warn(Stage owner, String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.initOwner(owner);
        a.setTitle(title);
        a.setHeaderText(title);
        a.setContentText(msg);
        applyTheme(a);
        a.showAndWait();
    }

    private void applyTheme(Alert a) {
        var css = getClass().getResource("/css/DarkTheme.css");
        if (css != null) a.getDialogPane().getStylesheets().add(css.toExternalForm());
    }
}
