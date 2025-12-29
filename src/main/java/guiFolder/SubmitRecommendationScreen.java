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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * SubmitRecommendationScreen (Citizen)
 * 
 * GUI screen that allows a Citizen to submit one recommendation vote
 * for a selected ministry. Adapted to work with the existing CLI-based backend.
 */
public class SubmitRecommendationScreen {

    private final User user;
    private static final String VOTES_CSV_FILE = "NecessaryFilesAndData/ProposalsFromCitizens/VotesData.csv";
    private static final String MINISTRIES_REC = "NecessaryFilesAndData/ProposalsFromCitizens/MinistryVotes.txt";
    
    // All voting data (20 ministries x 6 values)
    private static int[][] allVotes = new int[20][6];

    public SubmitRecommendationScreen(User user) {
        this.user = user;
    }

    public void show(Stage stage) {

        // Initialize and load votes
        initializeCSV();
        loadVotesFromCSV();

        // ===== Title =====
        Label title = new Label("Submit Recommendation");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // ===== Ministry selector =====
        ComboBox<String> ministryBox = new ComboBox<>();
        List<String> ministries = getAvailableMinistries();
        ministryBox.getItems().addAll(ministries);
        ministryBox.setPromptText("Select a Ministry");

        // ===== Options (RadioButtons) =====
        ToggleGroup optionsGroup = new ToggleGroup();
        VBox optionsBox = new VBox(8);
        optionsBox.setPadding(new Insets(10));
        optionsBox.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 6; -fx-background-radius: 6;");

        Label optionsHint = new Label("Select one investment category:");
        optionsHint.setStyle("-fx-font-weight: bold;");

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: #cc0000;");

        Button submitButton = new Button("Submit");
        submitButton.setDisable(true);

        Button backButton = new Button("Back");

        // Helper: rebuild options list when ministry changes
        Runnable rebuildOptions = () -> {
            optionsBox.getChildren().clear();
            optionsBox.getChildren().add(optionsHint);

            String selectedMinistry = ministryBox.getValue();
            if (selectedMinistry == null) {
                submitButton.setDisable(true);
                return;
            }

            String[] opts = getOptionsForMinistry(selectedMinistry);
            if (opts.length == 0) {
                submitButton.setDisable(true);
                Label noOptions = new Label("No options available for this ministry.");
                optionsBox.getChildren().add(noOptions);
                return;
            }

            for (int i = 0; i < opts.length; i++) {
                RadioButton rb = new RadioButton((i + 1) + ". " + opts[i]);
                rb.setToggleGroup(optionsGroup);
                rb.setUserData(i); // store optionIndex (0..4)
                optionsBox.getChildren().add(rb);
            }

            submitButton.setDisable(true);
            statusLabel.setText("");
        };

        ministryBox.valueProperty().addListener((obs, oldV, newV) -> {
            optionsGroup.selectToggle(null);
            rebuildOptions.run();
        });

        optionsGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            statusLabel.setText("");
            submitButton.setDisable(newT == null || ministryBox.getValue() == null);
        });

        submitButton.setOnAction(e -> {
            String ministry = ministryBox.getValue();
            Toggle selected = optionsGroup.getSelectedToggle();

            if (ministry == null) {
                statusLabel.setText("Please select a ministry.");
                return;
            }
            if (selected == null) {
                statusLabel.setText("Please select one category.");
                return;
            }

            int optionIndex = (int) selected.getUserData();

            try {
                submitRecommendation(ministry, optionIndex);

                Alert ok = new Alert(Alert.AlertType.INFORMATION);
                ok.setTitle("Submitted");
                ok.setHeaderText("Thank you!");
                ok.setContentText("Your recommendation was submitted successfully.");
                ok.showAndWait();

                // Reset UI after submission
                optionsGroup.selectToggle(null);
                submitButton.setDisable(true);
                statusLabel.setText("");

            } catch (RuntimeException ex) {
                statusLabel.setText("Error saving recommendation: " + ex.getMessage());
            }
        });

        backButton.setOnAction(e -> new ViewEditBudgetScreen(user, null).show(stage));

        // ===== Layout =====
        HBox buttons = new HBox(10, submitButton, backButton);
        buttons.setAlignment(Pos.CENTER);

        VBox center = new VBox(12, title, ministryBox, optionsBox, statusLabel, buttons);
        center.setAlignment(Pos.TOP_CENTER);
        center.setPadding(new Insets(16));
        center.setMaxWidth(520);

        BorderPane root = new BorderPane(center);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 640, 520);
        stage.setTitle("Submit Recommendation");
        stage.setScene(scene);
        stage.show();

        // Initial setup
        rebuildOptions.run();
    }

    // ===== BACKEND HELPER METHODS =====

    /**
     * Returns list of all ministry names
     */
    private List<String> getAvailableMinistries() {
        List<String> ministries = new ArrayList<>();
        for (Ministry m : CreatingMinistries.ministries2026) {
            if (m != null) {
                ministries.add(m.getMinistryName());
            }
        }
        return ministries;
    }

    /**
     * Returns the options array for a given ministry
     */
    private String[] getOptionsForMinistry(String ministryName) {
        // These are copied from RecommendationSystem
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

        if (ministryName.equalsIgnoreCase("Ministry of Interior")) {
            return interiorOptions;
        } else if (ministryName.equalsIgnoreCase("Ministry of Foreign Affairs")) {
            return foreignAffairsOptions;
        } else if (ministryName.equalsIgnoreCase("Ministry of National Defense")) {
            return nationalDefenseOptions;
        } else if (ministryName.equalsIgnoreCase("Ministry of Health")) {
            return healthOptions;
        } else if (ministryName.equalsIgnoreCase("Ministry of Justice")) {
            return justiceOptions;
        } else if (ministryName.equalsIgnoreCase("Ministry of Education, Religious Affairs, and Sports")) {
            return educationOptions;
        } else if (ministryName.equalsIgnoreCase("Ministry of Culture")) {
            return cultureOptions;
        } else if (ministryName.equalsIgnoreCase("Ministry of National Economy and Finance")) {
            return economyOptions;
        } else if (ministryName.equalsIgnoreCase("Ministry of Rural Development and Food")) {
            return ruralOptions;
        } else if (ministryName.equalsIgnoreCase("Ministry of Environment and Energy")) {
            return environmentOptions;
        } else if (ministryName.equalsIgnoreCase("Ministry of Labor and Social Security")) {
            return laborOptions;
        } else if (ministryName.equalsIgnoreCase("Ministry of Social Cohesion and Family")) {
            return socialCohesionOptions;
        } else if (ministryName.equalsIgnoreCase("Ministry of Development")) {
            return developmentOptions;
        } else if (ministryName.equalsIgnoreCase("Ministry of Infrastructure and Transport")) {
            return infrastructureOptions;
        } else if (ministryName.equalsIgnoreCase("Ministry of Shipping and Island Policy")) {
            return shippingOptions;
        } else if (ministryName.equalsIgnoreCase("Ministry of Tourism")) {
            return tourismOptions;
        } else if (ministryName.equalsIgnoreCase("Ministry of Digital Governance")) {
            return digitalGovOptions;
        } else if (ministryName.equalsIgnoreCase("Ministry of Migration and Asylum")) {
            return migrationOptions;
        } else if (ministryName.equalsIgnoreCase("Ministry of Citizen Protection")) {
            return citizenProtectionOptions;
        } else if (ministryName.equalsIgnoreCase("Ministry of Climate Crisis and Civil Protection")) {
            return climateOptions;
        }
        return new String[0];
    }

    /**
     * Returns ministry index for array access
     */
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
            if (ministryNames[i].equalsIgnoreCase(ministryName)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Submits a recommendation (mimics backend logic)
     */
    private void submitRecommendation(String ministryName, int optionIndex) {
        int ministryIdx = getMinistryIndex(ministryName);
        if (ministryIdx == -1) {
            throw new RuntimeException("Invalid ministry name");
        }

        // Increment total votes
        allVotes[ministryIdx][0]++;
        // Increment specific option votes
        allVotes[ministryIdx][optionIndex + 1]++;

        saveVotesToCSV();
        updateMinistryFiles();
    }

    /**
     * Initializes CSV file if it doesn't exist
     */
    private void initializeCSV() {
        File csvFile = new File(VOTES_CSV_FILE);
        if (csvFile.exists()) {
            return;
        }

        try {
            csvFile.getParentFile().mkdirs();
            try (PrintWriter pw = new PrintWriter(new FileWriter(csvFile))) {
                for (int i = 0; i < 20; i++) {
                    pw.println("0,0,0,0,0,0");
                }
            }
        } catch (IOException e) {
            System.err.println("Error creating CSV file");
        }
    }

    /**
     * Loads votes from CSV into memory
     */
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

    /**
     * Saves votes from memory to CSV
     */
    private void saveVotesToCSV() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(VOTES_CSV_FILE))) {
            for (int i = 0; i < 20; i++) {
                pw.println(allVotes[i][0] + "," + allVotes[i][1] + "," + allVotes[i][2] + "," + 
                          allVotes[i][3] + "," + allVotes[i][4] + "," + allVotes[i][5]);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save votes");
        }
    }

    /**
     * Updates individual ministry text files with vote statistics
     */
    private void updateMinistryFiles() {
        for (int i = 0; i < CreatingMinistries.ministries2026.length; i++) {
            if (CreatingMinistries.ministries2026[i] == null) continue;

            String ministryName = CreatingMinistries.ministries2026[i].getMinistryName();
            String filePath = "NecessaryFilesAndData/ProposalsFromCitizens/CitizenFor" + ministryName + ".txt";
            String[] options = getOptionsForMinistry(ministryName);

            try {
                new File(filePath).getParentFile().mkdirs();
                try (FileWriter fw = new FileWriter(filePath, false);
                     PrintWriter pw = new PrintWriter(fw)) {

                    pw.println("Total Votes for " + ministryName + ": " + allVotes[i][0]);

                    for (int j = 0; j < options.length; j++) {
                        int votes = allVotes[i][j + 1];
                        double percentage = allVotes[i][0] > 0 ? (double) votes / allVotes[i][0] * 100 : 0;
                        pw.println(options[j] + ", Votes from Citizens: " + votes + ", " + 
                                  Ministry.getFormattedBudget(percentage) + "%");
                    }
                }
            } catch (IOException e) {
                System.err.println("Error updating ministry file: " + ministryName);
            }
        }

        // Update summary file
        try {
            new File(MINISTRIES_REC).getParentFile().mkdirs();
            try (FileWriter fw = new FileWriter(MINISTRIES_REC, false);
                 PrintWriter pw = new PrintWriter(fw)) {

                int totalVotes = 0;
                for (int i = 0; i < 20; i++) {
                    totalVotes += allVotes[i][0];
                }
                pw.println("Total Votes: " + totalVotes);

                for (int i = 0; i < CreatingMinistries.ministries2026.length; i++) {
                    if (CreatingMinistries.ministries2026[i] != null) {
                        pw.println(CreatingMinistries.ministries2026[i].getMinistryName() + 
                                  ", " + allVotes[i][0] + " Votes");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error updating summary file");
        }
    }
}