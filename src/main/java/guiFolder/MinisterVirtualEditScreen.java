package guiFolder;

import java.util.Arrays;

import UserFeatures.DraftEditSession;
import UserFeatures.DraftProposalExporter;
import UserFeatures.Ministry;
import UserManagement.CurrentSession;
import UserManagement.User;
import UserManagement.UserManager;
import javafx.animation.FadeTransition;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MinisterVirtualEditScreen {

    private final User user;
    private final UserManager userManager;

    public MinisterVirtualEditScreen(User user, UserManager userManager) {
        this.user = user;
        this.userManager = userManager;
    }

    public void show(Stage stage) {

        CurrentSession.setUser(user);

        // init draft session if not initialized
        if (!DraftEditSession.isInitialized()) {
            // consistent rule: start from 0 balance (reallocation-only)
            DraftEditSession.resetFromCurrent(0);
        }

        Label appLogo = new Label("BudgetReviewByCC");
        appLogo.getStyleClass().add("app-logo");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(14, appLogo, spacer);
        topBar.getStyleClass().add("topbar");
        topBar.setPadding(new Insets(14));

        Label title = new Label("Minister Draft Edit");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Create draft edits (no official changes) and submit to Prime Minister.");
        subtitle.getStyleClass().add("subtitle");

        Label balanceChip = new Label("Draft Balance: " + Ministry.getFormattedBudget(DraftEditSession.getDraftBalance()));
        balanceChip.getStyleClass().add("chip");

        Label editsChip = new Label("Draft edits: " + DraftEditSession.getHistory().size());
        editsChip.getStyleClass().add("chip");

        VBox heroCard = new VBox(10, title, subtitle, new HBox(10, balanceChip, editsChip));
        heroCard.getStyleClass().addAll("card", "toolbar-card", "hero-card", "virtual-hero");
        heroCard.setMaxWidth(860);

        GridPane grid = new GridPane();
        grid.getStyleClass().add("action-grid");
        grid.setHgap(14);
        grid.setVgap(14);
        grid.setAlignment(Pos.TOP_CENTER);

        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(50);
        col.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col, col);

        VBox simpleEdit = actionCard(
                "Simple Draft Edit",
                "Edit one ministry using a fixed amount (draft).",
                "/icons/wand.png",
                () -> openSimpleDraftEditDialog(stage, balanceChip, editsChip)
        );
        simpleEdit.getStyleClass().add("primary-action");

        VBox bulkEdit = actionCard(
                "Bulk Draft Edit",
                "Apply changes to multiple ministries (draft).",
                "/icons/bulk.png",
                () -> new BulkEditDraftScreen(user, userManager).show(stage)
        );
        bulkEdit.getStyleClass().add("primary-action");

        VBox history = actionCard(
                "Draft History",
                "Review and undo your draft changes.",
                "/icons/history.png",
                () -> new DraftHistoryScreen(user, userManager).show(stage)
        );
        history.getStyleClass().add("primary-action");

        VBox send = actionCard(
                "Send to Prime Minister",
                "Export draft proposal into ProposalsFromMinisters/.",
                "/icons/selected.png",
                () -> DraftProposalExporter.exportAndNotify(stage, user)
        );
        send.getStyleClass().add("primary-action");

        VBox reset = actionCard(
                "Reset Draft",
                "Discard all draft edits and restore from current official budgets.",
                "/icons/reset.png",
                () -> resetDraft(stage, balanceChip, editsChip)
        );
        reset.getStyleClass().addAll("danger-action");

        grid.add(simpleEdit, 0, 0);
        grid.add(bulkEdit,   1, 0);
        grid.add(history,    0, 1);
        grid.add(send,       1, 1);
        grid.add(reset,      0, 2);
        GridPane.setColumnSpan(reset, 2);

        VBox content = new VBox(16, heroCard, new Separator(), grid);
        content.setPadding(new Insets(18));
        content.getStyleClass().add("virtual-content");

        Button backBtn = new Button("Back");
        backBtn.getStyleClass().addAll("button", "subtle");
        backBtn.setOnAction(e -> new EditBudgetScreen(user, userManager).show(stage));

        HBox footer = new HBox(backBtn);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(12, 18, 18, 18));

        BorderPane root = new BorderPane();
        root.getStyleClass().add("virtual-edit-root");
        root.setTop(topBar);
        root.setCenter(content);
        root.setBottom(footer);

        Scene scene = new Scene(root,
                stage.getWidth() > 0 ? stage.getWidth() : 1100,
                stage.getHeight() > 0 ? stage.getHeight() : 760
        );

        var css = getClass().getResource("/css/DarkTheme.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());

        applyScenePreserveWindow(stage, scene, "Minister Draft Edit");

        FadeTransition ft = new FadeTransition(Duration.millis(200), root);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    private VBox actionCard(String title, String desc, String iconPath, Runnable onClick) {
        Node iconNode = safeIcon(iconPath, 34);

        Label t = new Label(title);
        t.getStyleClass().add("action-title");

        Label d = new Label(desc);
        d.getStyleClass().add("action-desc");
        d.setWrapText(true);

        VBox text = new VBox(5, t, d);

        Label chevron = new Label("›");
        chevron.getStyleClass().add("chevron");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(14, iconNode, text, spacer, chevron);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(row);
        card.getStyleClass().addAll("card", "action-card", "image-card");
        card.setMinHeight(118);

        card.setFocusTraversable(true);
        card.setOnMouseClicked(e -> onClick.run());
        card.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) onClick.run();
        });

        return card;
    }

    private Node safeIcon(String iconPath, double size) {
        try {
            var stream = MinisterVirtualEditScreen.class.getResourceAsStream(iconPath);
            if (stream == null) throw new IllegalStateException("Missing icon: " + iconPath);
            ImageView icon = new ImageView(new Image(stream));
            icon.setFitWidth(size);
            icon.setFitHeight(size);
            return icon;
        } catch (Exception ex) {
            Label fallback = new Label("⬤");
            fallback.getStyleClass().add("icon-fallback");
            return fallback;
        }
    }

    private void openSimpleDraftEditDialog(Stage parentStage, Label balanceChip, Label editsChip) {

        Stage dialog = new Stage();
        dialog.initOwner(parentStage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Simple Draft Edit");

        Label title = new Label("Edit Single Ministry (Draft)");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Select ministry, choose change type and enter amount (draft only).");
        subtitle.getStyleClass().add("subtitle");

        VBox header = new VBox(6, title, subtitle);

        ComboBox<String> ministryBox = new ComboBox<>();
        Arrays.stream(DraftEditSession.getSandbox())
                .filter(m -> m != null && m.getMinistryName() != null)
                .map(Ministry::getMinistryName)
                .sorted(String::compareToIgnoreCase)
                .forEach(ministryBox.getItems()::add);

        ministryBox.setPromptText("Select Ministry");
        ministryBox.setMaxWidth(Double.MAX_VALUE);

        Label currentBudgetLabel = new Label("Current Draft Budget: —");
        currentBudgetLabel.getStyleClass().add("subtitle");

        ToggleGroup changeGroup = new ToggleGroup();
        ToggleButton increaseBtn = new ToggleButton("Increase");
        ToggleButton decreaseBtn = new ToggleButton("Decrease");
        increaseBtn.setToggleGroup(changeGroup);
        decreaseBtn.setToggleGroup(changeGroup);
        increaseBtn.setSelected(true);

        HBox changeTypeBox = new HBox(10, increaseBtn, decreaseBtn);
        changeTypeBox.setAlignment(Pos.CENTER_LEFT);

        TextField amountField = new TextField();
        amountField.setPromptText("Amount (e.g., 1000000)");
        amountField.setMaxWidth(Double.MAX_VALUE);

        amountField.setTextFormatter(new TextFormatter<>(change -> {
            String t = change.getControlNewText().trim();
            if (t.isEmpty()) return change;
            if (t.matches("\\d+(\\.\\d{0,2})?")) return change;
            return null;
        }));

        Label balanceLabel = new Label("Draft Balance: " + Ministry.getFormattedBudget(DraftEditSession.getDraftBalance()));
        balanceLabel.getStyleClass().add("subtitle");

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error");
        errorLabel.setWrapText(true);

        Button applyBtn = new Button("Apply Draft");
        applyBtn.getStyleClass().addAll("button", "primary");
        applyBtn.setDisable(true);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().addAll("button", "subtle");
        cancelBtn.setOnAction(e -> dialog.close());

        HBox buttons = new HBox(10, cancelBtn, applyBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setMinWidth(130);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(c1, c2);

        form.addRow(0, new Label("Ministry:"), ministryBox);
        form.addRow(1, new Label(""), currentBudgetLabel);
        form.addRow(2, new Label("Change:"), changeTypeBox);
        form.addRow(3, new Label("Amount:"), amountField);
        form.addRow(4, new Label(""), balanceLabel);

        applyBtn.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> ministryBox.getValue() == null || amountField.getText().trim().isEmpty(),
                        ministryBox.valueProperty(),
                        amountField.textProperty()
                )
        );

        ministryBox.valueProperty().addListener((obs, o, n) -> {
            errorLabel.setText("");
            if (n != null) {
                int idx = DraftEditSession.findIndexByName(n);
                if (idx >= 0 && DraftEditSession.getSandbox()[idx] != null) {
                    double budget = DraftEditSession.getSandbox()[idx].getBudget();
                    currentBudgetLabel.setText("Current Draft Budget: " + Ministry.getFormattedBudget(budget));
                } else {
                    currentBudgetLabel.setText("Current Draft Budget: —");
                }
            } else {
                currentBudgetLabel.setText("Current Draft Budget: —");
            }
        });

        applyBtn.setOnAction(e -> {
            errorLabel.setText("");

            String ministry = ministryBox.getValue();
            if (ministry == null) { errorLabel.setText("Please select a ministry."); return; }

            double amount;
            try {
                amount = Double.parseDouble(amountField.getText().trim());
                if (amount <= 0) { errorLabel.setText("Amount must be positive."); return; }
            } catch (Exception ex) {
                errorLabel.setText("Invalid amount.");
                return;
            }

            boolean isIncrease = changeGroup.getSelectedToggle() == increaseBtn;

            String err = DraftEditSession.applyFixed(ministry, isIncrease, amount);
            if (err != null) { errorLabel.setText(err); return; }

            balanceChip.setText("Draft Balance: " + Ministry.getFormattedBudget(DraftEditSession.getDraftBalance()));
            editsChip.setText("Draft edits: " + DraftEditSession.getHistory().size());
            balanceLabel.setText("Draft Balance: " + Ministry.getFormattedBudget(DraftEditSession.getDraftBalance()));

            dialog.close();
        });

        VBox card = new VBox(12, header, new Separator(), form, errorLabel, buttons);
        card.getStyleClass().addAll("card", "toolbar-card");
        card.setPadding(new Insets(18));
        card.setMaxWidth(680);

        VBox root = new VBox(card);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(18));

        Scene scene = new Scene(root, 820, 560);
        var css = getClass().getResource("/css/DarkTheme.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());

        dialog.setScene(scene);
        dialog.centerOnScreen();
        dialog.show();
    }

    private void resetDraft(Stage stage, Label balanceChip, Label editsChip) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.initOwner(stage);
        confirm.initModality(Modality.WINDOW_MODAL);
        confirm.setTitle("Reset Draft");
        confirm.setHeaderText("Discard draft edits?");
        confirm.setContentText("This will restore draft sandbox from current loaded budgets.");

        var css = getClass().getResource("/css/DarkTheme.css");
        if (css != null) confirm.getDialogPane().getStylesheets().add(css.toExternalForm());

        confirm.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK) return;

            DraftEditSession.resetFromCurrent(0);
            balanceChip.setText("Draft Balance: " + Ministry.getFormattedBudget(DraftEditSession.getDraftBalance()));
            editsChip.setText("Draft edits: " + DraftEditSession.getHistory().size());

            show(stage);
        });
    }

    private static void applyScenePreserveWindow(Stage stage, Scene scene, String title) {
        boolean wasShowing = stage.isShowing();
        double x = stage.getX();
        double y = stage.getY();
        double w = stage.getWidth();
        double h = stage.getHeight();
        boolean max = stage.isMaximized();
        boolean fs = stage.isFullScreen();

        stage.setScene(scene);
        stage.setTitle(title);

        if (!wasShowing) {
            stage.show();
            stage.centerOnScreen();
            return;
        }

        stage.setMaximized(max);
        stage.setFullScreen(fs);

        if (!max && !fs && w > 0 && h > 0) {
            stage.setWidth(w);
            stage.setHeight(h);
            stage.setX(x);
            stage.setY(y);
        }
    }
}
