package guiFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        headerTop.setAlignment(Pos.CENTER_LEFT);

        VBox headerCard = new VBox(10, headerTop, subtitle);
        headerCard.getStyleClass().addAll("card", "toolbar-card");

        // ===== Actions as cards in a grid =====
        GridPane grid = new GridPane();
        grid.getStyleClass().add("action-grid");
        grid.setHgap(14);
        grid.setVgap(14);
        grid.setPadding(new Insets(6, 0, 0, 0));
        grid.setAlignment(Pos.TOP_CENTER);

        int r = 0, c = 0;

        // View Budget (all)
        Button viewBtn = makeActionButton("View Budget", "Browse government budget tables.");
        viewBtn.setOnAction(e -> new ViewBudgetScreen(user, userManager).show(stage));
        addToGrid(grid, wrapActionCard(viewBtn), r, c++);

        // Edit / Propose (not citizen)
        if (user.getRole() != User.Role.CITIZEN) {
            String txt = (user.getRole() == User.Role.MINISTRYMEMBER) ? "Propose Edit" : "Edit Budget";
            String desc = (user.getRole() == User.Role.MINISTRYMEMBER)
                    ? "Submit proposals for your ministry."
                    : "Edit budgets with governor permissions.";

            Button editBtn = makeActionButton(txt, desc);
            editBtn.setOnAction(e -> new EditBudgetScreen(user, userManager).show(stage));
            if (c > 1) { r++; c = 0; }
            addToGrid(grid, wrapActionCard(editBtn), r, c++);
        }

        // Virtual Edit (citizen)
        if (user.getRole() == User.Role.CITIZEN) {
            Button virtualBtn = makeActionButton("Virtual Edit", "Simulate changes without affecting official data.");
            virtualBtn.setOnAction(e -> new VirtualEditScreen(user, userManager).show(stage));
            if (c > 1) { r++; c = 0; }
            addToGrid(grid, wrapActionCard(virtualBtn), r, c++);
        }

        // Compare (all)
        Button compareBtn = makeActionButton("Compare Budgets", "Compare years and view differences.");
        compareBtn.setOnAction(e -> new CompareScreen(user, userManager).show(stage));
        if (c > 1) { r++; c = 0; }
        addToGrid(grid, wrapActionCard(compareBtn), r, c++);

        // Recommendations / Statistics (role-based)
        String recTitle;
        String recDesc;
        switch (user.getRole()) {
            case GOVERNOR -> {
                recTitle = "View Statistics";
                recDesc = "Review trends, totals and distributions.";
            }
            case CITIZEN -> {
                recTitle = "Submit Recommendation";
                recDesc = "Send your proposal to the government.";
            }
            default -> { // MINISTRYMEMBER
                recTitle = "View Citizen Proposals";
                recDesc = "Review and evaluate citizen submissions.";
            }
        }

        Button recBtn = makeActionButton(recTitle, recDesc);
        recBtn.setOnAction(e -> {
            switch (user.getRole()) {
                case CITIZEN -> new SubmitRecommendationScreen(user).show(stage);
                case MINISTRYMEMBER -> new ViewRecommendationsScreen(user, userManager).show(stage);
                case GOVERNOR -> new ViewStatisticsScreen(user).show(stage);
            }
        });
        if (c > 1) { r++; c = 0; }
        addToGrid(grid, wrapActionCard(recBtn), r, c++);

        // Tax Receipt (citizen only)
        if (user.getRole() == User.Role.CITIZEN) {
            Button taxBtn = makeActionButton("Tax Receipt", "Generate and view your tax receipt.");
            taxBtn.setOnAction(e -> new TaxReceiptScreen(user, userManager).show(stage));
            if (c > 1) { r++; c = 0; }
            addToGrid(grid, wrapActionCard(taxBtn), r, c++);
        }

        // ===== Footer actions =====
        Button logoutBtn = new Button("Logout");
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

    private static VBox wrapActionCard(Button btn) {
        VBox box = new VBox(10, btn);
        box.getStyleClass().addAll("card", "action-card");
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private static Button makeActionButton(String title, String desc) {
        // Using a button with multi-line text (title + description) gives “app card” feel.
        Button b = new Button(title + "\n" + desc);
        b.getStyleClass().addAll("button", "action-button");
        b.setMaxWidth(Double.MAX_VALUE);
        b.setWrapText(true);
        b.setAlignment(Pos.CENTER_LEFT);
        return b;
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

    private static VBox actionCardWithImage(String title, String desc, String imagePath, Runnable onClick) {
    ImageView img = new ImageView(new Image(
            ViewEditBudgetScreen.class.getResourceAsStream(imagePath)
    ));
    img.setFitWidth(56);
    img.setFitHeight(56);
    img.setPreserveRatio(true);

    Label t = new Label(title);
    t.getStyleClass().add("action-title");

    Label d = new Label(desc);
    d.getStyleClass().add("action-desc");
    d.setWrapText(true);

    VBox text = new VBox(4, t, d);

    HBox content = new HBox(12, img, text);
    content.setAlignment(Pos.CENTER_LEFT);

    VBox card = new VBox(content);
    card.getStyleClass().addAll("card", "action-card", "image-card");
    card.setOnMouseClicked(e -> onClick.run());

    return card;
}

}
