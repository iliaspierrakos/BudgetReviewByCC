package UserManagement;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class TestUser {

    @Test
    public void testUserConstructorAndGetters() {
        User user = new User("nikos", "pass123", User.Role.CITIZEN);

        assertEquals("nikos", user.getUsername(), "Username should be stored correctly");
        assertEquals("pass123", user.getPassword(), "Password should be stored correctly");
        assertEquals(User.Role.CITIZEN, user.getRole(), "Role should match constructor value");
    }

    @Test
    public void testToString() {
        User user = new User("anna", "mypass", User.Role.MINISTRYMEMBER);

        String expected = "anna (MINISTRYMEMBER)";
        assertEquals(expected, user.toString(), "toString() format is incorrect");
    }
}
