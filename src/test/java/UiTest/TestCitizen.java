package UserManagement;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class TestCitizen {

    @Test
    public void testConstructorSetsCitizenRole() {
        Citizen citizen = new Citizen("john_doe", "mypassword");

        assertEquals("john_doe", citizen.getUsername(), "Username should be assigned correctly");
        assertEquals("mypassword", citizen.getPassword(), "Password should be stored correctly");
        assertEquals(User.Role.CITIZEN, citizen.getRole(), "Citizen role should always be CITIZEN");
    }

    @Test
    public void testToString() {
        Citizen citizen = new Citizen("maria", "12345");

        String expected = "Citizen: maria";
        assertEquals(expected, citizen.toString(), "toString() output must match expected format");
    }
}
