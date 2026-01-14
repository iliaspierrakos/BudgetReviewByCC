package guiFolder;

import UserFeatures.Edit;
import UserFeatures.Ministry;
import UserManagement.CurrentSession;
import UserManagement.User;
import UserManagement.UserManager;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * <h1>GovernorCheckScreen</h1>
 *
 * <p>
 * JavaFX inbox screen for the Governor/Prime Minister to review minister proposals stored under
 * {@code ProposalsFromMinisters/}. A proposal is displayed, parsed into draft edits, and can be
 * either accepted (applied to the official budgets) or rejected (deleted without applying).
 * </p>
 *
 * <h2>Proposal format compatibility</h2>
 * <ul>
 * <li>Human-readable lines under {@code Draft edits:} (legacy/loose parsing).</li>
 * <li>Machine-readable lines in exact format:
 * {@code EDIT|<ministry>|<Increase/Decrease>|<amount>|<fixed/percent>}</li>
 * </ul>
 *
 * <h2>Important parsing rule</h2>
 * <p>
 * If a proposal file contains any {@code EDIT|...} lines, this screen will parse and use
 * <b>only</b> those machine-readable edits. The human-readable draft section will be ignored to
 * prevent duplicate entries in the UI table.
 * </p>
 */
public class GovernorCheckScreen {

  /**
   * The proposals directory (classpath/resources location used by the exporter).
   */
  private static final Path PROPOSALS_DIR =
      Paths.get("src/main/resources/NecessaryFilesAndData/ProposalsFromMinisters");

  private final User user;
  private final UserManager userManager;

  /**
   * Creates a new Governor proposals inbox screen.
   *
   * @param user the active user
   * @param userManager application user manager
   */
  public GovernorCheckScreen(User user, UserManager userManager) {
    this.user = user;
    this.userManager = userManager;
  }

  /**
   * Builds and displays the proposals inbox on the given stage.
   *
   * <p>
   * The screen lists proposals, shows a parsed preview with edits and reasoning, and allows
   * accepting or rejecting a proposal.
   * </p>
   *
   * @param stage the primary application stage
   */
  public void show(Stage stage) {
    CurrentSession.setUser(user);

    final boolean wasMaximized = stage.isMaximized();
    final boolean wasFullScreen = stage.isFullScreen();
    final double prevW = stage.getWidth();
    final double prevH = stage.getHeight();
    final double prevX = stage.getX();
    final double prevY = stage.getY();

    Label appLogo = new Label("BudgetReviewByCC");
    appLogo.getStyleClass().add("app-logo");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    HBox topBar = new HBox(14, appLogo, spacer);
    topBar.getStyleClass().add("topbar");
    topBar.setAlignment(Pos.CENTER_LEFT);
    topBar.setPadding(new Insets(14, 18, 14, 18));

    Label title = new Label("Governor • Proposals Inbox");
    title.getStyleClass().add("title");

    Label subtitle = new Label("Review and approve the latest proposals from ministers.");
    subtitle.getStyleClass().add("subtitle");

    VBox heroCard = new VBox(10, title, subtitle);
    heroCard.getStyleClass().addAll("card", "toolbar-card", "hero-card");
    heroCard.setMaxWidth(Double.MAX_VALUE);
    heroCard.setMinHeight(Region.USE_PREF_SIZE);

    TableView<FileRow> filesTable = new TableView<>();
    filesTable.getStyleClass().addAll("budget-table");
    filesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    filesTable.setFixedCellSize(44);
    filesTable.setMinHeight(0);
    filesTable.setMaxHeight(Double.MAX_VALUE);

    TableColumn<FileRow, String> colMinistry = new TableColumn<>("Ministry");
    colMinistry
        .setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getMinistryDisplay()));

    TableColumn<FileRow, String> colFrom = new TableColumn<>("From");
    colFrom.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getFromDisplay()));
    colFrom.setStyle("-fx-alignment: CENTER;");

    TableColumn<FileRow, String> colDate = new TableColumn<>("Submitted");
    colDate.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getSubmittedText()));
    colDate.setStyle("-fx-alignment: CENTER-RIGHT;");

    filesTable.getColumns().addAll(colMinistry, colFrom, colDate);

    Button refreshBtn = new Button("Refresh");
    refreshBtn.getStyleClass().addAll("button", "subtle");

    Label leftTitle = new Label("Proposals");
    leftTitle.getStyleClass().add("section-title");

    Region leftSpacer = new Region();
    HBox.setHgrow(leftSpacer, Priority.ALWAYS);

    HBox leftHeader = new HBox(10, leftTitle, leftSpacer, refreshBtn);
    leftHeader.setAlignment(Pos.CENTER_LEFT);

    VBox listCard = new VBox(12, leftHeader, filesTable);
    listCard.getStyleClass().addAll("card", "table-card");
    listCard.setPadding(new Insets(14));
    listCard.setMinHeight(0);
    listCard.setMaxHeight(Double.MAX_VALUE);

    VBox.setVgrow(filesTable, Priority.ALWAYS);

    Label selectedLabel = new Label("No proposal selected.");
    selectedLabel.getStyleClass().add("subtitle");

    Label tsLabel = new Label("Timestamp: —");
    tsLabel.getStyleClass().add("chip");

    TableView<EditRow> editsTable = new TableView<>();
    editsTable.getStyleClass().addAll("budget-table", "proposal-table");
    editsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    editsTable.setFixedCellSize(42);
    editsTable.setMinHeight(0);
    editsTable.setMaxHeight(Double.MAX_VALUE);

    TableColumn<EditRow, String> cAction = new TableColumn<>("Action");
    cAction.setCellValueFactory(cd -> cd.getValue().actionProperty());

    TableColumn<EditRow, String> cMin = new TableColumn<>("Ministry");
    cMin.setCellValueFactory(cd -> cd.getValue().ministryProperty());

    TableColumn<EditRow, String> cType = new TableColumn<>("Type");
    cType.setCellValueFactory(cd -> cd.getValue().typeProperty());

    TableColumn<EditRow, String> cAmount = new TableColumn<>("Amount");
    cAmount.setCellValueFactory(cd -> cd.getValue().amountProperty());
    cAmount.setStyle("-fx-alignment: CENTER-RIGHT;");

    TableColumn<EditRow, String> cRaw = new TableColumn<>("Raw");
    cRaw.setCellValueFactory(cd -> cd.getValue().rawProperty());

    editsTable.getColumns().addAll(cAction, cMin, cType, cAmount, cRaw);

    cAction.setCellFactory(col -> new TableCell<>() {
      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        getStyleClass().removeAll("increase", "decrease");

        if (empty || item == null) {
          setText(null);
          return;
        }

        setText(item);
        if (item.equalsIgnoreCase("Increase")) {
          getStyleClass().add("increase");
        }
        if (item.equalsIgnoreCase("Decrease")) {
          getStyleClass().add("decrease");
        }
      }
    });

    Label reasonTitle = new Label("Reasoning");
    reasonTitle.getStyleClass().add("section-title");

    TextArea reasonArea = new TextArea();
    reasonArea.setEditable(false);
    reasonArea.setWrapText(true);
    reasonArea.getStyleClass().add("proposal-reason");
    reasonArea.setPrefRowCount(4);
    reasonArea.setMinHeight(Region.USE_PREF_SIZE);

    Label error = new Label();
    error.getStyleClass().add("error");
    error.setWrapText(true);
    error.setMinHeight(Region.USE_PREF_SIZE);

    Button acceptBtn = new Button("Accept");
    acceptBtn.getStyleClass().addAll("button", "primary");
    acceptBtn.setDisable(true);

    Button rejectBtn = new Button("Reject");
    rejectBtn.getStyleClass().addAll("button", "danger");
    rejectBtn.setDisable(true);

    HBox actions = new HBox(10, rejectBtn, acceptBtn);
    actions.setAlignment(Pos.CENTER_RIGHT);
    actions.setMinHeight(Region.USE_PREF_SIZE);

    Label previewTitle = new Label("Proposal Preview");
    previewTitle.getStyleClass().add("section-title");

    Region metaSpacer = new Region();
    HBox.setHgrow(metaSpacer, Priority.ALWAYS);

    HBox metaRow = new HBox(10, previewTitle, metaSpacer, tsLabel);
    metaRow.setAlignment(Pos.CENTER_LEFT);
    metaRow.setMinHeight(Region.USE_PREF_SIZE);

    VBox previewCard =
        new VBox(12, selectedLabel, metaRow, editsTable, reasonTitle, reasonArea, error, actions);
    previewCard.getStyleClass().add("card");
    previewCard.setPadding(new Insets(14));
    previewCard.setMinHeight(0);
    previewCard.setMaxHeight(Double.MAX_VALUE);

    VBox.setVgrow(editsTable, Priority.ALWAYS);

    Button backBtn = new Button("⟵ Back");
    backBtn.getStyleClass().addAll("button", "subtle");
    backBtn.setOnAction(e -> new ViewEditBudgetScreen(user, userManager).show(stage));

    HBox footer = new HBox(backBtn);
    footer.setAlignment(Pos.CENTER_LEFT);
    footer.setPadding(new Insets(12, 18, 18, 18));
    footer.setMinHeight(Region.USE_PREF_SIZE);

    HBox centerRow = new HBox(16, listCard, previewCard);
    centerRow.setPadding(new Insets(18));
    HBox.setHgrow(listCard, Priority.ALWAYS);
    HBox.setHgrow(previewCard, Priority.ALWAYS);

    VBox center = new VBox(14, heroCard, centerRow);
    VBox.setVgrow(centerRow, Priority.ALWAYS);

    BorderPane root = new BorderPane();
    root.setTop(topBar);
    root.setCenter(center);
    root.setBottom(footer);

    Scene scene = stage.getScene();
    if (scene == null) {
      scene = new Scene(root, stage.getWidth() > 0 ? stage.getWidth() : 1180,
          stage.getHeight() > 0 ? stage.getHeight() : 760);
      var cssUrl = getClass().getResource("/css/DarkTheme.css");
      if (cssUrl != null) {
        scene.getStylesheets().add(cssUrl.toExternalForm());
      }
      stage.setScene(scene);
    } else {
      scene.setRoot(root);
      var cssUrl = getClass().getResource("/css/DarkTheme.css");
      if (cssUrl != null) {
        String css = cssUrl.toExternalForm();
        if (!scene.getStylesheets().contains(css)) {
          scene.getStylesheets().add(css);
        }
      }
    }

    stage.setTitle("Governor • Proposals");
    stage.show();

    if (wasFullScreen) {
      stage.setFullScreen(true);
    } else if (wasMaximized) {
      stage.setMaximized(true);
    } else {
      if (prevW > 0 && prevH > 0) {
        stage.setWidth(prevW);
        stage.setHeight(prevH);
        stage.setX(prevX);
        stage.setY(prevY);
      }
    }

    ObservableList<FileRow> rows = FXCollections.observableArrayList();
    ObservableList<EditRow> editRows = FXCollections.observableArrayList();
    editsTable.setItems(editRows);

    Runnable clearPreview = () -> {
      selectedLabel.setText("No proposal selected.");
      tsLabel.setText("Timestamp: —");
      editRows.clear();
      reasonArea.clear();
      acceptBtn.setDisable(true);
      rejectBtn.setDisable(true);
    };

    Runnable reload = () -> {
      error.setText("");
      rows.setAll(loadProposalRows());
      filesTable.setItems(rows);
      filesTable.getSelectionModel().clearSelection();
      clearPreview.run();
    };

    refreshBtn.setOnAction(e -> reload.run());

    filesTable.getSelectionModel().selectedItemProperty().addListener((obs, ov, row) -> {
      error.setText("");

      if (row == null) {
        clearPreview.run();
        return;
      }

      selectedLabel.setText("Selected: " + row.getMinistryDisplay());

      try {
        List<String> lines = Files.readAllLines(row.getPath(), StandardCharsets.UTF_8);

        String submitted = findSubmittedTimestamp(lines);
        tsLabel.setText("Timestamp: " + (submitted.isBlank() ? "—" : submitted));

        List<Edit> edits = parseEdits(lines);
        editRows.setAll(buildEditRows(edits));

        reasonArea.setText(findReasoning(lines));

        boolean hasEdits = !edits.isEmpty();
        acceptBtn.setDisable(!hasEdits);
        rejectBtn.setDisable(false);

      } catch (Exception ex) {
        clearPreview.run();
        error.setText("Could not read proposal: " + ex.getMessage());
      }
    });

    filesTable.setOnKeyPressed(ev -> {
      if (ev.getCode() == KeyCode.DELETE) {
        rejectBtn.fire();
      }
    });

    acceptBtn.setOnAction(e -> {
      error.setText("");
      FileRow selected = filesTable.getSelectionModel().getSelectedItem();
      if (selected == null) {
        error.setText("Select a proposal first.");
        return;
      }

      boolean ok = confirm(stage, "Accept proposal?",
          "This will apply the edits to the real budgets.", "Proceed?");
      if (!ok) {
        return;
      }

      try {
        acceptProposal(selected.getPath());
        info(stage, "Accepted", "Proposal applied",
            "Proposal applied successfully and removed from inbox.");
        reload.run();
      } catch (Exception ex) {
        error.setText("Failed to accept proposal: " + ex.getMessage());
      }
    });

    rejectBtn.setOnAction(e -> {
      error.setText("");
      FileRow selected = filesTable.getSelectionModel().getSelectedItem();
      if (selected == null) {
        error.setText("Select a proposal first.");
        return;
      }

      boolean ok = confirm(stage, "Reject proposal?",
          "This will remove the proposal without applying changes.", "Proceed?");
      if (!ok) {
        return;
      }

      try {
        rejectProposal(selected.getPath());
        info(stage, "Rejected", "Proposal removed", "Proposal removed from inbox.");
        reload.run();
      } catch (Exception ex) {
        error.setText("Failed to reject proposal: " + ex.getMessage());
      }
    });

    reload.run();

    FadeTransition ft = new FadeTransition(Duration.millis(220), root);
    ft.setFromValue(0);
    ft.setToValue(1);
    ft.play();
  }

  /**
   * Loads proposal files from disk and maps them into table rows.
   *
   * <p>
   * Files must end with {@code .txt} and contain {@code proposal} in the name, matching the
   * exporter/inbox filtering convention.
   * </p>
   *
   * @return sorted list of proposal file rows (newest first)
   */
  private List<FileRow> loadProposalRows() {
    try {
      if (!Files.exists(PROPOSALS_DIR)) {
        return new ArrayList<>();
      }

      List<Path> files = Files.list(PROPOSALS_DIR).filter(Files::isRegularFile).filter(p -> {
        String n = p.getFileName().toString().toLowerCase(Locale.ROOT);
        return n.endsWith(".txt") && (n.startsWith("proposal_") || n.contains("proposal"));
      }).collect(Collectors.toList());

      List<FileRow> rows = new ArrayList<>();
      for (Path p : files)
        rows.add(buildFileRow(p));

      rows.sort(Comparator.comparing(FileRow::getSubmittedSortKey).reversed());
      return rows;

    } catch (Exception ex) {
      return new ArrayList<>();
    }
  }

  /**
   * Builds a table row for a single proposal file by reading its metadata from content.
   *
   * @param file proposal file path
   *
   * @return resolved file row (best-effort)
   */
  private FileRow buildFileRow(Path file) {
    try {
      List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
      String from = findFrom(lines);
      String submitted = findSubmittedTimestamp(lines);
      String ministry = findFirstMinistryName(lines);
      return new FileRow(file, ministry, from, submitted);
    } catch (Exception e) {
      return new FileRow(file, file.getFileName().toString(), "", "");
    }
  }

  /**
   * Accepts a proposal by parsing its edits, applying them to official budgets, then deleting the
   * proposal file from the inbox.
   *
   * @param proposalFile proposal path
   *
   * @throws Exception if parsing/applying or deletion fails
   */
  private void acceptProposal(Path proposalFile) throws Exception {
    List<String> lines = Files.readAllLines(proposalFile, StandardCharsets.UTF_8);
    List<Edit> edits = parseEdits(lines);

    if (edits.isEmpty()) {
      throw new IllegalStateException("No valid edits found in proposal.");
    }

    for (Edit ed : edits) {
      Edit.applyEdit(ed, false, false);
    }

    Files.deleteIfExists(proposalFile);
  }

  /**
   * Rejects a proposal by deleting it from the inbox without applying changes.
   *
   * @param proposalFile proposal path
   *
   * @throws Exception if deletion fails
   */
  private void rejectProposal(Path proposalFile) throws Exception {
    Files.deleteIfExists(proposalFile);
  }

  /**
   * Extracts the sender username from a proposal file content.
   *
   * @param lines file lines
   *
   * @return sender username or empty string
   */
  private String findFrom(List<String> lines) {
    for (String raw : lines) {
      String t = raw == null ? "" : raw.trim();
      if (t.toLowerCase(Locale.ROOT).startsWith("from:")) {
        return t.substring("from:".length()).trim();
      }
    }
    return "";
  }

  /**
   * Extracts a submitted timestamp from a proposal file content.
   *
   * <p>
   * Normalizes ISO timestamps by removing fractional seconds and using a space between date and
   * time.
   * </p>
   *
   * @param lines file lines
   *
   * @return timestamp string or empty string
   */
  private String findSubmittedTimestamp(List<String> lines) {
    for (String raw : lines) {
      String t = raw == null ? "" : raw.trim();
      if (t.toLowerCase(Locale.ROOT).startsWith("submitted:")) {
        String ts = t.substring("submitted:".length()).trim();
        if (ts.contains(".")) {
          ts = ts.substring(0, ts.indexOf('.'));
        }
        return ts.replace('T', ' ');
      }
    }
    return "";
  }

  /**
   * Attempts to extract the first ministry name from the human draft section.
   *
   * @param lines proposal file lines
   *
   * @return ministry name (without "Ministry of") or empty string
   */
  private String findFirstMinistryName(List<String> lines) {
    boolean inDraft = false;
    for (String raw : lines) {
      String t = raw == null ? "" : raw.trim();
      if (t.equalsIgnoreCase("Draft edits:")) {
        inDraft = true;
        continue;
      }
      if (!inDraft) {
        continue;
      }
      if (t.isBlank()) {
        continue;
      }

      if (t.toLowerCase(Locale.ROOT).startsWith("ministry of ")
          && (t.toLowerCase(Locale.ROOT).contains(" increased by ")
              || t.toLowerCase(Locale.ROOT).contains(" decreased by "))) {
        String full = extractMinistryNameFromDraftLine(t);
        return full.replaceFirst("(?i)^Ministry of\\s+", "");
      }
    }
    return "";
  }

  /**
   * Extracts the reasoning string from the proposal file.
   *
   * @param lines file lines
   *
   * @return reasoning text or "—" when missing
   */
  private String findReasoning(List<String> lines) {
    for (String raw : lines) {
      String t = raw == null ? "" : raw.trim();
      if (t.toLowerCase(Locale.ROOT).startsWith("reason:")) {
        return t.substring("reason:".length()).trim();
      }
    }
    return "—";
  }

  /**
   * Parses edits from proposal file content.
   *
   * <p>
   * <b>De-duplication guarantee:</b> If any machine-readable {@code EDIT|...} lines exist, the
   * parser returns only those edits and ignores the human {@code Draft edits:} section. This
   * prevents duplicate edits from appearing in the preview table.
   * </p>
   *
   * @param lines proposal file lines
   *
   * @return parsed edits (possibly empty)
   */
  private List<Edit> parseEdits(List<String> lines) {
    List<Edit> edits = new ArrayList<>();

    // 1) Prefer machine-readable edits.
    for (String raw : lines) {
      String t = raw == null ? "" : raw.trim();
      if (t.startsWith("EDIT|")) {
        try {
          edits.add(Edit.parse(t));
        } catch (Exception ignore) {
        }
      }
    }

    // If any EDIT| lines exist, return only these (avoid duplicates from the human section).
    if (!edits.isEmpty()) {
      return edits;
    }
    // 2) Fallback to human-readable "Draft edits:" section.
    boolean inDraft = false;

    for (String raw : lines) {
      String t = raw == null ? "" : raw.trim();
      if (t.isBlank()) {
        continue;
      }

      if (t.equalsIgnoreCase("Draft edits:")) {
        inDraft = true;
        continue;
      }
      if (!inDraft) {
        continue;
      }
      Edit parsed = parseHumanDraftEditLine(t);
      if (parsed != null) {
        edits.add(parsed);
      }
    }

    return edits;
  }

  /**
   * Parses a human-readable draft edit line.
   *
   * <p>
   * Expected patterns:
   * </p>
   * <ul>
   * <li>{@code <Ministry> Increased by <amount> <fixed/percent>}</li>
   * <li>{@code <Ministry> Decreased by <amount> <fixed/percent>}</li>
   * </ul>
   *
   * @param line line text
   *
   * @return parsed {@link Edit} or null if unsupported/unparseable
   */
  private Edit parseHumanDraftEditLine(String line) {
    String lower = line.toLowerCase(Locale.ROOT);

    boolean increased = lower.contains(" increased by ");
    boolean decreased = lower.contains(" decreased by ");

    String change;
    String marker;
    if (increased) {
      change = "Increase";
      marker = " increased by ";
    } else if (decreased) {
      change = "Decrease";
      marker = " decreased by ";
    } else {
      return null;
    }

    int idx = lower.indexOf(marker);
    if (idx < 0) {
      return null;
    }

    String namePart = line.substring(0, idx).trim();
    String rest = line.substring(idx + marker.length()).trim();

    String[] tokens = rest.split("\\s+");
    if (tokens.length < 1) {
      return null;
    }

    String amountToken = tokens[0];
    String changeType = tokens.length >= 2 ? tokens[tokens.length - 1] : "fixed";

    double amount;
    try {
      amount = parseAmountLoose(amountToken);
    } catch (Exception e) {
      return null;
    }

    return new Edit(namePart, change, amount, changeType);
  }

  /**
   * Extracts the ministry name portion from a human draft line by removing the trailing
   * "increased/decreased by ..." part.
   *
   * @param line the full human draft line
   *
   * @return ministry name portion
   */
  private String extractMinistryNameFromDraftLine(String line) {
    String lower = line.toLowerCase(Locale.ROOT);
    int idx1 = lower.indexOf(" increased by ");
    int idx2 = lower.indexOf(" decreased by ");
    int cut = -1;
    if (idx1 >= 0) {
      cut = idx1;
    }
    if (idx2 >= 0) {
      cut = (cut == -1) ? idx2 : Math.min(cut, idx2);
    }
    return (cut == -1) ? line.trim() : line.substring(0, cut).trim();
  }

  /**
   * Parses a numeric amount token in a "loose" way, supporting:
   * <ul>
   * <li>Thousand separators: {@code 1.234.567}</li>
   * <li>Comma decimals: {@code 123,45}</li>
   * <li>Mixed formats: resolves the last separator as decimal</li>
   * </ul>
   *
   * @param token amount token
   *
   * @return parsed double value
   */
  private double parseAmountLoose(String token) {
    String t = token.trim();

    if (t.matches("\\d{1,3}(\\.\\d{3})+")) {
      t = t.replace(".", "");
    } else if (t.contains(",") && !t.contains(".")) {
      t = t.replace(",", ".");
    } else if (t.contains(",") && t.contains(".")) {
      int lastComma = t.lastIndexOf(',');
      int lastDot = t.lastIndexOf('.');
      char dec = lastComma > lastDot ? ',' : '.';

      if (dec == ',') {
        t = t.replace(".", "");
        t = t.replace(",", ".");
      } else {
        t = t.replace(",", "");
      }
    }

    return Double.parseDouble(t);
  }

  /**
   * Converts parsed edits into UI table rows.
   *
   * @param edits parsed edits
   *
   * @return list of edit rows (never null)
   */
  private List<EditRow> buildEditRows(List<Edit> edits) {
    List<EditRow> out = new ArrayList<>();
    for (Edit e : edits)
      out.add(EditRow.from(e));
    if (out.isEmpty()) {
      out.add(EditRow.empty("No edits found under 'Draft edits:'"));
    }
    return out;
  }

  /**
   * Table model representing a proposal file entry in the left list.
   */
  private static class FileRow {
    private final Path path;
    private final String ministryDisplay;
    private final String from;
    private final String submitted;

    FileRow(Path path, String ministryDisplay, String from, String submitted) {
      this.path = path;
      this.ministryDisplay = ministryDisplay == null ? "" : ministryDisplay.trim();
      this.from = from == null ? "" : from.trim();
      this.submitted = submitted == null ? "" : submitted.trim();
    }

    Path getPath() {
      return path;
    }

    String getMinistryDisplay() {
      if (!ministryDisplay.isBlank()) {
        return ministryDisplay;
      }
      return path.getFileName().toString();
    }

    String getFromDisplay() {
      return from.isBlank() ? "—" : from;
    }

    String getSubmittedText() {
      return submitted.isBlank() ? "—" : submitted;
    }

    String getSubmittedSortKey() {
      return submitted.isBlank() ? "" : submitted.replace(" ", "T");
    }
  }

  /**
   * Table model representing a single parsed edit for preview.
   */
  public static class EditRow {
    private final SimpleStringProperty action = new SimpleStringProperty("");
    private final SimpleStringProperty ministry = new SimpleStringProperty("");
    private final SimpleStringProperty type = new SimpleStringProperty("");
    private final SimpleStringProperty amount = new SimpleStringProperty("");
    private final SimpleStringProperty raw = new SimpleStringProperty("");

    public SimpleStringProperty actionProperty() {
      return action;
    }

    public SimpleStringProperty ministryProperty() {
      return ministry;
    }

    public SimpleStringProperty typeProperty() {
      return type;
    }

    public SimpleStringProperty amountProperty() {
      return amount;
    }

    public SimpleStringProperty rawProperty() {
      return raw;
    }

    static EditRow from(Edit e) {
      EditRow r = new EditRow();

      String actionTxt = e.getChange();
      String ministryTxt = e.getName();
      if (ministryTxt != null) {
        ministryTxt = ministryTxt.replaceFirst("(?i)^Ministry of\\s+", "");
      }
      r.action.set(actionTxt == null || actionTxt.isBlank() ? "—" : actionTxt);
      r.ministry.set(ministryTxt == null || ministryTxt.isBlank() ? "—" : ministryTxt);

      String ct = e.getChangeType();
      r.type.set(ct == null || ct.isBlank() ? "—" : ct);

      r.amount.set(Ministry.getFormattedBudget(e.getAmount()));
      r.raw.set(e.serialize());

      return r;
    }

    static EditRow empty(String msg) {
      EditRow r = new EditRow();
      r.action.set("—");
      r.ministry.set("—");
      r.type.set("—");
      r.amount.set("—");
      r.raw.set(msg);
      return r;
    }
  }

  /**
   * Shows a confirmation dialog.
   *
   * @param owner owner stage
   * @param title dialog title
   * @param header dialog header
   * @param content dialog body
   *
   * @return true if user confirms (OK)
   */
  private boolean confirm(Stage owner, String title, String header, String content) {
    Alert a = new Alert(Alert.AlertType.CONFIRMATION);
    a.initOwner(owner);
    a.initModality(Modality.WINDOW_MODAL);
    a.setTitle(title);
    a.setHeaderText(header);
    a.setContentText(content);

    var css = getClass().getResource("/css/DarkTheme.css");
    if (css != null) {
      a.getDialogPane().getStylesheets().add(css.toExternalForm());
    }

    return a.showAndWait().filter(b -> b == ButtonType.OK).isPresent();
  }

  /**
   * Shows an informational dialog.
   *
   * @param owner owner stage
   * @param title dialog title
   * @param header dialog header
   * @param content dialog body
   */
  private void info(Stage owner, String title, String header, String content) {
    Alert a = new Alert(Alert.AlertType.INFORMATION);
    a.initOwner(owner);
    a.initModality(Modality.WINDOW_MODAL);
    a.setTitle(title);
    a.setHeaderText(header);
    a.setContentText(content);

    var css = getClass().getResource("/css/DarkTheme.css");
    if (css != null) {
      a.getDialogPane().getStylesheets().add(css.toExternalForm());

    }
    a.showAndWait();
  }
}
