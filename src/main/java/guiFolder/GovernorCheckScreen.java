package guiFolder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import UserFeatures.Edit;
import UserFeatures.Ministry;
import UserManagement.CurrentSession;
import UserManagement.User;
import UserManagement.UserManager;
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
 * GovernorCheckScreen
 *
 * <p>JavaFX screen that allows the Governor to review, accept, or reject
 * budget proposals submitted by ministers via text files stored on disk.</p>
 *
 * <h2>Supported proposal formats</h2>
 * <p>This screen supports the current "human-readable" proposal format:</p>
 * <pre>
 * MINISTER PROPOSAL
 * From: 1
 * Submitted: 2026-01-10T16:35:39.148120300
 *
 * Draft edits:
 * Ministry of Climate Crisis and Civil Protection Decreased by 2.000 fixed
 * </pre>
 *
 * <p>It also supports machine-readable edit lines if present:</p>
 * <pre>
 * EDIT|Ministry of Health|Increase|2000|fixed
 * </pre>
 *
 * <h2>Behavior</h2>
 * <ul>
 *   <li>Lists proposals from {@link #PROPOSALS_DIR} (e.g. files starting with {@code proposal_}).</li>
 *   <li>Shows a preview of all draft edits in a table (right side).</li>
 *   <li>Accept applies all edits to the real budgets via {@link Edit#applyEdit(Edit, boolean, boolean)}
 *       and deletes the proposal file.</li>
 *   <li>Reject deletes the proposal file without applying changes.</li>
 *   <li>Accept is disabled when no valid edits are found.</li>
 * </ul>
 *
 * <h2>UI Note (Scroll behavior)</h2>
 * <p>
 * The right-side edits table is configured to always be scrollable when rows exceed the available
 * height. This is achieved by:
 * </p>
 * <ul>
 *   <li>Allowing the table and its parent cards to be compressible: {@code setMinHeight(0)}</li>
 *   <li>Letting VBox allocate remaining height to the table: {@code VBox.setVgrow(editsTable, Priority.ALWAYS)}</li>
 *   <li>Letting the overall center row fill remaining space: {@code VBox.setVgrow(centerRow, Priority.ALWAYS)}</li>
 * </ul>
 */
public class GovernorCheckScreen {

    /** Directory containing incoming proposal files. */
    private static final Path PROPOSALS_DIR = Paths.get(
            "src/main/resources/NecessaryFilesAndData/ProposalsFromMinisters"
    );

    private final User user;
    private final UserManager userManager;

    /**
     * Constructs the Governor proposal review screen.
     *
     * @param user the currently authenticated user (Governor)
     * @param userManager the application user manager
     */
    public GovernorCheckScreen(User user, UserManager userManager) {
        this.user = user;
        this.userManager = userManager;
    }

    /**
     * Displays the Governor proposal review screen.
     *
     * <p>This method builds the JavaFX scene, loads proposal rows, binds
     * selection listeners, and wires up Accept/Reject actions.</p>
     *
     * @param stage the JavaFX stage to render onto
     */
    public void show(Stage stage) {
        CurrentSession.setUser(user);

        // ---------- Top bar ----------
        Label appLogo = new Label("BudgetReviewByCC");
        appLogo.getStyleClass().add("app-logo");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(14, appLogo, spacer);
        topBar.getStyleClass().add("topbar");
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(14, 18, 14, 18));

        // ---------- Header ----------
        Label title = new Label("Governor • Proposals Inbox");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Review and approve the latest proposals from ministers.");
        subtitle.getStyleClass().add("subtitle");

        VBox heroCard = new VBox(10, title, subtitle);
        heroCard.getStyleClass().addAll("card", "toolbar-card", "hero-card");
        heroCard.setMaxWidth(Double.MAX_VALUE);
        heroCard.setMinHeight(Region.USE_PREF_SIZE);

        // ---------- Left: proposals list ----------
        TableView<FileRow> filesTable = new TableView<>();
        filesTable.getStyleClass().addAll("budget-table");
        filesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        filesTable.setFixedCellSize(44);

        // good resizing & scroll behaviors in split layouts
        filesTable.setMinHeight(0);
        filesTable.setMaxHeight(Double.MAX_VALUE);

        TableColumn<FileRow, String> colMinistry = new TableColumn<>("Ministry");
        colMinistry.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getMinistryDisplay()));

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

        //  must allow VBox to shrink/grow this card
        listCard.setMinHeight(0);
        listCard.setMaxHeight(Double.MAX_VALUE);

        VBox.setVgrow(filesTable, Priority.ALWAYS);

        // ---------- Right: preview ----------
        Label selectedLabel = new Label("No proposal selected.");
        selectedLabel.getStyleClass().add("subtitle");

        Label tsLabel = new Label("Timestamp: —");
        tsLabel.getStyleClass().add("chip");

        TableView<EditRow> editsTable = new TableView<>();
        editsTable.getStyleClass().addAll("budget-table", "proposal-table");
        editsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        editsTable.setFixedCellSize(42);

        //  do NOT pin prefHeight; allow VBox to allocate height
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

        // Colorize Increase/Decrease
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
                if (item.equalsIgnoreCase("Increase")) getStyleClass().add("increase");
                if (item.equalsIgnoreCase("Decrease")) getStyleClass().add("decrease");
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

        VBox previewCard = new VBox(12, selectedLabel, metaRow, editsTable, reasonTitle, reasonArea, error, actions);
        previewCard.getStyleClass().add("card");
        previewCard.setPadding(new Insets(14));

        //  critical: allow this card to be resized so the table can scroll
        previewCard.setMinHeight(0);
        previewCard.setMaxHeight(Double.MAX_VALUE);

        VBox.setVgrow(editsTable, Priority.ALWAYS);

        // ---------- Footer ----------
        Button backBtn = new Button("⟵ Back");
        backBtn.getStyleClass().addAll("button", "subtle");
        backBtn.setOnAction(e -> new ViewEditBudgetScreen(user, userManager).show(stage));

        HBox footer = new HBox(backBtn);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(12, 18, 18, 18));
        footer.setMinHeight(Region.USE_PREF_SIZE);

        // ---------- Layout ----------
        HBox centerRow = new HBox(16, listCard, previewCard);
        centerRow.setPadding(new Insets(18));
        HBox.setHgrow(listCard, Priority.ALWAYS);
        HBox.setHgrow(previewCard, Priority.ALWAYS);

        // critical: centerRow must consume remaining vertical space
        VBox center = new VBox(14, heroCard, centerRow);
        VBox.setVgrow(centerRow, Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(center);
        root.setBottom(footer);

        Scene scene = new Scene(root, 1180, 760);
        var css = getClass().getResource("/css/DarkTheme.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Governor • Proposals");
        stage.show();

        // ---------- Data bindings ----------
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

        // selection -> parse file -> preview
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

        // convenience delete key -> reject
        filesTable.setOnKeyPressed(ev -> {
            if (ev.getCode() == KeyCode.DELETE) rejectBtn.fire();
        });

        // Accept
        acceptBtn.setOnAction(e -> {
            error.setText("");
            FileRow selected = filesTable.getSelectionModel().getSelectedItem();
            if (selected == null) { error.setText("Select a proposal first."); return; }

            boolean ok = confirm(stage,
                    "Accept proposal?",
                    "This will apply the edits to the real budgets.",
                    "Proceed?");
            if (!ok) return;

            try {
                acceptProposal(selected.getPath());
                info(stage, "Accepted", "Proposal applied", "Proposal applied successfully and removed from inbox.");
                reload.run();
            } catch (Exception ex) {
                error.setText("Failed to accept proposal: " + ex.getMessage());
            }
        });

        // Reject
        rejectBtn.setOnAction(e -> {
            error.setText("");
            FileRow selected = filesTable.getSelectionModel().getSelectedItem();
            if (selected == null) { error.setText("Select a proposal first."); return; }

            boolean ok = confirm(stage,
                    "Reject proposal?",
                    "This will remove the proposal without applying changes.",
                    "Proceed?");
            if (!ok) return;

            try {
                rejectProposal(selected.getPath());
                info(stage, "Rejected", "Proposal removed", "Proposal removed from inbox.");
                reload.run();
            } catch (Exception ex) {
                error.setText("Failed to reject proposal: " + ex.getMessage());
            }
        });

        // initial load
        reload.run();

        FadeTransition ft = new FadeTransition(Duration.millis(220), root);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    /* =========================================================
       Loading proposals
       ========================================================= */

    private List<FileRow> loadProposalRows() {
        try {
            if (!Files.exists(PROPOSALS_DIR)) return new ArrayList<>();

            List<Path> files = Files.list(PROPOSALS_DIR)
                    .filter(Files::isRegularFile)
                    .filter(p -> {
                        String n = p.getFileName().toString().toLowerCase(Locale.ROOT);
                        return n.endsWith(".txt") && (n.startsWith("proposal_") || n.contains("proposal"));
                    })
                    .collect(Collectors.toList());

            List<FileRow> rows = new ArrayList<>();
            for (Path p : files) rows.add(buildFileRow(p));

            rows.sort(Comparator.comparing(FileRow::getSubmittedSortKey).reversed());
            return rows;

        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

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

    /* =========================================================
       Accept / Reject
       ========================================================= */

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

    private void rejectProposal(Path proposalFile) throws Exception {
        Files.deleteIfExists(proposalFile);
    }

    /* =========================================================
       Parsing proposal content
       ========================================================= */

    private String findFrom(List<String> lines) {
        for (String raw : lines) {
            String t = raw == null ? "" : raw.trim();
            if (t.toLowerCase(Locale.ROOT).startsWith("from:")) {
                return t.substring("from:".length()).trim();
            }
        }
        return "";
    }

    private String findSubmittedTimestamp(List<String> lines) {
        for (String raw : lines) {
            String t = raw == null ? "" : raw.trim();
            if (t.toLowerCase(Locale.ROOT).startsWith("submitted:")) {
                String ts = t.substring("submitted:".length()).trim();
                if (ts.contains(".")) ts = ts.substring(0, ts.indexOf('.'));
                return ts.replace('T', ' ');
            }
        }
        return "";
    }

    private String findFirstMinistryName(List<String> lines) {
        boolean inDraft = false;
        for (String raw : lines) {
            String t = raw == null ? "" : raw.trim();
            if (t.equalsIgnoreCase("Draft edits:")) {
                inDraft = true;
                continue;
            }
            if (!inDraft) continue;
            if (t.isBlank()) continue;

            if (t.toLowerCase(Locale.ROOT).startsWith("ministry of ")
                    && (t.toLowerCase(Locale.ROOT).contains(" increased by ")
                    || t.toLowerCase(Locale.ROOT).contains(" decreased by "))) {
                String full = extractMinistryNameFromDraftLine(t);
                return full.replaceFirst("(?i)^Ministry of\\s+", "");
            }
        }
        return "";
    }

    private String findReasoning(List<String> lines) {
        for (String raw : lines) {
            String t = raw == null ? "" : raw.trim();
            if (t.toLowerCase(Locale.ROOT).startsWith("reason:")) {
                return t.substring("reason:".length()).trim();
            }
        }
        return "—";
    }

    private List<Edit> parseEdits(List<String> lines) {
        List<Edit> edits = new ArrayList<>();
        boolean inDraft = false;

        for (String raw : lines) {
            String t = raw == null ? "" : raw.trim();
            if (t.isBlank()) continue;

            if (t.startsWith("EDIT|")) {
                try { edits.add(Edit.parse(t)); } catch (Exception ignore) {}
                continue;
            }

            if (t.equalsIgnoreCase("Draft edits:")) {
                inDraft = true;
                continue;
            }
            if (!inDraft) continue;

            Edit parsed = parseHumanDraftEditLine(t);
            if (parsed != null) edits.add(parsed);
        }

        return edits;
    }

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
        if (idx < 0) return null;

        String namePart = line.substring(0, idx).trim();
        String rest = line.substring(idx + marker.length()).trim();

        String[] tokens = rest.split("\\s+");
        if (tokens.length < 1) return null;

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

    private String extractMinistryNameFromDraftLine(String line) {
        String lower = line.toLowerCase(Locale.ROOT);
        int idx1 = lower.indexOf(" increased by ");
        int idx2 = lower.indexOf(" decreased by ");
        int cut = -1;
        if (idx1 >= 0) cut = idx1;
        if (idx2 >= 0) cut = (cut == -1) ? idx2 : Math.min(cut, idx2);
        return (cut == -1) ? line.trim() : line.substring(0, cut).trim();
    }

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

    private List<EditRow> buildEditRows(List<Edit> edits) {
        List<EditRow> out = new ArrayList<>();
        for (Edit e : edits) out.add(EditRow.from(e));
        if (out.isEmpty()) out.add(EditRow.empty("No edits found under 'Draft edits:'"));
        return out;
    }

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

        Path getPath() { return path; }

        String getMinistryDisplay() {
            if (!ministryDisplay.isBlank()) return ministryDisplay;
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

    public static class EditRow {
        private final SimpleStringProperty action = new SimpleStringProperty("");
        private final SimpleStringProperty ministry = new SimpleStringProperty("");
        private final SimpleStringProperty type = new SimpleStringProperty("");
        private final SimpleStringProperty amount = new SimpleStringProperty("");
        private final SimpleStringProperty raw = new SimpleStringProperty("");

        public SimpleStringProperty actionProperty() { return action; }
        public SimpleStringProperty ministryProperty() { return ministry; }
        public SimpleStringProperty typeProperty() { return type; }
        public SimpleStringProperty amountProperty() { return amount; }
        public SimpleStringProperty rawProperty() { return raw; }

        static EditRow from(Edit e) {
            EditRow r = new EditRow();

            String actionTxt = e.getChange();
            String ministryTxt = e.getName();
            if (ministryTxt != null) ministryTxt = ministryTxt.replaceFirst("(?i)^Ministry of\\s+", "");

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

    /* =========================================================
       Dialog helpers
       ========================================================= */

    private boolean confirm(Stage owner, String title, String header, String content) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.initOwner(owner);
        a.initModality(Modality.WINDOW_MODAL);
        a.setTitle(title);
        a.setHeaderText(header);
        a.setContentText(content);

        var css = getClass().getResource("/css/DarkTheme.css");
        if (css != null) a.getDialogPane().getStylesheets().add(css.toExternalForm());

        return a.showAndWait().filter(b -> b == ButtonType.OK).isPresent();
    }

    private void info(Stage owner, String title, String header, String content) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.initOwner(owner);
        a.initModality(Modality.WINDOW_MODAL);
        a.setTitle(title);
        a.setHeaderText(header);
        a.setContentText(content);

        var css = getClass().getResource("/css/DarkTheme.css");
        if (css != null) a.getDialogPane().getStylesheets().add(css.toExternalForm());

        a.showAndWait();
    }
}
