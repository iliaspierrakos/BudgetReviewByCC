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
    private int necessity;
    
    public RecommendationSystem() {}

    public RecommendationSystem(String d, String m, int n) {
        this.demand = d;
        this.targetMinistry = m;
        this.necessity = n;
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
        
        System.out.println("What is your recommendation?");
        this.demand = s.nextLine();
        
        System.out.println("On a scale of 1-10, how necessary is this?");
        this.necessity = s.nextInt();
        s.nextLine();        
        saveRecommendation();
    }
    private void saveRecommendation() {
        String filePath = "NecessaryFilesAndData/ProposalsFromCitizens/CitizenFor" + targetMinistry + ".txt";
        
        try (FileWriter fw = new FileWriter(filePath, true);
             PrintWriter pw = new PrintWriter(fw)) {
            
            pw.println("--- New Recommendation ---");
            pw.print("Target: " + this.targetMinistry);
            pw.print(" Suggestion: " + this.demand);
            pw.println(" Necessity Score: " + this.necessity + "/10");
            
            System.out.println("Thank you! Your recommendation has been filed.");
            
        } catch (IOException e) {
            System.out.println("Error saving your recommendation.");
            e.printStackTrace();
        }
    }
}