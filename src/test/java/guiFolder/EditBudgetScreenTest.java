package guiFolder;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class EditBudgetScreenTest {

    @BeforeAll
    static void startJavaFx() throws Exception {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(latch::countDown);
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (IllegalStateException alreadyStarted) {
        }
    }

    @Test
    void applyScenePreserveWindow_whenStageNotShowing_setsSceneTitleAndShows() throws Exception {
        Fx.run(() -> {
            Stage stage = new Stage();
            Scene scene = new Scene(new VBox(), 400, 300);

            call_applyScenePreserveWindow(stage, scene, "Edit Budget");

            assertEquals("Edit Budget", stage.getTitle());
            assertSame(scene, stage.getScene());
            assertTrue(stage.isShowing());
            stage.close();
        });
    }

    @Test
    void applyScenePreserveWindow_whenStageShowing_preservesWindowBounds() throws Exception {
        Fx.run(() -> {
            Stage stage = new Stage();
            stage.setWidth(800);
            stage.setHeight(600);
            stage.setX(120);
            stage.setY(90);

            Scene scene1 = new Scene(new VBox(), 800, 600);
            stage.setScene(scene1);
            stage.show();

            double oldW = stage.getWidth();
            double oldH = stage.getHeight();
            double oldX = stage.getX();
            double oldY = stage.getY();

            Scene scene2 = new Scene(new VBox(), 300, 200);

            call_applyScenePreserveWindow(stage, scene2, "New Title");

            assertEquals("New Title", stage.getTitle());
            assertSame(scene2, stage.getScene());
            assertEquals(oldW, stage.getWidth(), 0.0001);
            assertEquals(oldH, stage.getHeight(), 0.0001);
            assertEquals(oldX, stage.getX(), 0.0001);
            assertEquals(oldY, stage.getY(), 0.0001);

            stage.close();
        });
    }

    @Test
    void applyScenePreserveWindow_whenStageMaximized_preservesMaximizedState() throws Exception {
        Fx.run(() -> {
            Stage stage = new Stage();
            stage.setScene(new Scene(new VBox(), 800, 600));
            stage.show();

            stage.setMaximized(true);
            assertTrue(stage.isMaximized());

            Scene newScene = new Scene(new VBox(), 200, 200);
            call_applyScenePreserveWindow(stage, newScene, "Max Test");

            assertEquals("Max Test", stage.getTitle());
            assertTrue(stage.isMaximized(), "Το maximized state πρέπει να παραμείνει true.");

            stage.close();
        });
    }

    @Test
    void buildSidePanel_containsExpectedTexts() throws Exception {
        Fx.run(() -> {
            EditBudgetScreen screen = new EditBudgetScreen(null, null);

            VBox side = call_buildSidePanel(screen);
            String allText = side.lookupAll(".label")
                    .stream()
                    .map(n -> ((javafx.scene.control.Label) n).getText())
                    .reduce("", (a, b) -> a + "\n" + b);

            assertTrue(allText.contains("Editing modes"));
            assertTrue(allText.contains("• Simple Edit: change one ministry"));
            assertTrue(allText.contains("• Bulk Edit: apply changes to many"));
            assertTrue(allText.contains("• History: review audit trail"));

            assertTrue(allText.contains("Balance rules"));
            assertTrue(allText.contains("• Increase uses available balance"));
            assertTrue(allText.contains("• Decrease returns balance back"));
            assertTrue(allText.contains("• You can’t decrease below 0"));
        });
    }

    private static void call_applyScenePreserveWindow(Stage stage, Scene scene, String title) throws Exception {
        Method m = EditBudgetScreen.class.getDeclaredMethod(
                "applyScenePreserveWindow", Stage.class, Scene.class, String.class
        );
        m.setAccessible(true);
        m.invoke(null, stage, scene, title);
    }

    private static VBox call_buildSidePanel(EditBudgetScreen screen) throws Exception {
        Method m = EditBudgetScreen.class.getDeclaredMethod("buildSidePanel");
        m.setAccessible(true);
        return (VBox) m.invoke(screen);
    }

    private static final class Fx {
        static void run(FxAction action) throws Exception {
            if (Platform.isFxApplicationThread()) {
                action.run();
                return;
            }

            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<Throwable> err = new AtomicReference<>();

            Platform.runLater(() -> {
                try {
                    action.run();
                } catch (Throwable t) {
                    err.set(t);
                } finally {
                    latch.countDown();
                }
            });

            if (!latch.await(5, TimeUnit.SECONDS)) {
                fail("Timeout στο FX thread.");
            }
            if (err.get() != null) {
                Throwable t = err.get();
                if (t instanceof Exception e) throw e;
                if (t instanceof Error e) throw e;
                throw new RuntimeException(t);
            }
        }

        @FunctionalInterface
        interface FxAction {
            void run() throws Exception;
        }
    }
}
