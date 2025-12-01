package UserManagement;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class TestGovernor {

    @Test
    public void testConstructorSetsGovernorRole() {
        Governor gov = new Governor("pm", "securePass!");

        assertEquals("pm", gov.getUsername(), "Username should be assigned correctly");
        assertEquals("securePass!", gov.getPassword(), "Password should be stored correctly");

        // Role must be Governor because Governor extends User
        assertEquals(User.Role.GOVERNOR, gov.getRole(), "Governor role should always be GOVERNOR");
    }

    @Test
    public void testToString() {
        Governor gov = new Governor("alex", "pass123");

        String expected = "Governor: alex";
        assertEquals(expected, gov.toString(), "toString() output must match expected format");
    }
}
