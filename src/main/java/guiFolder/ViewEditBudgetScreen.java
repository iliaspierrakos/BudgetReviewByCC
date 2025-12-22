package guiFolder;

import UserManagement.User;
import UserManagement.UserManager;
import UserFeatures.*;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * GUI equivalent of CLI ViewEditBudget.
 * 
 * - Loads all necessary data ONCE
 * - Shows available actions based on user role
 * - Routes user to the correct feature screen
 */
public class ViewEditBudgetScreen {

    private final User user;
    private final UserManager userManager;

    /** ensures data is loaded only once */
    private static boolean initialized = false;

    public ViewEditBudgetScreen(User user, UserManager userManager) {
        this.user = user;
        this.userManager = userManager;
        initializeDataIfNeeded();
    }

    /**
     * Equivalent to the "initial setup" part of CLI ViewEditBudget.
     * Runs only once per application lifecycle.
     */
    private void initializeDataIfNeeded() {
        if (initialized) return;

        MinistriesBudgets budg = new MinistriesBudgets();
        Ministries min = new Ministries();

        for (int year = 2020; year <= 2026; year++) {
            budg.budget(java.nio.file.Path.of(
                    "NecessaryFilesAndData/BudgetReview" + year + ".txt"));
        }

        min.minlist();

        for (int year = 2020; year <= 2026; year++) {
            CreatingMinistries.ministryCreation(
                    java.nio.file.Path.of(
                            "NecessaryFilesAndData/MinistriesBudgets" + year + ".csv"));
        }

        initialized = true;
    }

    /**
     * Displays the feature-selection menu (GUI version of CLI menu).
     */
    public void show(Stage stage) {

        Label title = new Label("BUDGET MANAGEMENT");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Label roleLabel = new Label("Role: " + user.getRole());
        roleLabel.setStyle("-fx-font-size: 14px;");

        Button viewButton = new Button("View Budget");
        Button editButton = new Button();
        Button historyButton = new Button("Edit History");
        Button compareButton = new Button("Compare");
        Button taxButton = new Button("Tax Receipt");
        Button logoutButton = new Button("Logout");

        viewButton.setMinWidth(240);
        editButton.setMinWidth(240);
        historyButton.setMinWidth(240);
        compareButton.setMinWidth(240);
        taxButton.setMinWidth(240);
        logoutButton.setMinWidth(240);

        // --- VIEW (all roles) ---
        viewButton.setOnAction(e ->
                new ViewScreen(user, userManager).show(stage)
        );

        // --- ROLE-BASED EDIT ---
        switch (user.getRole()) {

            case CITIZEN -> {
                editButton.setText("Virtual Edit");
                editButton.setOnAction(e ->
                        new Edit().collectData()
                );
            }

            case MINISTRYMEMBER -> {
                editButton.setText("Propose Edit");
                editButton.setOnAction(e ->
                        new Propose().editProposal(user.getUsername())
                );
            }

            case GOVERNOR -> {
                editButton.setText("Edit Budget");
                editButton.setOnAction(e ->
                        new Edit().collectData()
                );
            }
        }

        // --- HISTORY ---
        historyButton.setOnAction(e ->
                new EditHistory().showHistory()
        );

        // --- COMPARE ---
        compareButton.setOnAction(e ->
                Compare.comparingMinistries()
        );

        // --- TAX RECEIPT ---
        taxButton.setOnAction(e ->
                new TaxReceiptVisualizer().show()
        );

        // --- LOGOUT ---
        logoutButton.setOnAction(e ->
                new StartMenuScreen(userManager).show(stage)
        );

        VBox layout = new VBox(
                12,
                title,
                roleLabel,
                viewButton,
                editButton,
                historyButton,
                compareButton,
                taxButton,
                logoutButton
        );

        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        stage.setScene(new Scene(layout, 460, 560));
        stage.setTitle("Features");
        stage.show();
    }
}
