package guiFolder;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import UserManagement.MinistryMember;
import UserManagement.User;
import UserManagement.UserManager;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * ViewRecommendationsScreen
 *
 * Ministry Member screen for viewing citizen recommendations in a TableView.
 *
 * UI UPGRADES (no CSS edits):
 * - Preserve window state (fullscreen/max/size/position) + reuse scene root (NO JUMP)
 * - Gold accents (inline only)
 * - Subtle fade in
 * - "Pie chart" button (πίτα) that opens ViewRecommendationStatisticsScreen
 */
public class ViewRecommendationsScreen {

    private final User user;
    private final UserManager userManager;

    private static final Path DATA_DIR =
            Path.of("src/main/resources/NecessaryFilesAndData/ProposalsFromCitizens");

    public ViewRecommendationsScreen(User user, UserManager userManager) {
        this.user = user;
        this.userManager = userManager;
    }

    public void show(Stage stage) {

        /* ================= ACCESS CONTROL ================= */
        if (!(user instanceof MinistryMember)) {
            Alert a = new Alert(Alert.AlertType.ERROR,
            "Access denied: Only Ministry Members can view recommendations.");
            var css = getClass().getResource("/css/DarkTheme.css");
            if (css != null) a.getDialogPane().getStylesheets().add(css.toExternalForm());
            a.getDialogPane().setStyle(
            "-fx-border-color: rgba(212,175,55,0.22);" +
            "-fx-border-radius: 16;" +
            "-fx-background-radius: 16;"
            );
            a.showAndWait();
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

        MinistryMember mm = (MinistryMember) user;
        String ministryName = mm.getMinistryName();

        Path filePath = DATA_DIR.resolve("CitizenForMinistry of " + ministryName + ".txt");
        boolean hasFile = Files.exists(filePath);

        /* ================= HEADER ================= */
        Label title = new Label("CITIZEN RECOMMENDATIONS");
        title.getStyleClass().add("title");
        title.setStyle("-fx-effect: dropshadow(gaussian, rgba(212,175,55,0.18), 16, 0.20, 0, 0);");

        Label subtitle = new Label("Ministry: " + ministryName);
        subtitle.getStyleClass().add("subtitle");

        Label hint = new Label("Here you can see votes per category and the probability distribution.");
        hint.getStyleClass().add("subtitle");
        hint.setStyle("-fx-opacity: 0.85;");

        VBox headerCard = new VBox(6, title, subtitle, hint);
        headerCard.getStyleClass().addAll("card", "toolbar-card");
        headerCard.setPadding(new Insets(18));
        headerCard.setStyle("-fx-border-color: rgba(212,175,55,0.14);");

        /* ================= TABLE ================= */
        TableView<RecRow> table = new TableView<>();
        table.getStyleClass().addAll("budget-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<RecRow, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setMinWidth(420);

        TableColumn<RecRow, Integer> votesCol = new TableColumn<>("Votes");
        votesCol.setCellValueFactory(new PropertyValueFactory<>("votes"));
        votesCol.setMinWidth(120);

        TableColumn<RecRow, String> probCol = new TableColumn<>("Probability");
        probCol.setCellValueFactory(new PropertyValueFactory<>("probabilityText"));
        probCol.setMinWidth(150);

        table.getColumns().addAll(categoryCol, votesCol, probCol);

        Label placeholder = new Label(
            hasFile
            ? "No data rows found in file."
            : "No citizen recommendations have been submitted yet for this ministry."
        );
        placeholder.getStyleClass().add("subtitle");
        table.setPlaceholder(placeholder);

        ObservableList<RecRow> rows = FXCollections.observableArrayList();
        int totalVotes = 0;

        if (hasFile) {
            try (BufferedReader br = Files.newBufferedReader(filePath)) {
                String line;
                while ((line = br.readLine()) != null) {

                    // ignore summary
                    if (line.startsWith("Total Votes")) continue;

                    // expected: "Category, Votes from Citizens: 12, ..."
                    String[] parts = line.split(", Votes from Citizens:");
                    if (parts.length < 2) continue;

                    String category = parts[0].trim();

                    String right = parts[1].trim();
                    String votesStr = right.split(",")[0].trim();
                    int votes;
                    try {
                        votes = Integer.parseInt(votesStr);
                    } catch (NumberFormatException ex) {
                        continue;
                    }

                    // keep zero rows if you want; here we show only positives
                    if (votes <= 0) continue;

                    rows.add(new RecRow(category, votes));
                    totalVotes += votes;
                }
            } catch (IOException e) {
                Alert a = new Alert(Alert.AlertType.ERROR, "Error reading recommendations file.");
                var css = getClass().getResource("/css/DarkTheme.css");
                if (css != null) a.getDialogPane().getStylesheets().add(css.toExternalForm());
                a.getDialogPane().setStyle("-fx-border-color: rgba(212,175,55,0.22); -fx-border-radius: 16; -fx-background-radius: 16;");
                a.showAndWait();
            }
        }

        // compute probabilities
        if (totalVotes > 0) {
            for (RecRow r : rows) {
                double pct = (r.getVotes() * 100.0) / totalVotes;
                r.setProbabilityText(String.format("%.1f%%", pct));
            }
        }

        table.setItems(rows);
        VBox.setVgrow(table, Priority.ALWAYS);

        VBox tableCard = new VBox(12, table);
        tableCard.getStyleClass().addAll("card", "table-card");
        tableCard.setPadding(new Insets(16));
        VBox.setVgrow(tableCard, Priority.ALWAYS);
        tableCard.setStyle("-fx-border-color: rgba(212,175,55,0.12);");

        // small status
        Label status = new Label(
                hasFile
                        ? ("Loaded: " + filePath.getFileName() + " • Total votes: " + totalVotes)
                        : "Waiting for first submissions…"
        );
        status.getStyleClass().add("subtitle");
        status.setStyle("-fx-opacity: 0.78;");

        /* ================= BUTTONS ================= */
        Button pieButton = new Button("View Pie Chart");
        pieButton.getStyleClass().addAll("button", "primary");
        pieButton.setStyle("-fx-effect: dropshadow(gaussian, rgba(212,175,55,0.16), 18, 0.22, 0, 8);");
        pieButton.setDisable(!hasFile || totalVotes == 0);

        // tooltip for pie button
        Tooltip.install(pieButton, new Tooltip("Show distribution as a pie chart (probabilities)."));

        pieButton.setOnAction(e ->
            new ViewRecommendationStatisticsScreen(user, userManager).show(stage)
        );

        Button backButton = new Button("Back");
        backButton.getStyleClass().addAll("button", "subtle");
        backButton.setStyle("-fx-border-color: rgba(212,175,55,0.18);");

        backButton.setOnAction(e ->
            new ViewEditBudgetScreen(user, userManager).show(stage)
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(12, status, spacer, pieButton, backButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        /* ================= ROOT ================= */
        VBox content = new VBox(18, headerCard, tableCard, actions);
        content.setPadding(new Insets(26));
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        BorderPane root = new BorderPane(content);

        /* ================= SCENE (REUSE + NO JUMP) ================= */
        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root, 1100, 720);
            scene.getStylesheets().add(
                    getClass().getResource("/css/DarkTheme.css").toExternalForm()
            );
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
            String css = getClass().getResource("/css/DarkTheme.css").toExternalForm();
            if (!scene.getStylesheets().contains(css)) scene.getStylesheets().add(css);
        }

        stage.setTitle("View Recommendations");
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

        // Subtle fade in
        FadeTransition ft = new FadeTransition(Duration.millis(200), root);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    /* ================= TABLE ROW MODEL ================= */
    public static class RecRow {
        private final String category;
        private final int votes;
        private String probabilityText = "";

        public RecRow(String category, int votes) {
            this.category = category;
            this.votes = votes;
        }

        public String getCategory() { return category; }
        public int getVotes() { return votes; }
        public String getProbabilityText() { return probabilityText; }
        public void setProbabilityText(String probabilityText) { this.probabilityText = probabilityText; }
    }
}
