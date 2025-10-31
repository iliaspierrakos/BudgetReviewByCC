import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Ministries {

    public static void main(String[] args) {

        String fileName = "ministries.txt";


        String[] ministries = {
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

        // Προσπάθεια εγγραφής στο αρχείο
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (String ministry : ministries) {
                writer.write(ministry);
                writer.newLine();
            }
            System.out.println("Ministries successfully written to " + fileName);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
}
