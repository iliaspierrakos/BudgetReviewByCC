package guiFolder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import UserFeatures.CreatingMinistries;
import UserManagement.User;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * ViewStatisticsScreen
 * 
 * Governor-only screen that displays aggregated statistics
 * from citizen recommendations.
 */
public class ViewStatisticsScreen {

    private final User user;
    private static final String VOTES_CSV_FILE = "src/main/resources/NecessaryFilesAndData/ProposalsFromCitizens/VotesData.csv";
    private static int[][] allVotes = new int[20][6];

    public ViewStatisticsScreen(User user) {
        this.user = user;
    }

    public void show(Stage stage) {

        // --- Access control ---
        if (user.getRole() != User.Role.GOVERNOR) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Access denied. Governor only.");
            alert.showAndWait();
            return;
        }

        // Load votes data
        initializeCSV();
        loadVotesFromCSV();

        Label title = new Label("Citizen Recommendation Statistics");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        /* =========================
           TABLE
           ========================= */
        TableView<StatsRow> table = new TableView<>();

        TableColumn<StatsRow, String> ministryCol =
                new TableColumn<>("Ministry");
        ministryCol.setCellValueFactory(
                new PropertyValueFactory<>("ministry"));
        ministryCol.setPrefWidth(360);

        TableColumn<StatsRow, Integer> votesCol =
                new TableColumn<>("Total Votes");
        votesCol.setCellValueFactory(
                new PropertyValueFactory<>("totalVotes"));
        votesCol.setPrefWidth(160);

        table.getColumns().addAll(ministryCol, votesCol);

        /* =========================
           LOAD DATA
           ========================= */
        ObservableList<StatsRow> data = FXCollections.observableArrayList();

        for (int i = 0; i < CreatingMinistries.ministries2026.length; i++) {
            if (CreatingMinistries.ministries2026[i] != null) {
                String ministry = CreatingMinistries.ministries2026[i].getMinistryName();
                int totalVotes = allVotes[i][0];
                data.add(new StatsRow(ministry, totalVotes));
            }
        }

        table.setItems(data);

        Button backButton = new Button("Back");
        backButton.setOnAction(e ->
                new ViewEditBudgetScreen(user, null).show(stage)
        );

        HBox bottom = new HBox(backButton);
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(title);
        BorderPane.setAlignment(title, Pos.CENTER);
        BorderPane.setMargin(title, new Insets(10));
        root.setCenter(table);
        root.setBottom(bottom);

        Scene scene = new Scene(root, 600, 480);
        stage.setTitle("View Statistics");
        stage.setScene(scene);
        stage.show();
    }

    /* =========================
       BACKEND HELPER METHODS
       ========================= */

    /**
     * Initializes CSV file if it doesn't exist
     */
    private void initializeCSV() {
        File csvFile = new File(VOTES_CSV_FILE);
        if (csvFile.exists()) {
            return;
        }

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

    /**
     * Loads votes from CSV into memory
     */
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

    /* =========================
       TABLE ROW MODEL
       ========================= */
    public static class StatsRow {
        private final String ministry;
        private final int totalVotes;

        public StatsRow(String ministry, int totalVotes) {
            this.ministry = ministry;
            this.totalVotes = totalVotes;
        }

        public String getMinistry() {
            return ministry;
        }

        public int getTotalVotes() {
            return totalVotes;
        }
    }
}