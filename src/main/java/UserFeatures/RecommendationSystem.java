package UserFeatures;
/**
 * This is a class for Citizens to create recommendations for Ministers.
 * Each recommendation is identified by the actual demand (String), the ministry it is targeted (String)
 * and the level of necessity (int).
 * Each recommendation is saved in a file where all recommendations are stored for each ministry.
 * Only the responsible minister and the Governor can have access to this file.
 */
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class RecommendationSystem {
    private String targetMinistry;
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
    static int[] interiorVotes = new int[6];
    static int[] foreignAffairsVotes = new int[6];
    static int[] nationalDefenseVotes = new int[6];
    static int[] healthVotes = new int[6];
    static int[] justiceVotes = new int[6];
    static int[] educationVotes = new int[6];
    static int[] cultureVotes = new int[6];
    static int[] economyVotes = new int[6];
    static int[] ruralVotes = new int[6];
    static int[] environmentVotes = new int[6];
    static int[] laborVotes = new int[6];
    static int[] socialCohesionVotes = new int[6];
    static int[] developmentVotes = new int[6];
    static int[] infrastructureVotes = new int[6];
    static int[] shippingVotes = new int[6];
    static int[] tourismVotes = new int[6];
    static int[] digitalGovVotes = new int[6];
    static int[] migrationVotes = new int[6];
    static int[] citizenProtectionVotes = new int[6];
    static int[] climateVotes = new int[6];


    public RecommendationSystem() {}

    public RecommendationSystem(String m) {
        this.targetMinistry = m;
    }
    public void castRecommendation() {
        Scanner sc = new Scanner(System.in);
        collectInfo(sc);

    }
    public void collectInfo(Scanner s) {
    System.out.println("*** Citizen Recommendation Form ***");


        System.out.println("Which Ministry is this recommendation for? (e.g. Health, Education)");
        System.out.print("Ministry of: ");
        String temp = "Ministry of " + s.nextLine();
        var e =  new Edit();
        this.targetMinistry = e.validityCheck(temp);
        String filePath = "NecessaryFilesAndData/ProposalsFromCitizens/CitizenFor" + targetMinistry + ".txt";
        try (FileWriter fw = new FileWriter(filePath, false);
            PrintWriter pw = new PrintWriter(fw)) {
            String[] options = {};
            int[] votes = {};
            System.out.println("Available categories for Investment:");
            int i = 1;
            int choice;
            if (targetMinistry.equalsIgnoreCase("Ministry of Interior")) {
                options = interiorOptions;
                votes = interiorVotes;
            } else if (targetMinistry.equalsIgnoreCase("Ministry of Foreign Affairs")) {
                options = foreignAffairsOptions;
                votes = foreignAffairsVotes;
            } else if (targetMinistry.equalsIgnoreCase("Ministry of National Defense")) {
                options = nationalDefenseOptions;
                votes = nationalDefenseVotes;
            } else if (targetMinistry.equalsIgnoreCase("Ministry of Health")) {
                options = healthOptions;
                votes = healthVotes;
            } else if (targetMinistry.equalsIgnoreCase("Ministry of Justice")) {
                options = justiceOptions;
                votes = justiceVotes;
            } else if (targetMinistry.equalsIgnoreCase("Ministry of Education, Religious Affairs, and Sports")) {
                options = educationOptions;
                votes = educationVotes;
            } else if (targetMinistry.equalsIgnoreCase("Ministry of Culture")) {
                options = cultureOptions;
                votes = cultureVotes;
            } else if (targetMinistry.equalsIgnoreCase("Ministry of National Economy and Finance")) {
                options = economyOptions;
                votes = economyVotes;
            } else if (targetMinistry.equalsIgnoreCase("Ministry of Rural Development and Food")) {
                options = ruralOptions;
                votes = ruralVotes;
            } else if (targetMinistry.equalsIgnoreCase("Ministry of Environment and Energy")) {
                options = environmentOptions;
                votes = environmentVotes;
            } else if (targetMinistry.equalsIgnoreCase("Ministry of Labor and Social Security")) {
                options = laborOptions;
                votes = laborVotes;
            } else if (targetMinistry.equalsIgnoreCase("Ministry of Social Cohesion and Family")) {
                options = socialCohesionOptions;
                votes = socialCohesionVotes;
            } else if (targetMinistry.equalsIgnoreCase("Ministry of Development")) {
                options = developmentOptions;
                votes = developmentVotes;
            } else if (targetMinistry.equalsIgnoreCase("Ministry of Infrastructure and Transport")) {
                options = infrastructureOptions;
                votes = infrastructureVotes;
            } else if (targetMinistry.equalsIgnoreCase("Ministry of Shipping and Island Policy")) {
                options = shippingOptions;
                votes = shippingVotes;
            } else if (targetMinistry.equalsIgnoreCase("Ministry of Tourism")) {
                options = tourismOptions;
                votes = tourismVotes;
            } else if (targetMinistry.equalsIgnoreCase("Ministry of Digital Governance")) {
                options = digitalGovOptions;
                votes = digitalGovVotes;
            } else if (targetMinistry.equalsIgnoreCase("Ministry of Migration and Asylum")) {
                options = migrationOptions;
                votes = migrationVotes;
            } else if (targetMinistry.equalsIgnoreCase("Ministry of Citizen Protection")) {
                options = citizenProtectionOptions;
                votes = citizenProtectionVotes;
            } else if (targetMinistry.equalsIgnoreCase("Ministry of Climate Crisis and Civil Protection")) {
                options = climateOptions;
                votes = climateVotes;
            }
            for (String opt : options) {
                System.out.println(i + ". " + opt);
                i++;
            }
            choice = validChoice();
            votes[0]++;
            votes[choice]++;
            pw.println("Total Votes for " + targetMinistry + ": " + votes[0]);
            i = 1;
            double percentage;
            for (String opt : options) {
               percentage = (double)votes[i] / votes[0] * 100;
               pw.println(opt + ", Votes from Citizens: " + votes[i] + ", " + Ministry.getFormattedBudget(percentage) + "%");
               i++;
            }

            System.out.println("Thank you! Your recommendation has been filed.");

        } catch (IOException ex) {
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
}
    //private void saveRecommendation() {
        //String filePath = "NecessaryFilesAndData/ProposalsFromCitizens/CitizenFor" + targetMinistry + ".txt";

        //try (FileWriter fw = new FileWriter(filePath, true);
           // PrintWriter pw = new PrintWriter(fw)) {

           // pw.println("--- New Recommendation ---");
          //  pw.print("Target: " + this.targetMinistry);


         //   System.out.println("Thank you! Your recommendation has been filed.");

      //  } catch (IOException e) {
       //     System.out.println("Error saving your recommendation.");
       //     e.printStackTrace();
       // }
   // }
