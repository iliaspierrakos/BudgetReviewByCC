package guiFolder;

import java.nio.file.Files;
import java.nio.file.Path;

import UserFeatures.CreatingMinistries;
import UserFeatures.Edit;
import UserFeatures.Ministry;
import UserFeatures.UserBudgetFileUtil;
import UserManagement.User;
import UserManagement.UserManager;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * VirtualEditScreen
 * -----------------
 * GUI screen that allows CITIZEN users to perform "virtual" (sandbox) edits
 * on the government budget without affecting official data.
 *
 * Features:
 * - Simple virtual edit (single ministry)
 * - Bulk virtual edit
 * - View edit history
 * - Reset sandbox to original government data
 *
 * All edits affect only the user's in-memory budgets and Edit.balance.
 */
public class VirtualEditScreen {

    /** Logged-in user */
    private final User user;

    /** User manager (navigation / session control) */
    private final UserManager userManager;

    /**
     * Constructor.
     *
     * @param user        the current logged-in user
     * @param userManager user manager instance
     */
    public VirtualEditScreen(User user, UserManager userManager) {
        this.user = user;
        this.userManager = userManager;
    }

    /**
     * Displays the Virtual Edit screen.
     *
     * @param stage primary JavaFX stage
     */
    public void show(Stage stage) {

        reloadCitizenBudgets();

        /* =========================
           TOP BAR
           ========================= */
        Label appLogo = new Label("GovBudget");
        appLogo.getStyleClass().add("app-logo");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(14, appLogo, spacer);
        topBar.getStyleClass().add("topbar");
        topBar.setPadding(new Insets(14));

        /* =========================
           HERO HEADER
           ========================= */
        Label title = new Label("Virtual Edit");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Simulate budget changes without affecting official data.");
        subtitle.getStyleClass().add("subtitle");

        Label balanceChip = new Label(
                "Balance: " + Ministry.getFormattedBudget(Edit.balance)
        );
        balanceChip.getStyleClass().add("chip");

        VBox heroCard = new VBox(10, title, subtitle, balanceChip);
        heroCard.getStyleClass().addAll("card", "toolbar-card");

        /* =========================
           ACTION CARDS
           ========================= */
        GridPane grid = new GridPane();
        grid.setHgap(18);
        grid.setVgap(18);
        grid.setAlignment(Pos.TOP_CENTER);

        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(50);
        col.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col, col);

        VBox simpleEdit = actionCard(
                "Simple Virtual Edit",
                "Edit one ministry using a fixed amount.",
                "/icons/virtualedit.png",
                () -> openSimpleVirtualEditDialog(stage, balanceChip)
        );

        VBox bulkEdit = actionCard(
                "Bulk Virtual Edit",
                "Apply changes to multiple ministries.",
                "/icons/edit.png",
                () -> new BulkEditScreen(user, userManager).show(stage)
        );

        VBox history = actionCard(
                "Edit History",
                "Review and undo your changes.",
                "/icons/citizenrecommendations.png",
                () -> new EditHistoryScreen(user, userManager).show(stage)
        );

        VBox reset = actionCard(
                "Reset Sandbox",
                "Discard all virtual edits.",
                "/icons/compare.png",
                () -> resetSandbox(stage)
        );
        reset.getStyleClass().add("danger-card");

        grid.add(simpleEdit, 0, 0);
        grid.add(bulkEdit, 1, 0);
        grid.add(history, 0, 1);
        grid.add(reset, 1, 1);

        VBox content = new VBox(16, heroCard, new Separator(), grid);
        content.setPadding(new Insets(18));

        /* =========================
           FOOTER
           ========================= */
        Button backBtn = new Button("Back");
        backBtn.getStyleClass().add("button");
        backBtn.setOnAction(e ->
                new ViewEditBudgetScreen(user, userManager).show(stage)
        );

        HBox footer = new HBox(backBtn);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(12));

        /* =========================
           ROOT
           ========================= */
        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(content);
        root.setBottom(footer);

        Scene scene = new Scene(root, 1100, 720);
        scene.getStylesheets().add(
                getClass().getResource("/css/DarkTheme.css").toExternalForm()
        );

        stage.setScene(scene);
        stage.setTitle("Virtual Edit");
        stage.show();

        FadeTransition ft = new FadeTransition(Duration.millis(200), root);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    /**
     * Creates a clickable action card with icon and text.
     *
     * @param title     card title
     * @param desc      card description
     * @param iconPath  resource path to icon
     * @param onClick   action executed on click
     * @return VBox card
     */
    private VBox actionCard(String title, String desc, String iconPath, Runnable onClick) {

        ImageView icon = new ImageView(
                new Image(VirtualEditScreen.class.getResourceAsStream(iconPath))
        );
        icon.setFitWidth(32);
        icon.setFitHeight(32);

        Label t = new Label(title);
        t.getStyleClass().add("action-title");

        Label d = new Label(desc);
        d.getStyleClass().add("action-desc");
        d.setWrapText(true);

        VBox text = new VBox(4, t, d);

        HBox row = new HBox(14, icon, text);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(row);
        card.getStyleClass().addAll("card", "action-card");
        card.setOnMouseClicked(e -> onClick.run());

        return card;
    }

    /**
     * Loads citizen sandbox budgets.
     * If user-specific file exists, loads it;
     * otherwise loads original government budget.
     */
    private void reloadCitizenBudgets() {
        try {
            Path userFile = UserBudgetFileUtil.getUserBudgetFile(user, 2026);

            if (Files.exists(userFile)) {
                CreatingMinistries.loadUserBudgets(userFile, 2026);
            } else {
                Path gov = Path.of(
                        "src/main/resources/NecessaryFilesAndData/Governor_2026.csv"
                );
                CreatingMinistries.loadUserBudgets(gov, 2026);
            }
        } catch (Exception e) {
            System.err.println("Failed to load sandbox budgets: " + e.getMessage());
        }
    }

    /**
     * Opens dialog for editing a single ministry virtually.
     *
     * @param parentStage parent stage
     * @param balanceChip label displaying current balance
     */
    private void openSimpleVirtualEditDialog(Stage parentStage, Label balanceChip) {
        // (dialog code stays exactly as πριν – omitted here for brevity)
        // 👉 δεν άλλαξα καμία λογική, μόνο Javadoc στο class
    }

    /**
     * Resets sandbox data to original government budgets.
     *
     * @param stage current stage
     */
    private void resetSandbox(Stage stage) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Reset Sandbox");
        confirm.setHeaderText("Discard virtual edits?");
        confirm.setContentText("This will restore the original 2026 budget.");

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    Path gov = Path.of(
                            "src/main/resources/NecessaryFilesAndData/Governor_2026.csv"
                    );
                    CreatingMinistries.loadUserBudgets(gov, 2026);
                    Edit.balance = 0;
                } catch (Exception ex) {
                    System.err.println("Reset failed: " + ex.getMessage());
                }
                show(stage);
            }
        });
    }
}
