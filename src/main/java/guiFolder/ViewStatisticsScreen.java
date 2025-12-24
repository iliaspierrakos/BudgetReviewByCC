package guiFolder;

import UserFeatures.RecommendationSystem;
import UserManagement.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.List;

/**
 * ViewStatisticsScreen
 *
 * Governor-only screen that displays aggregated statistics
 * from citizen recommendations.
 */
public class ViewStatisticsScreen {

    private final User user;
    private final RecommendationSystem recSystem;

    public ViewStatisticsScreen(User user) {
        this.user = user;
        this.recSystem = new RecommendationSystem();
    }

    public void show(Stage stage) {

        // --- Access control ---
        if (user.getRole() != User.Role.GOVERNOR) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Access denied. Governor only.");
            alert.showAndWait();
            return;
        }

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
           LOAD DATA (CLEAN API)
           ========================= */
        ObservableList<StatsRow> data = FXCollections.observableArrayList();

        List<String> ministries = recSystem.getAvailableMinistries();
        for (String ministry : ministries) {
            int totalVotes = recSystem.getTotalVotesForMinistry(ministry);
            data.add(new StatsRow(ministry, totalVotes));
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
