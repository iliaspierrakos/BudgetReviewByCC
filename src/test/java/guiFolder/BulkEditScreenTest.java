package guiFolder;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import org.junit.jupiter.api.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class BulkEditScreenTest {

    @BeforeAll
    static void startJavaFx() throws Exception {
        if (!Fx.isStarted()) {
            Fx.start();
        }
    }
    @Test
    void previewRow_gettersReturnConstructorValues() {
        BulkEditScreen.PreviewRow row =
                new BulkEditScreen.PreviewRow("Health", "100", "120", "+20");

        assertAll(
                () -> assertEquals("Health", row.getMinistry()),
                () -> assertEquals("100", row.getPrevious()),
                () -> assertEquals("120", row.getNow()),
                () -> assertEquals("+20", row.getDelta())
        );
    }
    @Test
    void previewRow_allowsEmptyStrings() {
        BulkEditScreen.PreviewRow row =
                new BulkEditScreen.PreviewRow("", "", "", "");

        assertAll(
                () -> assertEquals("", row.getMinistry()),
                () -> assertEquals("", row.getPrevious()),
                () -> assertEquals("", row.getNow()),
                () -> assertEquals("", row.getDelta())
        );
    }
    @Test
    void ministryPickRow_gettersReturnConstructorValues() {
        BulkEditScreen.MinistryPickRow row =
                new BulkEditScreen.MinistryPickRow(7, "Education", "999");

        assertAll(
                () -> assertEquals(7, row.getIndex()),
                () -> assertEquals("Education", row.getMinistry()),
                () -> assertEquals("999", row.getCurrent())
        );
    }
    @Test
    void ministryPickRow_storesNegativeIndexAsIs() {
        BulkEditScreen.MinistryPickRow row =
                new BulkEditScreen.MinistryPickRow(-1, "X", "Y");

        assertAll(
                () -> assertEquals(-1, row.getIndex()),
                () -> assertEquals("X", row.getMinistry()),
                () -> assertEquals("Y", row.getCurrent())
        );
    }
    @Test
    void safeIcon_whenResourceMissing_returnsFallbackLabelWithStyleClass() throws Exception {
        BulkEditScreen screen = constructWithNulls();

        Method safeIcon = BulkEditScreen.class.getDeclaredMethod("safeIcon", String.class, double.class);
        safeIcon.setAccessible(true);

        Node node = Fx.call(() -> (Node) safeIcon.invoke(screen, "/icons/__definitely_missing__.png", 34.0));

        assertNotNull(node);
        assertInstanceOf(Label.class, node);

        Label fallback = (Label) node;
        assertTrue(
                fallback.getStyleClass().contains("icon-fallback"),
                "Expected fallback Label to have style class 'icon-fallback'."
        );
    }
    @Test
    void formatDelta_whenZero_returnsLiteralZero() throws Exception {
        BulkEditScreen screen = constructWithNulls();

        Method formatDelta = BulkEditScreen.class.getDeclaredMethod("formatDelta", double.class);
        formatDelta.setAccessible(true);

        String s = Fx.call(() -> (String) formatDelta.invoke(screen, 0.0));

        assertEquals("0", s);
    }
    private static BulkEditScreen constructWithNulls() throws Exception {
        for (Constructor<?> c : BulkEditScreen.class.getConstructors()) {
            if (c.getParameterCount() == 2) {
                return (BulkEditScreen) c.newInstance(null, null);
            }
        }
        throw new IllegalStateException("BulkEditScreen(User, UserManager) public constructor not found.");
    }
    static final class Fx {
        private static volatile boolean started = false;

        static boolean isStarted() {
            return started;
        }
        static void start() throws Exception {
            if (started) return;

            CountDownLatch latch = new CountDownLatch(1);
            try {
                Platform.startup(() -> {
                    started = true;
                    latch.countDown();
                });
            } catch (IllegalStateException alreadyStarted) {
                started = true;
                latch.countDown();
            }

            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("JavaFX Platform did not start in time.");
            }
        }
        static <T> T call(CallableOnFx<T> action) throws Exception {
            if (!started) start();
            if (Platform.isFxApplicationThread()) {
                return action.call();
            }

            AtomicReference<T> result = new AtomicReference<>();
            AtomicReference<Throwable> err = new AtomicReference<>();
            CountDownLatch latch = new CountDownLatch(1);

            Platform.runLater(() -> {
                try {
                    result.set(action.call());
                } catch (Throwable t) {
                    err.set(t);
                } finally {
                    latch.countDown();
                }
            });

            if (!latch.await(5, TimeUnit.SECONDS)) {
                fail("Timed out waiting for FX task.");
            }

            if (err.get() != null) {
                Throwable t = err.get();
                if (t instanceof Exception e) throw e;
                if (t instanceof Error e) throw e;
                throw new RuntimeException(t);
            }

            return result.get();
        }

        @FunctionalInterface
        interface CallableOnFx<T> {
            T call() throws Exception;
        }
    }
}
