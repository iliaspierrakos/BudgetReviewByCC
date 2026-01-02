package guiFolder;

import UserFeatures.Compare;
import UserFeatures.Compare.CompareRow;
import UserManagement.User;
import UserManagement.UserManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * JavaFX screen for comparing ministry budgets between two years.
 * (UI improved – logic untouched)
 */
public class CompareScreen {

    private final User user;
    private final UserManager userManager;

    public CompareScreen(User user, UserManager userManager) {
        this.user = user;
        this.userManager = userManager;
    }

    public void show(Stage stage) {

        /* ================= TITLE ================= */
        Label title = new Label("COMPARE BUDGETS");
        title.getStyleClass().add("title");

        /* ================= CONTROLS ================= */
        ComboBox<Integer> firstYearBox = new ComboBox<>();
        ComboBox<Integer> secondYearBox = new ComboBox<>();

        firstYearBox.getItems().addAll(2020, 2021, 2022, 2023, 2024, 2025, 2026);
        secondYearBox.getItems().addAll(2020, 2021, 2022, 2023, 2024, 2025, 2026);

        firstYearBox.setValue(2025);
        secondYearBox.setValue(2026);

        Button compareButton = new Button("Compare");
        compareButton.getStyleClass().addAll("button", "primary");

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("button");

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error");

        GridPane controlsGrid = new GridPane();
        controlsGrid.setHgap(14);
        controlsGrid.setVgap(14);

        controlsGrid.addRow(0, new Label("First year:"), firstYearBox);
        controlsGrid.addRow(1, new Label("Second year:"), secondYearBox);

        HBox buttons = new HBox(12, compareButton, backButton);
        buttons.setAlignment(Pos.CENTER_LEFT);
        controlsGrid.add(buttons, 0, 2, 2, 1);

        VBox controlsCard = new VBox(14, title, controlsGrid, errorLabel);
        controlsCard.getStyleClass().add("card");
        controlsCard.setPadding(new Insets(22));
        controlsCard.setMinWidth(320);
        controlsCard.setMaxWidth(380);

        /* ================= TABLE ================= */
        TableView<CompareRow> table = new TableView<>();
        table.getStyleClass().add("budget-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<CompareRow, String> ministryCol =
                new TableColumn<>("Ministry");
        ministryCol.setCellValueFactory(
                new PropertyValueFactory<>("ministry")
        );

        TableColumn<CompareRow, String> firstCol =
                new TableColumn<>("First Year Budget");
        firstCol.setCellValueFactory(
                new PropertyValueFactory<>("firstYearBudget")
        );

        TableColumn<CompareRow, String> secondCol =
                new TableColumn<>("Second Year Budget");
        secondCol.setCellValueFactory(
                new PropertyValueFactory<>("secondYearBudget")
        );

        table.getColumns().addAll(ministryCol, firstCol, secondCol);

        VBox tableCard = new VBox(table);
        tableCard.getStyleClass().add("card");
        tableCard.setPadding(new Insets(12));

        HBox.setHgrow(tableCard, Priority.ALWAYS);

        /* ================= ACTIONS (UNCHANGED) ================= */
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
                new ViewEditBudgetScreen(user, userManager).show(stage)
        );

        /* ================= MAIN LAYOUT ================= */
        HBox mainContent = new HBox(22, controlsCard, tableCard);
        mainContent.setPadding(new Insets(26));
        mainContent.setAlignment(Pos.TOP_LEFT);

        BorderPane root = new BorderPane(mainContent);

        Scene scene = new Scene(root, 1200, 700);
        scene.getStylesheets().add(
                getClass().getResource("/css/DarkTheme.css").toExternalForm()
        );

        stage.setTitle("Compare Budgets");
        stage.setScene(scene);
        stage.show();
    }
}
