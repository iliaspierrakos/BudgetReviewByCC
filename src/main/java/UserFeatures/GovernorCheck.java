package UserFeatures;

import java.io.File;
import java.nio.file.*;
import java.util.*;

public class GovernorCheck {

    // Προσπαθεί 3 πιθανές τοποθεσίες (ανάλογα από που τρέχει το app)
    private static final Path[] CANDIDATE_DIRS = new Path[] {
            Path.of("src/main/resources/NecessaryFilesAndData/ProposalsFromMinisters"),
            Path.of("NecessaryFilesAndData/ProposalsFromMinisters"),
            Path.of("target/classes/NecessaryFilesAndData/ProposalsFromMinisters")
    };

    private Path proposalsDirResolved() {
        for (Path p : CANDIDATE_DIRS) {
            if (Files.exists(p) && Files.isDirectory(p)) return p;
        }
        // αν δεν υπάρχει κανένα, γύρνα το πρώτο (ώστε να δείξουμε debug με απόλυτο path)
        return CANDIDATE_DIRS[0];
    }

    private final Scanner scanner = new Scanner(System.in);

    public void viewProposalsNames() {
        Path proposalsDir = proposalsDirResolved();

        // ===== DEBUG =====
        System.out.println("\n[GovernorCheck DEBUG]");
        System.out.println("Working dir (user.dir): " + System.getProperty("user.dir"));
        System.out.println("Resolved proposals dir: " + proposalsDir.toAbsolutePath());
        System.out.println("Exists? " + Files.exists(proposalsDir) + " | isDir? " + Files.isDirectory(proposalsDir));
        // ================

        try {
            Files.createDirectories(proposalsDir);
        } catch (Exception e) {
            System.out.println("Cannot create/access proposals folder: " + e.getMessage());
            return;
        }

        File folder = proposalsDir.toFile();

        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));

        // ===== DEBUG =====
        System.out.println("listFiles() returned: " + (files == null ? "null" : files.length + " files"));
        if (files != null) {
            for (File f : files) {
                System.out.println(" - " + f.getName() + " | lastModified=" + new Date(f.lastModified()));
            }
        }
        System.out.println("[/GovernorCheck DEBUG]\n");
        // ================

        if (files == null || files.length == 0) {
            System.out.println("No proposal .txt files found in: " + proposalsDir.toAbsolutePath());
            return;
        }

        Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));

        System.out.println("Proposal files (most recently updated first):");
        for (int i = 0; i < files.length; i++) {
            System.out.println((i + 1) + ") " + files[i].getName());
        }

        int choice = readIntInRange("Select a file (1-" + files.length + "): ", 1, files.length);
        Path filePath = files[choice - 1].toPath();

        showFileContents(filePath);
    }

    private void showFileContents(Path filePath) {
        System.out.println("\n--- File: " + filePath.toAbsolutePath() + " ---");
        try {
            List<String> lines = Files.readAllLines(filePath);
            if (lines.isEmpty()) {
                System.out.println("(empty file)");
            } else {
                for (String l : lines) System.out.println(l);
            }
        } catch (Exception e) {
            System.out.println("Could not read file: " + e.getMessage());
        }
        System.out.println("--- End ---\n");
    }

    private int readIntInRange(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int n = Integer.parseInt(line);
                if (n >= min && n <= max) return n;
            } catch (NumberFormatException ignored) {}
            System.out.println("Invalid input. Please type a number between " + min + " and " + max + ".");
        }
    }
}
