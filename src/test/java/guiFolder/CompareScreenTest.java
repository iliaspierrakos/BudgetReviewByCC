package guiFolder;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class CompareScreenTest {

    private Locale oldLocale;

    private double parseBudgetToDouble(String text) {
        try {
            Method m = CompareScreen.class.getDeclaredMethod("parseBudgetToDouble", String.class);
            m.setAccessible(true);
            return (double) m.invoke(null, text);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String formatDelta(double value) {
        try {
            Method m = CompareScreen.class.getDeclaredMethod("formatDelta", double.class);
            m.setAccessible(true);
            return (String) m.invoke(null, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void setUp() {
        oldLocale = Locale.getDefault();
        Locale.setDefault(Locale.US);
    }

    @AfterEach
    void tearDown() {
        Locale.setDefault(oldLocale);
    }

    @Test
    void parseBudgetToDouble_nullOrEmpty_returnsZero() {
        assertEquals(0.0, parseBudgetToDouble(null), 1e-9);
        assertEquals(0.0, parseBudgetToDouble(""), 1e-9);
        assertEquals(0.0, parseBudgetToDouble("   "), 1e-9);
        assertEquals(0.0, parseBudgetToDouble("-"), 1e-9);
    }

    @Test
    void parseBudgetToDouble_plainNumbers_work() {
        assertEquals(1234.0, parseBudgetToDouble("1234"), 1e-9);
        assertEquals(-1234.0, parseBudgetToDouble("-1234"), 1e-9);
        assertEquals(12.34, parseBudgetToDouble("12.34"), 1e-9);
        assertEquals(12.34, parseBudgetToDouble("12,34"), 1e-9);
    }

    @Test
    void parseBudgetToDouble_usFormat_works() {
        assertEquals(1234567.89, parseBudgetToDouble("1,234,567.89"), 1e-9);
        assertEquals(-1234567.89, parseBudgetToDouble("-1,234,567.89"), 1e-9);
    }

    @Test
    void parseBudgetToDouble_euFormat_works() {
        assertEquals(1234567.89, parseBudgetToDouble("1.234.567,89"), 1e-9);
        assertEquals(-1234567.89, parseBudgetToDouble("-1.234.567,89"), 1e-9);
    }

    @Test
    void parseBudgetToDouble_ignoresCurrencyAndText() {
        assertEquals(1234.50, parseBudgetToDouble("€ 1,234.50"), 1e-9);
        assertEquals(1234.50, parseBudgetToDouble("USD 1,234.50 abc"), 1e-9);
        assertEquals(-1234.50, parseBudgetToDouble("- €1,234.50"), 1e-9);
    }

    @Test
    void parseBudgetToDouble_garbage_returnsZero() {
        assertEquals(0.0, parseBudgetToDouble("hello"), 1e-9);
        assertEquals(0.0, parseBudgetToDouble("..,,--"), 1e-9);
    }

    @Test
    void formatDelta_positive_negative_zero_formatIsCorrect() {
        assertEquals("+1.00", formatDelta(1.0));
        assertEquals("-1.00", formatDelta(-1.0));
        assertEquals("0.00", formatDelta(0.0));
    }

    @Test
    void formatDelta_addsThousandsSeparatorForLargeNumbers() {
        assertEquals("+1,234.50", formatDelta(1234.5));
        assertEquals("-1,234.50", formatDelta(-1234.5));
    }

    @Test
    void formatDelta_then_parseBudgetToDouble_givesBackSameValueRoundedTo2Decimals() {
        double value = 1234.567;
        String formatted = formatDelta(value);
        double parsedBack = parseBudgetToDouble(formatted);
        assertEquals(1234.57, parsedBack, 1e-9);
    }
}
