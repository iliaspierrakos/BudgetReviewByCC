package UserFeatures;

import java.nio.file.Path;

/**
 * Helper class for initializing ViewEditBudget data structures for GUI.
 * 
 * This initialization process MUST match what BudgetReviewMain does in CLI mode:
 * 1. Create ministries.txt (list of ministry names)
 * 2. Process budget files to create MinistriesBudgets*.csv files
 * 3. Create Ministry objects from those CSV files
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
            // Step 1: Create ministries.txt (list of ministry names)
            Ministries min = new Ministries();
            min.minlist();
            
            // Step 2: Process budget files to create CSV files
            MinistriesBudgets budg = new MinistriesBudgets();
            for (int year = 2020; year <= 2026; year++) {
                budg.budget(Path.of("NecessaryFilesAndData/BudgetReview" + year + ".txt"));
            }
            
            // Step 3: Create Ministry objects for years 2020-2025
            for (int year = 2020; year <= 2025; year++) {
                CreatingMinistries.ministryCreation(
                    Path.of("NecessaryFilesAndData/MinistriesBudgets" + year + ".csv")
                );
            }
            
            // Step 4: Load 2026 governor draft (or create it if it doesn't exist)
            CreatingMinistries.loadGovernorDraft(2026);
            
            initialized = true;
            
        } catch (Exception e) {
            System.err.println("Error during initialization: " + e.getMessage());
            e.printStackTrace();
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