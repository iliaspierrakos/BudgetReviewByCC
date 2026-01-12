package guiFolder;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import UserManagement.MinistryMember;
import UserManagement.UserManager;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class ViewRecommendationsScreenTest {

    private static final Path DATA_DIR =
            Path.of("src/main/resources/NecessaryFilesAndData/ProposalsFromCitizens");

    private Path createdFile;

    @BeforeAll
    static void startJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
        
        }
    }

    @AfterEach
    void cleanup() throws Exception {
        if (createdFile != null) {
            Files.deleteIfExists(createdFile);
            createdFile = null;
        }
    }

    @Test
    void pieButtonDisabledWhenNoFileExists() throws Exception {

        MinistryMember member =
                new MinistryMember("user1", "1234", "Interior");

        UserManager userManager = new UserManager();

        Path file = DATA_DIR.resolve("CitizenForMinistry of Interior.txt");
        Files.deleteIfExists(file);

        Stage stage = new Stage();

        runOnFxAndWait(() ->
                new ViewRecommendationsScreen(member, userManager).show(stage)
        );

        Button pieButton = (Button) findByText(stage.getScene(), "View Pie Chart");

        assertNotNull(pieButton);
        assertTrue(pieButton.isDisabled(),
                "Pie Chart button should be disabled when file does not exist");

        runOnFxAndWait(stage::close);
    }

    @Test
    void loadsRowsAndEnablesPieButtonWhenFileExists() throws Exception {

        MinistryMember member =
                new MinistryMember("user2", "1234", "Interior");

        UserManager userManager = new UserManager();

        Files.createDirectories(DATA_DIR);
        createdFile = DATA_DIR.resolve("CitizenForMinistry of Interior.txt");

        String content =
                "Total Votes for Ministry of Interior: 10\n" +
                "Digital public services, Votes from Citizens: 6, 60.00%\n" +
                "Training of public employees, Votes from Citizens: 4, 40.00%\n" +
                "Municipality infrastructure, Votes from Citizens: 0, 0.00%\n";

        Files.writeString(createdFile, content);

        Stage stage = new Stage();

        runOnFxAndWait(() ->
                new ViewRecommendationsScreen(member, userManager).show(stage)
        );

        Button pieButton = (Button) findByText(stage.getScene(), "View Pie Chart");
        assertNotNull(pieButton);
        assertFalse(pieButton.isDisabled(),
                "Pie Chart button should be enabled when votes exist");

        @SuppressWarnings("unchecked")
        TableView<ViewRecommendationsScreen.RecRow> table =
                (TableView<ViewRecommendationsScreen.RecRow>)
                        findFirstByType(stage.getScene().getRoot(), TableView.class);

        assertNotNull(table);
        assertEquals(2, table.getItems().size(),
                "Only categories with votes > 0 should be shown");

        boolean found60 = table.getItems().stream()
                .anyMatch(r -> r.getCategory().contains("Digital")
                        && r.getProbabilityText().equals("60.0%"));

        boolean found40 = table.getItems().stream()
                .anyMatch(r -> r.getCategory().contains("Training")
                        && r.getProbabilityText().equals("40.0%"));

        assertTrue(found60);
        assertTrue(found40);

        runOnFxAndWait(stage::close);
    }

    private static void runOnFxAndWait(Runnable action) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            action.run();
            latch.countDown();
        });
        assertTrue(latch.await(3, TimeUnit.SECONDS));
    }

    private static Node findByText(Scene scene, String text) {
        return findByText(scene.getRoot(), text);
    }

    private static Node findByText(Node node, String text) {
        if (node instanceof Button b && text.equals(b.getText())) {
            return b;
        }
        if (node instanceof javafx.scene.Parent p) {
            for (Node child : p.getChildrenUnmodifiable()) {
                Node found = findByText(child, text);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static Node findFirstByType(Node node, Class<?> type) {
        if (type.isInstance(node)) return node;

        if (node instanceof javafx.scene.Parent p) {
            for (Node child : p.getChildrenUnmodifiable()) {
                Node found = findFirstByType(child, type);
                if (found != null) return found;
            }
        }
        return null;
    }
}
