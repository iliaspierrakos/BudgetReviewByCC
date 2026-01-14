package guiFolder;

import UserFeatures.CreatingMinistries;
import UserFeatures.Edit;
import UserFeatures.Ministry;
import UserFeatures.UserBudgetFileUtil;
import UserFeatures.UserBudgetPersistence;
import UserManagement.CurrentSession;
import UserManagement.User;
import UserManagement.UserManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
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

public class VirtualEditScreen {

  private final User user;
  private final UserManager userManager;

  public VirtualEditScreen(User user, UserManager userManager) {
    this.user = user;
    this.userManager = userManager;
  }

  public void show(Stage stage) {

    CurrentSession.setUser(user);
    reloadCitizenBudgets();

    Label appLogo = new Label("BudgetReviewByCC");
    appLogo.getStyleClass().add("app-logo");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    HBox topBar = new HBox(14, appLogo, spacer);
    topBar.getStyleClass().add("topbar");
    topBar.setPadding(new Insets(14));

    Label title = new Label("Virtual Edit");
    title.getStyleClass().add("title");

    Label subtitle = new Label("Simulate budget changes without affecting official data.");
    subtitle.getStyleClass().add("subtitle");

    Label balanceChip = new Label("Balance: " + Ministry.getFormattedBudget(Edit.balance));
    balanceChip.getStyleClass().add("chip");

    VBox heroCard = new VBox(10, title, subtitle, balanceChip);
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

    VBox simpleEdit = actionCard("Simple Virtual Edit", "Edit one ministry using a fixed amount.",
        "/icons/wand.png", () -> openSimpleVirtualEditDialog(stage, balanceChip));
    simpleEdit.getStyleClass().add("primary-action");

    VBox bulkEdit = actionCard("Bulk Virtual Edit", "Apply changes to multiple ministries.",
        "/icons/bulk.png", () -> new BulkEditScreen(user, userManager).show(stage));
    bulkEdit.getStyleClass().add("primary-action"); // ⭐ make it the featured one

    VBox history = actionCard("Edit History", "Review and undo your changes.", "/icons/history.png",
        () -> new EditHistoryScreen(user, userManager).show(stage));
    history.getStyleClass().add("primary-action");

    VBox reset = actionCard("Reset", "Discard all virtual edits.", "/icons/reset.png",
        () -> resetSandbox(stage, balanceChip));
    reset.getStyleClass().addAll("danger-action"); // 🚨 danger look (override)

    grid.add(simpleEdit, 0, 0);
    grid.add(bulkEdit, 1, 0);
    grid.add(history, 0, 1);
    grid.add(reset, 1, 1);

    VBox content = new VBox(16, heroCard, new Separator(), grid);
    content.setPadding(new Insets(18));
    content.getStyleClass().add("virtual-content");

    Button backBtn = new Button("Back");
    backBtn.getStyleClass().addAll("button", "subtle");
    backBtn.setOnAction(e -> new ViewEditBudgetScreen(user, userManager).show(stage));

    HBox footer = new HBox(backBtn);
    footer.setAlignment(Pos.CENTER_LEFT);
    footer.setPadding(new Insets(12, 18, 18, 18));

    BorderPane root = new BorderPane();
    root.getStyleClass().add("virtual-edit-root"); // 🎬 scoped wow
    root.setTop(topBar);
    root.setCenter(content);
    root.setBottom(footer);

    Scene scene = new Scene(root, stage.getWidth() > 0 ? stage.getWidth() : 1100,
        stage.getHeight() > 0 ? stage.getHeight() : 720);

    var css = getClass().getResource("/css/DarkTheme.css");
    if (css != null) {
      scene.getStylesheets().add(css.toExternalForm());
    }

    applyScenePreserveWindow(stage, scene, "Virtual Edit");

    FadeTransition ft = new FadeTransition(Duration.millis(200), root);
    ft.setFromValue(0);
    ft.setToValue(1);
    ft.play();
  }

  private VBox actionCard(String title, String desc, String iconPath, Runnable onClick) {
    Node iconNode = safeIcon(iconPath, 34);
    iconNode.getStyleClass().add("action-icon"); // for glow tweaks

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
    card.getStyleClass().add("virtual-action"); // scoped hook
    card.setMinHeight(118);

    card.setFocusTraversable(true);
    card.setOnMouseClicked(e -> onClick.run());
    card.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) {
        onClick.run();
      }
    });

    return card;
  }

  private Node safeIcon(String iconPath, double size) {
    try {
      var stream = VirtualEditScreen.class.getResourceAsStream(iconPath);
      if (stream == null) {
        throw new IllegalStateException("Missing icon: " + iconPath);
      }
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

  private void reloadCitizenBudgets() {
    try {
      Path userFile = UserBudgetFileUtil.getUserBudgetFile(user, 2026);

      if (Files.exists(userFile)) {
        CreatingMinistries.loadUserBudgets(userFile, 2026);
      } else {
        Path gov = Path.of("src/main/resources/NecessaryFilesAndData/Governor_2026.csv");
        CreatingMinistries.loadUserBudgets(gov, 2026);

        Edit.balance = 0;
        UserBudgetPersistence.saveUserBudgets(user, CreatingMinistries.ministries2026, 2026);
      }
    } catch (Exception e) {
      System.err.println("Failed to load sandbox budgets: " + e.getMessage());
    }
  }

  private void openSimpleVirtualEditDialog(Stage parentStage, Label balanceChip) {

    Stage dialog = new Stage();
    dialog.initOwner(parentStage);
    dialog.initModality(Modality.WINDOW_MODAL);
    dialog.setTitle("Simple Virtual Edit");

    Label title = new Label("Edit Single Ministry (Virtual)");
    title.getStyleClass().add("title");

    Label subtitle = new Label("Select ministry, choose change type and enter amount.");
    subtitle.getStyleClass().add("subtitle");

    VBox header = new VBox(6, title, subtitle);

    ComboBox<String> ministryBox = new ComboBox<>();
    Arrays.stream(CreatingMinistries.ministries2026)
        .filter(m -> m != null && m.getMinistryName() != null).map(Ministry::getMinistryName)
        .sorted(String::compareToIgnoreCase).forEach(ministryBox.getItems()::add);

    ministryBox.setPromptText("Select Ministry");
    ministryBox.setMaxWidth(Double.MAX_VALUE);

    Label currentBudgetLabel = new Label("Current Budget: —");
    currentBudgetLabel.getStyleClass().add("subtitle");

    ToggleGroup changeGroup = new ToggleGroup();
    ToggleButton increaseBtn = new ToggleButton("Increase");
    ToggleButton decreaseBtn = new ToggleButton("Decrease");
    increaseBtn.setToggleGroup(changeGroup);
    decreaseBtn.setToggleGroup(changeGroup);
    increaseBtn.setSelected(true);

    increaseBtn.getStyleClass().addAll("segment-btn", "increase");
    decreaseBtn.getStyleClass().addAll("segment-btn", "decrease");

    HBox changeTypeBox = new HBox(10, increaseBtn, decreaseBtn);
    changeTypeBox.setAlignment(Pos.CENTER_LEFT);
    changeTypeBox.getStyleClass().add("segmented-box");

    TextField amountField = new TextField();
    amountField.setPromptText("Amount (e.g., 1000000)");
    amountField.setMaxWidth(Double.MAX_VALUE);

    amountField.setTextFormatter(new TextFormatter<>(change -> {
      String t = change.getControlNewText().trim();
      if (t.isEmpty()) {
        return change;
      }
      if (t.matches("\\d+(\\.\\d{0,2})?")) {
        return change;
      }
      return null;
    }));

    Label balanceLabel =
        new Label("Available Balance: " + Ministry.getFormattedBudget(Edit.balance));
    balanceLabel.getStyleClass().add("subtitle");

    Label errorLabel = new Label();
    errorLabel.getStyleClass().add("error");
    errorLabel.setWrapText(true);

    Button applyBtn = new Button("Apply");
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

    applyBtn.disableProperty()
        .bind(Bindings.createBooleanBinding(
            () -> ministryBox.getValue() == null || amountField.getText().trim().isEmpty(),
            ministryBox.valueProperty(), amountField.textProperty()));

    ministryBox.valueProperty().addListener((obs, o, n) -> {
      errorLabel.setText("");
      if (n != null) {
        double budget = Ministry.budgetSearchByName(n, CreatingMinistries.ministries2026);
        currentBudgetLabel.setText("Current Budget: " + Ministry.getFormattedBudget(budget));
      } else {
        currentBudgetLabel.setText("Current Budget: —");
      }
    });

    amountField.textProperty().addListener((obs, o, n) -> errorLabel.setText(""));

    applyBtn.setOnAction(e -> {
      errorLabel.setText("");

      String ministry = ministryBox.getValue();
      String amountStr = amountField.getText().trim();
      if (ministry == null) {
        errorLabel.setText("Please select a ministry.");
        return;
      }

      double amount;
      try {
        amount = Double.parseDouble(amountStr);
        if (amount <= 0) {
          errorLabel.setText("Amount must be positive.");
          return;
        }
      } catch (NumberFormatException ex) {
        errorLabel.setText("Invalid amount.");
        return;
      }

      boolean isIncrease = changeGroup.getSelectedToggle() == increaseBtn;
      String changeType = isIncrease ? "Increase" : "Decrease";

      double currentBudget =
          Ministry.budgetSearchByName(ministry, CreatingMinistries.ministries2026);

      if (!isIncrease && amount > currentBudget) {
        errorLabel.setText("Cannot decrease more than current budget.");
        return;
      }
      if (isIncrease && amount > Edit.balance) {
        errorLabel.setText("Insufficient balance.");
        return;
      }

      Edit editObj = new Edit(ministry, changeType, amount, "fixed");
      Edit.history.addEdit(editObj);
      Edit.applyEdit(editObj, false, false);

      UserBudgetPersistence.saveUserBudgets(user, CreatingMinistries.ministries2026, 2026);

      balanceChip.setText("Balance: " + Ministry.getFormattedBudget(Edit.balance));
      balanceLabel.setText("Available Balance: " + Ministry.getFormattedBudget(Edit.balance));

      double newBudget = Ministry.budgetSearchByName(ministry, CreatingMinistries.ministries2026);

      showThemedAlert(Alert.AlertType.INFORMATION, "Success", "Budget Updated Successfully",
          "Ministry: " + ministry + "\n" + "Action: " + changeType + " by "
              + Ministry.getFormattedBudget(amount) + "\n" + "New Budget: "
              + Ministry.getFormattedBudget(newBudget) + "\n" + "Available Balance: "
              + Ministry.getFormattedBudget(Edit.balance));

      dialog.close();
    });

    VBox card = new VBox(12, header, new Separator(), form, errorLabel, buttons);
    card.getStyleClass().addAll("card", "toolbar-card", "virtual-dialog-card");
    card.setPadding(new Insets(18));
    card.setMaxWidth(680);

    VBox root = new VBox(card);
    root.getStyleClass().add("virtual-dialog-root");
    root.setAlignment(Pos.CENTER);
    root.setPadding(new Insets(18));

    Scene scene = new Scene(root, 820, 560);
    var css = getClass().getResource("/css/DarkTheme.css");
    if (css != null) {
      scene.getStylesheets().add(css.toExternalForm());
    }

    dialog.setScene(scene);
    dialog.centerOnScreen();
    dialog.show();
  }

  private void resetSandbox(Stage stage, Label balanceChip) {
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.initOwner(stage);
    confirm.initModality(Modality.WINDOW_MODAL);
    confirm.setTitle("Reset Sandbox");
    confirm.setHeaderText("Discard virtual edits?");
    confirm.setContentText("This will restore the original 2026 budget and reset your balance.");

    var css = getClass().getResource("/css/DarkTheme.css");
    if (css != null) {
      confirm.getDialogPane().getStylesheets().add(css.toExternalForm());
    }

    confirm.showAndWait().ifPresent(btn -> {
      if (btn != ButtonType.OK) {
        return;
      }
      try {
        Path gov = Path.of("src/main/resources/NecessaryFilesAndData/Governor_2026.csv");
        CreatingMinistries.loadUserBudgets(gov, 2026);

        Edit.balance = 0;
        Edit.history.clear();

        UserBudgetPersistence.saveUserBudgets(user, CreatingMinistries.ministries2026, 2026);

        balanceChip.setText("Balance: " + Ministry.getFormattedBudget(Edit.balance));
      } catch (Exception ex) {
        System.err.println("Reset failed: " + ex.getMessage());
      }

      show(stage);
    });
  }

  private void showThemedAlert(Alert.AlertType type, String title, String header, String content) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(header);
    alert.setContentText(content);

    var css = getClass().getResource("/css/DarkTheme.css");
    if (css != null) {
      alert.getDialogPane().getStylesheets().add(css.toExternalForm());
    }

    alert.showAndWait();
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
