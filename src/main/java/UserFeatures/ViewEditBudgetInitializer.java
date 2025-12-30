package UserFeatures;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Initialization class for GUI mode.
 * Ensures all necessary data files and directories exist before the application runs.
 */
public class ViewEditBudgetInitializer {

    private static boolean initialized = false;

    /**
     * Ensures the application is properly initialized.
     * Safe to call multiple times - only runs once.
     */
    public static void ensureInitialized() {
        if (initialized) {
            return;
        }

        try {
            System.out.println("🔄 Initializing application...");

            // Step 1: Create all necessary directories
            createDirectories();

            // Step 2: Copy BudgetReview files from resources to working directory
            copyBudgetReviewFiles();

            // Step 3: Create ministries.txt (list of ministry names)
            Ministries min = new Ministries();
            min.minlist();

            // Step 4: Process BudgetReview files → create CSV files
            MinistriesBudgets budg = new MinistriesBudgets();
            for (int year = 2020; year <= 2026; year++) {
                budg.budget(Path.of("NecessaryFilesAndData/BudgetReview" + year + ".txt"));
            }

            // Step 5: Create Ministry objects for years 2020-2025
            for (int year = 2020; year <= 2025; year++) {
                CreatingMinistries.ministryCreation(
                    Path.of("NecessaryFilesAndData/MinistriesBudgets" + year + ".csv")
                );
            }

            // Step 6: Set up OriginalBudget folder with 2026 baseline
            setupOriginalBudgetFolder();

            // Step 7: Load 2026 governor draft (creates Governor_2026.csv if needed)
            CreatingMinistries.loadGovernorDraft(2026);

            initialized = true;
      

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates all necessary directories for the application.
     */
    private static void createDirectories() throws IOException {

        Files.createDirectories(Path.of("NecessaryFilesAndData"));
        Files.createDirectories(Path.of("NecessaryFilesAndData/OriginalBudget"));
        Files.createDirectories(Path.of("NecessaryFilesAndData/ProposalsFromCitizens"));
        Files.createDirectories(Path.of("NecessaryFilesAndData/ProposalsFromMinisters"));
        Files.createDirectories(Path.of("NecessaryFilesAndData/UserBudgets"));

    }

    /**
     * Copies BudgetReview files from resources to working directory.
     * These files are needed as source data for CSV generation.
     */
    private static void copyBudgetReviewFiles() throws IOException {

        for (int year = 2020; year <= 2026; year++) {
            String filename = "BudgetReview" + year + ".txt";
            Path targetPath = Path.of("NecessaryFilesAndData/" + filename);

            // Only copy if file doesn't already exist
            if (Files.exists(targetPath)) {
                System.out.println("   ✓ " + filename + " already exists");
                continue;
            }

            // Try to load from classpath (resources)
            String resourcePath = "/NecessaryFilesAndData/" + filename;
            InputStream resourceStream = ViewEditBudgetInitializer.class.getResourceAsStream(resourcePath);

            if (resourceStream == null) {
                System.err.println(" Warning: " + filename + " not found in resources");
                continue;
            }

            // Copy from classpath to working directory
            Files.copy(resourceStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            resourceStream.close();
        }
    }

    /**
     * Sets up the OriginalBudget folder with the 2026 baseline.
     * This is used as a restore point for the governor.
     */
    private static void setupOriginalBudgetFolder() throws IOException {

        Path originalFile = Path.of(
            "NecessaryFilesAndData/OriginalBudget/MinistriesBudgets2026_original.csv"
        );
        Path sourceFile = Path.of("NecessaryFilesAndData/MinistriesBudgets2026.csv");

        // Only copy if original doesn't exist and source does
        if (!Files.exists(originalFile) && Files.exists(sourceFile)) {
            Files.copy(sourceFile, originalFile, StandardCopyOption.REPLACE_EXISTING);
        } 
    }
}