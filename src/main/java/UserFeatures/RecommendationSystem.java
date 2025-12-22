package UserFeatures;
/**
 * This is a class for Citizens to create recommendations for Ministers.
 * Uses Bash script to load votes line by line from CSV
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class RecommendationSystem {
    private String targetMinistry;
    private static final String VOTES_CSV_FILE = "NecessaryFilesAndData/ProposalsFromCitizens/VotesData.csv";
    private static final String BASH_LOAD_SCRIPT = "UserFeatures/LoadVotes.sh";
    private static final String MINISTRIES_REC = "NecessaryFilesAndData/ProposalsFromCitizens/MinistryVotes.txt";


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

    static int[][] allVotes = new int[20][6];  // 20 ministries x 6 votes



    public RecommendationSystem() {}

    public RecommendationSystem(String m) {
        this.targetMinistry = m;
    }

    public void castRecommendation() {
        Scanner sc = new Scanner(System.in);
        collectInfo(sc);
    }

    public void collectInfo(Scanner s) {
        initializeCSV();
        System.out.println("Waiting...");
        loadVotesFromCSV();
        System.out.println("*** Citizen Recommendation Form ***");

        System.out.println("Which Ministry is this recommendation for? (e.g. Health)");
        System.out.print("Ministry of: ");
        String temp = "Ministry of " + s.nextLine();
        var e = new Edit();
        this.targetMinistry = e.validityCheck(temp);
        String filePath = "NecessaryFilesAndData/ProposalsFromCitizens/CitizenFor" + targetMinistry + ".txt";

        try (FileWriter fw = new FileWriter(filePath, false);
            PrintWriter pw = new PrintWriter(fw)) {

            String[] options = {};
            int ministryIdx = -1;
            System.out.println("Available categories for Investment:");
            int i = 1;
            int choice;

            if (targetMinistry.equalsIgnoreCase("Ministry of Interior")) {
                options = interiorOptions;
                ministryIdx = 0;
            } else if (targetMinistry.equalsIgnoreCase("Ministry of Foreign Affairs")) {
                options = foreignAffairsOptions;
                ministryIdx = 1;
            } else if (targetMinistry.equalsIgnoreCase("Ministry of National Defense")) {
                options = nationalDefenseOptions;
                ministryIdx = 2;
            } else if (targetMinistry.equalsIgnoreCase("Ministry of Health")) {
                options = healthOptions;
                ministryIdx = 3;
            } else if (targetMinistry.equalsIgnoreCase("Ministry of Justice")) {
                options = justiceOptions;
                ministryIdx = 4;
            } else if (targetMinistry.equalsIgnoreCase("Ministry of Education, Religious Affairs, and Sports")) {
                options = educationOptions;
                ministryIdx = 5;
            } else if (targetMinistry.equalsIgnoreCase("Ministry of Culture")) {
                options = cultureOptions;
                ministryIdx = 6;
            } else if (targetMinistry.equalsIgnoreCase("Ministry of National Economy and Finance")) {
                options = economyOptions;
                ministryIdx = 7;
            } else if (targetMinistry.equalsIgnoreCase("Ministry of Rural Development and Food")) {
                options = ruralOptions;
                ministryIdx = 8;
            } else if (targetMinistry.equalsIgnoreCase("Ministry of Environment and Energy")) {
                options = environmentOptions;
                ministryIdx = 9;
            } else if (targetMinistry.equalsIgnoreCase("Ministry of Labor and Social Security")) {
                options = laborOptions;
                ministryIdx = 10;
            } else if (targetMinistry.equalsIgnoreCase("Ministry of Social Cohesion and Family")) {
                options = socialCohesionOptions;
                ministryIdx = 11;
            } else if (targetMinistry.equalsIgnoreCase("Ministry of Development")) {
                options = developmentOptions;
                ministryIdx = 12;
            } else if (targetMinistry.equalsIgnoreCase("Ministry of Infrastructure and Transport")) {
                options = infrastructureOptions;
                ministryIdx = 13;
            } else if (targetMinistry.equalsIgnoreCase("Ministry of Shipping and Island Policy")) {
                options = shippingOptions;
                ministryIdx = 14;
            } else if (targetMinistry.equalsIgnoreCase("Ministry of Tourism")) {
                options = tourismOptions;
                ministryIdx = 15;
            } else if (targetMinistry.equalsIgnoreCase("Ministry of Digital Governance")) {
                options = digitalGovOptions;
                ministryIdx = 16;
            } else if (targetMinistry.equalsIgnoreCase("Ministry of Migration and Asylum")) {
                options = migrationOptions;
                ministryIdx = 17;
            } else if (targetMinistry.equalsIgnoreCase("Ministry of Citizen Protection")) {
                options = citizenProtectionOptions;
                ministryIdx = 18;
            } else if (targetMinistry.equalsIgnoreCase("Ministry of Climate Crisis and Civil Protection")) {
                options = climateOptions;
                ministryIdx = 19;
            }


            for (String opt : options) {
                System.out.println(i + ". " + opt);
                i++;
            }

            choice = validChoice();
            allVotes[ministryIdx][0]++;
            allVotes[ministryIdx][choice]++;

            saveVotesToCSV();

           pw.println("Total Votes for " + targetMinistry + ": " + allVotes[ministryIdx][0]);
            i = 1;
            double percentage;
            for (String opt : options) {
                percentage = (double)allVotes[ministryIdx][i] / allVotes[ministryIdx][0] * 100;
                pw.println(opt + ", Votes from Citizens: " + allVotes[ministryIdx][i] + ", " + Ministry.getFormattedBudget(percentage) + "%");
                i++;
            }


            System.out.println("Thank you! Your recommendation has been filed.");

        } catch (IOException ex) {
            System.out.println("Error saving your recommendation.");
            ex.printStackTrace();
        }
        try (FileWriter fw = new FileWriter(MINISTRIES_REC, false);
            PrintWriter pw = new PrintWriter(fw)) {
                int totalVotes = 0;
                for (int i = 0; i<20 ; i++) {
                    totalVotes = allVotes[i][0];
                }
                pw.println("Total Votes:" + totalVotes);
                for (int i = 0; i < CreatingMinistries.ministries2026.length; i++) {
                    pw.println(CreatingMinistries.ministries2026[i].getMinistryName() +"," + allVotes[i][0] + " Votes");
                }
            }catch (IOException ex) {
            System.out.println("Error saving your recommendation.");
            ex.printStackTrace();
        }

    }

    public int validChoice() {
        Scanner s = new Scanner(System.in);
        int choice;

        do {
            System.out.print("Select a number (1-5): ");
            choice = s.nextInt();
        } while (choice < 1 || choice > 5);

        return choice;
    }

    private static void loadVotesFromCSV() {
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
            System.err.println("Error");
        }
    }



    private static void saveVotesToCSV() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(VOTES_CSV_FILE))) {
            for (int i = 0; i < 20; i++) {
                pw.println(allVotes[i][0] + "," + allVotes[i][1] + "," + allVotes[i][2] + "," + allVotes[i][3] + "," + allVotes[i][4] + "," + allVotes[i][5]);
            }
        } catch (IOException e) {
            System.err.println("Error saving votes to CSV");
        }
    }
    private static void initializeCSV() {
        File csvFile = new File(VOTES_CSV_FILE);


        if (csvFile.exists()) {
            return;
        }

        try {


            try (PrintWriter pw = new PrintWriter(new FileWriter(csvFile))) {
                for (int i = 0; i < 20; i++) {
                    pw.println("0,0,0,0,0,0");
                }
            }


        } catch (IOException e) {
            System.err.println("Error creating CSV file");
            e.printStackTrace();
        }
    }
}