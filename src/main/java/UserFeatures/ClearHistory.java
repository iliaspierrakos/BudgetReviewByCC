package UserFeatures;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

public class ClearHistory {
    public static void clearFile(String filePath) {
        try (FileWriter fw = new FileWriter(filePath, false)) {

        } catch (IOException e) {}
    }
}