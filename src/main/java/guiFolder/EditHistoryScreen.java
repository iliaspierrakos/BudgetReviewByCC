package guiFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import UserFeatures.Edit;
import UserManagement.User;
import UserManagement.UserManager;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

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

        // ===== Header =====
        Label title = new Label("Edit History");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Review changes and undo recent operations.");
        subtitle.getStyleClass().add("subtitle");

        VBox headerCard = new VBox(8, title, subtitle);
        headerCard.getStyleClass().addAll("card", "toolbar-card");

        // ===== Table =====
        TableView<HistoryRow> table = new TableView<>();
        table.getStyleClass().add("budget-table");
        table.setPlaceholder(new Label("No changes yet."));
        table.setPrefHeight(320);
        table.setFixedCellSize(46);

        // KEY: μην στριμώχνει τις στήλες -> horizontal scroll όταν χρειάζεται
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        TableColumn<HistoryRow, String> colMin = new TableColumn<>("Ministry");
        colMin.setCellValueFactory(new PropertyValueFactory<>("ministry"));
        colMin.setPrefWidth(560);
        colMin.setMinWidth(340);

        TableColumn<HistoryRow, String> colPrev = new TableColumn<>("Previous");
        colPrev.setCellValueFactory(new PropertyValueFactory<>("previous"));
        colPrev.setPrefWidth(220);
        colPrev.setMinWidth(170);

        TableColumn<HistoryRow, String> colNew = new TableColumn<>("New");
        colNew.setCellValueFactory(new PropertyValueFactory<>("now"));
        colNew.setPrefWidth(220);
        colNew.setMinWidth(170);

        TableColumn<HistoryRow, String> colDelta = new TableColumn<>("Change");
        colDelta.setCellValueFactory(new PropertyValueFactory<>("delta"));
        colDelta.setPrefWidth(150);
        colDelta.setMinWidth(120);

        // --- Ministry: NO WRAP + CLIP + tooltip ---
        colMin.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                    return;
                }
                setText(item);
                setWrapText(false);
                setTextOverrun(OverrunStyle.CLIP);
                setTooltip(new Tooltip(item));
                setAlignment(Pos.CENTER_LEFT);
            }
        });

        // --- Numbers: NO WRAP + CLIP + right align + tooltip ---
        colPrev.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                    return;
                }
                setText(item);
                setWrapText(false);
                setTextOverrun(OverrunStyle.CLIP);
                setAlignment(Pos.CENTER_RIGHT);
                setTooltip(new Tooltip(item));
            }
        });

        colNew.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                    return;
                }
                setText(item);
                setWrapText(false);
                setTextOverrun(OverrunStyle.CLIP);
                setAlignment(Pos.CENTER_RIGHT);
                setTooltip(new Tooltip(item));
            }
        });

        // --- Delta: NO WRAP + χρώμα + tooltip ---
        colDelta.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                    setStyle("");
                    return;
                }
                setText(item);
                setWrapText(false);
                setTextOverrun(OverrunStyle.CLIP);
                setAlignment(Pos.CENTER_RIGHT);
                setTooltip(new Tooltip(item));

                if (item.startsWith("-")) {
                    setStyle("-fx-text-fill: #ff6b6b;");
                } else if (item.startsWith("+")) {
                    setStyle("-fx-text-fill: #2ecc71;");
                } else {
                    setStyle("-fx-text-fill: #e7eaf0;");
                }
            }
        });

        table.getColumns().addAll(colMin, colPrev, colNew, colDelta);

        List<HistoryRow> rows = parseHistoryFile();
        table.setItems(FXCollections.observableArrayList(rows));

        Label tableLbl = new Label("Recent changes");
        tableLbl.getStyleClass().add("subtitle");

        VBox tableCard = new VBox(10, tableLbl, table);
        tableCard.getStyleClass().addAll("card", "table-card");
        VBox.setVgrow(table, Priority.NEVER);

        // ===== Undo =====
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

            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.OK) {
                    for (int i = 0; i < num; i++) Edit.history.undo();
                    show(stage);
                }
            });
        });

        VBox undoCard = new VBox(
                10,
                new Label("Undo"),
                new HBox(12, new Label("Changes:"), undoSpinner, undoBtn),
                status
        );
        undoCard.getStyleClass().addAll("card");
        ((HBox) undoCard.getChildren().get(1)).setAlignment(Pos.CENTER_LEFT);

        // ===== Footer =====
        Button clearBtn = new Button("Clear History");
        clearBtn.getStyleClass().addAll("button", "danger");
        clearBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.initOwner(stage);
            confirm.initModality(Modality.WINDOW_MODAL);
            confirm.setTitle("Clear History");
            confirm.setHeaderText("Delete edit history?");
            confirm.setContentText("This will permanently remove the history file.");

            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.OK) {
                    try { Files.deleteIfExists(HISTORY_PATH); } catch (IOException ignored) {}
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

        // ===== Page =====
        VBox page = new VBox(14, headerCard, tableCard, undoCard, footer);
        page.setPadding(new Insets(18));

        Scene scene = new Scene(new BorderPane(page), 980, 720);

        var css = getClass().getResource("/css/DarkTheme.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Edit History");
        stage.show();
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

        if (out.isEmpty()) out.add(new HistoryRow("No edit history available.", "", "", ""));
        return out;
    }

    private String calcDelta(String prev, String now) {
        try {
            long a = parseGreekNumber(prev);
            long b = parseGreekNumber(now);
            long d = b - a;

            if (d > 0) return "+" + formatGreekNumber(d);
            if (d < 0) return "-" + formatGreekNumber(Math.abs(d));
            return "0";
        } catch (Exception e) {
            return "";
        }
    }

    private long parseGreekNumber(String s) {
        String digits = s.replace(".", "").replace(",", "").trim();
        return Long.parseLong(digits);
    }

    private String formatGreekNumber(long n) {
        String s = Long.toString(n);
        StringBuilder sb = new StringBuilder();
        int c = 0;
        for (int i = s.length() - 1; i >= 0; i--) {
            sb.append(s.charAt(i));
            c++;
            if (c == 3 && i != 0) { sb.append('.'); c = 0; }
        }
        return sb.reverse().toString();
    }
}
