package UserManagementTest;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;

import UserManagement.CurrentSession;
import UserManagement.User;
import UserManagement.User.Role;

/**
 * Unit tests for {@link CurrentSession}.
 *
 * <p>
 * These tests verify correct storage and retrieval of the
 * currently logged-in user using static session access.
 * </p>
 */
public class TestCurrentSession {

    @AfterEach
    void clearSession() {
    CurrentSession.setUser(null);
    }

    @Test
    void testGetUserInitiallyNull() {
        assertNull(CurrentSession.getUser(),
        "Current user should be null before any login");
    }

    @Test
    void testSetAndGetUser() {
        User user = new User("testUser", "password", Role.CITIZEN);

        CurrentSession.setUser(user);

        assertNotNull(CurrentSession.getUser(),
        "User should not be null after setting");
        assertSame(user, CurrentSession.getUser(),
        "Retrieved user should be the same instance that was set");
    }

    @Test
    void testClearUser() {
        User user = new User("anotherUser", "password", Role.CITIZEN);
        CurrentSession.setUser(user);

        CurrentSession.setUser(null);

        assertNull(CurrentSession.getUser(),
            "Current user should be null after clearing the session");
    }

    @Test
    void testReplaceUser() {
        User user1 = new User("user1", "pass1", Role.CITIZEN);
        User user2 = new User("user2", "pass2", Role.GOVERNOR);

        CurrentSession.setUser(user1);
        CurrentSession.setUser(user2);

        assertSame(user2, CurrentSession.getUser(),
                "New user should replace the previous user in the session");
    }
}
