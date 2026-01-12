package guiFolder;

import UserFeatures.CreatingMinistries;
import UserFeatures.Ministry;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SubmitRecommendationScreenTest {

    private static final Path BASE_DIR =
            Path.of("src/main/resources/NecessaryFilesAndData/ProposalsFromCitizens");

    private static final Path VOTES_CSV_PATH = BASE_DIR.resolve("VotesData.csv");
    private static final Path MINISTRIES_REC_PATH = BASE_DIR.resolve("MinistryVotes.txt");

    private Path tempBackupDir;

    @BeforeEach
    void setup() throws Exception {
        Files.createDirectories(BASE_DIR);

        tempBackupDir = Files.createTempDirectory("votes_backup_");
        backupFolder(BASE_DIR, tempBackupDir);

        resetAllVotes();

        CreatingMinistries.ministries2026 = new Ministry[20];

        Files.deleteIfExists(VOTES_CSV_PATH);
        Files.deleteIfExists(MINISTRIES_REC_PATH);
    }

    @AfterEach
    void tearDown() throws Exception {

        deleteFolderContents(BASE_DIR);

        restoreFolder(tempBackupDir, BASE_DIR);

        deleteFolderContents(tempBackupDir);
        Files.deleteIfExists(tempBackupDir);
    }

    @Test
    void getOptionsForMinistry_knownMinistry_returns5Options() throws Exception {
        SubmitRecommendationScreen screen = new SubmitRecommendationScreen(null, null);

        String[] opts = call_getOptionsForMinistry(screen, "Ministry of Health");

        assertEquals(5, opts.length);
        assertEquals("More doctors and nurses", opts[0]);
    }

    @Test
    void getOptionsForMinistry_unknownMinistry_returnsEmptyArray() throws Exception {
        SubmitRecommendationScreen screen = new SubmitRecommendationScreen(null, null);

        String[] opts = call_getOptionsForMinistry(screen, "Not A Real Ministry");

        assertNotNull(opts);
        assertEquals(0, opts.length);
    }

    @Test
    void getMinistryIndex_isCaseInsensitive_andReturnsMinusOneForUnknown() throws Exception {
        SubmitRecommendationScreen screen = new SubmitRecommendationScreen(null, null);

        assertEquals(0, call_getMinistryIndex(screen, "ministry of interior"));
        assertEquals(3, call_getMinistryIndex(screen, "MINISTRY OF HEALTH"));
        assertEquals(-1, call_getMinistryIndex(screen, "nope"));
    }

    @Test
    void initializeCSV_creates20ZeroLines() throws Exception {
        SubmitRecommendationScreen screen = new SubmitRecommendationScreen(null, null);

        call_initializeCSV(screen);

        assertTrue(Files.exists(VOTES_CSV_PATH));

        List<String> lines = Files.readAllLines(VOTES_CSV_PATH);
        assertEquals(20, lines.size());

        for (String line : lines) {
            assertEquals("0,0,0,0,0,0", line.trim());
        }
    }

    @Test
    void submitRecommendation_updatesCsvRowForMinistry() throws Exception {
        SubmitRecommendationScreen screen = new SubmitRecommendationScreen(null, null);

        call_initializeCSV(screen);

        call_submitRecommendation(screen, "Ministry of Health", 2);

        List<String> lines = Files.readAllLines(VOTES_CSV_PATH);
        assertEquals(20, lines.size());

        String[] parts = lines.get(3).trim().split(",");
        assertEquals("1", parts[0].trim());
        assertEquals("1", parts[3].trim()); 
    }

    private static String[] call_getOptionsForMinistry(SubmitRecommendationScreen screen, String ministry) throws Exception {
        Method m = SubmitRecommendationScreen.class.getDeclaredMethod("getOptionsForMinistry", String.class);
        m.setAccessible(true);
        return (String[]) m.invoke(screen, ministry);
    }

    private static int call_getMinistryIndex(SubmitRecommendationScreen screen, String ministry) throws Exception {
        Method m = SubmitRecommendationScreen.class.getDeclaredMethod("getMinistryIndex", String.class);
        m.setAccessible(true);
        return (int) m.invoke(screen, ministry);
    }

    private static void call_initializeCSV(SubmitRecommendationScreen screen) throws Exception {
        Method m = SubmitRecommendationScreen.class.getDeclaredMethod("initializeCSV");
        m.setAccessible(true);
        m.invoke(screen);
    }

    private static void call_submitRecommendation(SubmitRecommendationScreen screen, String ministryName, int optionIndex) throws Exception {
        Method m = SubmitRecommendationScreen.class.getDeclaredMethod("submitRecommendation", String.class, int.class);
        m.setAccessible(true);
        m.invoke(screen, ministryName, optionIndex);
    }

    private static void resetAllVotes() throws Exception {
        Field f = SubmitRecommendationScreen.class.getDeclaredField("allVotes");
        f.setAccessible(true);
        f.set(null, new int[20][6]);
    }

    private static void backupFolder(Path from, Path to) throws Exception {
        if (!Files.exists(from)) return;

        Files.walk(from).forEach(p -> {
            try {
                Path dest = to.resolve(from.relativize(p).toString());
                if (Files.isDirectory(p)) {
                    Files.createDirectories(dest);
                } else {
                    Files.createDirectories(dest.getParent());
                    Files.copy(p, dest, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (Exception ignored) {}
        });
    }

    private static void restoreFolder(Path from, Path to) throws Exception {
        if (!Files.exists(from)) return;

        Files.walk(from).forEach(p -> {
            try {
                Path dest = to.resolve(from.relativize(p).toString());
                if (Files.isDirectory(p)) {
                    Files.createDirectories(dest);
                } else {
                    Files.createDirectories(dest.getParent());
                    Files.copy(p, dest, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (Exception ignored) {}
        });
    }

    private static void deleteFolderContents(Path dir) throws Exception {
        if (!Files.exists(dir)) return;

        Files.walk(dir)
                .sorted(Comparator.reverseOrder())
                .filter(p -> !p.equals(dir))
                .forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (Exception ignored) {}
                });
    }
}
