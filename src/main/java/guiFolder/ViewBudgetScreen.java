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
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ViewBudgetScreen {

    private final User user;
    private final UserManager userManager;

    public ViewBudgetScreen(User user, UserManager userManager) {
        this.user = user;
        this.userManager = userManager;
    }

    public void show(Stage stage) {

        /* =========================
           HEADER (Title + Controls Card)
           ========================= */
        Label title = new Label("View Government Budget");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Select a year and load the budget table.");
        subtitle.getStyleClass().add("subtitle");

        ComboBox<Integer> yearBox = new ComboBox<>();
        yearBox.getItems().addAll(2020, 2021, 2022, 2023, 2024, 2025, 2026);
        yearBox.setValue(2026);

        CheckBox sortBox = new CheckBox("Sort by budget (descending)");

        Button loadButton = new Button("Load");
        loadButton.getStyleClass().addAll("button", "primary");

        Button backButton = new Button("Back");
        backButton.getStyleClass().addAll("button", "subtle");

        // Make buttons nicer width
        loadButton.setMinWidth(110);
        backButton.setMinWidth(110);

        Label yearLabel = new Label("Year:");
        yearLabel.getStyleClass().add("subtitle");

        // Spacer so actions align to the right nicely
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox controlsRow = new HBox(
                12,
                yearLabel,
                yearBox,
                new Separator(),
                sortBox,
                spacer,
                loadButton,
                backButton
        );
        controlsRow.setAlignment(Pos.CENTER_LEFT);

        VBox headerCard = new VBox(10, title, subtitle, new Separator(), controlsRow);
        headerCard.getStyleClass().addAll("card", "toolbar-card");

        /* =========================
           TABLE (Card + Styling)
           ========================= */
        TableView<GovBudgetRow> table = new TableView<>();
        table.getStyleClass().add("budget-table");
        table.setPlaceholder(new Label("No data loaded"));

        TableColumn<GovBudgetRow, String> ministryCol = new TableColumn<>("Ministry");
        ministryCol.setCellValueFactory(new PropertyValueFactory<>("ministry"));
        ministryCol.setMinWidth(280);
        ministryCol.setPrefWidth(360);

        TableColumn<GovBudgetRow, String> budgetCol = new TableColumn<>("Budget");
        budgetCol.setCellValueFactory(new PropertyValueFactory<>("budgetText"));
        budgetCol.setMinWidth(150);
        budgetCol.setPrefWidth(170);

        TableColumn<GovBudgetRow, String> percentCol = new TableColumn<>("Percentage");
        percentCol.setCellValueFactory(new PropertyValueFactory<>("percentText"));
        percentCol.setMinWidth(130);
        percentCol.setPrefWidth(150);

        // Let columns resize nicely
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().addAll(ministryCol, budgetCol, percentCol);

        VBox tableCard = new VBox(table);
        tableCard.getStyleClass().addAll("card", "table-card");
        VBox.setVgrow(table, Priority.ALWAYS);

        /* =========================
           ACTIONS
           ========================= */
        loadButton.setOnAction(e -> {
            Integer year = yearBox.getValue();
            boolean sort = sortBox.isSelected();

            table.getItems().clear();

            // Load Personal vs Original for Citizens viewing 2026
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
                                Path govPath = Path.of("src/main/resources/NecessaryFilesAndData/Governor_2026.csv");
                                CreatingMinistries.loadUserBudgets(govPath, 2026);
                            } catch (Exception ex) {
                                showError("Failed to load original budget: " + ex.getMessage());
                            }
                        }
                    });
                } else {
                    // No personal budget exists - load original
                    try {
                        Path govPath = Path.of("src/main/resources/NecessaryFilesAndData/Governor_2026.csv");
                        CreatingMinistries.loadUserBudgets(govPath, 2026);
                    } catch (Exception ex) {
                        showError("Failed to load budget: " + ex.getMessage());
                    }
                }
            }

            ObservableList<GovBudgetRow> rows = FXCollections.observableArrayList(
                    getGovBudgetRows(year, sort)
            );
            table.setItems(rows);
        });

        backButton.setOnAction(e -> new ViewEditBudgetScreen(user, userManager).show(stage));

        /* =========================
           ROOT
           ========================= */
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(18));
        root.setTop(headerCard);
        root.setCenter(tableCard);
        BorderPane.setMargin(tableCard, new Insets(14, 0, 0, 0));

        Scene scene = new Scene(root, 880, 580);
        scene.getStylesheets().add(
                getClass().getResource("/css/DarkTheme.css").toExternalForm()
        );

        stage.setScene(scene);
        stage.setTitle("View Government Budget");
        stage.show();
    }

    private List<GovBudgetRow> getGovBudgetRows(int year, boolean sort) {
        List<GovBudgetRow> rows = new ArrayList<>();

        Ministry[] selectedMinistries = ViewGovernmentBudget.ministryYear(year);
        if (selectedMinistries == null) return rows;

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

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class GovBudgetRow {
        private final String ministry;
        private final String budgetText;
        private final String percentText;

        public GovBudgetRow(String ministry, String budgetText, String percentText) {
            this.ministry = ministry;
            this.budgetText = budgetText;
            this.percentText = percentText;
        }

        public String getMinistry() { return ministry; }
        public String getBudgetText() { return budgetText; }
        public String getPercentText() { return percentText; }
    }
}
