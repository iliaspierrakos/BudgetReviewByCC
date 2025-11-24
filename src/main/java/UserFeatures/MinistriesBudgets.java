package UserFeatures;

import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.*;

/**
 * The MinistriesBudgets class processes budget data from an external text file and
 * proceeds into extracting relevant lines into a CSV format.
 * The procedure is based on regular expressions 
 * to filter the records and pick out the ones that belong to specific ministries
 * and have certain expense codes. 
 */
public class MinistriesBudgets {
    
    /**
     * Executes the process of reading the input file, filtering the data,
     * and creating the output file, in accordance with the specifications.
     * 
     * * The method uses **try-with-resources** to ensure automatic
     * management of resources (BufferedReader, BufferedWriter) and handles any
     * input/output errors ({@code IOException}).
     */
    public void budget() {
        Path inputFile = Path.of("NecessaryFilesAndData/BudgetReview2025.txt");
        Path outputFile = Path.of("NecessaryFilesAndData/MinistriesBudgets.csv");

        // Regular expressions for filtering
        Pattern startsWith10 = Pattern.compile("^10");
        Pattern containsMinistry = Pattern.compile("Υπουργείο");

        try (BufferedReader reader = Files.newBufferedReader(inputFile, StandardCharsets.UTF_8);
             BufferedWriter writer = Files.newBufferedWriter(
                     outputFile,
                     StandardCharsets.UTF_8,
                     StandardOpenOption.CREATE,
                     StandardOpenOption.APPEND)) {

            String line;
            int count = 0;

            while ((line = reader.readLine()) != null && count < 20) {
                if (startsWith10.matcher(line).find() && containsMinistry.matcher(line).find()) {
                    writer.write(line);
                    writer.newLine();
                    count++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
