import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.*;

public class MinistriesBudgets {
    public void budget() {
        Path inputFile = Path.of("BudgetReview2025.txt");
        Path outputFile = Path.of("MinistriesBudgets.csv");

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

            System.out.println("Extracted " + count + " lines to " + outputFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
