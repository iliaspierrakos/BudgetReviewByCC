package UserFeatures;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Helper class for initializing ViewEditBudget data structures for GUI.
 * 
 * This initialization process MUST match what BudgetReviewMain does in CLI mode:
 * 1. Create all necessary directories
 * 2. Create ministries.txt (list of ministry names)
 * 3. Process budget files to create MinistriesBudgets*.csv files
 * 4. Create Ministry objects from those CSV files
 * 5. Set up OriginalBudget folder with copy of 2026 budget
 */
public class ViewEditBudgetInitializer {
    
    private static boolean initialized = false;
    
    /**
     * Ensures that all necessary data structures are initialized.
     * This method should be called before any GUI screen that needs budget data.
     * It's safe to call multiple times - it only initializes once.
     */
    public static void ensureInitialized() {
        if (initialized) {
            return;
        }
        
        try {
            // Step 1: Create all necessary directories
            createDirectories();
            
            // Step 2: Create ministries.txt (list of ministry names)
            Ministries min = new Ministries();
            min.minlist();
            
            // Step 3: Process budget files to create CSV files
            MinistriesBudgets budg = new MinistriesBudgets();
            for (int year = 2020; year <= 2026; year++) {
                budg.budget(Path.of("NecessaryFilesAndData/BudgetReview" + year + ".txt"));
            }
            
            // Step 4: Create Ministry objects for years 2020-2025
            for (int year = 2020; year <= 2025; year++) {
                CreatingMinistries.ministryCreation(
                    Path.of("NecessaryFilesAndData/MinistriesBudgets" + year + ".csv")
                );
            }
            
            // Step 5: Set up OriginalBudget folder
            setupOriginalBudgetFolder();
            
            // Step 6: Load 2026 governor draft (or create it if it doesn't exist)
            CreatingMinistries.loadGovernorDraft(2026);
            
            initialized = true;
            
            System.out.println("✓ Budget system initialized successfully!");
            
        } catch (Exception e) {
            System.err.println("Error during initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Creates all necessary directories for the application.
     */
    private static void createDirectories() {
        try {
            // Main data directory
            Files.createDirectories(Path.of("NecessaryFilesAndData"));
            
            // Subdirectories
            Files.createDirectories(Path.of("NecessaryFilesAndData/OriginalBudget"));
            Files.createDirectories(Path.of("NecessaryFilesAndData/ProposalsFromCitizens"));
            Files.createDirectories(Path.of("NecessaryFilesAndData/ProposalsFromMinisters"));
            Files.createDirectories(Path.of("NecessaryFilesAndData/UserBudgets"));
            
            System.out.println("✓ Created necessary directories");
            
        } catch (IOException e) {
            System.err.println("Error creating directories: " + e.getMessage());
        }
    }
    
    /**
     * Sets up the OriginalBudget folder by copying the 2026 budget as a baseline.
     * This is used as the "original" budget that can be reset to.
     */
    private static void setupOriginalBudgetFolder() {
        try {
            Path originalFile = Path.of("NecessaryFilesAndData/OriginalBudget/MinistriesBudgets2026_original.csv");
            Path sourceFile = Path.of("NecessaryFilesAndData/MinistriesBudgets2026.csv");
            
            // Only create if it doesn't exist (don't overwrite existing original)
            if (!Files.exists(originalFile) && Files.exists(sourceFile)) {
                Files.copy(sourceFile, originalFile);
                System.out.println("✓ Created original budget baseline");
            }
            
        } catch (IOException e) {
            System.err.println("Error setting up original budget: " + e.getMessage());
        }
    }
    
    /**
     * Forces re-initialization on next call to ensureInitialized().
     * Use this if you need to reload data from disk.
     */
    public static void reset() {
        initialized = false;
    }
}