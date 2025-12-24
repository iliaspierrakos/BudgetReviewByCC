package guiFolder;

import UserFeatures.View;
import UserFeatures.View.GovBudgetRow;
import UserManagement.User;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * JavaFX screen for viewing the government budget.
 *
 * <p>This screen displays budget data for a selected year,
 * optionally sorted by budget size.</p>
 *
 * <p>It does NOT read any files directly. All data are retrieved
 * from the {@link View} logic class.</p>
 */
public class ViewBudgetScreen {

    private final User user;
    private final View viewLogic = new View();

    public ViewBudgetScreen(User user) {
        this.user = user;
    }

    public void show(Stage stage) {

        /* =========================
           CONTROLS
           ========================= */
        ComboBox<Integer> yearBox = new ComboBox<>();
        yearBox.getItems().addAll(2020, 2021, 2022, 2023, 2024, 2025, 2026);
        yearBox.setValue(2026);

        CheckBox sortBox = new CheckBox("Sort by budget (descending)");

        Button loadButton = new Button("Load");
        Button backButton = new Button("Back");

        HBox controls = new HBox(
                10,
                new Label("Year:"),
                yearBox,
                sortBox,
                loadButton,
                backButton
        );
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(10));

        /* =========================
           TABLE
           ========================= */
        TableView<GovBudgetRow> table = new TableView<>();
        table.setPlaceholder(new Label("No data loaded"));

        TableColumn<GovBudgetRow, String> ministryCol =
                new TableColumn<>("Ministry");
        ministryCol.setCellValueFactory(
                new PropertyValueFactory<>("ministry"));
        ministryCol.setPrefWidth(260);

        TableColumn<GovBudgetRow, String> budgetCol =
                new TableColumn<>("Budget");
        budgetCol.setCellValueFactory(
                new PropertyValueFactory<>("budgetText"));
        budgetCol.setPrefWidth(150);

        TableColumn<GovBudgetRow, String> percentCol =
                new TableColumn<>("Percentage");
        percentCol.setCellValueFactory(
                new PropertyValueFactory<>("percentText"));
        percentCol.setPrefWidth(130);

        table.getColumns().addAll(ministryCol, budgetCol, percentCol);

        /* =========================
           ACTIONS
           ========================= */
        loadButton.setOnAction(e -> {
            Integer year = yearBox.getValue();
            boolean sort = sortBox.isSelected();

            table.getItems().clear();

            table.setItems(
                    FXCollections.observableArrayList(
                            viewLogic.getGovBudgetRowsForGui(year, sort)
                    )
            );
        });

        backButton.setOnAction(e ->
                new ViewEditBudgetScreen(user, null).show(stage)
        );

        /* =========================
           ROOT
           ========================= */
        BorderPane root = new BorderPane();
        root.setTop(controls);
        root.setCenter(table);
        root.setPadding(new Insets(10));

        stage.setScene(new Scene(root, 620, 450));
        stage.setTitle("View Government Budget");
        stage.show();
    }
}
