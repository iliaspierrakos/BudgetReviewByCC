package guiFolder;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import UserManagement.MinistryMember;
import UserManagement.User;
import UserManagement.UserManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ViewRecommendationStatisticsScreen {

    private final User user;
    private final UserManager userManager;

    private static final Path DATA_DIR =
        Path.of("src/main/resources/NecessaryFilesAndData/ProposalsFromCitizens");

    public ViewRecommendationStatisticsScreen(User user, UserManager userManager) {
        this.user = user;
        this.userManager = userManager;
    }

    public void show(Stage stage) {

    // =========================
    // WINDOW STATE SNAPSHOT (NO JUMP)
    // =========================
    final boolean wasMaximized = stage.isMaximized();
    final boolean wasFullScreen = stage.isFullScreen();
    final double prevW = stage.getWidth();
    final double prevH = stage.getHeight();
    final double prevX = stage.getX();
    final double prevY = stage.getY();

    Label title = new Label("Citizen Recommendations – Statistics");
    title.getStyleClass().add("title");
    title.setStyle("-fx-text-fill: #f0f2f8; -fx-effect: dropshadow(gaussian, rgba(212,175,55,0.18), 16, 0.20, 0, 0);");

    VBox chartsContainer = new VBox(26);
    chartsContainer.setPadding(new Insets(20));
    chartsContainer.setAlignment(Pos.TOP_CENTER);

    // =========================
    // CONTENT
    // =========================
    if (user instanceof MinistryMember) {

        MinistryMember member = (MinistryMember) user;
        String ministryName = member.getMinistryName();

        title.setText("Citizen Recommendations – Ministry of " + ministryName);

        Path targetFile = DATA_DIR.resolve("CitizenForMinistry of " + ministryName + ".txt");

        if (Files.exists(targetFile)) {
            PieChart chart = createProbabilityChartForFile(targetFile);
            if (chart != null) {
                chartsContainer.getChildren().add(chart);

                VBox summaryBox = createSummaryBox(targetFile);
                if (summaryBox != null) {
                    chartsContainer.getChildren().add(summaryBox);
                }
            } else {
                Label msg = new Label("No citizen recommendations data available for your ministry yet.");
                msg.getStyleClass().add("subtitle");
                chartsContainer.getChildren().add(msg);
            }
        } else {
            Label msg = new Label("No citizen recommendations file found for Ministry of " + ministryName + ".");
            msg.getStyleClass().add("subtitle");
            chartsContainer.getChildren().add(msg);
        }

    } else {
        // Governor or other roles - show all charts
        try (Stream<Path> files = Files.list(DATA_DIR)) {
            files
                .filter(f -> f.toString().endsWith(".txt"))
                .sorted()
                .forEach(file -> {
                    PieChart chart = createProbabilityChartForFile(file);
                    if (chart != null) {
                        chartsContainer.getChildren().add(chart);

                        VBox summaryBox = createSummaryBox(file);
                        if (summaryBox != null) {
                            chartsContainer.getChildren().add(summaryBox);
                        }
                    }
                });
        } catch (IOException e) {
            Label msg = new Label("Error loading recommendation files.");
            msg.getStyleClass().add("error");
            chartsContainer.getChildren().add(msg);
        }
    }

    // =========================
    // LAYOUT
    // =========================
    ScrollPane scrollPane = new ScrollPane(chartsContainer);
    scrollPane.setFitToWidth(true);
    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

    Button backButton = new Button("Back");
    backButton.getStyleClass().addAll("button", "subtle");
    backButton.setStyle("-fx-border-color: rgba(212,175,55,0.20);");
    backButton.setOnAction(e -> new ViewEditBudgetScreen(user, userManager).show(stage));

    VBox bottom = new VBox(backButton);
    bottom.setAlignment(Pos.CENTER_RIGHT);
    bottom.setPadding(new Insets(10, 18, 16, 18));

    BorderPane root = new BorderPane();
    root.setTop(title);
    BorderPane.setAlignment(title, Pos.CENTER);
    BorderPane.setMargin(title, new Insets(20, 0, 0, 0));
    root.setCenter(scrollPane);
    root.setBottom(bottom);

    // =========================
    // SCENE (REUSE + NO JUMP)
    // =========================
    Scene scene = stage.getScene();
    if (scene == null) {
        scene = new Scene(root);
        var cssUrl = getClass().getResource("/css/DarkTheme.css");
        if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());
        stage.setScene(scene);
    } else {
        scene.setRoot(root);
        var cssUrl = getClass().getResource("/css/DarkTheme.css");
        if (cssUrl != null) {
            String css = cssUrl.toExternalForm();
            if (!scene.getStylesheets().contains(css)) {
                scene.getStylesheets().add(css);
            }
        }
    }

    stage.setTitle("Recommendation Statistics");
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

    /**
     * Probability pie chart:
     * slice value = votes/total (0..1)
     * labels show: Category (votes, xx.x%)
     * tooltips show: votes/total (xx.x%)
     */
    private PieChart createProbabilityChartForFile(Path file) {

        String ministryName = extractMinistryName(file.getFileName().toString());

        // temp storage
        class Row { String cat; int votes; Row(String c,int v){cat=c;votes=v;} }
        List<Row> rows = new ArrayList<>();

        int totalVotes = 0;

        try (BufferedReader br = Files.newBufferedReader(file)) {
            String line;

            while ((line = br.readLine()) != null) {

                if (line.startsWith("Total Votes")) continue;

                String[] parts = line.split(", Votes from Citizens:");
                if (parts.length < 2) continue;

                String category = parts[0].trim();
                int votes = Integer.parseInt(parts[1].split(",")[0].trim());

                totalVotes += votes;
                rows.add(new Row(category, votes));
            }

        } catch (Exception e) {
            return null;
        }

        if (totalVotes <= 0) return null;

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        for (Row r : rows) {
            if (r.votes <= 0) continue;
            double prob = r.votes / (double) totalVotes; // 0..1
            PieChart.Data d = new PieChart.Data(r.cat, prob);
            pieData.add(d);
        }

        if (pieData.isEmpty()) return null;

        PieChart chart = new PieChart(pieData);
        chart.setTitle("Ministry of " + ministryName + " (Total: " + totalVotes + " votes)");
        chart.setLabelsVisible(true);
        chart.setLegendVisible(true);

        final int finalTotal = totalVotes;

        // Update labels + tooltips once nodes exist
        chart.getData().forEach(d -> {
            // Convert back from probability to votes for display:
            // votes ≈ prob * total. But we want exact votes from file.
            // We'll look it up from 'rows' by category name.
            int votes = rows.stream()
            .filter(r -> r.cat.equals(d.getName()))
            .map(r -> r.votes)
            .findFirst()
            .orElse((int) Math.round(d.getPieValue() * finalTotal));
            double pct = (votes * 100.0) / finalTotal;

            // Label text = category (votes, %)
            d.nameProperty().set(
                d.getName() + " (" + votes + " votes, " + String.format("%.1f%%", pct) + ")"
            );

            // Tooltip on slice
            d.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    Tooltip tip = new Tooltip(votes + " / " + finalTotal + "  (" + String.format("%.1f%%", pct) + ")");
                    Tooltip.install(newNode, tip);
                }
            });
        });

        // Optional GOLD accent around chart area (inline only)
        chart.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-color: rgba(212,175,55,0.16);" +
            "-fx-border-radius: 16;" +
            "-fx-background-radius: 16;"
        );

        return chart;
    }

    private VBox createSummaryBox(Path file) {
        String ministryName = extractMinistryName(file.getFileName().toString());

        int totalVotes = 0;
        int totalCategories = 0;
        int categoriesWithVotes = 0;

        try (BufferedReader br = Files.newBufferedReader(file)) {
            String line;

            while ((line = br.readLine()) != null) {
                if (line.startsWith("Total Votes")) continue;

                String[] parts = line.split(", Votes from Citizens:");
                if (parts.length < 2) continue;

                int votes = Integer.parseInt(parts[1].split(",")[0].trim());

                totalCategories++;
                totalVotes += votes;

                if (votes > 0) categoriesWithVotes++;
            }

        } catch (Exception e) {
            return null;
        }

        if (totalCategories == 0) return null;

        VBox summaryBox = new VBox(8);
        summaryBox.setPadding(new Insets(15));
        summaryBox.getStyleClass().add("card");
        summaryBox.setMaxWidth(900);

        // GOLD subtle edge
        summaryBox.setStyle("-fx-border-color: rgba(212,175,55,0.18);");

        Label summaryTitle = new Label("Summary for Ministry of " + ministryName);
        summaryTitle.getStyleClass().add("subtitle");

        Label statsLabel = new Label(
            "Total Votes: " + totalVotes + " | " +
            "Categories with votes: " + categoriesWithVotes + "/" + totalCategories
        );
        statsLabel.getStyleClass().add("subtitle");

        summaryBox.getChildren().addAll(summaryTitle, statsLabel);

        return summaryBox;
    }

    private String extractMinistryName(String fileName) {
        return fileName
        .replace("CitizenForMinistry of ", "")
        .replace(".txt", "");
    }
}
