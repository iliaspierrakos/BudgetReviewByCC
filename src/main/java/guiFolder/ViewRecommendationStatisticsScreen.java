package guiFolder;

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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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

        Label title = new Label("Citizen Recommendations – Statistics");
        title.getStyleClass().add("title");

        VBox chartsContainer = new VBox(30);
        chartsContainer.setPadding(new Insets(20));
        chartsContainer.setAlignment(Pos.TOP_CENTER);

        // Check if user is a ministry member
        if (user instanceof MinistryMember) {
            MinistryMember member = (MinistryMember) user;
            String ministryName = member.getMinistryName();
            
            // Update title to show specific ministry
            title.setText("Citizen Recommendations – Ministry of " + ministryName);
            
            // Load only the chart for this ministry
            Path targetFile = DATA_DIR.resolve("CitizenForMinistry of " + ministryName + ".txt");
            
            if (Files.exists(targetFile)) {
                PieChart chart = createChartForFile(targetFile);
                if (chart != null) {
                    chartsContainer.getChildren().add(chart);
                    
                    // Add summary statistics below the chart
                    VBox summaryBox = createSummaryBox(targetFile);
                    if (summaryBox != null) {
                        chartsContainer.getChildren().add(summaryBox);
                    }
                } else {
                    chartsContainer.getChildren().add(
                            new Label("No citizen recommendations data available for your ministry yet.")
                    );
                }
            } else {
                chartsContainer.getChildren().add(
                        new Label("No citizen recommendations file found for Ministry of " + ministryName + ".")
                );
            }
            
        } else {
            // For Governor or other roles - show all charts
            try (Stream<Path> files = Files.list(DATA_DIR)) {
                files
                    .filter(f -> f.toString().endsWith(".txt"))
                    .sorted()
                    .forEach(file -> {
                        PieChart chart = createChartForFile(file);
                        if (chart != null) {
                            chartsContainer.getChildren().add(chart);
                            
                            // Add summary for each ministry
                            VBox summaryBox = createSummaryBox(file);
                            if (summaryBox != null) {
                                chartsContainer.getChildren().add(summaryBox);
                            }
                        }
                    });
            } catch (IOException e) {
                chartsContainer.getChildren().add(
                        new Label("Error loading recommendation files.")
                );
            }
        }

        ScrollPane scrollPane = new ScrollPane(chartsContainer);
        scrollPane.setFitToWidth(true);

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("button");
        backButton.setOnAction(e ->
                new ViewEditBudgetScreen(user, userManager).show(stage)
        );

        VBox bottom = new VBox(backButton);
        bottom.setAlignment(Pos.CENTER_RIGHT);
        bottom.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(title);
        BorderPane.setAlignment(title, Pos.CENTER);
        BorderPane.setMargin(title, new Insets(20, 0, 0, 0));
        root.setCenter(scrollPane);
        root.setBottom(bottom);

        Scene scene = new Scene(root, 1100, 750);
        scene.getStylesheets().add(
                getClass().getResource("/css/DarkTheme.css").toExternalForm()
        );

        stage.setTitle("Recommendation Statistics");
        stage.setScene(scene);
        stage.show();
    }

    private PieChart createChartForFile(Path file) {

        String ministryName = extractMinistryName(file.getFileName().toString());
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        int totalVotes = 0;

        try (BufferedReader br = Files.newBufferedReader(file)) {
            String line;

            while ((line = br.readLine()) != null) {

                if (line.startsWith("Total Votes")) {
                    continue;
                }

                String[] parts = line.split(", Votes from Citizens:");
                if (parts.length < 2) continue;

                String category = parts[0].trim();
                int votes = Integer.parseInt(
                        parts[1].split(",")[0].trim()
                );

                totalVotes += votes;

                if (votes > 0) {
                    pieData.add(new PieChart.Data(category, votes));
                }
            }

        } catch (Exception e) {
            return null;
        }

        if (pieData.isEmpty()) {
            return null;
        }

        PieChart chart = new PieChart(pieData);
        chart.setTitle("Ministry of " + ministryName + " (Total: " + totalVotes + " votes)");
        chart.setLabelsVisible(true);
        chart.setLegendVisible(true);

        // Calculate percentages and update labels
        final int finalTotalVotes = totalVotes;
        chart.getData().forEach(d -> {
            int votes = (int) d.getPieValue();
            double percentage = (votes * 100.0) / finalTotalVotes;
            d.nameProperty().set(
                    d.getName() + " (" + votes + " votes, " + 
                    String.format("%.1f%%", percentage) + ")"
            );
        });

        return chart;
    }

    private VBox createSummaryBox(Path file) {
        String ministryName = extractMinistryName(file.getFileName().toString());
        
        List<String> categoryData = new ArrayList<>();
        int totalVotes = 0;
        int totalCategories = 0;
        int categoriesWithVotes = 0;

        try (BufferedReader br = Files.newBufferedReader(file)) {
            String line;

            while ((line = br.readLine()) != null) {
                if (line.startsWith("Total Votes")) {
                    continue;
                }

                String[] parts = line.split(", Votes from Citizens:");
                if (parts.length < 2) continue;

                String category = parts[0].trim();
                int votes = Integer.parseInt(parts[1].split(",")[0].trim());
                
                totalCategories++;
                totalVotes += votes;
                
                if (votes > 0) {
                    categoriesWithVotes++;
                    categoryData.add(category + ": " + votes + " votes");
                }
            }

        } catch (Exception e) {
            return null;
        }

        if (totalCategories == 0) {
            return null;
        }

        VBox summaryBox = new VBox(8);
        summaryBox.setPadding(new Insets(15));
        summaryBox.getStyleClass().add("card");
        summaryBox.setMaxWidth(800);

        Label summaryTitle = new Label("Summary for Ministry of " + ministryName);
        summaryTitle.getStyleClass().add("subtitle");

        Label statsLabel = new Label(
            "Total Votes: " + totalVotes + " | " +
            "Categories with votes: " + categoriesWithVotes + "/" + totalCategories
        );

        summaryBox.getChildren().addAll(summaryTitle, statsLabel);

        return summaryBox;
    }

    private String extractMinistryName(String fileName) {
        return fileName
                .replace("CitizenForMinistry of ", "")
                .replace(".txt", "");
    }
}
