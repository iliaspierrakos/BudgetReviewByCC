package guiFolder;

import UserManagement.User;
import UserManagement.UserManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Central features menu shown after successful login.
 * All users (Citizen, MinistryMember, Governor) can VIEW the budget.
 */
public class FeaturesScreen {

    private final User loggedInUser;
    private final UserManager userManager;

    public FeaturesScreen(User loggedInUser, UserManager userManager) {
        this.loggedInUser = loggedInUser;
        this.userManager = userManager;
    }

    public void show(Stage stage) {

        // ===== Title =====
        Label title = new Label("FEATURES");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Label roleLabel = new Label("Role: " + loggedInUser.getRole());
        roleLabel.setStyle("-fx-font-size: 14px;");

        // ===== Buttons =====
        Button viewBudgetBtn = new Button("VIEW BUDGET");
        Button virtualEditBtn = new Button("VIRTUAL EDIT");
        Button compareBtn = new Button("COMPARE");
        Button recommendationBtn = new Button("SUBMIT RECOMMENDATION");
        Button taxReceiptBtn = new Button("TAX RECEIPT");
        Button backBtn = new Button("LOGOUT");

        viewBudgetBtn.setMinWidth(240);
        taxReceiptBtn.setMinWidth(240);
        virtualEditBtn.setMinWidth(240);
        compareBtn.setMinWidth(240);
        recommendationBtn.setMinWidth(240);
        backBtn.setMinWidth(240);

       
        viewBudgetBtn.setOnAction(e ->
                new ViewBudgetScreen(loggedInUser, userManager).show(stage)
        );

       
        taxReceiptBtn.setOnAction(e ->
                showComingSoon(stage, "TAX RECEIPT")
        );

        virtualEditBtn.setOnAction(e ->
                showComingSoon(stage, "VIRTUAL EDIT")
        );

        compareBtn.setOnAction(e ->
                showComingSoon(stage, "COMPARE")
        );

        recommendationBtn.setOnAction(e ->
                showComingSoon(stage, "SUBMIT RECOMMENDATION")
        );

        backBtn.setOnAction(e ->
                new StartMenuScreen(userManager).show(stage)
        );

       
        VBox layout = new VBox(
                15,
                title,
                roleLabel,
                viewBudgetBtn,
                virtualEditBtn,
                compareBtn,
                recommendationBtn,
                taxReceiptBtn,
                backBtn
        );

        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(25));

        stage.setScene(new Scene(layout, 440, 480));
        stage.setTitle("Features");
        stage.show();
    }


    private void showComingSoon(Stage stage, String featureName) {
        Label label = new Label(featureName + " — coming soon");
        label.setStyle("-fx-font-size: 18px;");

        Button back = new Button("Back");
        back.setOnAction(e -> show(stage));

        VBox box = new VBox(20, label, back);
        box.setAlignment(Pos.CENTER);

        stage.setScene(new Scene(box, 400, 250));
    }
}
