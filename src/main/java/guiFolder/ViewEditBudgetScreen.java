package guiFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import UserFeatures.ClearHistory;
import UserFeatures.Edit;
import UserFeatures.EditHistoryList;
import UserFeatures.ViewEditBudgetInitializer;
import UserManagement.CurrentSession;
import UserManagement.User;
import UserManagement.UserManager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ViewEditBudgetScreen {

    private final User user;
    private final UserManager userManager;

    public ViewEditBudgetScreen(User user, UserManager userManager) {
        this.user = user;
        this.userManager = userManager;
    }

    public void show(Stage stage) {

        CurrentSession.setUser(user);
        ViewEditBudgetInitializer.ensureInitialized();

        // ===== Header card =====
        Label title = new Label("Welcome, " + user.getUsername());
        title.getStyleClass().add("title");

        Label subtitle = new Label("Choose an action to continue.");
        subtitle.getStyleClass().add("subtitle");

        Label roleBadge = new Label(roleText(user.getRole()));
        roleBadge.getStyleClass().addAll("badge", roleBadgeClass(user.getRole()));

        Region spacer = new Region();
        HBox headerTop = new HBox(12, title, spacer, roleBadge);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        headerTop.setAlignment(Pos.CENTER_LEFT);

        VBox headerCard = new VBox(10, headerTop, subtitle);
        headerCard.getStyleClass().addAll("card", "toolbar-card");

        // ===== Actions grid =====
        GridPane grid = new GridPane();
        grid.getStyleClass().add("action-grid");
        grid.setHgap(14);
        grid.setVgap(14);
        grid.setPadding(new Insets(6, 0, 0, 0));
        grid.setAlignment(Pos.TOP_CENTER);

        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(50);
        col.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col, col);

        int r = 0, c = 0;

        addToGrid(grid, actionCard(
                "View Budget",
                "Browse government budget tables.",
                () -> new ViewBudgetScreen(user, userManager).show(stage)
        ), r, c++);

        if (user.getRole() != User.Role.CITIZEN) {
            String txt = (user.getRole() == User.Role.MINISTRYMEMBER) ? "Propose Edit" : "Edit Budget";
            String desc = (user.getRole() == User.Role.MINISTRYMEMBER)
                    ? "Submit proposals for your ministry."
                    : "Edit budgets with governor permissions.";

            if (c > 1) { r++; c = 0; }
            addToGrid(grid, actionCard(txt, desc,
                    () -> new EditBudgetScreen(user, userManager).show(stage)
            ), r, c++);
        }

        if (user.getRole() == User.Role.CITIZEN) {
            if (c > 1) { r++; c = 0; }
            addToGrid(grid, actionCard(
                    "Virtual Edit",
                    "Simulate changes without affecting official data.",
                    () -> new VirtualEditScreen(user, userManager).show(stage)
            ), r, c++);
        }

        if (c > 1) { r++; c = 0; }
        addToGrid(grid, actionCard(
                "Compare Budgets",
                "Compare years and view differences.",
                () -> new CompareScreen(user, userManager).show(stage)
        ), r, c++);

        String recTitle;
        String recDesc;
        Runnable recAction;

        switch (user.getRole()) {
            case GOVERNOR -> {
                recTitle = "View Statistics";
                recDesc = "Review trends, totals and distributions.";
                recAction = () -> new ViewStatisticsScreen(user).show(stage);
            }
            case CITIZEN -> {
                recTitle = "Submit Recommendation";
                recDesc = "Send your proposal to the government.";
                recAction = () -> new SubmitRecommendationScreen(user).show(stage);
            }
            default -> {
                recTitle = "View Citizen Proposals";
                recDesc = "Review and evaluate citizen submissions.";
                recAction = () -> new ViewRecommendationsScreen(user, userManager).show(stage);
            }
        }

        if (c > 1) { r++; c = 0; }
        addToGrid(grid, actionCard(recTitle, recDesc, recAction), r, c++);

        if (user.getRole() == User.Role.CITIZEN) {
            if (c > 1) { r++; c = 0; }
            addToGrid(grid, actionCard(
                    "Tax Receipt",
                    "Generate and view your tax receipt.",
                    () -> new TaxReceiptScreen(user, userManager).show(stage)
            ), r, c++);
        }

        // ===== Footer =====
        javafx.scene.control.Button logoutBtn = new javafx.scene.control.Button("Logout");
        logoutBtn.getStyleClass().addAll("button", "danger");
        logoutBtn.setOnAction(e -> {
            cleanupOnLogout();
            new StartMenuScreen(userManager).show(stage);
        });

        HBox footer = new HBox(logoutBtn);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(14, 0, 0, 0));

        VBox center = new VBox(14, headerCard, new Separator(), grid, footer);
        center.setPadding(new Insets(18));

        BorderPane root = new BorderPane(center);

        Scene scene = new Scene(root, 920, 640);
        scene.getStylesheets().add(
                getClass().getResource("/css/DarkTheme.css").toExternalForm()
        );

        stage.setScene(scene);
        stage.setTitle("Main Menu");
        stage.show();
    }

    private static VBox actionCard(String title, String desc, Runnable onClick) {
        Label t = new Label(title);
        t.getStyleClass().add("action-title");

        Label d = new Label(desc);
        d.getStyleClass().add("action-desc");
        d.setWrapText(true);

        VBox text = new VBox(4, t, d);
        text.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(text);
        card.getStyleClass().addAll("card", "action-card", "image-card");
        card.setOnMouseClicked(e -> onClick.run());

        return card;
    }

    private static void addToGrid(GridPane grid, VBox node, int row, int col) {
        grid.add(node, col, row);
        GridPane.setFillWidth(node, true);
        GridPane.setFillHeight(node, true);
    }

    private static String roleText(User.Role role) {
        return switch (role) {
            case CITIZEN -> "CITIZEN";
            case MINISTRYMEMBER -> "MINISTRY MEMBER";
            case GOVERNOR -> "GOVERNOR";
        };
    }

    private static String roleBadgeClass(User.Role role) {
        return switch (role) {
            case CITIZEN -> "badge-citizen";
            case MINISTRYMEMBER -> "badge-ministry";
            case GOVERNOR -> "badge-governor";
        };
    }

    private void cleanupOnLogout() {
        try {
            ClearHistory.clearFile(Path.of("src/main/resources/NecessaryFilesAndData/edithistory.txt"));

            for (int year = 2020; year <= 2026; year++) {
                ClearHistory.clearFile(Path.of("src/main/resources/NecessaryFilesAndData/view" + year + ".txt"));
            }

            for (int year1 = 2020; year1 <= 2026; year1++) {
                for (int year2 = 2020; year2 <= 2026; year2++) {
                    Files.deleteIfExists(
                            Paths.get("src/main/resources/NecessaryFilesAndData/compare" + year1 + "with" + year2 + ".txt")
                    );
                }
            }

            Edit.balance = 0;
            Edit.history = new EditHistoryList();

        } catch (IOException ex) {
            System.err.println("Cleanup failed: " + ex.getMessage());
        }
    }

    @SuppressWarnings("unused")
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
