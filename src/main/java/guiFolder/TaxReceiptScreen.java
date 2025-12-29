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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * TaxReceiptScreen
 * 
 * GUI implementation of the Personal Tax Receipt feature.
 * Allows citizens to see how their tax money is distributed across ministries.
 */
public class TaxReceiptScreen {

    private final User user;
    private final UserManager userManager;

    public TaxReceiptScreen(User user, UserManager userManager) {
        this.user = user;
        this.userManager = userManager;
    }

    public void show(Stage stage) {

        Label title = new Label("PERSONAL TAX RECEIPT");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // ===== Input Fields =====
        TextField incomeField = new TextField();
        incomeField.setPromptText("Annual Income (e.g. 25000)");

        Spinner<Integer> kidsSpinner = new Spinner<>(0, 20, 0);
        kidsSpinner.setEditable(true);

        Spinner<Integer> ageSpinner = new Spinner<>(18, 120, 25);
        ageSpinner.setEditable(true);

        Button generateBtn = new Button("Generate");
        Button backBtn = new Button("Back");

        Label summaryLabel = new Label();
        summaryLabel.setStyle("-fx-text-fill: #1e3a5f; -fx-font-weight: bold;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        // ===== Form Layout =====
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10));
        form.addRow(0, new Label("Income (€):"), incomeField);
        form.addRow(1, new Label("Children:"), kidsSpinner);
        form.addRow(2, new Label("Age:"), ageSpinner);

        HBox actions = new HBox(10, generateBtn, backBtn);
        actions.setAlignment(Pos.CENTER_LEFT);
        form.add(actions, 0, 3, 2, 1);

        // ===== Table =====
        TableView<TaxRow> table = new TableView<>();
        table.setPlaceholder(new Label("No receipt generated"));

        TableColumn<TaxRow, String> ministryCol = new TableColumn<>("Ministry");
        ministryCol.setCellValueFactory(new PropertyValueFactory<>("ministry"));
        ministryCol.setPrefWidth(420);

        TableColumn<TaxRow, String> shareCol = new TableColumn<>("Your Share (€)");
        shareCol.setCellValueFactory(new PropertyValueFactory<>("shareText"));
        shareCol.setPrefWidth(160);

        table.getColumns().addAll(ministryCol, shareCol);

        // ===== Generate Button Action =====
        generateBtn.setOnAction(e -> {
            errorLabel.setText("");
            summaryLabel.setText("");
            table.getItems().clear();

            // Validate income
            double income;
            try {
                String raw = incomeField.getText() == null ? "" : incomeField.getText().trim();
                if (raw.isEmpty()) throw new NumberFormatException();
                income = Double.parseDouble(raw);
                if (income < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                errorLabel.setText("Please enter a valid positive income number.");
                return;
            }

            int kids = kidsSpinner.getValue();
            int age = ageSpinner.getValue();

            // Validate age
            if (age < 18) {
                errorLabel.setText("You must be at least 18 years old.");
                return;
            }

            try {
                // Calculate tax using backend logic
                double tax = calculateTax(income, kids, age);

                summaryLabel.setText(
                    "Income: " + Ministry.getFormattedBudget(income) + " € | Estimated Tax: " + 
                    Ministry.getFormattedBudget(tax) + " €"
                );

                // Calculate distribution
                List<TaxRow> rows = calculateDistribution(tax);
                table.setItems(FXCollections.observableArrayList(rows));

            } catch (Exception ex) {
                errorLabel.setText("Error calculating tax: " + ex.getMessage());
            }
        });

        backBtn.setOnAction(e ->
            new ViewEditBudgetScreen(user, userManager).show(stage)
        );

        // ===== Layout =====
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

    /**
     * Calculates tax based on Greek tax brackets.
     * Logic copied from TaxReceiptVisualizer.
     */
    private double calculateTax(double income, int kids, int age) {
        double tax = 0;

        // Bracket 1: 0 - 10,000
        double rate1;
        if (kids >= 4) {
            rate1 = 0;
        } else {
            rate1 = 0.09;
        }
        if (age <= 25 || (age <= 30 && kids >= 4)) {
            rate1 = 0;
        }
        if (income <= 10000) {
            tax += income * rate1;
        } else {
            tax += 10000 * rate1;
        }

        // Bracket 2: 10,001 - 20,000
        if (income > 10000) {
            double rate2;
            rate2 = switch (kids) {
                case 0 -> 0.20;
                case 1 -> 0.18;
                case 2 -> 0.16;
                case 3 -> 0.09;
                default -> 0;
            };
            if (age <= 25 || (age <= 30 && kids >= 4)) {
                rate2 = 0;
            } else if (age <= 30) {
                rate2 = 0.09;
            }

            if (income <= 20000) {
                tax += (income - 10000) * rate2;
            } else {
                tax += 10000 * rate2;
            }
        }

        // Bracket 3: 20,001 - 30,000
        if (income > 20000) {
            double rate3 = switch (kids) {
                case 0 -> 0.26;
                case 1 -> 0.24;
                case 2 -> 0.22;
                case 3 -> 0.20;
                default -> 0.18;
            };

            if (income <= 30000) {
                tax += (income - 20000) * rate3;
            } else {
                tax += 10000 * rate3;
            }
        }

        // Bracket 4: 30,001 - 40,000
        if (income > 30000) {
            if (income <= 40000) {
                tax += (income - 30000) * 0.34;
            } else {
                tax += 10000 * 0.34;
            }
        }

        // Bracket 5: 40,001 - 60,000
        if (income > 40000) {
            if (income <= 60000) {
                tax += (income - 40000) * 0.39;
            } else {
                tax += 20000 * 0.39;
            }
        }

        // Bracket 6: 60,001+
        if (income > 60000) {
            tax += (income - 60000) * 0.44;
        }

        return tax;
    }

    /**
     * Calculates how the tax is distributed across ministries.
     */
    private List<TaxRow> calculateDistribution(double tax) {
        List<TaxRow> rows = new ArrayList<>();

        // Calculate total government budget
        double totalGovBudget = 0;
        for (Ministry m : CreatingMinistries.ministries2026) {
            if (m != null) {
                totalGovBudget += m.getBudget();
            }
        }

        if (totalGovBudget == 0) {
            throw new RuntimeException("Total Government Budget is zero.");
        }

        // Calculate each ministry's share
        for (Ministry m : CreatingMinistries.ministries2026) {
            if (m != null) {
                double percentage = m.getBudget() / totalGovBudget;
                double myContribution = tax * percentage;

                // Only show if contribution > 0.01€
                if (myContribution > 0.01) {
                    rows.add(new TaxRow(
                        m.getMinistryName(),
                        String.format("%.2f", myContribution)
                    ));
                }
            }
        }

        // Add total row
        rows.add(new TaxRow("TOTAL TAX PAID", String.format("%.2f", tax)));

        return rows;
    }

    /**
     * Table row model for JavaFX TableView
     */
    public static class TaxRow {
        private final String ministry;
        private final String shareText;

        public TaxRow(String ministry, String shareText) {
            this.ministry = ministry;
            this.shareText = shareText;
        }

        public String getMinistry() {
            return ministry;
        }

        public String getShareText() {
            return shareText;
        }
    }
}