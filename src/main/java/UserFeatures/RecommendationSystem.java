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
    private String demand;
    private String targetMinistry;

    public RecommendationSystem() {}

    public RecommendationSystem(String d, String m) {
        this.demand = d;
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
        String temp = "Ministry of: " + s.nextLine();
        var e =  new Edit();
        this.targetMinistry = e.validityCheck(temp);
        if (targetMinistry.equalsIgnoreCase("Interior")) {

            System.out.println("1. Digital public services");
            System.out.println("2. Training of public employees");
            System.out.println("3. Municipality infrastructure");
            System.out.println("4. Faster citizen services");
            System.out.println("5. Transparency systems");

        } else if (targetMinistry.equalsIgnoreCase("Foreign Affairs")) {

            System.out.println("1. Embassies modernization");
            System.out.println("2. Support for exports");
            System.out.println("3. International cooperation");
            System.out.println("4. Digital consular services");
            System.out.println("5. Cultural promotion abroad");

        } else if (targetMinistry.equalsIgnoreCase("National Defense")) {

            System.out.println("1. New military equipment");
            System.out.println("2. Soldier training");
            System.out.println("3. Military bases upgrades");
            System.out.println("4. Cyber defense");
            System.out.println("5. Defense research");

        } else if (targetMinistry.equalsIgnoreCase("Health")) {

            System.out.println("1. More doctors and nurses");
            System.out.println("2. More ambulances");
            System.out.println("3. Hospital upgrades");
            System.out.println("4. Medical equipment");
            System.out.println("5. Prevention programs");

        } else if (targetMinistry.equalsIgnoreCase("Justice")) {

            System.out.println("1. Faster courts");
            System.out.println("2. Digital court systems");
            System.out.println("3. More judges");
            System.out.println("4. Prison improvements");
            System.out.println("5. Legal aid");

        } else if (targetMinistry.equalsIgnoreCase("Education")) {

            System.out.println("1. School renovations");
            System.out.println("2. Teacher hiring");
            System.out.println("3. Digital classrooms");
            System.out.println("4. University funding");
            System.out.println("5. Sports facilities");

        } else if (targetMinistry.equalsIgnoreCase("Culture")) {

            System.out.println("1. Protection of monuments");
            System.out.println("2. Museum upgrades");
            System.out.println("3. Support for artists");
            System.out.println("4. Cultural events");
            System.out.println("5. Digital culture");

        } else if (targetMinistry.equalsIgnoreCase("National Economy and Finance")) {

            System.out.println("1. Tax system improvements");
            System.out.println("2. Support for businesses");
            System.out.println("3. Fight tax evasion");
            System.out.println("4. Digital payments");
            System.out.println("5. Economic data systems");

        } else if (targetMinistry.equalsIgnoreCase("Rural Development and Food")) {

            System.out.println("1. Support for farmers");
            System.out.println("2. Modern farming equipment");
            System.out.println("3. Irrigation systems");
            System.out.println("4. Food quality control");
            System.out.println("5. Green farming");

        } else if (targetMinistry.equalsIgnoreCase("Environment and Energy")) {

            System.out.println("1. Renewable energy");
            System.out.println("2. Energy saving programs");
            System.out.println("3. Recycling systems");
            System.out.println("4. Nature protection");
            System.out.println("5. Clean energy infrastructure");

        } else if (targetMinistry.equalsIgnoreCase("Labor and Social Security")) {

            System.out.println("1. Job creation programs");
            System.out.println("2. Worker training");
            System.out.println("3. Digital social security");
            System.out.println("4. Workplace safety");
            System.out.println("5. Youth employment");

        } else if (targetMinistry.equalsIgnoreCase("Social Cohesion and Family")) {

            System.out.println("1. Child support services");
            System.out.println("2. Family benefits");
            System.out.println("3. Social housing");
            System.out.println("4. Elderly care");
            System.out.println("5. Support for vulnerable groups");

        } else if (targetMinistry.equalsIgnoreCase("Development")) {

            System.out.println("1. Business investments");
            System.out.println("2. Green parks");
            System.out.println("3. Support for startups");
            System.out.println("4. Regional development");
            System.out.println("5. Innovation funding");

        } else if (targetMinistry.equalsIgnoreCase("Infrastructure and Transport")) {

            System.out.println("1. Road construction");
            System.out.println("2. Public transport");
            System.out.println("3. Railway upgrades");
            System.out.println("4. Traffic safety");
            System.out.println("5. Smart transport systems");

        } else if (targetMinistry.equalsIgnoreCase("Shipping and Island Policy")) {

            System.out.println("1. Port upgrades");
            System.out.println("2. New ferries");
            System.out.println("3. Island connections");
            System.out.println("4. Maritime safety");
            System.out.println("5. Green shipping");

        } else if (targetMinistry.equalsIgnoreCase("Tourism")) {

            System.out.println("1. Hotel infrastructure");
            System.out.println("2. Tourism promotion");
            System.out.println("3. Sustainable tourism");
            System.out.println("4. Digital booking platforms");
            System.out.println("5. Tourism training");

        } else if (targetMinistry.equalsIgnoreCase("Digital Governance")) {

            System.out.println("1. Online public services");
            System.out.println("2. Cybersecurity");
            System.out.println("3. Digital IDs");
            System.out.println("4. Government apps");
            System.out.println("5. Data systems");

        } else if (targetMinistry.equalsIgnoreCase("Migration and Asylum")) {

            System.out.println("1. Reception centers");
            System.out.println("2. Faster asylum process");
            System.out.println("3. Language courses");
            System.out.println("4. Healthcare access");
            System.out.println("5. Integration programs");

        } else if (targetMinistry.equalsIgnoreCase("Citizen Protection")) {

            System.out.println("1. More police officers");
            System.out.println("2. Police equipment");
            System.out.println("3. Emergency response");
            System.out.println("4. Crime prevention");
            System.out.println("5. Public safety training");

        } else if (targetMinistry.equalsIgnoreCase("Climate Crisis and Civil Protection")) {

            System.out.println("1. Firefighting equipment");
            System.out.println("2. Flood protection");
            System.out.println("3. Early warning systems");
            System.out.println("4. Climate adaptation");
            System.out.println("5. Emergency training");
        }
        saveRecommendation();
    }
    private void saveRecommendation() {
        String filePath = "NecessaryFilesAndData/ProposalsFromCitizens/CitizenFor" + targetMinistry + ".txt";

        try (FileWriter fw = new FileWriter(filePath, true);
            PrintWriter pw = new PrintWriter(fw)) {

            pw.println("--- New Recommendation ---");
            pw.print("Target: " + this.targetMinistry);
            pw.print(" Suggestion: " + this.demand);


            System.out.println("Thank you! Your recommendation has been filed.");

        } catch (IOException e) {
            System.out.println("Error saving your recommendation.");
            e.printStackTrace();
        }
    }
}