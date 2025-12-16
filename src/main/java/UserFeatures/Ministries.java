package UserFeatures;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


/**
 * This class provides a method that creates the "ministries.txt" text file
 * with a predefined list of ministries. This text file is then used for extracting
 * the ministry names in other classes.
 */
public class Ministries {

    public void minlist() {

        String fileName = "NecessaryFilesAndData/ministries.txt";

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


        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (String ministry : ministries) {
                writer.write(ministry);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
}
