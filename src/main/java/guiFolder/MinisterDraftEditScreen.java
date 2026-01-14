package guiFolder;

import UserFeatures.CreatingMinistries;
import UserFeatures.Edit;
import UserFeatures.Ministry;
import UserManagement.CurrentSession;
import UserManagement.MinistryMember;
import UserManagement.User;
import UserManagement.UserManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * MinisterDraftEditScreen
 *
 * Draft-only budget editing screen for Ministry Members.
 *
 * <p>
 * Behavior:
 * <ul>
 * <li>All edits are applied to an in-memory sandbox copy of budgets.</li>
 * <li>No official budgets or user files are modified.</li>
 * <li>Edits are stored in {@link Edit#history}.</li>
 * <li>The draft can be exported as a proposal file for Prime Minister review.</li>
 * </ul>
 *
 * <p>
 * The Prime Minister's decision becomes the new official state for all users.
 * </p>
 */
public class MinisterDraftEditScreen {

  private final User user;
  private final UserManager userManager;

  /** Sandbox copy of ministries (draft state) */
  private Ministry[] sandbox;

  public MinisterDraftEditScreen(User user, UserManager userManager) {
    this.user = user;
    this.userManager = userManager;
  }

  public void show(Stage stage) {

    if (!(user instanceof MinistryMember)) {
      Alert a = new Alert(Alert.AlertType.ERROR, "Access denied.");
      a.showAndWait();
      return;
    }

    CurrentSession.setUser(user);
    initSandbox();

    Label title = new Label("Draft Edit – Proposal Mode");
    title.getStyleClass().add("title");

    Label subtitle = new Label("Create draft budget edits. No official data is modified.\n"
        + "You may submit the draft to the Prime Minister for review.");
    subtitle.getStyleClass().add("subtitle");

    Label balanceChip = new Label("Draft Balance: " + Ministry.getFormattedBudget(Edit.balance));
    balanceChip.getStyleClass().add("chip");

    VBox hero = new VBox(8, title, subtitle, balanceChip);
    hero.getStyleClass().addAll("card", "toolbar-card");
    hero.setPadding(new Insets(16));

    Button simpleBtn = new Button("Simple Draft Edit");
    simpleBtn.setOnAction(e -> openSimpleDraftDialog(stage, balanceChip));

    Button bulkBtn = new Button("Bulk Draft Edit");
    bulkBtn.setOnAction(e -> new BulkEditScreen(user, userManager, true).show(stage));

    Button historyBtn = new Button("View Draft History");
    historyBtn.setOnAction(e -> new EditHistoryScreen(user, userManager).show(stage));

    Button sendBtn = new Button("Send Proposal to Prime Minister");
    sendBtn.getStyleClass().add("primary");
    sendBtn.setOnAction(e -> exportProposal(stage));

    Button backBtn = new Button("Back");
    backBtn.setOnAction(e -> new EditBudgetScreen(user, userManager).show(stage));

    VBox actions = new VBox(12, simpleBtn, bulkBtn, historyBtn, new Separator(), sendBtn, backBtn);
    actions.setAlignment(Pos.CENTER_LEFT);

    VBox content = new VBox(16, hero, actions);
    content.setPadding(new Insets(18));

    BorderPane root = new BorderPane(content);

    Scene scene = new Scene(root, stage.getWidth() > 0 ? stage.getWidth() : 900,
        stage.getHeight() > 0 ? stage.getHeight() : 600);

    var css = getClass().getResource("/css/DarkTheme.css");
    if (css != null) {
      scene.getStylesheets().add(css.toExternalForm());
    }
    stage.setScene(scene);
    stage.setTitle("Draft Edit (Proposal)");
    stage.show();

    FadeTransition ft = new FadeTransition(Duration.millis(180), root);
    ft.setFromValue(0);
    ft.setToValue(1);
    ft.play();
  }

  /**
   * Initializes sandbox budgets as a deep copy of official data.
   */
  private void initSandbox() {
    sandbox = new Ministry[CreatingMinistries.ministries2026.length];

    for (int i = 0; i < sandbox.length; i++) {
      Ministry m = CreatingMinistries.ministries2026[i];
      if (m == null) {
        continue;
      }

      // IMPORTANT: constructor must exist in your Ministry class
      sandbox[i] = new Ministry(m.getMinistryName(), m.getBudget());
    }

    CreatingMinistries.ministries2026 = sandbox;
    Edit.balance = 0;
    Edit.history.clear();
  }

  /**
   * Opens simple single-ministry draft edit dialog.
   */
  private void openSimpleDraftDialog(Stage parent, Label balanceChip) {

    Stage dialog = new Stage();
    dialog.initOwner(parent);
    dialog.initModality(Modality.WINDOW_MODAL);
    dialog.setTitle("Simple Draft Edit");

    ComboBox<String> ministryBox = new ComboBox<>();
    for (Ministry m : sandbox) {
      if (m != null) {
        ministryBox.getItems().add(m.getMinistryName());
      }
    }

    ToggleGroup tg = new ToggleGroup();
    ToggleButton inc = new ToggleButton("Increase");
    ToggleButton dec = new ToggleButton("Decrease");
    inc.setToggleGroup(tg);
    dec.setToggleGroup(tg);
    inc.setSelected(true);

    TextField amountField = new TextField();
    amountField.setPromptText("Amount");

    Label error = new Label();
    error.getStyleClass().add("error");

    Button apply = new Button("Apply");
    apply.setOnAction(e -> {
      try {
        String min = ministryBox.getValue();
        double amt = Double.parseDouble(amountField.getText());
        boolean isInc = tg.getSelectedToggle() == inc;

        Edit ed = new Edit(min, isInc ? "Increase" : "Decrease", amt, "fixed");
        Edit.history.addEdit(ed);
        Edit.applyEdit(ed, false, true);

        balanceChip.setText("Draft Balance: " + Ministry.getFormattedBudget(Edit.balance));
        dialog.close();
      } catch (Exception ex) {
        error.setText("Invalid input.");
      }
    });

    VBox box =
        new VBox(10, new Label("Ministry"), ministryBox, inc, dec, amountField, error, apply);
    box.setPadding(new Insets(18));

    dialog.setScene(new Scene(box, 360, 420));
    dialog.show();
  }

  /**
   * Exports the current draft edits to a proposal file.
   */
  private void exportProposal(Stage stage) {
    try {
      Path dir = Path.of("src/main/resources/NecessaryFilesAndData/ProposalsFromMinisters");
      Files.createDirectories(dir);

      String safeName = user.getUsername().replaceAll("\\W+", "");
      Path file = dir.resolve("MinistryOf" + safeName + "_" + System.currentTimeMillis() + ".txt");

      StringBuilder sb = new StringBuilder();
      sb.append("MINISTRY PROPOSAL\n");
      sb.append("From: ").append(user.getUsername()).append("\n");
      sb.append("Submitted: ").append(LocalDateTime.now()).append("\n\n");

      for (Edit e : Edit.history.getEditList()) {
        sb.append(e).append("\n");
      }

      Files.writeString(file, sb.toString());

      Alert ok = new Alert(Alert.AlertType.INFORMATION, "Proposal sent successfully.");
      ok.showAndWait();

      new EditBudgetScreen(user, userManager).show(stage);

    } catch (IOException ex) {
      Alert err = new Alert(Alert.AlertType.ERROR, "Failed to write proposal file.");
      err.showAndWait();
    }
  }
}
