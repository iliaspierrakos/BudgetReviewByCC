package UserFeatures;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class ClearHistory {
    public static void clearFile(Path filePath) {
        try (FileWriter fw = new FileWriter(filePath.toFile(), false)) {

        } catch (IOException e) {}
    }
}