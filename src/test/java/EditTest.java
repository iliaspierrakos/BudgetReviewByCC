/**
 * Unit tests for Edit class.
 * The tests verify both the increases and the decreases for 
 * each ministry's budget. 
 */
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class EditTest {

    @BeforeEach
    void setup() {
        CreatingMinistries.ministries = new Ministry[3];

        CreatingMinistries.ministries[0] =
                new Ministry("Ministry of Health", 1000);

        CreatingMinistries.ministries[1] =
                new Ministry("Ministry of Education", 2000);

        CreatingMinistries.ministries[2] =
                new Ministry("Ministry of Finance", 3000);

        Edit.balance = 0;
    }

    @Test
    void testIncreaseBudget() {
        Edit edit = new Edit("Ministry of Health", "Increase", 500);

        edit.editingbudget(edit);

        double updated = CreatingMinistries.ministries[0].getBudget();
        assertEquals(1500, updated);
    }

    @Test
    void testDecreaseBudget() {
        Edit edit = new Edit("Ministry of Finance", "Decrease", 800);

        edit.editingbudget(edit);

        double updated = CreatingMinistries.ministries[2].getBudget();
        assertEquals(2200, updated);
    }
}
 

}