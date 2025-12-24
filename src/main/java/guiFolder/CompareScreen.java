package guiFolder;

import UserFeatures.Compare;
import UserFeatures.Compare.CompareRow;
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

/**
 * JavaFX screen for comparing ministry budgets between two years.
 *
 * <p>This class belongs to the GUI layer and delegates all logic
 * to the {@link Compare} class.</p>
 */
public class CompareScreen {

    private final User user;

    public CompareScreen(User user) {
        this.user = user;
    }

    public void show(Stage stage) {

        /* =========================
           Top controls
           ========================= */
        ComboBox<Integer> firstYearBox = new ComboBox<>();
        ComboBox<Integer> secondYearBox = new ComboBox<>();
        firstYearBox.getItems().addAll(2020, 2021, 2022, 2023, 2024, 2025, 2026);
        secondYearBox.getItems().addAll(2020, 2021, 2022, 2023, 2024, 2025, 2026);

        firstYearBox.setValue(2025);
        secondYearBox.setValue(2026);

        Button compareButton = new Button("Compare");
        Button backButton = new Button("Back");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        HBox controls = new HBox(
                10,
                new Label("First year:"),
                firstYearBox,
                new Label("Second year:"),
                secondYearBox,
                compareButton,
                backButton
        );
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(10));

        /* =========================
           TableView
           ========================= */
        TableView<CompareRow> table = new TableView<>();

        TableColumn<CompareRow, String> ministryCol =
                new TableColumn<>("Ministry");
        ministryCol.setCellValueFactory(
                new PropertyValueFactory<>("ministry"));
        ministryCol.setPrefWidth(260);

        TableColumn<CompareRow, String> firstCol =
                new TableColumn<>("First Year Budget");
        firstCol.setCellValueFactory(
                new PropertyValueFactory<>("firstYearBudget"));
        firstCol.setPrefWidth(160);

        TableColumn<CompareRow, String> secondCol =
                new TableColumn<>("Second Year Budget");
        secondCol.setCellValueFactory(
                new PropertyValueFactory<>("secondYearBudget"));
        secondCol.setPrefWidth(160);

        table.getColumns().addAll(ministryCol, firstCol, secondCol);

        /* =========================
           Actions
           ========================= */
        compareButton.setOnAction(e -> {
            int y1 = firstYearBox.getValue();
            int y2 = secondYearBox.getValue();

            if (y1 == y2) {
                errorLabel.setText("Years must be different.");
                table.getItems().clear();
                return;
            }

            errorLabel.setText("");

            ObservableList<CompareRow> data =
                    FXCollections.observableArrayList(
                            Compare.getComparisonRowsForGui(y1, y2)
                    );

            table.setItems(data);
        });

        backButton.setOnAction(e ->
                new ViewEditBudgetScreen(user, null).show(stage)
        );

        /* =========================
           Root layout
           ========================= */
        BorderPane root = new BorderPane();
        root.setTop(controls);
        root.setCenter(table);
        root.setBottom(errorLabel);
        BorderPane.setAlignment(errorLabel, Pos.CENTER);
        BorderPane.setMargin(errorLabel, new Insets(5));

        stage.setScene(new Scene(root, 650, 450));
        stage.setTitle("Compare Budgets");
        stage.show();
    }
}
