package guiFolder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import UserFeatures.CreatingMinistries;
import UserFeatures.Ministry;
import UserManagement.User;
import UserManagement.UserManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * SubmitRecommendationScreen (Citizen)
 *
 * GUI screen that allows a Citizen to submit one recommendation vote
 * for a selected ministry.
 *
 * (UI improved – logic unchanged)
 */
public class SubmitRecommendationScreen {

    private final User user;
    private final UserManager userManager;


    private static final String VOTES_CSV_FILE =
            "src/main/resources/NecessaryFilesAndData/ProposalsFromCitizens/VotesData.csv";
    private static final String MINISTRIES_REC =
            "src/main/resources/NecessaryFilesAndData/ProposalsFromCitizens/MinistryVotes.txt";

    private static int[][] allVotes = new int[20][6];

    public SubmitRecommendationScreen(User user, UserManager userManager) {
    this.user = user;
    this.userManager = userManager;
    }


    public void show(Stage stage) {

        initializeCSV();
        loadVotesFromCSV();

        /* ================= TOP BAR ================= */
        Label appLogo = new Label("GovBudget");
        appLogo.getStyleClass().add("app-logo");

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        HBox topBar = new HBox(14, appLogo, topSpacer);
        topBar.getStyleClass().add("topbar");
        topBar.setPadding(new Insets(14, 18, 14, 18));

        /* ================= HERO ================= */
        Label title = new Label("Submit Recommendation");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Choose a ministry and vote for one investment category.");
        subtitle.getStyleClass().add("subtitle");

        Label chip = new Label("Citizen • One vote per submit");
        chip.getStyleClass().add("chip");

        VBox hero = new VBox(10, title, subtitle, chip);
        hero.getStyleClass().addAll("card", "toolbar-card", "hero-card", "rec-hero");
        hero.setMaxWidth(Double.MAX_VALUE);

        /* ================= FORM: MINISTRY ================= */
        Label ministryLabel = new Label("Ministry");
        ministryLabel.getStyleClass().add("field-label");

        ComboBox<String> ministryBox = new ComboBox<>();
        ministryBox.getItems().addAll(getAvailableMinistries());
        ministryBox.setPromptText("Select a Ministry");
        ministryBox.setMaxWidth(Double.MAX_VALUE);

        VBox ministryCard = new VBox(10, ministryLabel, ministryBox);
        ministryCard.getStyleClass().addAll("card", "form-card");

        /* ================= OPTIONS ================= */
        Label optionsTitle = new Label("Investment Category");
        optionsTitle.getStyleClass().add("section-title");

        ToggleGroup optionsGroup = new ToggleGroup();

        Label helper = new Label("Select one investment category:");
        helper.getStyleClass().add("subtitle");

        VBox optionsBox = new VBox(10);
        optionsBox.getStyleClass().add("option-list");

        VBox optionsCard = new VBox(12, optionsTitle, helper, optionsBox);
        optionsCard.getStyleClass().addAll("card", "form-card");
        VBox.setVgrow(optionsCard, Priority.ALWAYS);

        /* ================= STATUS ================= */
        Label statusLabel = new Label();
        statusLabel.getStyleClass().addAll("inline-alert");
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);

        /* ================= BUTTONS ================= */
        Button submitButton = new Button("Submit");
        submitButton.getStyleClass().addAll("button", "primary");
        submitButton.setDisable(true);

        Button backButton = new Button("Back");
        backButton.getStyleClass().addAll("button", "subtle");

        HBox actions = new HBox(12, backButton, submitButton);
        actions.getStyleClass().add("cta-bar");
        actions.setAlignment(Pos.CENTER_RIGHT);

        /* ================= OPTIONS REBUILD (LOGIC SAME) ================= */
        Runnable rebuildOptions = () -> {
            optionsBox.getChildren().clear();

            String selectedMinistry = ministryBox.getValue();
            if (selectedMinistry == null) {
                submitButton.setDisable(true);
                statusLabel.setVisible(false);
                statusLabel.setManaged(false);
                return;
            }

            String[] opts = getOptionsForMinistry(selectedMinistry);
            if (opts.length == 0) {
                submitButton.setDisable(true);
                Label noOptions = new Label("No options available for this ministry.");
                noOptions.getStyleClass().add("subtitle");
                optionsBox.getChildren().add(noOptions);
                return;
            }

            for (int i = 0; i < opts.length; i++) {
                RadioButton rb = new RadioButton((i + 1) + ". " + opts[i]);
                rb.setToggleGroup(optionsGroup);
                rb.setUserData(i);
                rb.getStyleClass().add("option-tile");
                rb.setWrapText(true);
                rb.setMaxWidth(Double.MAX_VALUE);
                optionsBox.getChildren().add(rb);
            }

            submitButton.setDisable(true);
            statusLabel.setVisible(false);
            statusLabel.setManaged(false);
        };

        ministryBox.valueProperty().addListener((obs, oldV, newV) -> {
            optionsGroup.selectToggle(null);
            rebuildOptions.run();
        });

        optionsGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            statusLabel.setVisible(false);
            statusLabel.setManaged(false);
            submitButton.setDisable(newT == null || ministryBox.getValue() == null);
        });

        submitButton.setOnAction(e -> {
            String ministry = ministryBox.getValue();
            Toggle selected = optionsGroup.getSelectedToggle();

            if (ministry == null) {
                showInline(statusLabel, "Please select a ministry.");
                return;
            }
            if (selected == null) {
                showInline(statusLabel, "Please select one category.");
                return;
            }

            int optionIndex = (int) selected.getUserData();

            try {
                submitRecommendation(ministry, optionIndex);

                Alert ok = new Alert(Alert.AlertType.INFORMATION);
                ok.setHeaderText("Thank you!");
                ok.setContentText("Your recommendation was submitted successfully.");
                ok.getDialogPane().getStylesheets().add(
                        getClass().getResource("/css/DarkTheme.css").toExternalForm()
                );
                ok.showAndWait();

                optionsGroup.selectToggle(null);
                submitButton.setDisable(true);
                statusLabel.setVisible(false);
                statusLabel.setManaged(false);

            } catch (RuntimeException ex) {
                showInline(statusLabel, "Error saving recommendation: " + ex.getMessage());
            }
        });

        backButton.setOnAction(e -> new ViewEditBudgetScreen(user, userManager).show(stage));


        /* ================= ROOT LAYOUT ================= */
        VBox content = new VBox(16, hero, ministryCard, optionsCard, statusLabel, actions);
        content.getStyleClass().add("rec-content");
        content.setPadding(new Insets(18));
        content.setAlignment(Pos.TOP_CENTER);
        content.setMaxWidth(980);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("rec-root");
        root.setTop(topBar);
        root.setCenter(content);

        Scene scene = new Scene(root, 1100, 700);
        scene.getStylesheets().add(
                getClass().getResource("/css/DarkTheme.css").toExternalForm()
        );

        stage.setTitle("Submit Recommendation");
        stage.setScene(scene);
        stage.show();

        rebuildOptions.run();
    }

    private static void showInline(Label statusLabel, String msg) {
        statusLabel.setText(msg);
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }

    // ===== BACKEND HELPER METHODS (UNCHANGED) =====

    private List<String> getAvailableMinistries() {
        List<String> ministries = new ArrayList<>();
        for (Ministry m : CreatingMinistries.ministries2026) {
            if (m != null) {
                ministries.add(m.getMinistryName());
            }
        }
        return ministries;
    }

    private String[] getOptionsForMinistry(String ministryName) {
        String[] interiorOptions = {"Digital public services", "Training of public employees", "Municipality infrastructure", "Faster citizen services", "Transparency systems"};
        String[] foreignAffairsOptions = {"Embassies modernization", "Support for exports", "International cooperation", "Digital consular services", "Cultural promotion abroad"};
        String[] nationalDefenseOptions = {"New military equipment", "Soldier training", "Military bases upgrades", "Cyber defense", "Defense research"};
        String[] healthOptions = {"More doctors and nurses", "More ambulances", "Hospital upgrades", "Medical equipment", "Prevention programs"};
        String[] justiceOptions = {"Faster courts", "Digital court systems", "More judges", "Prison improvements", "Legal aid"};
        String[] educationOptions = {"School renovations", "Teacher hiring", "Digital classrooms", "University funding", "Sports facilities"};
        String[] cultureOptions = {"Protection of monuments", "Museum upgrades", "Support for artists", "Cultural events", "Digital culture"};
        String[] economyOptions = {"Tax system improvements", "Support for businesses", "Fight tax evasion", "Digital payments", "Economic data systems"};
        String[] ruralOptions = {"Support for farmers", "Modern farming equipment", "Irrigation systems", "Food quality control", "Green farming"};
        String[] environmentOptions = {"Renewable energy", "Energy saving programs", "Recycling systems", "Nature protection", "Clean energy infrastructure"};
        String[] laborOptions = {"Job creation programs", "Worker training", "Digital social security", "Workplace safety", "Youth employment"};
        String[] socialCohesionOptions = {"Child support services", "Family benefits", "Social housing", "Elderly care", "Support for vulnerable groups"};
        String[] developmentOptions = {"Business investments", "Green parks", "Support for startups", "Regional development", "Innovation funding"};
        String[] infrastructureOptions = {"Road construction", "Public transport", "Railway upgrades", "Traffic safety", "Smart transport systems"};
        String[] shippingOptions = {"Port upgrades", "New ferries", "Island connections", "Maritime safety", "Green shipping"};
        String[] tourismOptions = {"Hotel infrastructure", "Tourism promotion", "Sustainable tourism", "Digital booking platforms", "Tourism training"};
        String[] digitalGovOptions = {"Online public services", "Cybersecurity", "Digital IDs", "Government apps", "Data systems"};
        String[] migrationOptions = {"Reception centers", "Faster asylum process", "Language courses", "Healthcare access", "Integration programs"};
        String[] citizenProtectionOptions = {"More police officers", "Police equipment", "Emergency response", "Crime prevention", "Public safety training"};
        String[] climateOptions = {"Firefighting equipment", "Flood protection", "Early warning systems", "Climate adaptation", "Emergency training"};

        if (ministryName.equalsIgnoreCase("Ministry of Interior")) return interiorOptions;
        if (ministryName.equalsIgnoreCase("Ministry of Foreign Affairs")) return foreignAffairsOptions;
        if (ministryName.equalsIgnoreCase("Ministry of National Defense")) return nationalDefenseOptions;
        if (ministryName.equalsIgnoreCase("Ministry of Health")) return healthOptions;
        if (ministryName.equalsIgnoreCase("Ministry of Justice")) return justiceOptions;
        if (ministryName.equalsIgnoreCase("Ministry of Education, Religious Affairs, and Sports")) return educationOptions;
        if (ministryName.equalsIgnoreCase("Ministry of Culture")) return cultureOptions;
        if (ministryName.equalsIgnoreCase("Ministry of National Economy and Finance")) return economyOptions;
        if (ministryName.equalsIgnoreCase("Ministry of Rural Development and Food")) return ruralOptions;
        if (ministryName.equalsIgnoreCase("Ministry of Environment and Energy")) return environmentOptions;
        if (ministryName.equalsIgnoreCase("Ministry of Labor and Social Security")) return laborOptions;
        if (ministryName.equalsIgnoreCase("Ministry of Social Cohesion and Family")) return socialCohesionOptions;
        if (ministryName.equalsIgnoreCase("Ministry of Development")) return developmentOptions;
        if (ministryName.equalsIgnoreCase("Ministry of Infrastructure and Transport")) return infrastructureOptions;
        if (ministryName.equalsIgnoreCase("Ministry of Shipping and Island Policy")) return shippingOptions;
        if (ministryName.equalsIgnoreCase("Ministry of Tourism")) return tourismOptions;
        if (ministryName.equalsIgnoreCase("Ministry of Digital Governance")) return digitalGovOptions;
        if (ministryName.equalsIgnoreCase("Ministry of Migration and Asylum")) return migrationOptions;
        if (ministryName.equalsIgnoreCase("Ministry of Citizen Protection")) return citizenProtectionOptions;
        if (ministryName.equalsIgnoreCase("Ministry of Climate Crisis and Civil Protection")) return climateOptions;
        return new String[0];
    }

    private int getMinistryIndex(String ministryName) {
        String[] ministryNames = {
                "Ministry of Interior",
                "Ministry of Foreign Affairs",
                "Ministry of National Defense",
                "Ministry of Health",
                "Ministry of Justice",
                "Ministry of Education, Religious Affairs, and Sports",
                "Ministry of Culture",
                "Ministry of National Economy and Finance",
                "Ministry of Rural Development and Food",
                "Ministry of Environment and Energy",
                "Ministry of Labor and Social Security",
                "Ministry of Social Cohesion and Family",
                "Ministry of Development",
                "Ministry of Infrastructure and Transport",
                "Ministry of Shipping and Island Policy",
                "Ministry of Tourism",
                "Ministry of Digital Governance",
                "Ministry of Migration and Asylum",
                "Ministry of Citizen Protection",
                "Ministry of Climate Crisis and Civil Protection"
        };

        for (int i = 0; i < ministryNames.length; i++) {
            if (ministryNames[i].equalsIgnoreCase(ministryName)) return i;
        }
        return -1;
    }

    private void submitRecommendation(String ministryName, int optionIndex) {
        int ministryIdx = getMinistryIndex(ministryName);
        if (ministryIdx == -1) throw new RuntimeException("Invalid ministry name");

        allVotes[ministryIdx][0]++;
        allVotes[ministryIdx][optionIndex + 1]++;

        saveVotesToCSV();
        updateMinistryFiles();
    }

    private void initializeCSV() {
        File csvFile = new File(VOTES_CSV_FILE);
        if (csvFile.exists()) return;

        try {
            csvFile.getParentFile().mkdirs();
            try (PrintWriter pw = new PrintWriter(new FileWriter(csvFile))) {
                for (int i = 0; i < 20; i++) pw.println("0,0,0,0,0,0");
            }
        } catch (IOException e) {
            System.err.println("Error creating CSV file");
        }
    }

    private void loadVotesFromCSV() {
        try (BufferedReader br = new BufferedReader(new FileReader(VOTES_CSV_FILE))) {
            String line;
            int row = 0;
            while ((line = br.readLine()) != null && row < 20) {
                String[] values = line.split(",");
                for (int col = 0; col < 6 && col < values.length; col++) {
                    allVotes[row][col] = Integer.parseInt(values[col].trim());
                }
                row++;
            }
        } catch (Exception e) {
            System.err.println("Error loading votes");
        }
    }

    private void saveVotesToCSV() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(VOTES_CSV_FILE))) {
            for (int i = 0; i < 20; i++) {
                pw.println(allVotes[i][0] + "," + allVotes[i][1] + "," + allVotes[i][2] + ","
                        + allVotes[i][3] + "," + allVotes[i][4] + "," + allVotes[i][5]);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save votes");
        }
    }

    private void updateMinistryFiles() {
        // (UNCHANGED) — kept as in your original
        for (int i = 0; i < CreatingMinistries.ministries2026.length; i++) {
            if (CreatingMinistries.ministries2026[i] == null) continue;

            String ministryName = CreatingMinistries.ministries2026[i].getMinistryName();
            String filePath = "src/main/resources/NecessaryFilesAndData/ProposalsFromCitizens/CitizenFor" + ministryName + ".txt";
            String[] options = getOptionsForMinistry(ministryName);

            try {
                new File(filePath).getParentFile().mkdirs();
                try (FileWriter fw = new FileWriter(filePath, false);
                     PrintWriter pw = new PrintWriter(fw)) {

                    pw.println("Total Votes for " + ministryName + ": " + allVotes[i][0]);

                    for (int j = 0; j < options.length; j++) {
                        int votes = allVotes[i][j + 1];
                        double percentage = allVotes[i][0] > 0 ? (double) votes / allVotes[i][0] * 100 : 0;
                        pw.println(options[j] + ", Votes from Citizens: " + votes + ", "
                                + Ministry.getFormattedBudget(percentage) + "%");
                    }
                }
            } catch (IOException e) {
                System.err.println("Error updating ministry file: " + ministryName);
            }
        }

        try {
            new File(MINISTRIES_REC).getParentFile().mkdirs();
            try (FileWriter fw = new FileWriter(MINISTRIES_REC, false);
                 PrintWriter pw = new PrintWriter(fw)) {

                int totalVotes = 0;
                for (int i = 0; i < 20; i++) totalVotes += allVotes[i][0];
                pw.println("Total Votes: " + totalVotes);

                for (int i = 0; i < CreatingMinistries.ministries2026.length; i++) {
                    if (CreatingMinistries.ministries2026[i] != null) {
                        pw.println(CreatingMinistries.ministries2026[i].getMinistryName()
                                + ", " + allVotes[i][0] + " Votes");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error updating summary file");
        }
    }
}
