package guiFolder;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import javafx.application.Platform;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ViewRecommendationStatisticsScreenTest {

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {

        }
    }

    @TempDir
    Path tempDir;

    @Test
    void extractMinistryName_removesCitizenForPrefixAndTxt() throws Exception {
        ViewRecommendationStatisticsScreen screen =
                new ViewRecommendationStatisticsScreen(null, null);

        Method extract = ViewRecommendationStatisticsScreen.class
                .getDeclaredMethod("extractMinistryName", String.class);
        extract.setAccessible(true);

        String a = (String) extract.invoke(screen, "CitizenForMinistry of Interior.txt");
        assertEquals("Ministry of Interior", a);

        String b = (String) extract.invoke(screen, "CitizenForInterior.txt");
        assertEquals("Interior", b);

        String c = (String) extract.invoke(screen, "CitizenForMinistry of Health.txt");
        assertEquals("Ministry of Health", c);
    }

    @Test
    void normalizeMinistryTitle_addsMinistryOfOnlyWhenNeeded() throws Exception {
        ViewRecommendationStatisticsScreen screen =
                new ViewRecommendationStatisticsScreen(null, null);

        Method normalize = ViewRecommendationStatisticsScreen.class
                .getDeclaredMethod("normalizeMinistryTitle", String.class);
        normalize.setAccessible(true);

        String a = (String) normalize.invoke(screen, "Interior");
        assertEquals("Ministry of Interior", a);

        String b = (String) normalize.invoke(screen, "Ministry of Interior");
        assertEquals("Ministry of Interior", b);
    }

    @Test
    void createProbabilityChartForFile_readsVotesAndCreatesChart() throws Exception {
        Path f = tempDir.resolve("CitizenForMinistry of Interior.txt");

        String fileText = ""
                + "Total Votes for Ministry of Interior: 10\n"
                + "Digital public services, Votes from Citizens: 6, 60.00%\n"
                + "Training of public employees, Votes from Citizens: 4, 40.00%\n"
                + "Municipality infrastructure, Votes from Citizens: 0, 0.00%\n";

        Files.writeString(f, fileText);

        ViewRecommendationStatisticsScreen screen =
                new ViewRecommendationStatisticsScreen(null, null);

        Method m = ViewRecommendationStatisticsScreen.class
                .getDeclaredMethod("createProbabilityChartForFile", Path.class);
        m.setAccessible(true);

        PieChart chart = (PieChart) m.invoke(screen, f);

        assertNotNull(chart, "Chart should be created when total votes > 0");
        assertTrue(chart.getTitle().contains("Total: 10 votes"), "Title should include total votes");

        assertEquals(2, chart.getData().size());

        double p1 = chart.getData().get(0).getPieValue();
        double p2 = chart.getData().get(1).getPieValue();
        assertTrue(p1 > 0 && p1 < 1);
        assertTrue(p2 > 0 && p2 < 1);

        assertEquals(1.0, p1 + p2, 0.0001);
    }

    @Test
    void createSummaryBox_calculatesTotalsCorrectly() throws Exception {
        Path f = tempDir.resolve("CitizenForMinistry of Health.txt");

        String fileText = ""
                + "Total Votes for Ministry of Health: 7\n"
                + "More doctors and nurses, Votes from Citizens: 2, 28.57%\n"
                + "More ambulances, Votes from Citizens: 0, 0.00%\n"
                + "Hospital upgrades, Votes from Citizens: 5, 71.43%\n";

        Files.writeString(f, fileText);

        ViewRecommendationStatisticsScreen screen =
                new ViewRecommendationStatisticsScreen(null, null);

        Method m = ViewRecommendationStatisticsScreen.class
                .getDeclaredMethod("createSummaryBox", Path.class);
        m.setAccessible(true);

        VBox box = (VBox) m.invoke(screen, f);

        assertNotNull(box, "Summary box should exist");
        assertFalse(box.getChildren().isEmpty(), "Summary box should have labels");

        String combined = box.getChildren().toString();
        assertTrue(combined.contains("Total Votes: 7"), "Should mention total votes = 7");
        assertTrue(combined.contains("Categories with votes: 2/3"), "2 categories had votes > 0 out of 3");
    }
}
