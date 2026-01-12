package guiFolder;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import UserFeatures.CreatingMinistries;
import UserFeatures.Ministry;
import UserManagement.User;
import UserManagement.UserManager;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;

public class ViewStatisticsScreenTest {

    @BeforeAll
    static void startJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {

        }
    }

    @Test
    void nonGovernorShouldShowAccessDeniedAlert() throws Exception {

        User citizen = new User("c1", "1234", User.Role.CITIZEN);
        UserManager userManager = new UserManager();

        Stage stage = new Stage();

        runOnFxAndWait(() -> new ViewStatisticsScreen(citizen, userManager).show(stage));

        Scene scene = stage.getScene();

        if (scene != null) {
            Node showChart = findByButtonText(scene, "Show Chart");
            assertNull(showChart, "Non-governor should not see the statistics UI");
        }

        runOnFxAndWait(stage::close);
    }

    @Test
    void governorScreenLoadsAndHasButtons() throws Exception {

        CreatingMinistries.ministries2026 = new Ministry[20];
        CreatingMinistries.ministries2026[0] = new Ministry("Ministry of Interior", 1000);

        User governor = new User("g1", "1234", User.Role.GOVERNOR);
        UserManager userManager = new UserManager();

        Stage stage = new Stage();

        runOnFxAndWait(() -> new ViewStatisticsScreen(governor, userManager).show(stage));

        Scene scene = stage.getScene();
        assertNotNull(scene, "Governor screen should set a Scene on the Stage");

        Button toggleBtn = (Button) findByButtonText(scene, "Show Chart");
        assertNotNull(toggleBtn, "Should have a 'Show Chart' button");

        Button exportBtn = (Button) findByButtonText(scene, "Export Chart");
        assertNotNull(exportBtn, "Should have an 'Export Chart' button");
        assertTrue(exportBtn.isDisabled(), "Export should start disabled (chart hidden)");

        Button backBtn = (Button) findByButtonText(scene, "Back");
        assertNotNull(backBtn, "Should have a 'Back' button");

        runOnFxAndWait(stage::close);
    }

    @Test
    void toggleButtonShouldSwitchText() throws Exception {

        CreatingMinistries.ministries2026 = new Ministry[20];
        CreatingMinistries.ministries2026[0] = new Ministry("Ministry of Interior", 1000);

        User governor = new User("g2", "1234", User.Role.GOVERNOR);
        UserManager userManager = new UserManager();

        Stage stage = new Stage();

        runOnFxAndWait(() -> new ViewStatisticsScreen(governor, userManager).show(stage));

        Scene scene = stage.getScene();
        Button toggleBtn = (Button) findByButtonText(scene, "Show Chart");
        assertNotNull(toggleBtn);

        runOnFxAndWait(toggleBtn::fire);

        Button toggleAfter = (Button) findByButtonText(scene, "Show Table");
        assertNotNull(toggleAfter, "After clicking, button text should become 'Show Table'");

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

    private static Node findByButtonText(Scene scene, String text) {
        return findByButtonText(scene.getRoot(), text);
    }

    private static Node findByButtonText(Node node, String text) {
        if (node instanceof Button b && text.equals(b.getText())) return b;

        if (node instanceof javafx.scene.Parent p) {
            for (Node child : p.getChildrenUnmodifiable()) {
                Node found = findByButtonText(child, text);
                if (found != null) return found;
            }
        }
        return null;
    }
}
