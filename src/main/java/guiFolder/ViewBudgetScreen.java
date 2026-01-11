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
import guiFolder.ViewBudgetScreen.GovBudgetRow;
import javafx.animation.FadeTransition;
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
import javafx.util.Duration;

public class ViewBudgetScreen {

    private final User user;
    private final UserManager userManager;

    public ViewBudgetScreen(User user, UserManager userManager) {
        this.user = user;
        this.userManager = userManager;
    }

    public void show(Stage stage) {

        // =========================
        // Window state snapshot (so it doesn't jump/resize)
        // =========================
        final boolean wasMaximized = stage.isMaximized();
        final boolean wasFullScreen = stage.isFullScreen();
        final double prevW = stage.getWidth();
        final double prevH = stage.getHeight();
        final double prevX = stage.getX();
        final double prevY = stage.getY();

        /* =========================
           TOP APP BAR
           ========================= */
        Label appLogo = new Label("BudgetReviewByCC");
        appLogo.getStyleClass().add("app-logo");

        Label bell = new Label("🔔");
        bell.getStyleClass().add("top-icon");

        Label settings = new Label("⚙");
        settings.getStyleClass().add("top-icon");

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        HBox topBar = new HBox(14, appLogo, topSpacer, bell, settings);
        topBar.getStyleClass().add("topbar");
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(14, 18, 14, 18));

        /* =========================
           HERO HEADER (Title + Chips)
           ========================= */
        Label title = new Label("View Government Budget");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Select a year and load the budget table.");
        subtitle.getStyleClass().add("subtitle");

        Label chip1 = new Label("Budgets • Official Data");
        chip1.getStyleClass().add("chip");

        Label chip2 = new Label("Role: " + user.getRole().name());
        chip2.getStyleClass().add("chip");

        Label chip3 = new Label("Tip: Sort for top ministries");
        chip3.getStyleClass().add("chip");

        HBox chips = new HBox(10, chip1, chip2, chip3);
        chips.setAlignment(Pos.CENTER_LEFT);

        VBox heroCard = new VBox(10, title, subtitle, chips);
        heroCard.getStyleClass().addAll("card", "toolbar-card", "hero-card");

        /* =========================
           CONTROLS CARD
           ========================= */
        ComboBox<Integer> yearBox = new ComboBox<>();
        yearBox.getItems().addAll(2020, 2021, 2022, 2023, 2024, 2025, 2026);
        yearBox.setValue(2026);

        CheckBox sortBox = new CheckBox("Sort by budget (descending)");

        Button loadButton = new Button("Load");
        loadButton.getStyleClass().addAll("button", "primary");
        loadButton.setMinWidth(110);

        Button backButton = new Button("Back");
        backButton.getStyleClass().addAll("button", "subtle");
        backButton.setMinWidth(110);

        Label yearLabel = new Label("Year:");
        yearLabel.getStyleClass().add("subtitle");

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

        VBox controlsCard = new VBox(10, new Label("Controls"), new Separator(), controlsRow);
        controlsCard.getStyleClass().addAll("card", "glass-card", "controls-card");

        // style the small heading label
        ((Label) controlsCard.getChildren().get(0)).getStyleClass().add("section-title");

        /* =========================
           TABLE (Card + Styling)
           ========================= */
        TableView<GovBudgetRow> table = new TableView<>();
        table.getStyleClass().addAll("budget-table");
        table.setPlaceholder(new Label("No data loaded. Select a year and click Load."));
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<GovBudgetRow, String> ministryCol = new TableColumn<>("Ministry");
        ministryCol.setCellValueFactory(new PropertyValueFactory<>("ministry"));
        ministryCol.setMinWidth(280);

        TableColumn<GovBudgetRow, String> budgetCol = new TableColumn<>("Budget");
        budgetCol.setCellValueFactory(new PropertyValueFactory<>("budgetText"));
        budgetCol.setMinWidth(160);

        TableColumn<GovBudgetRow, String> percentCol = new TableColumn<>("Percentage");
        percentCol.setCellValueFactory(new PropertyValueFactory<>("percentText"));
        percentCol.setMinWidth(140);

        table.getColumns().addAll(ministryCol, budgetCol, percentCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox tableCard = new VBox(10, new Label("Budget Table"), table);
        ((Label) tableCard.getChildren().get(0)).getStyleClass().add("section-title");
        tableCard.getStyleClass().addAll("card", "table-card");

        /* =========================
           RIGHT SIDE PANEL (fills empty space)
           ========================= */
        VBox sidePanel = buildSidePanel(user);
        sidePanel.setMinWidth(280);
        sidePanel.setMaxWidth(280);

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
           LAYOUT: left content + right panel
           ========================= */
        VBox leftContent = new VBox(14, heroCard, controlsCard, tableCard);
        leftContent.setMaxWidth(760);
        leftContent.setFillWidth(true);
        HBox.setHgrow(leftContent, Priority.ALWAYS);

        HBox mainRow = new HBox(18, leftContent, sidePanel);
        mainRow.setAlignment(Pos.TOP_CENTER);
        mainRow.setPadding(new Insets(18));

        /* =========================
           ROOT + SCENE
           ========================= */
        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(mainRow);

        // =========================
        // Scene handling WITHOUT jumping
        // =========================
        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root); // no fixed size
            scene.getStylesheets().add(
                    getClass().getResource("/css/DarkTheme.css").toExternalForm()
            );
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
            String css = getClass().getResource("/css/DarkTheme.css").toExternalForm();
            if (!scene.getStylesheets().contains(css)) {
                scene.getStylesheets().add(css);
            }
        }

        stage.setTitle("View Government Budget");
        stage.show();

        // Restore window state (fullscreen/max/normal) exactly
        if (wasFullScreen) {
            stage.setFullScreen(true);
        } else if (wasMaximized) {
            stage.setMaximized(true);
        } else {
            if (prevW > 0 && prevH > 0) {
                stage.setWidth(prevW);
                stage.setHeight(prevH);
                stage.setX(prevX);
                stage.setY(prevY);
            }
        }

        FadeTransition ft = new FadeTransition(Duration.millis(200), root);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    private static VBox buildSidePanel(User user) {
        Label t1 = new Label("Quick insights");
        t1.getStyleClass().add("side-title");

        Label l1 = new Label("• Choose a year to load budgets");
        Label l2 = new Label("• Sort to bring highest budgets first");
        Label l3 = new Label("• Citizens can view their edits for 2026");

        l1.getStyleClass().add("side-text");
        l2.getStyleClass().add("side-text");
        l3.getStyleClass().add("side-text");

        VBox card1 = new VBox(10, t1, l1, l2, l3);
        card1.getStyleClass().addAll("card", "side-card");

        Label t2 = new Label("What you’re viewing");
        t2.getStyleClass().add("side-title");

        String roleLine = switch (user.getRole()) {
            case CITIZEN -> "• You have read access (and personal edits for 2026)";
            case MINISTRYMEMBER -> "• You can propose edits for your ministry";
            case GOVERNOR -> "• You review official budgets and statistics";
        };

        Label l4 = new Label(roleLine);
        l4.getStyleClass().add("side-text");

        VBox card2 = new VBox(10, t2, l4);
        card2.getStyleClass().addAll("card", "side-card");

        VBox side = new VBox(14, card1, card2);
        side.setAlignment(Pos.TOP_LEFT);
        return side;
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
