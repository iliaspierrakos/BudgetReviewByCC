// File: src/main/java/guiFolder/TaxReceiptScreen.java
package guiFolder;

import UserFeatures.TaxReceipt;
import UserFeatures.TaxReceipt.TaxRow;
import UserFeatures.TaxReceipt.TaxResult;
import UserFeatures.ViewEditBudget;
import UserManagement.User;
import UserManagement.UserManager;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class TaxReceiptScreen {

    private final User user;
    private final UserManager userManager;

    public TaxReceiptScreen(User user, UserManager userManager) {
        this.user = user;
        this.userManager = userManager;
    }

    public void show(Stage stage) {

        // ensure budgets loaded
        ViewEditBudget.ensureInitialized();

        Label title = new Label("PERSONAL TAX RECEIPT");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField incomeField = new TextField();
        incomeField.setPromptText("Annual Income (e.g. 25000)");

        Spinner<Integer> kidsSpinner = new Spinner<>(0, 20, 0);
        kidsSpinner.setEditable(true);

        Spinner<Integer> ageSpinner = new Spinner<>(18, 120, 25);
        ageSpinner.setEditable(true);

        Button generateBtn = new Button("Generate");
        Button backBtn = new Button("Back");

        Label summaryLabel = new Label();
        summaryLabel.setStyle("-fx-text-fill: #1e3a5f;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10));
        form.addRow(0, new Label("Income:"), incomeField);
        form.addRow(1, new Label("Children:"), kidsSpinner);
        form.addRow(2, new Label("Age:"), ageSpinner);

        HBox actions = new HBox(10, generateBtn, backBtn);
        actions.setAlignment(Pos.CENTER_LEFT);
        form.add(actions, 0, 3, 2, 1);

        TableView<TaxRow> table = new TableView<>();
        table.setPlaceholder(new Label("No receipt generated"));

        TableColumn<TaxRow, String> ministryCol = new TableColumn<>("Ministry");
        ministryCol.setCellValueFactory(new PropertyValueFactory<>("ministry"));
        ministryCol.setPrefWidth(420);

        TableColumn<TaxRow, String> shareCol = new TableColumn<>("Your Share (€)");
        shareCol.setCellValueFactory(new PropertyValueFactory<>("shareText"));
        shareCol.setPrefWidth(160);

        table.getColumns().addAll(ministryCol, shareCol);

        generateBtn.setOnAction(e -> {
            errorLabel.setText("");
            summaryLabel.setText("");
            table.getItems().clear();

            double income;
            try {
                String raw = incomeField.getText() == null ? "" : incomeField.getText().trim();
                if (raw.isEmpty()) throw new NumberFormatException();
                income = Double.parseDouble(raw);
            } catch (NumberFormatException ex) {
                errorLabel.setText("Please enter a valid income number.");
                return;
            }

            int kids = kidsSpinner.getValue();
            int age = ageSpinner.getValue();

            try {
                TaxResult result = TaxReceipt.generateForGui(income, kids, age);

                summaryLabel.setText(
                    "Income: " + result.getIncomeText() + " | Estimated Tax: " + result.getTaxText()
                );

                table.setItems(FXCollections.observableArrayList(result.getRows()));
            } catch (Exception ex) {
                errorLabel.setText(ex.getMessage());
            }
        });

        backBtn.setOnAction(e ->
            new ViewEditBudgetScreen(user, userManager).show(stage)
        );

        VBox top = new VBox(8, title, form, summaryLabel, errorLabel);
        top.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(table);
        BorderPane.setMargin(table, new Insets(10));

        stage.setScene(new Scene(root, 620, 520));
        stage.setTitle("Tax Receipt");
        stage.show();
    }
}
