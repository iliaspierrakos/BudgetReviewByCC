package UserManagementTest;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import UserManagement.User;
import UserManagement.User.Role;

/**
 * Unit tests for {@link User}.
 *
 * <p>
 * These tests verify correct initialization, accessor behavior,
 * role assignment, and string representation.
 * </p>
 */
public class TestUser {

    /**
     * Verifies that the constructor correctly initializes all fields.
     */
    @Test
    void testUserConstructorAndGetters() {
        User user = new User("john", "secret", Role.CITIZEN);

        assertEquals("john", user.getUsername(),
                "Username should be set by constructor");
        assertEquals("secret", user.getPassword(),
                "Password should be set by constructor");
        assertEquals(Role.CITIZEN, user.getRole(),
                "Role should be set by constructor");
    }

    /**
     * Verifies that different roles can be assigned correctly.
     */
    @Test
    void testUserRoles() {
        User governor = new User("gov", "pass", Role.GOVERNOR);
        User member = new User("member", "pass", Role.MINISTRYMEMBER);

        assertEquals(Role.GOVERNOR, governor.getRole(),
                "Governor role should be assigned correctly");
        assertEquals(Role.MINISTRYMEMBER, member.getRole(),
                "Ministry member role should be assigned correctly");
    }

    /**
     * Verifies the string representation format of the user.
     */
    @Test
    void testToStringFormat() {
        User user = new User("alice", "1234", Role.GOVERNOR);

        String result = user.toString();

        assertEquals("alice (GOVERNOR)", result,
                "toString should return 'username (ROLE)'");
    }

    /**
     * Verifies behavior when username or password is empty.
     *
     * <p>
     * The class does not enforce validation, so empty values
     * should be stored and returned as-is.
     * </p>
     */
    @Test
    void testEmptyUsernameAndPassword() {
        User user = new User("", "", Role.CITIZEN);

        assertEquals("", user.getUsername(),
                "Empty username should be allowed");
        assertEquals("", user.getPassword(),
                "Empty password should be allowed");
        assertEquals(Role.CITIZEN, user.getRole(),
                "Role should still be assigned correctly");
    }

    /**
     * Verifies behavior when null values are provided.
     *
     * <p>
     * Since no validation is performed, null values
     * should be stored without throwing exceptions.
     * </p>
     */
    @Test
    void testNullValues() {
        User user = new User(null, null, null);

        assertNull(user.getUsername(),
                "Null username should be stored");
        assertNull(user.getPassword(),
                "Null password should be stored");
        assertNull(user.getRole(),
                "Null role should be stored");
    }
}
