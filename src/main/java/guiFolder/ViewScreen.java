package guiFolder;

import java.lang.classfile.Label;
import java.util.List;

import javax.swing.table.TableColumn;
import javax.swing.text.TableView;

import UserFeatures.View;
import UserFeatures.View.GovBudgetRow;
import UserManagement.User;
import UserManagement.UserManager;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main.java.guiFolder.ViewEditBudgetScreen;

public class ViewScreen {

    private final User loggedInUser;
    private final UserManager userManager;
    private final View viewBackend = new View();

    public ViewScreen(User loggedInUser, UserManager userManager) {
        this.loggedInUser = loggedInUser;
        this.userManager = userManager;
    }

    public void show(Stage stage) {

        Label title = new Label("VIEW GOVERNMENT BUDGET");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        ComboBox<Integer> yearBox = new ComboBox<>(
                FXCollections.observableArrayList(2020, 2021, 2022, 2023, 2024, 2025, 2026)
        );
        yearBox.setPromptText("Select year");

        CheckBox sortBox = new CheckBox("Sort by budget");

        Button loadButton = new Button("Load");
        loadButton.setDisable(true);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        TableView<GovBudgetRow> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Select a year to view the budget"));

        TableColumn<GovBudgetRow, String> ministryCol = new TableColumn<>("Ministry");
        ministryCol.setCellValueFactory(new PropertyValueFactory<>("ministry"));

        TableColumn<GovBudgetRow, String> budgetCol = new TableColumn<>("Budget");
        budgetCol.setCellValueFactory(new PropertyValueFactory<>("budgetText"));
        budgetCol.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<GovBudgetRow, String> percentCol = new TableColumn<>("Percentage");
        percentCol.setCellValueFactory(new PropertyValueFactory<>("percentText"));
        percentCol.setStyle("-fx-alignment: CENTER-RIGHT;");

        table.getColumns().addAll(ministryCol, budgetCol, percentCol);

        Runnable reload = () -> {
            errorLabel.setText("");
            Integer year = yearBox.getValue();

            if (year == null) {
                table.getItems().clear();
                return;
            }

            List<GovBudgetRow> rows = viewBackend.getGovBudgetRowsForGui(year, sortBox.isSelected());
            table.getItems().setAll(rows);

            title.setText("VIEW GOVERNMENT BUDGET — " + year);

            if (rows.isEmpty()) {
                errorLabel.setText("No data available for year " + year);
            }
        };

        yearBox.valueProperty().addListener((obs, o, n) -> {
            loadButton.setDisable(n == null);
            reload.run();
        });

        sortBox.selectedProperty().addListener((obs, o, n) -> reload.run());
        loadButton.setOnAction(e -> reload.run());

        Button backButton = new Button("Back");
        backButton.setOnAction(e ->
        new ViewEditBudgetScreen(loggedInUser, userManager).show(stage)
        );


        HBox controls = new HBox(10, yearBox, sortBox, loadButton);
        controls.setAlignment(Pos.CENTER);

        VBox layout = new VBox(12, title, controls, table, errorLabel, backButton);
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setPadding(new Insets(15));

        stage.setScene(new Scene(layout, 800, 520));
        stage.setTitle("View Budget");
        stage.show();
    }
}
