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
 * Handles citizen recommendations and voting results.
 * GUI-friendly (no Scanner, no CLI interaction).
 *
 * All runtime files are stored under:
 *   data/recommendation/
 */
public class RecommendationSystem {

    /* =========================
       PATHS (RUNTIME DATA)
       ========================= */
    private static final String BASE_DIR = "data/recommendation/";
    private static final String PROPOSALS_DIR = BASE_DIR;
    private static final String VOTES_FILE = BASE_DIR + "VotesData.csv";
    private static final String SUMMARY_FILE = BASE_DIR + "MinistryVotes.txt";

    /* =========================
       OPTIONS PER MINISTRY
       ========================= */
    private static final Map<String, String[]> OPTIONS = new LinkedHashMap<>();

    static {
        OPTIONS.put("Ministry of Interior", new String[]{
                "Digital public services",
                "Training of public employees",
                "Municipality infrastructure",
                "Faster citizen services",
                "Transparency systems"
        });

        OPTIONS.put("Ministry of Foreign Affairs", new String[]{
                "Embassies modernization",
                "Support for exports",
                "International cooperation",
                "Digital consular services",
                "Cultural promotion abroad"
        });

        OPTIONS.put("Ministry of National Defense", new String[]{
                "New military equipment",
                "Soldier training",
                "Military bases upgrades",
                "Cyber defense",
                "Defense research"
        });

        OPTIONS.put("Ministry of Health", new String[]{
                "More doctors and nurses",
                "More ambulances",
                "Hospital upgrades",
                "Medical equipment",
                "Prevention programs"
        });

        OPTIONS.put("Ministry of Justice", new String[]{
                "Faster courts",
                "Digital court systems",
                "More judges",
                "Prison improvements",
                "Legal aid"
        });

        OPTIONS.put("Ministry of Education, Religious Affairs, and Sports", new String[]{
                "School renovations",
                "Teacher hiring",
                "Digital classrooms",
                "University funding",
                "Sports facilities"
        });

        OPTIONS.put("Ministry of Culture", new String[]{
                "Protection of monuments",
                "Museum upgrades",
                "Support for artists",
                "Cultural events",
                "Digital culture"
        });

        OPTIONS.put("Ministry of National Economy and Finance", new String[]{
                "Tax system improvements",
                "Support for businesses",
                "Fight tax evasion",
                "Digital payments",
                "Economic data systems"
        });

        OPTIONS.put("Ministry of Rural Development and Food", new String[]{
                "Support for farmers",
                "Modern farming equipment",
                "Irrigation systems",
                "Food quality control",
                "Green farming"
        });

        OPTIONS.put("Ministry of Environment and Energy", new String[]{
                "Renewable energy",
                "Energy saving programs",
                "Recycling systems",
                "Nature protection",
                "Clean energy infrastructure"
        });

        OPTIONS.put("Ministry of Labor and Social Security", new String[]{
                "Job creation programs",
                "Worker training",
                "Digital social security",
                "Workplace safety",
                "Youth employment"
        });

        OPTIONS.put("Ministry of Social Cohesion and Family", new String[]{
                "Child support services",
                "Family benefits",
                "Social housing",
                "Elderly care",
                "Support for vulnerable groups"
        });

        OPTIONS.put("Ministry of Development", new String[]{
                "Business investments",
                "Green parks",
                "Support for startups",
                "Regional development",
                "Innovation funding"
        });

        OPTIONS.put("Ministry of Infrastructure and Transport", new String[]{
                "Road construction",
                "Public transport",
                "Railway upgrades",
                "Traffic safety",
                "Smart transport systems"
        });

        OPTIONS.put("Ministry of Shipping and Island Policy", new String[]{
                "Port upgrades",
                "New ferries",
                "Island connections",
                "Maritime safety",
                "Green shipping"
        });

        OPTIONS.put("Ministry of Tourism", new String[]{
                "Hotel infrastructure",
                "Tourism promotion",
                "Sustainable tourism",
                "Digital booking platforms",
                "Tourism training"
        });

        OPTIONS.put("Ministry of Digital Governance", new String[]{
                "Online public services",
                "Cybersecurity",
                "Digital IDs",
                "Government apps",
                "Data systems"
        });

        OPTIONS.put("Ministry of Migration and Asylum", new String[]{
                "Reception centers",
                "Faster asylum process",
                "Language courses",
                "Healthcare access",
                "Integration programs"
        });

        OPTIONS.put("Ministry of Citizen Protection", new String[]{
                "More police officers",
                "Police equipment",
                "Emergency response",
                "Crime prevention",
                "Public safety training"
        });

        OPTIONS.put("Ministry of Climate Crisis and Civil Protection", new String[]{
                "Firefighting equipment",
                "Flood protection",
                "Early warning systems",
                "Climate adaptation",
                "Emergency training"
        });
    }

    /**
     * votes[row][0] = total votes per ministry
     * votes[row][1..5] = votes per option
     */
    private final int[][] votes;

    public RecommendationSystem() {
        createDirectories();
        votes = new int[OPTIONS.size()][6];
        initializeVotesFile();
        loadVotesFromFile();
    }

    /* ==========================================================
       PUBLIC API (USED BY GUI)
       ========================================================== */

    public List<String> getAvailableMinistries() {
        return new ArrayList<>(OPTIONS.keySet());
    }

    public String[] getOptionsForMinistry(String ministry) {
        return OPTIONS.getOrDefault(ministry, new String[0]);
    }

    public void submitRecommendation(String ministry, int optionIndex) {
        int idx = getMinistryIndex(ministry);
        if (idx == -1 || optionIndex < 0 || optionIndex > 4) return;

        votes[idx][0]++;
        votes[idx][optionIndex + 1]++;

        saveVotesToFile();
        saveProposalFile(ministry, idx);
        saveSummaryFile();
    }

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

    /* ==========================================================
       INTERNAL HELPERS
       ========================================================== */

    private void createDirectories() {
        File dir = new File(BASE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private int getMinistryIndex(String ministry) {
        int i = 0;
        for (String key : OPTIONS.keySet()) {
            if (key.equalsIgnoreCase(ministry)) return i;
            i++;
        }
        return -1;
    }

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
    public int getTotalVotesForMinistry(String ministry) {
    int idx = getMinistryIndex(ministry);
    return idx == -1 ? 0 : votes[idx][0];
    }

}
