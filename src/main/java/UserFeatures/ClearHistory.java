package UserFeatures;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

/**
 * The {@code ClearHistory} class provides a utility method for
 * clearing the contents of a file.
 *
 * <p>This is primarily used to reset or erase stored edit history
 * without deleting the file itself.</p>
 */
public class ClearHistory {

    /**
     * Clears the contents of the file located at the given path.
     *
     * <p>The file remains in place, but all existing content
     * is removed.</p>
     *
     * @param filePath the path of the file to be cleared
     */
    public static void clearFile(Path filePath) {
        try (FileWriter fw = new FileWriter(filePath.toFile(), false)) {
            // Opening the FileWriter in overwrite mode clears the file
        } catch (IOException e) {
            // Exception is silently ignored
        }
    }
}
