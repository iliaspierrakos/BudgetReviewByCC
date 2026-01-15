package guiFolder;

import UserFeatures.CreatingMinistries;
import UserFeatures.Ministry;
import UserManagement.User;
import UserManagement.UserManager;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class TaxReceiptScreen {

  private final User user;
  private final UserManager userManager;

  public TaxReceiptScreen(User user, UserManager userManager) {
    this.user = user;
    this.userManager = userManager;
  }

  public void show(Stage stage) {

    /* ================= TITLE + SUBTITLE ================= */
    Label title = new Label("Personal Tax Receipt");
    title.getStyleClass().add("title");

    Label subtitle = new Label(
        "Enter your details to estimate tax and see how it distributes across ministries.");
    subtitle.getStyleClass().add("subtitle");
    subtitle.setWrapText(true);

    /* ================= INPUTS ================= */
    TextField incomeField = new TextField();
    incomeField.setPromptText("Annual Income (e.g. 25000)");
    incomeField.setMaxWidth(Double.MAX_VALUE);

    Spinner<Integer> kidsSpinner = new Spinner<>(0, 20, 0);
    kidsSpinner.setEditable(true);
    kidsSpinner.setMaxWidth(Double.MAX_VALUE);

    Spinner<Integer> ageSpinner = new Spinner<>(18, 120, 25);
    ageSpinner.setEditable(true);
    ageSpinner.setMaxWidth(Double.MAX_VALUE);

    Button generateBtn = new Button("Generate");
    generateBtn.getStyleClass().addAll("button", "primary");
    generateBtn.setDefaultButton(true);

    Button backBtn = new Button("Back");
    backBtn.getStyleClass().add("button");
    backBtn.setCancelButton(true);

    Label summaryLabel = new Label();
    summaryLabel.getStyleClass().addAll("badge", "badge-ministry");
    summaryLabel.setWrapText(true);
    summaryLabel.setVisible(false);

    Label errorLabel = new Label();
    errorLabel.getStyleClass().add("error");
    errorLabel.setWrapText(true);

    /* ================= FORM (clean field layout) ================= */
    GridPane form = new GridPane();
    form.setHgap(14);
    form.setVgap(10);

    ColumnConstraints c1 = new ColumnConstraints();
    c1.setMinWidth(110);
    c1.setHgrow(Priority.NEVER);

    ColumnConstraints c2 = new ColumnConstraints();
    c2.setHgrow(Priority.ALWAYS);

    form.getColumnConstraints().addAll(c1, c2);

    // Field labels (make them readable on dark)
    Label incomeLbl = new Label("Income (€)");
    incomeLbl.getStyleClass().add("subtitle");

    Label kidsLbl = new Label("Children");
    kidsLbl.getStyleClass().add("subtitle");

    Label ageLbl = new Label("Age");
    ageLbl.getStyleClass().add("subtitle");

    form.add(incomeLbl, 0, 0);
    form.add(incomeField, 1, 0);

    form.add(kidsLbl, 0, 1);
    form.add(kidsSpinner, 1, 1);

    form.add(ageLbl, 0, 2);
    form.add(ageSpinner, 1, 2);

    HBox actions = new HBox(12, generateBtn, backBtn);
    actions.setAlignment(Pos.CENTER_LEFT);

    VBox formCard = new VBox(14, title, subtitle, form, actions, summaryLabel, errorLabel);
    formCard.getStyleClass().add("card");
    formCard.setPadding(new Insets(22));
    formCard.setMinWidth(380);
    formCard.setMaxWidth(460);

    /* ================= TABLE ================= */
    TableView<TaxRow> table = new TableView<>();
    table.getStyleClass().addAll("table-view", "budget-table");
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    table.setPlaceholder(makeEmptyState("No results yet", "Fill in the form and press Generate."));

    TableColumn<TaxRow, String> ministryCol = new TableColumn<>("Ministry");
    ministryCol.setCellValueFactory(new PropertyValueFactory<>("ministry"));
    ministryCol.setMinWidth(260);

    TableColumn<TaxRow, String> shareCol = new TableColumn<>("Your Share (€)");
    shareCol.setCellValueFactory(new PropertyValueFactory<>("shareText"));
    shareCol.setMinWidth(180);

    table.getColumns().setAll(ministryCol, shareCol);

    Label tableTitle = new Label("Distribution");
    tableTitle.getStyleClass().add("section-title");

    Label tableHint = new Label(
        "Shows your estimated contribution per ministry based on the national budget split.");
    tableHint.getStyleClass().add("subtitle");
    tableHint.setWrapText(true);

    VBox tableHeader = new VBox(6, tableTitle, tableHint);

    VBox tableCard = new VBox(12, tableHeader, table);
    tableCard.getStyleClass().addAll("card", "table-card");
    tableCard.setPadding(new Insets(14));

    HBox.setHgrow(tableCard, Priority.ALWAYS);

    /* ================= TOP BAR ================= */
    Label appLogo = new Label("BudgetReview");
    appLogo.getStyleClass().add("app-logo");

    Label screenChip = new Label("Tax Receipt");
    screenChip.getStyleClass().add("chip");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    // simple "icon" labels (use ikonli if you want later)
    Label settings = new Label("⚙");
    settings.getStyleClass().add("top-icon");
    settings.setTooltip(new Tooltip("Settings"));

    Label info = new Label("ⓘ");
    info.getStyleClass().add("top-icon");
    info.setTooltip(new Tooltip("Info"));

    HBox topbar = new HBox(12, appLogo, screenChip, spacer, info, settings);
    topbar.getStyleClass().add("topbar");
    topbar.setPadding(new Insets(14, 20, 14, 20));
    topbar.setAlignment(Pos.CENTER_LEFT);

    /* ================= MAIN CONTENT (centered container) ================= */
    HBox mainRow = new HBox(22, formCard, tableCard);
    mainRow.setAlignment(Pos.TOP_CENTER);

    VBox content = new VBox(18, mainRow);
    content.setPadding(new Insets(26));
    content.setAlignment(Pos.TOP_CENTER);

    BorderPane root = new BorderPane();
    root.setTop(topbar);
    root.setCenter(content);

    Scene scene = new Scene(root, 1200, 720);
    scene.getStylesheets().add(getClass().getResource("/css/DarkTheme.css").toExternalForm());

    /* ================= ACTIONS (UNCHANGED LOGIC) ================= */
    generateBtn.setOnAction(e -> {
      errorLabel.setText("");
      summaryLabel.setVisible(false);
      table.getItems().clear();

      double income;
      try {
        String raw = incomeField.getText();
        if (raw == null || raw.trim().isEmpty()) {
          throw new NumberFormatException();
        }
        income = Double.parseDouble(raw);
        if (income < 0) {
          throw new NumberFormatException();
        }
      } catch (NumberFormatException ex) {
        errorLabel.setText("Please enter a valid positive income number.");
        return;
      }

      int kids = kidsSpinner.getValue();
      int age = ageSpinner.getValue();

      if (age < 18) {
        errorLabel.setText("You must be at least 18 years old.");
        return;
      }

      try {
        double tax = calculateTax(income, kids, age);

        summaryLabel.setText("Income: " + Ministry.getFormattedBudget(income)
            + " €   •   Estimated Tax: " + Ministry.getFormattedBudget(tax) + " €");
        summaryLabel.setVisible(true);

        table.setItems(FXCollections.observableArrayList(calculateDistribution(tax)));
      } catch (Exception ex) {
        errorLabel.setText("Error calculating tax: " + ex.getMessage());
      }
    });

    backBtn.setOnAction(e -> new ViewEditBudgetScreen(user, userManager).show(stage));

    stage.setTitle("Tax Receipt");
    stage.setScene(scene);
    stage.show();
  }

  /* ---------- helpers ---------- */

  private VBox makeEmptyState(String title, String subtitle) {
    Label t = new Label(title);
    t.getStyleClass().add("action-title");

    Label s = new Label(subtitle);
    s.getStyleClass().add("action-desc");
    s.setWrapText(true);

    VBox box = new VBox(6, t, s);
    box.setAlignment(Pos.CENTER);
    box.setPadding(new Insets(18));
    return box;
  }

  /* ================= BACKEND (UNCHANGED) ================= */

  private double calculateTax(double income, int kids, int age) {
    double tax = 0;

    double rate1 = (kids >= 4 || age <= 25 || (age <= 30 && kids >= 4)) ? 0 : 0.09;
    tax += Math.min(income, 10000) * rate1;

    if (income > 10000) {
      double rate2 = switch (kids) {
        case 0 -> 0.20;
        case 1 -> 0.18;
        case 2 -> 0.16;
        case 3 -> 0.09;
        default -> 0.0;
      };

      if (age <= 25) {
        rate2 = 0.0;
      } else if (age <= 30) {
        rate2 = 0.09;
      }

      tax += Math.min(income - 10000, 10000) * rate2;
    }


    if (income > 20000) {
      tax += Math.min(income - 20000, 10000) * switch (kids) {
        case 0 -> 0.26;
        case 1 -> 0.24;
        case 2 -> 0.22;
        case 3 -> 0.20;
        default -> 0.18;
      };
    }
    if (income > 30000) {
      tax += Math.min(income - 30000, 10000) * 0.34;
    }

    if (income > 40000) {
      tax += Math.min(income - 40000, 20000) * 0.39;
    }

    if (income > 60000) {
      tax += (income - 60000) * 0.44;
    }

    return tax;
  }

  private List<TaxRow> calculateDistribution(double tax) {
    List<TaxRow> rows = new ArrayList<>();
    double totalGovBudget = 0;

    for (Ministry m : CreatingMinistries.ministries2026) {
      if (m != null) {
        totalGovBudget += m.getBudget();
      }
    }

    for (Ministry m : CreatingMinistries.ministries2026) {
      if (m != null) {
        double myContribution = tax * (m.getBudget() / totalGovBudget);
        if (myContribution > 0.01) {
          rows.add(new TaxRow(m.getMinistryName(), String.format("%.2f", myContribution)));
        }
      }
    }

    rows.add(new TaxRow("TOTAL TAX PAID", String.format("%.2f", tax)));
    return rows;
  }

  public static class TaxRow {
    private final String ministry;
    private final String shareText;

    public TaxRow(String ministry, String shareText) {
      this.ministry = ministry;
      this.shareText = shareText;
    }

    public String getMinistry() {
      return ministry;
    }

    public String getShareText() {
      return shareText;
    }
  }
}
