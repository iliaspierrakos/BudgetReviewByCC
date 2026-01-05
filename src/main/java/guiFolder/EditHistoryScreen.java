package guiFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import UserFeatures.Edit;
import UserFeatures.Ministry;
import UserFeatures.UserBudgetFileUtil;
import UserManagement.User;
import UserManagement.UserManager;

import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class EditHistoryScreen {

    private static final Path HISTORY_PATH =
            Path.of("src/main/resources/NecessaryFilesAndData/edithistory.txt");

    private final User user;
    private final UserManager userManager;

    public EditHistoryScreen(User user, UserManager userManager) {
        this.user = user;
        this.userManager = userManager;
    }

    public static class HistoryRow {
        private final SimpleStringProperty ministry = new SimpleStringProperty("");
        private final SimpleStringProperty previous = new SimpleStringProperty("");
        private final SimpleStringProperty now = new SimpleStringProperty("");
        private final SimpleStringProperty delta = new SimpleStringProperty("");

        public HistoryRow(String ministry, String previous, String now, String delta) {
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

        // TOP APP BAR
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
        Label title = new Label("Edit History");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Review changes and undo recent operations.");
        subtitle.getStyleClass().add("subtitle");

        Label chip1 = new Label("Audit Trail");
        chip1.getStyleClass().add("chip");
        Label chip2 = new Label("Undo supported");
        chip2.getStyleClass().add("chip");
        Label chip3 = new Label("Role: " + user.getRole().name());
        chip3.getStyleClass().add("chip");

        HBox chips = new HBox(10, chip1, chip2, chip3);
        chips.setAlignment(Pos.CENTER_LEFT);

        VBox heroCard = new VBox(10, title, subtitle, chips);
        heroCard.getStyleClass().addAll("card", "toolbar-card", "hero-card");

        // TABLE
        TableView<HistoryRow> table = new TableView<>();
        table.getStyleClass().add("budget-table");
        table.setPlaceholder(new Label("No changes yet."));
        table.setFixedCellSize(46);
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        TableColumn<HistoryRow, String> colMin = new TableColumn<>("Ministry");
        colMin.setCellValueFactory(new PropertyValueFactory<>("ministry"));
        colMin.setPrefWidth(520);
        colMin.setMinWidth(320);

        TableColumn<HistoryRow, String> colPrev = new TableColumn<>("Previous");
        colPrev.setCellValueFactory(new PropertyValueFactory<>("previous"));
        colPrev.setPrefWidth(210);
        colPrev.setMinWidth(170);

        TableColumn<HistoryRow, String> colNew = new TableColumn<>("New");
        colNew.setCellValueFactory(new PropertyValueFactory<>("now"));
        colNew.setPrefWidth(210);
        colNew.setMinWidth(170);

        TableColumn<HistoryRow, String> colDelta = new TableColumn<>("Change");
        colDelta.setCellValueFactory(new PropertyValueFactory<>("delta"));
        colDelta.setPrefWidth(150);
        colDelta.setMinWidth(120);

        colPrev.setStyle("-fx-alignment: CENTER-RIGHT;");
        colNew.setStyle("-fx-alignment: CENTER-RIGHT;");
        colDelta.setStyle("-fx-alignment: CENTER-RIGHT;");

        table.getColumns().addAll(colMin, colPrev, colNew, colDelta);
        table.setItems(FXCollections.observableArrayList(parseHistoryFile()));

        VBox tableCard = new VBox(10, new Label("Recent changes"), table);
        tableCard.getStyleClass().addAll("card", "table-card");
        VBox.setVgrow(table, Priority.ALWAYS);

        // UNDO
        int maxUndo = Math.max(0, Edit.history.getIndex() + 1);
        Spinner<Integer> undoSpinner = new Spinner<>(0, maxUndo, 0);
        undoSpinner.setEditable(true);
        undoSpinner.setPrefWidth(110);

        Button undoBtn = new Button("Undo");
        undoBtn.getStyleClass().addAll("button", "primary");
        undoBtn.setDisable(maxUndo == 0);

        Label status = new Label("");
        status.getStyleClass().add("subtitle");

        undoBtn.setOnAction(e -> {
            status.getStyleClass().remove("error");
            status.setText("");

            int num = undoSpinner.getValue();
            if (num <= 0) {
                status.getStyleClass().add("error");
                status.setText("Select how many changes to undo.");
                return;
            }
            if (num > maxUndo) {
                status.getStyleClass().add("error");
                status.setText("Cannot undo more than " + maxUndo + " changes.");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.initOwner(stage);
            confirm.initModality(Modality.WINDOW_MODAL);
            confirm.setTitle("Confirm Undo");
            confirm.setHeaderText("Undo " + num + " changes?");
            confirm.setContentText("This will reverse the last " + num + " budget changes.");
            confirm.getDialogPane().getStylesheets().add(
                    getClass().getResource("/css/DarkTheme.css").toExternalForm()
            );

            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.OK) {
                    for (int i = 0; i < num; i++) Edit.history.undo();

                    // persist after undo
                    try {
                        UserBudgetFileUtil.saveUserBudget(user, 2026);
                    } catch (Exception ex) {
                        System.err.println("Failed to save budgets: " + ex.getMessage());
                    }

                    show(stage);
                }
            });
        });

        VBox undoCard = new VBox(
                10,
                new Label("Undo"),
                new Separator(),
                new HBox(12, new Label("Changes:"), undoSpinner, undoBtn),
                status
        );
        undoCard.getStyleClass().addAll("card", "glass-card");

        // SIDE
        VBox sidePanel = buildSidePanel(maxUndo);
        sidePanel.setMinWidth(280);
        sidePanel.setMaxWidth(280);

        VBox leftContent = new VBox(14, heroCard, new Separator(), tableCard, undoCard);
        leftContent.setFillWidth(true);
        leftContent.setMaxWidth(780);
        HBox.setHgrow(leftContent, Priority.ALWAYS);

        HBox mainRow = new HBox(18, leftContent, sidePanel);
        mainRow.setAlignment(Pos.TOP_CENTER);
        mainRow.setPadding(new Insets(18));

        // FOOTER
        Button clearBtn = new Button("Clear History");
        clearBtn.getStyleClass().addAll("button", "danger");
        clearBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.initOwner(stage);
            confirm.initModality(Modality.WINDOW_MODAL);
            confirm.setTitle("Clear History");
            confirm.setHeaderText("Delete edit history?");
            confirm.setContentText("This will permanently remove the history file AND reset undo stack.");
            confirm.getDialogPane().getStylesheets().add(
                    getClass().getResource("/css/DarkTheme.css").toExternalForm()
            );

            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.OK) {
                    try { Files.deleteIfExists(HISTORY_PATH); } catch (IOException ignored) {}

                    // ✅ ΚΡΙΣΙΜΟ: καθάρισε και το in-memory history
                    Edit.history.clear();

                    show(stage);
                }
            });
        });

        Button backBtn = new Button("Back");
        backBtn.getStyleClass().addAll("button", "subtle");
        backBtn.setOnAction(e -> {
            if (user.getRole() == User.Role.CITIZEN) new VirtualEditScreen(user, userManager).show(stage);
            else new EditBudgetScreen(user, userManager).show(stage);
        });

        HBox footer = new HBox(10, clearBtn, backBtn);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(12, 18, 12, 18));
        footer.getStyleClass().add("footer-bar");

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(mainRow);
        root.setBottom(footer);

        Scene scene = new Scene(root, 1180, 760);
        var css = getClass().getResource("/css/DarkTheme.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Edit History");
        stage.show();

        FadeTransition ft = new FadeTransition(Duration.millis(200), root);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    private VBox buildSidePanel(int maxUndo) {
        Label t1 = new Label("Summary");
        t1.getStyleClass().add("side-title");

        Label l1 = new Label("• Available undos: " + maxUndo);
        Label l2 = new Label("• History file: edithistory.txt");
        Label l3 = new Label("• Undo saves immediately");

        l1.getStyleClass().add("side-text");
        l2.getStyleClass().add("side-text");
        l3.getStyleClass().add("side-text");

        VBox card1 = new VBox(10, t1, l1, l2, l3);
        card1.getStyleClass().addAll("card", "side-card");

        Label t2 = new Label("Tips");
        t2.getStyleClass().add("side-title");

        Label k1 = new Label("• Undo reverses the most recent edits first");
        Label k2 = new Label("• Clear history deletes the file & resets undo stack");
        k1.getStyleClass().add("side-text");
        k2.getStyleClass().add("side-text");

        VBox card2 = new VBox(10, t2, k1, k2);
        card2.getStyleClass().addAll("card", "side-card");

        VBox side = new VBox(14, card1, card2);
        side.setAlignment(Pos.TOP_LEFT);
        return side;
    }

    private List<HistoryRow> parseHistoryFile() {
        List<HistoryRow> out = new ArrayList<>();
        if (!Files.exists(HISTORY_PATH)) return out;

        try {
            for (String raw : Files.readAllLines(HISTORY_PATH)) {
                String line = raw.strip();
                if (line.isEmpty()) continue;

                if (line.startsWith("RECENT") || line.startsWith("MINISTRY")) continue;
                if (line.chars().allMatch(ch -> ch == '=' || ch == '-')) continue;

                String[] parts = line.split("\\s{2,}");
                if (parts.length < 3) continue;

                String ministry = parts[0].trim();
                String prev = parts[1].trim();
                String now  = parts[2].trim();

                out.add(new HistoryRow(ministry, prev, now, calcDelta(prev, now)));
            }
        } catch (IOException ignored) {}

        return out;
    }

    // ✅σωστό parse για "1.234.567,89"
    private double parseBudget(String s) {
        String clean = s.trim().replace(".", "").replace(",", ".");
        return Double.parseDouble(clean);
    }

    private String calcDelta(String prev, String now) {
        try {
            double a = parseBudget(prev);
            double b = parseBudget(now);
            double d = b - a;

            if (d > 0) return "+" + Ministry.getFormattedBudget(d);
            if (d < 0) return "-" + Ministry.getFormattedBudget(Math.abs(d));
            return "0";
        } catch (Exception e) {
            return "";
        }
    }
}
