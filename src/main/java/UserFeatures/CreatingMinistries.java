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
    public static Ministry[] ministries2020 = new Ministry[20];  //array used for saving the ministry objects
    public static Ministry[] ministries2021 = new Ministry[20];
    public static Ministry[] ministries2022 = new Ministry[20];
    public static Ministry[] ministries2023 = new Ministry[20];
    public static Ministry[] ministries2024 = new Ministry[20];
    public static Ministry[] ministries2025 = new Ministry[20];
    public static Ministry[] ministries2026 = new Ministry[20];
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
                    try {
                        // Remove . (thousands separator)
                        String cleanNumber = lastNumber.replace(".", "");
                        
                        // Replace , with . for decimals
                        cleanNumber = cleanNumber.replace(",", ".");
                        
                        budget = Double.parseDouble(cleanNumber);
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing budget for " + ministryName + ": " + lastNumber);
                        System.err.println("Exception: " + e.getMessage());
                        budget = 0.0;
                    }
                    if (year.equalsIgnoreCase("2020")) {
                        ministries2020[i]= new Ministry(ministryName, budget); //making the ministry objects and saving them in the array
                    } else if (year.equalsIgnoreCase("2021")) {
                        ministries2021[i]= new Ministry(ministryName, budget);
                    } else if (year.equalsIgnoreCase("2022")) {
                        ministries2022[i]= new Ministry(ministryName, budget);
                    } else if (year.equalsIgnoreCase("2023")) {
                        ministries2023[i]= new Ministry(ministryName, budget);
                    } else if (year.equalsIgnoreCase("2024")) {
                        ministries2024[i]= new Ministry(ministryName, budget);
                    } else if (year.equalsIgnoreCase("2025")) {
                        ministries2025[i]= new Ministry(ministryName, budget);
                    } else if (year.equalsIgnoreCase("2026")) {
                        ministries2026[i]= new Ministry(ministryName, budget);
                    }

                    writer.write(ministryName + " " + lastNumber);
                    writer.newLine();
                }



            }

        } catch (IOException e) {
            System.err.println("Error processing files: " + e.getMessage());
        }


    }
    public static void loadUserBudgets(Path file, int year) {
        try {
            List<String> lines = Files.readAllLines(file);

            for (String line : lines) {
                if (line.isBlank()) continue;

                String[] parts = line.split(",");
                if (parts[0].equalsIgnoreCase("BALANCE")) {
                    Edit.balance = Double.parseDouble(parts[1].trim());
                    continue;
                }
                if (parts.length != 2) continue;

                String ministryName = parts[0].trim();
                double budget = Double.parseDouble(parts[1].trim());
                for (Ministry m : ministries2026) {
                    if (m != null && m.getMinistryName().equalsIgnoreCase(ministryName)) {
                        m.setBudget(budget);
                        break;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void resetToOriginalBudgets(int year) {
        ministries2026 = new Ministry[20];
        ministryCreation(Path.of("NecessaryFilesAndData/MinistriesBudgets" + year + ".csv"));
    }
    public static void saveCurrentBudgetsAsOfficial(Path file, int year) {
        try {
            StringBuilder sb = new StringBuilder();

            for (Ministry m : ministries2026) {
                sb.append(m.getMinistryName()).append(",");
                sb.append(m.getBudget()).append("\n");
            }

            Files.writeString(file, sb.toString());

        } catch (IOException e) {
            System.out.println("Failed to publish official budget.");
        }
    }
     public static void loadOfficialBudgets(int year) {
        ministries2026 = new Ministry[20];
        ministryCreation(Path.of("NecessaryFilesAndData/MinistriesBudgets" + year + ".csv"));
    }
}
