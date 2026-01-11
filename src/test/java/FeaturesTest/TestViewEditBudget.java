package FeaturesTest;

import UserFeatures.CreatingMinistries;
import UserFeatures.Ministry;
import UserFeatures.ViewEditBudget;
import UserManagement.User;
import UserManagement.MinistryMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestViewEditBudget {

    @BeforeEach
    void setup() {
        CreatingMinistries.ministries2026 = new Ministry[]{
                new Ministry("Ministry of Health", 1000)
        };
    }

    /* ===============================
       viewBudget
       =============================== */

    @Test
    void testViewBudgetValidYear() {
        Ministry[] result = ViewEditBudget.viewBudget(2026);

        assertNotNull(result);
        assertEquals("Ministry of Health", result[0].getMinistryName());
    }

    @Test
    void testViewBudgetInvalidYearThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> ViewEditBudget.viewBudget(2019));
    }

    /* ===============================
       edit
       =============================== */

    @Test
    void testEditInvalidTypeThrows() {
        User user = new User("u", "p", User.Role.CITIZEN);

        assertThrows(IllegalArgumentException.class,
                () -> ViewEditBudget.edit(user, 0));
    }

    @Test
    void testEditBulkForMinistryMemberUnsupported() {
        MinistryMember mm =
                new MinistryMember("m", "p", "Ministry of Health");

        assertThrows(UnsupportedOperationException.class,
                () -> ViewEditBudget.edit(mm, 2));
    }

    /* ===============================
       recommendations
       =============================== */

    @Test
    void testRecommendationsForMinistryMemberUnsupported() {
        MinistryMember mm =
                new MinistryMember("m", "p", "Ministry of Health");

        assertThrows(UnsupportedOperationException.class,
                () -> ViewEditBudget.recommendations(mm));
    }

    /* ===============================
       taxReceipt
       =============================== */

    @Test
    void testTaxReceiptOnlyCitizenAllowed() {
        User gov = new User("g", "p", User.Role.GOVERNOR);

        assertThrows(IllegalStateException.class,
                () -> ViewEditBudget.taxReceipt(gov));
    }

    @Test
    void testTaxReceiptCitizenUnsupportedFlow() {
        User citizen = new User("c", "p", User.Role.CITIZEN);

        assertThrows(UnsupportedOperationException.class,
                () -> ViewEditBudget.taxReceipt(citizen));
    }

    /* ===============================
       initialization
       =============================== */

    @Test
    void testEnsureInitializedIdempotent() {
        assertDoesNotThrow(ViewEditBudget::ensureInitialized);
        assertDoesNotThrow(ViewEditBudget::ensureInitialized);
    }
}
