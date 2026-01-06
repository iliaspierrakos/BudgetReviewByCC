package guiFolder;

import UserFeatures.Compare;
import UserFeatures.Compare.CompareRow;
import UserManagement.User;
import UserManagement.UserManager;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * JavaFX screen for comparing ministry budgets between two years.
 * (UI improved – logic untouched)
 *
 * ✔ Adds Δ column (Second - First) with robust parsing for EU/US formatted numbers.
 * ✔ Adds hero/header + more "dashboard" layout (pure UI).
 */
public class CompareScreen {

    private final User user;
    private final UserManager userManager;

    public CompareScreen(User user, UserManager userManager) {
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

        /* ================= TOP BAR ================= */
        Label appLogo = new Label("GovBudget");
        appLogo.getStyleClass().add("app-logo");

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        HBox topBar = new HBox(14, appLogo, topSpacer);
        topBar.getStyleClass().add("topbar");
        topBar.setPadding(new Insets(14, 18, 14, 18));

        /* ================= HERO ================= */
        Label title = new Label("Compare Budgets");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Compare ministry budgets between two years and view the delta.");
        subtitle.getStyleClass().add("subtitle");

        Label chipA = new Label("Tip: Pick two different years");
        chipA.getStyleClass().add("chip");

        HBox heroChips = new HBox(10, chipA);
        heroChips.setAlignment(Pos.CENTER_LEFT);

        VBox hero = new VBox(10, title, subtitle, heroChips);
        hero.getStyleClass().addAll("card", "toolbar-card", "hero-card", "compare-hero");
        hero.setMaxWidth(Double.MAX_VALUE);

        /* ================= CONTROLS ================= */
        ComboBox<Integer> firstYearBox = new ComboBox<>();
        ComboBox<Integer> secondYearBox = new ComboBox<>();

        firstYearBox.getItems().addAll(2020, 2021, 2022, 2023, 2024, 2025, 2026);
        secondYearBox.getItems().addAll(2020, 2021, 2022, 2023, 2024, 2025, 2026);

        firstYearBox.setValue(2025);
        secondYearBox.setValue(2026);

        Label firstYearLabel = new Label("First year");
        firstYearLabel.getStyleClass().add("field-label");

        Label secondYearLabel = new Label("Second year");
        secondYearLabel.getStyleClass().add("field-label");

        Button compareButton = new Button("Compare");
        compareButton.getStyleClass().addAll("button", "primary");
        compareButton.setTooltip(new Tooltip("Compare the selected years"));

        Button backButton = new Button("Back");
        backButton.getStyleClass().addAll("button", "subtle");

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error");

        GridPane controlsGrid = new GridPane();
        controlsGrid.getStyleClass().add("compare-controls-grid");
        controlsGrid.setHgap(14);
        controlsGrid.setVgap(12);

        controlsGrid.add(firstYearLabel, 0, 0);
        controlsGrid.add(firstYearBox, 1, 0);
        controlsGrid.add(secondYearLabel, 0, 1);
        controlsGrid.add(secondYearBox, 1, 1);

        HBox buttons = new HBox(12, backButton, compareButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        Label controlsTitle = new Label("Controls");
        controlsTitle.getStyleClass().add("section-title");

        VBox controlsCard = new VBox(14, controlsTitle, controlsGrid, errorLabel, buttons);
        controlsCard.getStyleClass().addAll("card", "compare-controls-card");
        controlsCard.setPadding(new Insets(18));
        controlsCard.setMinWidth(320);
        controlsCard.setMaxWidth(380);

        /* ================= TABLE ================= */
        TableView<CompareRow> table = new TableView<>();
        table.getStyleClass().addAll("table-view", "budget-table", "compare-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<CompareRow, String> ministryCol = new TableColumn<>("Ministry");
        ministryCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getMinistry()));

        TableColumn<CompareRow, String> firstCol = new TableColumn<>("First Year");
        firstCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getFirstYearBudget()));

        TableColumn<CompareRow, String> secondCol = new TableColumn<>("Second Year");
        secondCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getSecondYearBudget()));

        //  Δ = Second - First (robust parsing)
        TableColumn<CompareRow, String> deltaCol = new TableColumn<>("Deviation = Second Year − First Year");
        deltaCol.setCellValueFactory(cd -> {
            CompareRow r = cd.getValue();
            double a = parseBudgetToDouble(r.getFirstYearBudget());
            double b = parseBudgetToDouble(r.getSecondYearBudget());
            double d = b - a;
            return new SimpleStringProperty(formatDelta(d));
        });

        deltaCol.setCellFactory(col -> new TableCell<CompareRow, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    getStyleClass().removeAll("delta-pos", "delta-neg", "delta-zero");
                    return;
                }
                setText(item);

                getStyleClass().removeAll("delta-pos", "delta-neg", "delta-zero");
                double v = parseBudgetToDouble(item);

                if (v > 0) getStyleClass().add("delta-pos");
                else if (v < 0) getStyleClass().add("delta-neg");
                else getStyleClass().add("delta-zero");
            }
        });

        deltaCol.setMaxWidth(120);
        deltaCol.setMinWidth(110);

        table.getColumns().addAll(ministryCol, firstCol, secondCol, deltaCol);

        // Empty state
        Label empty = new Label("Select two years and press Compare.");
        empty.getStyleClass().add("subtitle");
        empty.setPadding(new Insets(20));
        table.setPlaceholder(empty);

        VBox tableCard = new VBox(table);
        tableCard.getStyleClass().addAll("card", "table-card", "compare-table-card");
        tableCard.setPadding(new Insets(12));
        HBox.setHgrow(tableCard, Priority.ALWAYS);

        /* ================= ACTIONS ================= */
        compareButton.setOnAction(e -> {
            Integer y1 = firstYearBox.getValue();
            Integer y2 = secondYearBox.getValue();

            if (y1 == null || y2 == null) {
                errorLabel.setText("Please select both years.");
                table.getItems().clear();
                return;
            }

            if (y1.equals(y2)) {
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
            chipA.setText("Comparing: " + y1 + " vs " + y2);
        });

        backButton.setOnAction(e ->
                new ViewEditBudgetScreen(user, userManager).show(stage)
        );

        /* ================= MAIN LAYOUT ================= */
        VBox left = new VBox(14, hero, new Separator(), controlsCard);
        left.setMaxWidth(400);

        HBox mainContent = new HBox(22, left, tableCard);
        mainContent.getStyleClass().add("compare-content");
        mainContent.setPadding(new Insets(18));
        mainContent.setAlignment(Pos.TOP_CENTER);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("compare-root");
        root.setTop(topBar);
        root.setCenter(mainContent);

        /* ================= SCENE HANDLING WITHOUT JUMP ================= */
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

        stage.setTitle("Compare Budgets");
        stage.show();

        // Restore window state
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
    }

    /* ================= Helpers (display only) ================= */

    private static double parseBudgetToDouble(String s) {
        if (s == null) return 0.0;

        // Keep digits, dot, comma, minus
        String cleaned = s.replaceAll("[^0-9,\\.\\-]", "").trim();
        if (cleaned.isEmpty() || cleaned.equals("-")) return 0.0;

        boolean hasDot = cleaned.contains(".");
        boolean hasComma = cleaned.contains(",");

        // EU: 1.234.567,89  => remove dots, comma->dot
        // US: 1,234,567.89  => remove commas
        if (hasDot && hasComma) {
            int lastDot = cleaned.lastIndexOf('.');
            int lastComma = cleaned.lastIndexOf(',');

            if (lastComma > lastDot) {
                cleaned = cleaned.replace(".", "");
                cleaned = cleaned.replace(",", ".");
            } else {
                cleaned = cleaned.replace(",", "");
            }
        } else if (hasComma) {
            // Only comma: decimal OR thousands
            String[] parts = cleaned.split(",");
            String last = parts[parts.length - 1];
            if (last.length() == 3) {
                cleaned = cleaned.replace(",", "");
            } else {
                cleaned = cleaned.replace(",", ".");
            }
        } else if (hasDot) {
            // Only dot: decimal OR thousands
            String[] parts = cleaned.split("\\.");
            String last = parts[parts.length - 1];
            if (last.length() == 3) {
                cleaned = cleaned.replace(".", "");
            }
        }

        try {
            return Double.parseDouble(cleaned);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private static String formatDelta(double v) {
        return (v > 0 ? "+" : "") + String.format("%,.2f", v);
    }
}
