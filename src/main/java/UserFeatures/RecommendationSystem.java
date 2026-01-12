package UserFeatures;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * RecommendationSystem
 *
 * <p>Manages citizen recommendations and voting results for government ministries.</p>
 *
 * <p>This class is designed to be GUI-friendly:
 * it contains no Scanner usage and no direct console interaction.</p>
 *
 * <p>All runtime data files are stored under:
 * <code>data/recommendation/</code></p>
 *
 * <ul>
 *   <li>VotesData.csv — stores raw vote counts</li>
 *   <li>MinistryVotes.txt — summary of total votes per ministry</li>
 *   <li>CitizenFor&lt;Ministry&gt;.txt — detailed results per ministry</li>
 * </ul>
 */
public class RecommendationSystem {

    /** Base directory for all recommendation-related files */
    private static final String BASE_DIR = "data/recommendation/";

    /** Directory for proposal result files */
    private static final String PROPOSALS_DIR = BASE_DIR;

    /** CSV file storing votes per ministry and option */
    private static final String VOTES_FILE = BASE_DIR + "VotesData.csv";

    /** Summary file storing total votes per ministry */
    private static final String SUMMARY_FILE = BASE_DIR + "MinistryVotes.txt";

    /**
     * Available recommendation options per ministry.
     * Maintains insertion order for stable indexing.
     */
    private static final Map<String, String[]> OPTIONS = new LinkedHashMap<>();
    static {
        OPTIONS.put("Ministry of Finance", new String[]{
                "Increase budget",
                "Decrease budget",
                "Keep same",
                "Reduce taxes",
                "Increase taxes"
        });

        OPTIONS.put("Ministry of Health", new String[]{
                "Increase hospitals",
                "Hire staff",
                "Buy equipment",
                "Maintain budget",
                "Reduce spending"
        });

        OPTIONS.put("Ministry of Education", new String[]{
                "Hire teachers",
                "Improve infrastructure",
                "Digital education",
                "Maintain budget",
                "Reduce spending"
        });
    }


    /*
     * votes[row][0]   → total votes for the ministry
     * votes[row][1-5] → votes per recommendation option
     */
    private final int[][] votes;

    /**
     * Constructs the recommendation system.
     *
     * <p>Creates required directories, initializes vote storage files
     * if missing, and loads existing vote data.</p>
     */
    public RecommendationSystem() {
        createDirectories();
        votes = new int[OPTIONS.size()][6];
        initializeVotesFile();
        loadVotesFromFile();
    }

    /* ==========================================================
       PUBLIC API (USED BY GUI)
       ========================================================== */

    /**
     * Returns a list of all ministries available for recommendations.
     *
     * @return list of ministry names
     */
    public List<String> getAvailableMinistries() {
        return new ArrayList<>(OPTIONS.keySet());
    }

    /**
     * Returns the available recommendation options for a given ministry.
     *
     * @param ministry the ministry name
     * @return array of option descriptions (empty if ministry not found)
     */
    public String[] getOptionsForMinistry(String ministry) {
        return OPTIONS.getOrDefault(ministry, new String[0]);
    }

    /**
     * Submits a citizen recommendation vote.
     *
     * @param ministry the ministry being voted on
     * @param optionIndex index of the selected option (0–4)
     */
    public void submitRecommendation(String ministry, int optionIndex) {
        int idx = getMinistryIndex(ministry);
        if (idx == -1 || optionIndex < 0 || optionIndex > 4) return;

        votes[idx][0]++;
        votes[idx][optionIndex + 1]++;

        saveVotesToFile();
        saveProposalFile(ministry, idx);
        saveSummaryFile();
    }

    /**
     * Returns formatted voting results for a specific ministry.
     *
     * @param ministry the ministry name
     * @return list of result strings with vote counts and percentages
     */
    public List<String> getResultsForMinistry(String ministry) {
        int idx = getMinistryIndex(ministry);
        if (idx == -1) return List.of();

        int total = votes[idx][0];
        String[] options = OPTIONS.get(ministry);

        List<String> results = new ArrayList<>();
        for (int i = 0; i < options.length; i++) {
            int count = votes[idx][i + 1];
            double percent = total == 0 ? 0 : (count * 100.0 / total);
            results.add(
                options[i] + " → " + count + " votes (" +
                String.format("%.2f", percent) + "%)"
            );
        }
        return results;
    }

    /**
     * Returns the total number of votes submitted for a ministry.
     *
     * @param ministry the ministry name
     * @return total vote count
     */
    public int getTotalVotesForMinistry(String ministry) {
        int idx = getMinistryIndex(ministry);
        return idx == -1 ? 0 : votes[idx][0];
    }

    /* ==========================================================
       INTERNAL HELPERS
       ========================================================== */

    /** Creates the base directory if it does not exist */
    private void createDirectories() {
        File dir = new File(BASE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Returns the index of a ministry based on insertion order.
     *
     * @param ministry the ministry name
     * @return index or -1 if not found
     */
    private int getMinistryIndex(String ministry) {
        int i = 0;
        for (String key : OPTIONS.keySet()) {
            if (key.equalsIgnoreCase(ministry)) return i;
            i++;
        }
        return -1;
    }

    /** Initializes the votes CSV file if it does not exist */
    private void initializeVotesFile() {
        File file = new File(VOTES_FILE);
        if (file.exists()) return;

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            for (int i = 0; i < OPTIONS.size(); i++) {
                pw.println("0,0,0,0,0,0");
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot initialize VotesData.csv", e);
        }
    }

    /** Loads vote data from disk into memory */
    private void loadVotesFromFile() {
        try (BufferedReader br = new BufferedReader(new FileReader(VOTES_FILE))) {
            String line;
            int row = 0;
            while ((line = br.readLine()) != null && row < votes.length) {
                String[] parts = line.split(",");
                for (int i = 0; i < parts.length; i++) {
                    votes[row][i] = Integer.parseInt(parts[i].trim());
                }
                row++;
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot load VotesData.csv", e);
        }
    }

    /** Persists vote data to disk */
    private void saveVotesToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(VOTES_FILE))) {
            for (int[] row : votes) {
                pw.println(
                    row[0] + "," + row[1] + "," + row[2] + "," +
                    row[3] + "," + row[4] + "," + row[5]
                );
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot save VotesData.csv", e);
        }
    }

    /** Saves a detailed proposal file for a specific ministry */
    private void saveProposalFile(String ministry, int idx) {
        File file = new File(PROPOSALS_DIR + "CitizenFor" + ministry + ".txt");

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("Recommendations for " + ministry);
            pw.println("Total votes: " + votes[idx][0]);
            for (String line : getResultsForMinistry(ministry)) {
                pw.println(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot save proposal file", e);
        }
    }

    /** Saves a summary file containing total votes per ministry */
    private void saveSummaryFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(SUMMARY_FILE))) {
            int i = 0;
            for (String ministry : OPTIONS.keySet()) {
                pw.println(ministry + ": " + votes[i][0] + " votes");
                i++;
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot save summary file", e);
        }
    }
}
