package guiFolder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import UserFeatures.CreatingMinistries;
import UserManagement.User;
import UserManagement.UserManager;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.imageio.ImageIO;

/**
 * ViewStatisticsScreen
 *
 * Governor-only statistics screen.
 * Table + Chart with tooltips, animation and export.
 *
 * FIXES:
 * - Keeps old constructor (User) so your project compiles
 * - Also supports new constructor (User, UserManager)
 * - No window jumping (reuse Scene root + preserve stage state)
 * - Gold accents (inline only, no CSS edits)
 */
public class ViewStatisticsScreen {

    private final User user;
    private final UserManager userManager;

    private static final String VOTES_CSV_FILE =
            "src/main/resources/NecessaryFilesAndData/ProposalsFromCitizens/VotesData.csv";
    private static int[][] allVotes = new int[20][6];

    /** Backward-compatible constructor (keeps your existing calls working). */
    public ViewStatisticsScreen(User user) {
        this(user, null);
    }

    /** Preferred constructor (use when you have userManager). */
    public ViewStatisticsScreen(User user, UserManager userManager) {
        this.user = user;
        this.userManager = userManager;
    }

    public void show(Stage stage) {

        /* ================= ACCESS CONTROL ================= */
        if (user.getRole() != User.Role.GOVERNOR) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Access denied. Governor only.");
            var css = getClass().getResource("/css/DarkTheme.css");
            if (css != null) alert.getDialogPane().getStylesheets().add(css.toExternalForm());
            alert.getDialogPane().setStyle("-fx-border-color: rgba(212,175,55,0.22); -fx-border-radius: 16; -fx-background-radius: 16;");
            alert.showAndWait();
            return;
        }

        // =========================
        // WINDOW STATE SNAPSHOT (NO JUMP)
        // =========================
        final boolean wasMaximized = stage.isMaximized();
        final boolean wasFullScreen = stage.isFullScreen();
        final double prevW = stage.getWidth();
        final double prevH = stage.getHeight();
        final double prevX = stage.getX();
        final double prevY = stage.getY();

        initializeCSV();
        loadVotesFromCSV();

        /* ================= TITLE ================= */
        Label title = new Label("CITIZEN RECOMMENDATION STATISTICS");
        title.getStyleClass().add("title");
        // GOLD subtle glow
        title.setStyle("-fx-effect: dropshadow(gaussian, rgba(212,175,55,0.18), 16, 0.20, 0, 0);");

        /* ================= TABLE ================= */
        TableView<StatsRow> table = new TableView<>();
        table.getStyleClass().addAll("budget-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<StatsRow, String> ministryCol =
                new TableColumn<>("Ministry");
        ministryCol.setCellValueFactory(
                new PropertyValueFactory<>("ministry")
        );

        TableColumn<StatsRow, Integer> votesCol =
                new TableColumn<>("Total Votes");
        votesCol.setCellValueFactory(
                new PropertyValueFactory<>("totalVotes")
        );

        table.getColumns().addAll(ministryCol, votesCol);

        ObservableList<StatsRow> data = FXCollections.observableArrayList();
        for (int i = 0; i < CreatingMinistries.ministries2026.length; i++) {
            if (CreatingMinistries.ministries2026[i] != null) {
                data.add(new StatsRow(
                        CreatingMinistries.ministries2026[i].getMinistryName(),
                        allVotes[i][0]
                ));
            }
        }
        table.setItems(data);

        /* ================= CHART ================= */
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Ministry");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Total Votes");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setAnimated(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        for (StatsRow row : data) {
            series.getData().add(
                    new XYChart.Data<>(row.getMinistry(), row.getTotalVotes())
            );
        }

        chart.getData().add(series);

        /* ---- TOOLTIP FIX (CORRECT TIMING) ---- */
        Platform.runLater(() -> {
            for (XYChart.Data<String, Number> bar : series.getData()) {
                Node node = bar.getNode();
                if (node != null) {
                    Tooltip.install(
                            node,
                            new Tooltip(bar.getXValue() + " – " + bar.getYValue() + " votes")
                    );
                }
            }
        });

        VBox.setVgrow(chart, Priority.ALWAYS);

        /* ================= CARDS ================= */
        VBox tableCard = new VBox(table);
        tableCard.getStyleClass().addAll("card", "table-card");
        tableCard.setPadding(new Insets(12));
        VBox.setVgrow(table, Priority.ALWAYS);

        VBox chartCard = new VBox(chart);
        chartCard.getStyleClass().addAll("card", "table-card");
        chartCard.setPadding(new Insets(12));
        VBox.setVgrow(chartCard, Priority.ALWAYS);

        // GOLD subtle outline for chart card (only when visible)
        chartCard.setStyle("-fx-border-color: rgba(212,175,55,0.18);");

        chartCard.setVisible(false);
        chartCard.setManaged(false);

        /* ================= BUTTONS ================= */
        Button toggleBtn = new Button("Show Chart");
        toggleBtn.getStyleClass().addAll("button", "primary");
        toggleBtn.setStyle("-fx-effect: dropshadow(gaussian, rgba(212,175,55,0.16), 18, 0.22, 0, 8);");

        Button exportBtn = new Button("Export Chart");
        exportBtn.getStyleClass().addAll("button", "subtle");
        exportBtn.setDisable(true);
        exportBtn.setStyle("-fx-border-color: rgba(212,175,55,0.20);");

        Button backBtn = new Button("Back");
        backBtn.getStyleClass().addAll("button", "subtle");
        backBtn.setStyle("-fx-border-color: rgba(212,175,55,0.18);");

        toggleBtn.setOnAction(e -> {
            boolean showChart = !chartCard.isVisible();

            animate(chartCard);
            animate(tableCard);

            chartCard.setVisible(showChart);
            chartCard.setManaged(showChart);

            tableCard.setVisible(!showChart);
            tableCard.setManaged(!showChart);

            exportBtn.setDisable(!showChart);
            toggleBtn.setText(showChart ? "Show Table" : "Show Chart");
        });

        exportBtn.setOnAction(e -> exportChart(chart, stage));

        backBtn.setOnAction(e -> {
            if (userManager != null) {
                new ViewEditBudgetScreen(user, userManager).show(stage);
            } else {
                // Fallback: still avoid crash if someone used the old constructor
                stage.close();
            }
        });

        HBox actions = new HBox(12, toggleBtn, exportBtn, backBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox headerCard = new VBox(12, title, actions);
        headerCard.getStyleClass().addAll("card", "toolbar-card");
        headerCard.setPadding(new Insets(18));
        headerCard.setStyle("-fx-border-color: rgba(212,175,55,0.14);");

        /* ================= ROOT ================= */
        VBox content = new VBox(18, headerCard, tableCard, chartCard);
        content.setPadding(new Insets(26));
        content.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(tableCard, Priority.ALWAYS);
        VBox.setVgrow(chartCard, Priority.ALWAYS);

        BorderPane root = new BorderPane(content);

        /* ================= SCENE (REUSE + NO JUMP) ================= */
        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/css/DarkTheme.css").toExternalForm()
            );
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
            String css = getClass().getResource("/css/DarkTheme.css").toExternalForm();
            if (!scene.getStylesheets().contains(css)) scene.getStylesheets().add(css);
        }

        stage.setTitle("View Statistics");
        stage.show();

        // Restore window state (fullscreen/max/normal) exactly
        if (wasFullScreen) {
            stage.setFullScreen(true);
        } else if (wasMaximized) {
            stage.setMaximized(true);
        } else {
            if (prevW > 0 && prevH > 0) {
                stage.setWidth(prevW);
                stage.setHeight(prevH);
                stage.setX(prevX);
                stage.setY(prevY);
            }
        }
    }

    /* ================= ANIMATION ================= */
    private void animate(VBox node) {
        FadeTransition ft = new FadeTransition(Duration.millis(220), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    /* ================= EXPORT ================= */
    private void exportChart(BarChart<String, Number> chart, Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Chart");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PNG Image", "*.png")
        );

        File file = chooser.showSaveDialog(stage);
        if (file == null) return;

        try {
            WritableImage image = chart.snapshot(new SnapshotParameters(), null);
            ImageIO.write(
                    SwingFXUtils.fromFXImage(image, null),
                    "png",
                    file
            );
        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to export chart.");
            var css = getClass().getResource("/css/DarkTheme.css");
            if (css != null) alert.getDialogPane().getStylesheets().add(css.toExternalForm());
            alert.getDialogPane().setStyle("-fx-border-color: rgba(212,175,55,0.22); -fx-border-radius: 16; -fx-background-radius: 16;");
            alert.showAndWait();
        }
    }

    /* ================= BACKEND HELPERS (UNCHANGED) ================= */

    private void initializeCSV() {
        File csvFile = new File(VOTES_CSV_FILE);
        if (csvFile.exists()) return;

        try {
            csvFile.getParentFile().mkdirs();
            try (PrintWriter pw = new PrintWriter(new FileWriter(csvFile))) {
                for (int i = 0; i < 20; i++) {
                    pw.println("0,0,0,0,0,0");
                }
            }
        } catch (IOException e) {
            System.err.println("Error creating CSV file");
        }
    }

    private void loadVotesFromCSV() {
        try (BufferedReader br = new BufferedReader(new FileReader(VOTES_CSV_FILE))) {
            String line;
            int row = 0;
            while ((line = br.readLine()) != null && row < 20) {
                String[] values = line.split(",");
                for (int col = 0; col < 6 && col < values.length; col++) {
                    allVotes[row][col] = Integer.parseInt(values[col].trim());
                }
                row++;
            }
        } catch (Exception e) {
            System.err.println("Error loading votes");
        }
    }

    /* ================= TABLE ROW MODEL ================= */
    public static class StatsRow {
        private final String ministry;
        private final int totalVotes;

        public StatsRow(String ministry, int totalVotes) {
            this.ministry = ministry;
            this.totalVotes = totalVotes;
        }

        public String getMinistry() { return ministry; }
        public int getTotalVotes() { return totalVotes; }
    }
}
