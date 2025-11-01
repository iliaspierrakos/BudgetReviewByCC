import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class View {
    public void view() {
        Path budgetsFile = Path.of("MINISTRIESBUDGETS.csv");
        Path ministriesFile = Path.of("ministries.txt");
        Path outputFile = Path.of("view.txt");

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

                    writer.write(ministryName + " " + lastNumber);
                    writer.newLine();
                }

                System.out.println("Combined file created successfully: " + outputFile);
                System.out.println("File size: " + Files.size(outputFile) + " bytes");
                Files.lines(outputFile).forEach(System.out::println);
            }

        } catch (IOException e) {
            System.err.println("Error processing files: " + e.getMessage());
        }
    }
}
