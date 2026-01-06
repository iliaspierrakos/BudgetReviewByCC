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
 */
public class CreatingMinistries {

    public static Ministry[] ministries2020 = new Ministry[20];
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
                        // πιάσε το τελευταίο “νούμερο” (με . ,)
                        if (tokens[j].matches("[\\d\\.,]+")) {
                            lastNumber = tokens[j];
                            break;
                        }
                    }

                    double budget;
                    try {
                        String cleanNumber = lastNumber.replace(".", "");
                        cleanNumber = cleanNumber.replace(",", ".");
                        budget = Double.parseDouble(cleanNumber);
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing budget for " + ministryName + ": " + lastNumber);
                        System.err.println("Exception: " + e.getMessage());
                        budget = 0.0;
                    }

                    if (year.equalsIgnoreCase("2020")) ministries2020[i] = new Ministry(ministryName, budget);
                    else if (year.equalsIgnoreCase("2021")) ministries2021[i] = new Ministry(ministryName, budget);
                    else if (year.equalsIgnoreCase("2022")) ministries2022[i] = new Ministry(ministryName, budget);
                    else if (year.equalsIgnoreCase("2023")) ministries2023[i] = new Ministry(ministryName, budget);
                    else if (year.equalsIgnoreCase("2024")) ministries2024[i] = new Ministry(ministryName, budget);
                    else if (year.equalsIgnoreCase("2025")) ministries2025[i] = new Ministry(ministryName, budget);
                    else if (year.equalsIgnoreCase("2026")) ministries2026[i] = new Ministry(ministryName, budget);

                    writer.write(ministryName + " " + lastNumber);
                    writer.newLine();
                }
            }

        } catch (IOException e) {
            System.err.println("Error processing files: " + e.getMessage());
        }
    }

    public static void ministryCreationFromLoadedBudgets(int year) {
        Path ministriesFile = Path.of("src/main/resources/NecessaryFilesAndData/ministries.txt");
        Path budgetsFile = Path.of("src/main/resources/NecessaryFilesAndData/MinistriesBudgets" + year + ".csv");

        try {
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
                    if (tokens[j].matches("[\\d\\.,]+")) {
                        lastNumber = tokens[j];
                        break;
                    }
                }

                double budget;
                try {
                    String cleanNumber = lastNumber.replace(".", "");
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

    /**
     * ✅ FIXED CSV parsing:
     * - ministry names may contain commas
     * - budget is always AFTER the LAST comma
     * - BALANCE,<value> supported
     */
    public static void loadUserBudgets(Path file, int year) {
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);

            for (String raw : lines) {
                String line = raw == null ? "" : raw.trim();
                if (line.isEmpty()) continue;

                // BALANCE line (first comma is enough)
                if (line.regionMatches(true, 0, "BALANCE", 0, "BALANCE".length())) {
                    int idx = line.indexOf(',');
                    if (idx >= 0 && idx < line.length() - 1) {
                        String val = line.substring(idx + 1).trim();
                        Edit.balance = Double.parseDouble(val);
                    }
                    continue;
                }

                // ministry line: name may include commas -> use last comma
                int lastComma = line.lastIndexOf(',');
                if (lastComma <= 0 || lastComma >= line.length() - 1) continue;

                String ministryName = unquote(line.substring(0, lastComma).trim());
                String budgetStr = line.substring(lastComma + 1).trim();

                double budget = Double.parseDouble(budgetStr);

                // apply to current array (2026 in your flows)
                for (Ministry m : ministries2026) {
                    if (m != null && m.getMinistryName().equalsIgnoreCase(ministryName)) {
                        m.setBudget(budget);
                        break;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            // Δείχνουμε ακριβώς το file για debugging
            System.err.println("Failed to load budgets from: " + file);
            throw e;
        }
    }

    private static String unquote(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.length() >= 2 && t.startsWith("\"") && t.endsWith("\"")) {
            t = t.substring(1, t.length() - 1).replace("\"\"", "\"");
        }
        return t;
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
                if (m == null) continue;
                // γράφουμε "name",budget για να είναι safe με κόμματα
                sb.append("\"").append(m.getMinistryName().replace("\"", "\"\"")).append("\"").append(",");
                sb.append(m.getBudget()).append("\n");
            }

            Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);

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
