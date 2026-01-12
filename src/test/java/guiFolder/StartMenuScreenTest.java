package guiFolder;

import UserManagement.UserManager;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class StartMenuScreenTest {

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            
        }
    }

    @Test
    void constructor_storesUserManager() throws Exception {
        UserManager userManager = null;
        StartMenuScreen screen = new StartMenuScreen(userManager);

        Field f = StartMenuScreen.class.getDeclaredField("userManager");
        f.setAccessible(true);

        assertSame(userManager, f.get(screen));
    }

    @Test
    void show_setsStageTitleAndScene() throws Exception {
        runOnFxThread(() -> {
            StartMenuScreen screen = new StartMenuScreen(null);
            Stage stage = new Stage();

            screen.show(stage);

            assertNotNull(stage.getScene());
            assertEquals("Welcome", stage.getTitle());

            stage.close();
        });
    }

    @Test
    void show_createsButtonsWithCorrectText() throws Exception {
        runOnFxThread(() -> {
            StartMenuScreen screen = new StartMenuScreen(null);
            Stage stage = new Stage();

            screen.show(stage);

            Scene scene = stage.getScene();
            assertNotNull(scene);

            List<Button> buttons = findButtons(scene.getRoot());
            assertEquals(3, buttons.size());

            List<String> texts = buttons.stream().map(Button::getText).toList();
            assertTrue(texts.contains("Login"));
            assertTrue(texts.contains("Create account"));
            assertTrue(texts.contains("Exit"));

            stage.close();
        });
    }

    @Test
    void buttons_haveActionHandlers() throws Exception {
        runOnFxThread(() -> {
            StartMenuScreen screen = new StartMenuScreen(null);
            Stage stage = new Stage();

            screen.show(stage);

            List<Button> buttons = findButtons(stage.getScene().getRoot());

            Button login = buttons.stream().filter(b -> b.getText().equals("Login")).findFirst().orElseThrow();
            Button register = buttons.stream().filter(b -> b.getText().equals("Create account")).findFirst().orElseThrow();
            Button exit = buttons.stream().filter(b -> b.getText().equals("Exit")).findFirst().orElseThrow();

            assertNotNull(login.getOnAction());
            assertNotNull(register.getOnAction());
            assertNotNull(exit.getOnAction());

            stage.close();
        });
    }

    private static List<Button> findButtons(Node root) {
        List<Button> result = new ArrayList<>();

        if (root instanceof Button b) result.add(b);

        if (root instanceof VBox v) {
            for (Node n : v.getChildren()) result.addAll(findButtons(n));
        }

        if (root instanceof StackPane s) {
            for (Node n : s.getChildren()) result.addAll(findButtons(n));
        }

        return result;
    }

    private static void runOnFxThread(ThrowingRunnable r) throws Exception {
        if (Platform.isFxApplicationThread()) {
            r.run();
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                r.run();
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });

        if (!latch.await(5, TimeUnit.SECONDS)) {
            fail("Timeout waiting for FX thread.");
        }

        if (error.get() != null) {
            Throwable t = error.get();
            if (t instanceof Exception e) throw e;
            if (t instanceof Error e) throw e;
            throw new RuntimeException(t);
        }
    }

    @FunctionalInterface
    interface ThrowingRunnable {
        void run() throws Exception;
    }
}
