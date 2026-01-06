package guiFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import UserFeatures.ClearHistory;
import UserFeatures.Edit;
import UserFeatures.EditHistoryList;
import UserFeatures.ViewEditBudgetInitializer;
import UserManagement.CurrentSession;
import UserManagement.User;
import UserManagement.UserManager;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

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

        /* =========================
           WINDOW STATE SNAPSHOT (NO JUMP)
           ========================= */
        final boolean wasMax = stage.isMaximized();
        final boolean wasFull = stage.isFullScreen();
        final double prevW = stage.getWidth();
        final double prevH = stage.getHeight();
        final double prevX = stage.getX();
        final double prevY = stage.getY();

        /* =========================
           TOP APP BAR
           ========================= */
        Label appLogo = new Label("GovBudget");
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
           HERO HEADER
           ========================= */
        Label title = new Label("Welcome, " + user.getUsername());
        title.getStyleClass().add("title");

        Label subtitle = new Label("Choose an action to continue.");
        subtitle.getStyleClass().add("subtitle");

        Label roleBadge = new Label(roleText(user.getRole()));
        roleBadge.getStyleClass().addAll("badge", roleBadgeClass(user.getRole()), "badge-gold-border");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox headerTop = new HBox(12, title, spacer, roleBadge);
        headerTop.setAlignment(Pos.CENTER_LEFT);

        Label chip1 = new Label("2026 • Live Data");
        chip1.getStyleClass().add("chip");
        Label chip2 = new Label("Secure Session");
        chip2.getStyleClass().add("chip");
        Label chip3 = new Label("Role: " + roleText(user.getRole()));
        chip3.getStyleClass().add("chip");

        HBox chips = new HBox(10, chip1, chip2, chip3);
        chips.setAlignment(Pos.CENTER_LEFT);

        VBox headerCard = new VBox(10, headerTop, subtitle, chips);
        headerCard.getStyleClass().addAll("card", "toolbar-card", "hero-card", "gold-halo");
        headerCard.setMaxWidth(Double.MAX_VALUE);

        /* =========================
           FEATURED CARD (FULL WIDTH)
           ========================= */
        VBox featured = actionCard(
                "View Budget",
                "Browse government budget tables.",
                "/icons/chart.png",
                () -> new ViewBudgetScreen(user, userManager).show(stage),
                true
        );
        featured.setMaxWidth(Double.MAX_VALUE);

        /* =========================
           GRID OF ACTIONS (ROLE-BASED)
           ========================= */
        GridPane grid = new GridPane();
        grid.getStyleClass().add("action-grid");
        grid.setHgap(18);
        grid.setVgap(18);
        grid.setAlignment(Pos.TOP_CENTER);

        grid.getColumnConstraints().clear();
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(50);
        c1.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c1);

        List<VBox> cards = new ArrayList<>();

        if (user.getRole() != User.Role.CITIZEN) {
            String txt = (user.getRole() == User.Role.MINISTRYMEMBER) ? "Propose Edit" : "Edit Budget";
            String desc = (user.getRole() == User.Role.MINISTRYMEMBER)
                    ? "Submit proposals for your ministry."
                    : "Edit budgets with governor permissions.";

            cards.add(actionCard(
                    txt,
                    desc,
                    "/icons/edit.png",
                    () -> new EditBudgetScreen(user, userManager).show(stage),
                    false
            ));
        }

        if (user.getRole() == User.Role.CITIZEN) {
            cards.add(actionCard(
                    "Virtual Edit",
                    "Simulate changes without affecting official data.",
                    "/icons/wand.png",
                    () -> new VirtualEditScreen(user, userManager).show(stage),
                    false
            ));
        }

        cards.add(actionCard(
                "Compare Budgets",
                "Compare years and view differences.",
                "/icons/compare.png",
                () -> new CompareScreen(user, userManager).show(stage),
                false
        ));

        String recTitle, recDesc, recIcon;
        Runnable recAction;

        switch (user.getRole()) {
            case GOVERNOR -> {
                recTitle = "View Statistics";
                recDesc = "Review trends, totals and distributions.";
                recIcon = "/icons/stats.png";
                recAction = () -> new ViewStatisticsScreen(user).show(stage);
            }
            case CITIZEN -> {
                recTitle = "Submit Recommendation";
                recDesc = "Send your proposal to the government.";
                recIcon = "/icons/send.png";
                recAction = () -> new SubmitRecommendationScreen(user, userManager).show(stage);
            }
            default -> {
                recTitle = "View Citizen Proposals";
                recDesc = "Review and evaluate citizen submissions.";
                recIcon = "/icons/inbox.png";
                recAction = () -> new ViewRecommendationsScreen(user, userManager).show(stage);
            }
        }

        cards.add(actionCard(recTitle, recDesc, recIcon, recAction, false));

        if (user.getRole() == User.Role.CITIZEN) {
            cards.add(actionCard(
                    "Tax Receipt",
                    "Generate and view your tax receipt.",
                    "/icons/receipt.png",
                    () -> new TaxReceiptScreen(user, userManager).show(stage),
                    false
            ));
        }

        // Add cards into 2-column grid
        int r = 0, c = 0;
        for (VBox card : cards) {
            grid.add(card, c, r);
            GridPane.setFillWidth(card, true);
            GridPane.setFillHeight(card, true);
            c++;
            if (c > 1) { c = 0; r++; }
        }

        /* =========================
           LEFT CONTENT COLUMN
           ========================= */
        VBox leftContent = new VBox(14, headerCard, new Separator(), featured, grid);
        leftContent.setFillWidth(true);
        leftContent.setMaxWidth(700);
        grid.setMaxWidth(Double.MAX_VALUE);

        /* =========================
           RIGHT SIDE PANEL
           ========================= */
        VBox sidePanel = buildSidePanel(user);
        sidePanel.setMinWidth(280);
        sidePanel.setMaxWidth(280);

        /* =========================
           MAIN ROW
           ========================= */
        HBox mainRow = new HBox(18, leftContent, sidePanel);
        mainRow.setAlignment(Pos.TOP_CENTER);
        mainRow.setPadding(new Insets(18));
        HBox.setHgrow(leftContent, Priority.ALWAYS);

        /* =========================
           FOOTER (LOGOUT)
           ========================= */
        javafx.scene.control.Button logoutBtn = new javafx.scene.control.Button(" Logout");
        logoutBtn.getStyleClass().add("logout-pill");
        logoutBtn.setOnAction(e -> {
    try {
        cleanupOnLogout(); // μπορεί να αποτύχει (IO), δεν πρέπει να σταματήσει το logout
    } catch (Exception ex) {
        System.err.println("cleanupOnLogout failed: " + ex.getMessage());
        ex.printStackTrace();
    }

    try {
        // καθάρισε “logged in” state
        CurrentSession.setUser(null);

        if (userManager != null) {
            userManager.logout(); // θα την προσθέσεις στον UserManager
        }
    } catch (Exception ex) {
        System.err.println("Session/UserManager logout failed: " + ex.getMessage());
        ex.printStackTrace();
    }

    // ΠΑΝΤΑ γύρνα StartMenu
    new StartMenuScreen(userManager).show(stage);
});


        HBox footer = new HBox(logoutBtn);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(14, 18, 14, 18));
        footer.getStyleClass().add("footer-bar");

        /* =========================
           ROOT
           ========================= */
        BorderPane root = new BorderPane();
        root.getStyleClass().add("viewedit-root"); // scoped styling hook
        root.setTop(topBar);
        root.setCenter(mainRow);
        root.setBottom(footer);

        /* =========================
           SCENE (REUSE + NO JUMP)
           ========================= */
        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root);
            scene.getStylesheets().add(
                getClass().getResource("/css/DarkTheme.css").toExternalForm()
            );
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
            String css = getClass().getResource("/css/DarkTheme.css").toExternalForm();
            if (!scene.getStylesheets().contains(css)) scene.getStylesheets().add(css);
        }

        stage.setTitle("Main Menu");
        stage.show();

        // restore window state
        if (wasFull) {
            stage.setFullScreen(true);
        } else if (wasMax) {
            stage.setMaximized(true);
        } else {
            if (prevW > 0 && prevH > 0) {
                stage.setWidth(prevW);
                stage.setHeight(prevH);
                stage.setX(prevX);
                stage.setY(prevY);
            }
        }

        /* =========================
           ANIMATIONS (UNCHANGED)
           ========================= */
        FadeTransition screenFade = new FadeTransition(Duration.millis(220), root);
        screenFade.setFromValue(0);
        screenFade.setToValue(1);
        screenFade.play();

        List<Node> anim = new ArrayList<>();
        anim.add(featured);
        anim.addAll(grid.getChildren());
        anim.add(sidePanel);

        int delay = 0;
        for (Node node : anim) {
            node.setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(200), node);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.setDelay(Duration.millis(delay));
            ft.play();
            delay += 45;
        }
    }

    /* ========================= helpers ========================= */

    private static VBox buildSidePanel(User user) {
        Label t1 = new Label("Getting started");
        t1.getStyleClass().add("side-title");

        String lineA, lineB, lineC;

        if (user.getRole() == User.Role.CITIZEN) {
            lineA = "• Explore annual budget tables";
            lineB = "• Simulate changes with Virtual Edit";
            lineC = "• Submit a recommendation to the government";
        } else if (user.getRole() == User.Role.GOVERNOR) {
            lineA = "• Review totals and distributions";
            lineB = "• Compare years for trends";
            lineC = "• Approve and monitor official changes";
        } else {
            lineA = "• Propose edits for your ministry";
            lineB = "• Review citizen submissions";
            lineC = "• Compare and validate changes";
        }

        Label a = new Label(lineA);
        Label b = new Label(lineB);
        Label c = new Label(lineC);
        a.getStyleClass().add("side-text");
        b.getStyleClass().add("side-text");
        c.getStyleClass().add("side-text");

        Label t2 = new Label("Tips");
        t2.getStyleClass().add("side-title");

        Label tip1 = new Label("• Use Compare to see differences quickly");
        Label tip2 = new Label("• Your session is secured automatically");
        tip1.getStyleClass().add("side-text");
        tip2.getStyleClass().add("side-text");

        VBox card1 = new VBox(10, t1, a, b, c);
        card1.getStyleClass().addAll("card", "side-card");

        VBox card2 = new VBox(10, t2, tip1, tip2);
        card2.getStyleClass().addAll("card", "side-card");

        VBox side = new VBox(14, card1, card2);
        side.setAlignment(Pos.TOP_LEFT);
        side.getStyleClass().add("side-panel");

        return side;
    }

    private static VBox actionCard(String title, String desc, String iconPath, Runnable onClick, boolean featured) {

        ImageView icon = new ImageView(new Image(
                ViewEditBudgetScreen.class.getResourceAsStream(iconPath)
        ));
        icon.setFitWidth(34);
        icon.setFitHeight(34);
        icon.getStyleClass().add("action-icon");

        VBox iconBadge = new VBox(icon);
        iconBadge.setAlignment(Pos.CENTER);
        iconBadge.getStyleClass().add("icon-badge");
        if (featured) {
        iconBadge.getStyleClass().add("icon-badge-gold");
        }

        Label t = new Label(title);
        t.getStyleClass().add("action-title");

        Label d = new Label(desc);
        d.getStyleClass().add("action-desc");
        d.setWrapText(true);

        VBox text = new VBox(5, t, d);
        text.setAlignment(Pos.CENTER_LEFT);

        Label chevron = new Label("›");
        chevron.getStyleClass().addAll("chevron", "chevron-gold");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(14, iconBadge, text, spacer, chevron);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(row);
        card.getStyleClass().addAll("card", "action-card");
        if (featured) {
        card.getStyleClass().add("featured-card");
        }

        card.setOnMouseClicked(e -> onClick.run());
        return card;
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
