package FeaturesTest;

import UserFeatures.*;

import org.junit.Assert;
import org.junit.Test;
import java.util.ArrayList;

/**
 * Tests the logic for bulk editing multiple ministries.
 */
public class TestBulkEdit {

    /**
     * Verifies that user input strings are correctly converted to ministry indices.
     */
    @Test
    public void testFillingListWithIndex() {
        BulkEdit bulkEdit = new BulkEdit();
        CreatingMinistries.ministries2026 = new Ministry[20];
        CreatingMinistries.ministries2026[0] = new Ministry("Test 1", 100);
        CreatingMinistries.ministries2026[1] = new Ministry("Test 2", 200);

        // User input "1" corresponds to index 0, "2" to index 1
        String input = "1, 2, 99"; 
        ArrayList<Integer> indices = bulkEdit.fillingListWithIndex(input);

        Assert.assertEquals("failure wrong size", 2, indices.size());
        
        Assert.assertTrue("failure does not contain index 0", indices.contains(0));
        Assert.assertTrue("failure does not contain index 1", indices.contains(1));
    }
}