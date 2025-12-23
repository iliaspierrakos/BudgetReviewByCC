package guiFolder;

import UserFeatures.View;
import UserFeatures.View.GovBudgetRow;
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

public class ViewBudgetScreen {

    private final User user;
    private final View viewLogic = new View();

    public ViewBudgetScreen(User user) {
        this.user = user;
    }

    public void show(Stage stage) {

        /* =========================
           TOP CONTROLS
           ========================= */
        ComboBox<Integer> yearBox = new ComboBox<>();
        yearBox.getItems().addAll(2020, 2021, 2022, 2023, 2024, 2025, 2026);
        yearBox.setValue(2026);

        CheckBox sortBox = new CheckBox("Sort by budget (descending)");

        Button loadButton = new Button("Load");
        Button backButton = new Button("Back");

        HBox top = new HBox(10, new Label("Year:"), yearBox, sortBox, loadButton, backButton);
        top.setAlignment(Pos.CENTER);
        top.setPadding(new Insets(10));

        /* =========================
           TABLE
           ========================= */
        TableView<GovBudgetRow> table = new TableView<>();

        TableColumn<GovBudgetRow, String> ministryCol =
                new TableColumn<>("Ministry");
        ministryCol.setCellValueFactory(
                new PropertyValueFactory<>("ministry"));
        ministryCol.setPrefWidth(260);

        TableColumn<GovBudgetRow, String> budgetCol =
                new TableColumn<>("Budget");
        budgetCol.setCellValueFactory(
                new PropertyValueFactory<>("budgetText"));
        budgetCol.setPrefWidth(140);

        TableColumn<GovBudgetRow, String> percentCol =
                new TableColumn<>("Percentage");
        percentCol.setCellValueFactory(
                new PropertyValueFactory<>("percentText"));
        percentCol.setPrefWidth(120);

        table.getColumns().addAll(ministryCol, budgetCol, percentCol);

        /* =========================
           ACTIONS
           ========================= */
        loadButton.setOnAction(e -> {
            int year = yearBox.getValue();
            boolean sort = sortBox.isSelected();

            ObservableList<GovBudgetRow> data =
                    FXCollections.observableArrayList(
                            viewLogic.getGovBudgetRowsForGui(year, sort)
                    );

            table.setItems(data);
        });

        backButton.setOnAction(e -> {
            new ViewEditBudgetScreen(user, null).show(stage);
        });

        /* =========================
           ROOT
           ========================= */
        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(table);
        root.setPadding(new Insets(10));

        stage.setScene(new Scene(root, 600, 420));
        stage.setTitle("View Government Budget");
        stage.show();
    }
}
