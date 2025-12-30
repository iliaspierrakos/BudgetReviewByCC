package UserFeatures;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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
        Path ministriesFile = Path.of("src/main/resources/NecessaryFilesAndData/ministries.txt");
        Path outputFile = Path.of("src/main/resources/NecessaryFilesAndData/view" + year + ".txt");


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
                    double budget;
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

    /**
     * Creates ministry objects from already loaded budget data.
     * This method assumes that MinistriesBudgets.loadFromResources(year) 
     * has already been called and the data is available.
     * 
     * @param year the year for which to create ministry objects
     */
    public static void ministryCreationFromLoadedBudgets(int year) {
        Path ministriesFile = Path.of("src/main/resources/NecessaryFilesAndData/ministries.txt");
        Path budgetsFile = Path.of("src/main/resources/NecessaryFilesAndData/MinistriesBudgets" + year + ".csv");

        try {
            // Check if the budgets file exists (should have been created by loadFromResources)
            if (!Files.exists(budgetsFile)) {
                System.err.println("Warning: Budget file not found for year " + year + ": " + budgetsFile);
                return;
            }

            List<String> budgetLines = Files.readAllLines(budgetsFile, StandardCharsets.UTF_8);
            List<String> ministryNames = Files.readAllLines(ministriesFile, StandardCharsets.UTF_8);

            int total = Math.min(budgetLines.size(), ministryNames.size());
            Ministry[] targetArray = getYearArray(year);

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

                double budget;
                try {
                    // Remove . (thousands separator)
                    String cleanNumber = lastNumber.replace(".", "");
                    
                    // Replace , with . for decimals
                    cleanNumber = cleanNumber.replace(",", ".");
                    
                    budget = Double.parseDouble(cleanNumber);
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing budget for " + ministryName + ": " + lastNumber);
                    budget = 0.0;
                }

                targetArray[i] = new Ministry(ministryName, budget);
            }

        } catch (IOException e) {
            System.err.println("Error creating ministries from loaded budgets for year " + year + ": " + e.getMessage());
        }
    }

    /**
     * Helper method to get the appropriate ministry array for a given year.
     */
    private static Ministry[] getYearArray(int year) {
        return switch (year) {
            case 2020 -> ministries2020;
            case 2021 -> ministries2021;
            case 2022 -> ministries2022;
            case 2023 -> ministries2023;
            case 2024 -> ministries2024;
            case 2025 -> ministries2025;
            case 2026 -> ministries2026;
            default -> throw new IllegalArgumentException("Invalid year: " + year);
        };
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
    
    public static void resetGovernorToOriginal(int year) {
        Path original = Path.of(
            "src/main/resources/NecessaryFilesAndData/OriginalBudget/MinistriesBudgets" + year + "_original.csv"
        );
        Path governor = Path.of("src/main/resources/NecessaryFilesAndData/Governor_" + year + ".csv");

        ministries2026 = new Ministry[20];

        ministryCreation(original);

        saveCurrentBudgetsAsOfficial(governor, year);

        loadUserBudgets(governor, year);
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
        ministryCreation(Path.of("src/main/resources/NecessaryFilesAndData/MinistriesBudgets" + year + ".csv"));
    }
    
    public static void loadGovernorDraft(int year) {
        Path governorFile = Path.of("src/main/resources/NecessaryFilesAndData/Governor_" + year + ".csv");
        Path originalFile = Path.of(
            "src/main/resources/NecessaryFilesAndData/OriginalBudget/MinistriesBudgets" + year + "_original.csv"
        );

        ministries2026 = new Ministry[20];

        
        ministryCreation(originalFile);

        if (!Files.exists(governorFile)) {
            saveCurrentBudgetsAsOfficial(governorFile, year);
        }
        loadUserBudgets(governorFile, year);
    }
}