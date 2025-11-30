package UserFeatures;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * This class provides a static method to read ministry names and budgets
 * from files, create Ministry objects and then write a summary to an output file.
 * It requires the existence of a "Ministry" class with a constructor that accepts a String
 * for the name and a double for the budget.
 */
public class CreatingMinistries {
    public static Ministry[] ministries = new Ministry[20];  //array used for saving the ministry objects
       public static void ministryCreation(Path budgetsFile) {
        String fileName = budgetsFile.getFileName().toString();
        String year = fileName.replaceAll("\\D+", "");
        Path ministriesFile = Path.of("NecessaryFilesAndData/ministries.txt");
        Path outputFile = Path.of("NecessaryFilesAndData/view" + year + ".txt");


        try {
            List<String> budgetLines = Files.readAllLines(budgetsFile, StandardCharsets.UTF_8);
            List<String> ministryNames = Files.readAllLines(ministriesFile, StandardCharsets.UTF_8);

            int total = Math.min(budgetLines.size(), ministryNames.size());

            try (BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
                for (int i = 0; i < total; i++) {
                    String budgetLine = budgetLines.get(i).trim();
                    String ministryName = ministryNames.get(i).trim();
                    String[] tokens = budgetLine.split("\\s+");
                    String lastNumber = "N/A";


                    for (int j = tokens.length - 1; j >= 0; j--) {
                        if (tokens[j].matches("[\\d\\.]+")) {
                            lastNumber = tokens[j];
                            break;
                        }
                    }
                    double budget = 0.0;
                    lastNumber = lastNumber.replaceAll("\\.", "");
                    try {
                        //String cleanNumber = lastNumber.replace(".", "");
                        //cleanNumber = cleanNumber.replace(",", ".");
                        budget = Double.parseDouble(lastNumber);
                    } catch (Exception e) {
                        System.err.println("Error parsing budget for " + ministryName + ": " + lastNumber);
                        budget = 0.0;
                    }
                    ministries[i]= new Ministry(ministryName, budget); //making the ministry objects and saving them in the array


                    writer.write(ministryName + " " + lastNumber);
                    writer.newLine();
                    //System.out.println(ministryName + " " + lastNumber);


                }



            }

        } catch (IOException e) {
            System.err.println("Error processing files: " + e.getMessage());
        }
    }
}
