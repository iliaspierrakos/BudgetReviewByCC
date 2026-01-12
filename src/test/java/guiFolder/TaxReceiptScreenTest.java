package guiFolder;

import UserFeatures.CreatingMinistries;
import UserFeatures.Ministry;
import org.junit.jupiter.api.*;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaxReceiptScreenTest {

    private Ministry[] old2026;

    @BeforeEach
    void setUp() {

        old2026 = CreatingMinistries.ministries2026;

        CreatingMinistries.ministries2026 = new Ministry[20];
        CreatingMinistries.ministries2026[0] = new Ministry("Ministry A", 100.0);
        CreatingMinistries.ministries2026[1] = new Ministry("Ministry B", 300.0);
        CreatingMinistries.ministries2026[2] = new Ministry("Ministry C", 600.0);

    }

    @AfterEach
    void tearDown() {
        CreatingMinistries.ministries2026 = old2026;
    }

    @Test
    void calculateTax_basicCase_noKids_age30() throws Exception {
        TaxReceiptScreen screen = new TaxReceiptScreen(null, null);

        double tax = call_calculateTax(screen, 10_000, 0, 30);

        assertEquals(900.0, tax, 0.0001);
    }

    @Test
    void calculateTax_youngAgeUnder25_taxIsZeroForFirstTwoBrackets() throws Exception {
        TaxReceiptScreen screen = new TaxReceiptScreen(null, null);

        double tax = call_calculateTax(screen, 20_000, 0, 25);

        assertEquals(0.0, tax, 0.0001);
    }

    @Test
    void calculateTax_fourKids_rate1ZeroAndRate2Zero() throws Exception {
        TaxReceiptScreen screen = new TaxReceiptScreen(null, null);

        double tax = call_calculateTax(screen, 20_000, 4, 40);

        assertEquals(0.0, tax, 0.0001);
    }

    @Test
    void calculateDistribution_splitsTaxProportionally_andAddsTotalRow() throws Exception {
        TaxReceiptScreen screen = new TaxReceiptScreen(null, null);

        double tax = 100.0;
        List<TaxReceiptScreen.TaxRow> rows = call_calculateDistribution(screen, tax);

        assertEquals(4, rows.size());

        assertEquals("Ministry A", rows.get(0).getMinistry());
        assertEquals("10.00", rows.get(0).getShareText());

        assertEquals("Ministry B", rows.get(1).getMinistry());
        assertEquals("30.00", rows.get(1).getShareText());

        assertEquals("Ministry C", rows.get(2).getMinistry());
        assertEquals("60.00", rows.get(2).getShareText());

        assertEquals("TOTAL TAX PAID", rows.get(3).getMinistry());
        assertEquals("100.00", rows.get(3).getShareText());
    }

    private static double call_calculateTax(TaxReceiptScreen screen, double income, int kids, int age) throws Exception {
        Method m = TaxReceiptScreen.class.getDeclaredMethod("calculateTax", double.class, int.class, int.class);
        m.setAccessible(true);
        return (double) m.invoke(screen, income, kids, age);
    }

    @SuppressWarnings("unchecked")
    private static List<TaxReceiptScreen.TaxRow> call_calculateDistribution(TaxReceiptScreen screen, double tax) throws Exception {
        Method m = TaxReceiptScreen.class.getDeclaredMethod("calculateDistribution", double.class);
        m.setAccessible(true);
        return (List<TaxReceiptScreen.TaxRow>) m.invoke(screen, tax);
    }
}
