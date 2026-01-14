package UserFeatures;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.regex.Pattern;

/**
 * The MinistriesBudgets class processes budget data from an external text file and proceeds into
 * extracting relevant lines into a CSV format. The procedure is based on regular expressions to
 * filter the records and pick out the ones that belong to specific ministries and have certain
 * expense codes.
 */
public class MinistriesBudgets {
  public void budget(Path inputFile) {
    String fileName = inputFile.getFileName().toString();
    String year = fileName.replaceAll("\\D+", "");
    Path outputFile =
        Path.of("src/main/resources/NecessaryFilesAndData/MinistriesBudgets" + year + ".csv");

    // Regular expressions for filtering
    Pattern startsWith10 = Pattern.compile("^10");
    Pattern containsMinistry = Pattern.compile("Υπουργείο");

    try (BufferedReader reader = Files.newBufferedReader(inputFile, StandardCharsets.UTF_8);
        BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8,
            StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

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
