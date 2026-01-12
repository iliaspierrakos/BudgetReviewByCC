package guiFolder;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import UserManagement.User;
import UserManagement.UserManager;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class VirtualEditScreenTest {

    @BeforeAll
    static void startJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
        
        }
    }

    @Test
    void showShouldSetSceneAndTitle() throws Exception {
        User user = new User("u1", "1234", User.Role.CITIZEN);
        UserManager userManager = new UserManager();

        Stage stage = new Stage();

        runOnFxAndWait(() -> new VirtualEditScreen(user, userManager).show(stage));

        assertNotNull(stage.getScene(), "Stage should have a Scene after show()");
        assertEquals("Virtual Edit", stage.getTitle(), "Stage title should be 'Virtual Edit'");

        runOnFxAndWait(stage::close);
    }

    @Test
    void screenShouldContainMainTextsAndCards() throws Exception {
        User user = new User("u2", "1234", User.Role.CITIZEN);
        UserManager userManager = new UserManager();

        Stage stage = new Stage();

        runOnFxAndWait(() -> new VirtualEditScreen(user, userManager).show(stage));

        Scene scene = stage.getScene();
        assertNotNull(scene);

        assertNotNull(findLabelByText(scene, "Virtual Edit"), "Should show title 'Virtual Edit'");

        assertNotNull(
                findLabelByText(scene, "Simulate budget changes without affecting official data."),
                "Should show subtitle"
        );

        Label balanceLabel = findLabelStartsWith(scene, "Balance:");
        assertNotNull(balanceLabel, "Should show balance chip starting with 'Balance:'");

        assertNotNull(findLabelByText(scene, "Simple Virtual Edit"));
        assertNotNull(findLabelByText(scene, "Bulk Virtual Edit"));
        assertNotNull(findLabelByText(scene, "Edit History"));
        assertNotNull(findLabelByText(scene, "Reset"));

        Button backBtn = findButtonByText(scene, "Back");
        assertNotNull(backBtn, "Back button should exist");
        assertNotNull(backBtn.getOnAction(), "Back button should have an action handler");

        runOnFxAndWait(stage::close);
    }

    private static void runOnFxAndWait(Runnable action) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(3, TimeUnit.SECONDS), "FX thread timeout");
    }

    private static Label findLabelByText(Scene scene, String text) {
        Node n = findNode(scene.getRoot(), node -> (node instanceof Label l) && text.equals(l.getText()));
        return (Label) n;
    }

    private static Label findLabelStartsWith(Scene scene, String prefix) {
        Node n = findNode(scene.getRoot(), node -> {
            if (!(node instanceof Label l)) return false;
            String t = l.getText();
            return t != null && t.startsWith(prefix);
        });
        return (Label) n;
    }

    private static Button findButtonByText(Scene scene, String text) {
        Node n = findNode(scene.getRoot(), node -> (node instanceof Button b) && text.equals(b.getText()));
        return (Button) n;
    }

    private static Node findNode(Node root, java.util.function.Predicate<Node> pred) {
        if (pred.test(root)) return root;

        if (root instanceof javafx.scene.Parent p) {
            for (Node child : p.getChildrenUnmodifiable()) {
                Node found = findNode(child, pred);
                if (found != null) return found;
            }
        }
        return null;
    }
}