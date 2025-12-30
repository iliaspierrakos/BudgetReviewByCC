package guiFolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import UserFeatures.CreatingMinistries;
import UserFeatures.Ministry;
import UserFeatures.UserBudgetFileUtil;
import UserFeatures.ViewGovernmentBudget;
import UserManagement.User;
import UserManagement.UserManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
 * <p>Uses ViewGovernmentBudget backend class.</p>
 */
public class ViewBudgetScreen {

    private final User user;
    private final UserManager userManager;

    public ViewBudgetScreen(User user, UserManager userManager) {
        this.user = user;
        this.userManager = userManager;
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

            // ✅ NEW: Load Personal vs Original for Citizens viewing 2026
            if (year == 2026 && user.getRole() == User.Role.CITIZEN) {
                Path userFile = UserBudgetFileUtil.getUserBudgetFile(user, 2026);
                
                if (Files.exists(userFile)) {
                    Alert loadChoice = new Alert(Alert.AlertType.CONFIRMATION);
                    loadChoice.setTitle("Load Budget");
                    loadChoice.setHeaderText("Which budget do you want to view?");
                    loadChoice.setContentText("Choose between the original government budget or your personal edits.");
                    
                    ButtonType originalBtn = new ButtonType("Original Budget");
                    ButtonType personalBtn = new ButtonType("My Edits");
                    ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                    
                    loadChoice.getButtonTypes().setAll(originalBtn, personalBtn, cancelBtn);
                    
                    loadChoice.showAndWait().ifPresent(response -> {
                        if (response == personalBtn) {
                            try {
                                CreatingMinistries.loadUserBudgets(userFile, 2026);
                            } catch (Exception ex) {
                                showError("Failed to load personal budget: " + ex.getMessage());
                            }
                        } else if (response == originalBtn) {
                            try {
                                Path govPath = Path.of("NecessaryFilesAndData/Governor_2026.csv");
                                CreatingMinistries.loadUserBudgets(govPath, 2026);
                            } catch (Exception ex) {
                                showError("Failed to load original budget: " + ex.getMessage());
                            }
                        }
                    });
                } else {
                    // No personal budget exists - load original
                    try {
                        Path govPath = Path.of("NecessaryFilesAndData/Governor_2026.csv");
                        CreatingMinistries.loadUserBudgets(govPath, 2026);
                    } catch (Exception ex) {
                        showError("Failed to load budget: " + ex.getMessage());
                    }
                }
            }

            // Build rows
            ObservableList<GovBudgetRow> rows = FXCollections.observableArrayList(
                getGovBudgetRows(year, sort)
            );

            table.setItems(rows);
        });

        backButton.setOnAction(e ->
                new ViewEditBudgetScreen(user, userManager).show(stage)
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

    /**
     * Builds rows for GUI display using ViewGovernmentBudget logic.
     */
    private List<GovBudgetRow> getGovBudgetRows(int year, boolean sort) {
        List<GovBudgetRow> rows = new ArrayList<>();

        Ministry[] selectedMinistries = ViewGovernmentBudget.ministryYear(year);
        if (selectedMinistries == null) return rows;

        // Clone if sorting (to avoid modifying original)
        if (sort) {
            selectedMinistries = selectedMinistries.clone();
            sortingBudgets(selectedMinistries);
        }

        double total = 0;
        for (Ministry m : selectedMinistries) {
            if (m != null) total += m.getBudget();
        }
        if (total == 0) return rows;

        for (Ministry m : selectedMinistries) {
            if (m == null) continue;

            double budget = m.getBudget();
            double percent = (budget / total) * 100.0;

            String formattedBudget = Ministry.getFormattedBudget(budget);
            String formattedPercent = String.format("%.2f%%", percent).replace(".", ",");

            rows.add(new GovBudgetRow(m.getMinistryName(), formattedBudget, formattedPercent));
        }

        rows.add(new GovBudgetRow("TOTAL", Ministry.getFormattedBudget(total), "100,00%"));
        return rows;
    }

    /**
     * Sorts ministries by budget (descending) and alphabetically if equal.
     */
    private void sortingBudgets(Ministry[] ministries) {
        java.util.Arrays.sort(ministries, (m1, m2) -> {
            if (m1 == null && m2 == null) return 0;
            if (m1 == null) return 1;
            if (m2 == null) return -1;
            
            int budgetCompare = Double.compare(m2.getBudget(), m1.getBudget());
            
            if (budgetCompare == 0) {
                return m1.getMinistryName().compareToIgnoreCase(m2.getMinistryName());
            }
            
            return budgetCompare;
        });
    }

    /**
     * Shows error alert
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Table row model for JavaFX TableView
     */
    public static class GovBudgetRow {
        private final String ministry;
        private final String budgetText;
        private final String percentText;

        public GovBudgetRow(String ministry, String budgetText, String percentText) {
            this.ministry = ministry;
            this.budgetText = budgetText;
            this.percentText = percentText;
        }

        public String getMinistry() {
            return ministry;
        }

        public String getBudgetText() {
            return budgetText;
        }

        public String getPercentText() {
            return percentText;
        }
    }
}