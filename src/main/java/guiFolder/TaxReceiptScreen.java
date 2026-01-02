package guiFolder;

import java.util.ArrayList;
import java.util.List;

import UserFeatures.CreatingMinistries;
import UserFeatures.Ministry;
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

        /* ================= TITLE ================= */
        Label title = new Label("PERSONAL TAX RECEIPT");
        title.getStyleClass().add("title");

        /* ================= INPUTS ================= */
        TextField incomeField = new TextField();
        incomeField.setPromptText("Annual Income (e.g. 25000)");

        Spinner<Integer> kidsSpinner = new Spinner<>(0, 20, 0);
        kidsSpinner.setEditable(true);

        Spinner<Integer> ageSpinner = new Spinner<>(18, 120, 25);
        ageSpinner.setEditable(true);

        Button generateBtn = new Button("Generate");
        generateBtn.getStyleClass().addAll("button", "primary");

        Button backBtn = new Button("Back");
        backBtn.getStyleClass().add("button");

        Label summaryLabel = new Label();
        summaryLabel.getStyleClass().addAll("badge", "badge-ministry");
        summaryLabel.setVisible(false);

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error");

        /* ================= FORM ================= */
        GridPane form = new GridPane();
        form.setHgap(14);
        form.setVgap(14);

        form.addRow(0, new Label("Income (€):"), incomeField);
        form.addRow(1, new Label("Children:"), kidsSpinner);
        form.addRow(2, new Label("Age:"), ageSpinner);

        HBox actions = new HBox(12, generateBtn, backBtn);
        actions.setAlignment(Pos.CENTER_LEFT);
        form.add(actions, 0, 3, 2, 1);

        VBox formCard = new VBox(14, title, form, summaryLabel, errorLabel);
        formCard.getStyleClass().add("card");
        formCard.setPadding(new Insets(22));
        formCard.setMinWidth(360);
        formCard.setMaxWidth(420);

        /* ================= TABLE ================= */
        TableView<TaxRow> table = new TableView<>();
        table.getStyleClass().add("budget-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<TaxRow, String> ministryCol = new TableColumn<>("Ministry");
        ministryCol.setCellValueFactory(new PropertyValueFactory<>("ministry"));

        TableColumn<TaxRow, String> shareCol = new TableColumn<>("Your Share (€)");
        shareCol.setCellValueFactory(new PropertyValueFactory<>("shareText"));

        table.getColumns().addAll(ministryCol, shareCol);

        VBox tableCard = new VBox(table);
        tableCard.getStyleClass().add("card");
        tableCard.setPadding(new Insets(12));

        HBox.setHgrow(tableCard, Priority.ALWAYS);

        /* ================= MAIN CONTENT ================= */
        HBox mainContent = new HBox(22, formCard, tableCard);
        mainContent.setPadding(new Insets(26));
        mainContent.setAlignment(Pos.TOP_LEFT);

        /* ================= ACTIONS (UNCHANGED) ================= */
        generateBtn.setOnAction(e -> {
            errorLabel.setText("");
            summaryLabel.setVisible(false);
            table.getItems().clear();

            double income;
            try {
                String raw = incomeField.getText();
                if (raw == null || raw.trim().isEmpty()) throw new NumberFormatException();
                income = Double.parseDouble(raw);
                if (income < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                errorLabel.setText("Please enter a valid positive income number.");
                return;
            }

            int kids = kidsSpinner.getValue();
            int age = ageSpinner.getValue();

            if (age < 18) {
                errorLabel.setText("You must be at least 18 years old.");
                return;
            }

            try {
                double tax = calculateTax(income, kids, age);

                summaryLabel.setText(
                        "Income: " + Ministry.getFormattedBudget(income)
                                + " €  |  Estimated Tax: "
                                + Ministry.getFormattedBudget(tax) + " €"
                );
                summaryLabel.setVisible(true);

                table.setItems(FXCollections.observableArrayList(
                        calculateDistribution(tax)
                ));
            } catch (Exception ex) {
                errorLabel.setText("Error calculating tax: " + ex.getMessage());
            }
        });

        backBtn.setOnAction(e ->
                new ViewEditBudgetScreen(user, userManager).show(stage)
        );

        /* ================= ROOT ================= */
        BorderPane root = new BorderPane(mainContent);

        Scene scene = new Scene(root, 1200, 720);
        scene.getStylesheets().add(
                getClass().getResource("/css/DarkTheme.css").toExternalForm()
        );

        stage.setTitle("Tax Receipt");
        stage.setScene(scene);
        stage.show();
    }

    /* ================= BACKEND (UNCHANGED) ================= */

    private double calculateTax(double income, int kids, int age) {
        double tax = 0;

        double rate1 = (kids >= 4 || age <= 25 || (age <= 30 && kids >= 4)) ? 0 : 0.09;
        tax += Math.min(income, 10000) * rate1;

        if (income > 10000) {
            double rate2 = switch (kids) {
                case 0 -> 0.20;
                case 1 -> 0.18;
                case 2 -> 0.16;
                case 3 -> 0.09;
                default -> 0;
            };
            if (age <= 25) rate2 = 0;
            else if (age <= 30) rate2 = 0.09;
            tax += Math.min(income - 10000, 10000) * rate2;
        }

        if (income > 20000)
            tax += Math.min(income - 20000, 10000)
                    * switch (kids) {
                        case 0 -> 0.26;
                        case 1 -> 0.24;
                        case 2 -> 0.22;
                        case 3 -> 0.20;
                        default -> 0.18;
                    };

        if (income > 30000) tax += Math.min(income - 30000, 10000) * 0.34;
        if (income > 40000) tax += Math.min(income - 40000, 20000) * 0.39;
        if (income > 60000) tax += (income - 60000) * 0.44;

        return tax;
    }

    private List<TaxRow> calculateDistribution(double tax) {
        List<TaxRow> rows = new ArrayList<>();
        double totalGovBudget = 0;

        for (Ministry m : CreatingMinistries.ministries2026) {
            if (m != null) totalGovBudget += m.getBudget();
        }

        for (Ministry m : CreatingMinistries.ministries2026) {
            if (m != null) {
                double myContribution = tax * (m.getBudget() / totalGovBudget);
                if (myContribution > 0.01) {
                    rows.add(new TaxRow(
                            m.getMinistryName(),
                            String.format("%.2f", myContribution)
                    ));
                }
            }
        }

        rows.add(new TaxRow("TOTAL TAX PAID", String.format("%.2f", tax)));
        return rows;
    }

    public static class TaxRow {
        private final String ministry;
        private final String shareText;

        public TaxRow(String ministry, String shareText) {
            this.ministry = ministry;
            this.shareText = shareText;
        }

        public String getMinistry() { return ministry; }
        public String getShareText() { return shareText; }
    }
}
