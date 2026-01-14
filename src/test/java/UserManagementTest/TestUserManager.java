package UserManagementTest;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import UserManagement.User;
import UserManagement.UserManager;

/**
 * Unit tests for {@link UserManager}.
 *
 * <p>
 * These tests validate the core (non-UI) behavior of the user management subsystem: registration rules, login/logout
 * behavior, and persistence to {@code data/users.txt}.
 * </p>
 *
 * <p>
 * {@link UserManager} uses a fixed relative storage path ({@code data/users.txt}). Therefore, each test resets the file
 * to ensure isolation and repeatability.
 * </p>
 */
public class TestUserManager {

    /** Storage path used by {@link UserManager} (must match production code). */
    private static final Path USERS_FILE = Path.of("data", "users.txt");

    /**
     * Ensures the persistent user store starts empty for every test.
     *
     * @throws IOException
     *             if file operations fail
     */
    @BeforeEach
    void resetUsersFile() throws IOException {
        Files.createDirectories(USERS_FILE.getParent());
        Files.deleteIfExists(USERS_FILE);
        Files.createFile(USERS_FILE);
    }

    /**
     * Verifies that {@link UserManager} initializes storage on construction and guarantees that {@code data/users.txt}
     * exists.
     */
    @Test
    void testConstructorInitializesStorageFile() {
        new UserManager();
        assertTrue(Files.exists(USERS_FILE), "users.txt should exist after constructing UserManager");
    }

    /**
     * Verifies that registering a Citizen succeeds and is persisted to disk, and that a fresh {@link UserManager}
     * instance can load and authenticate that user.
     *
     * @throws IOException
     *             if reading the storage file fails
     */
    @Test
    void testRegisterCitizenPersistsAndLoads() throws IOException {
        UserManager um1 = new UserManager();

        boolean ok = um1.registerUser("alice", "pw", User.Role.CITIZEN);
        assertTrue(ok, "Citizen registration should succeed");

        // File should contain at least one line with alice
        String content = Files.readString(USERS_FILE, StandardCharsets.UTF_8);
        assertTrue(content.contains("alice"), "Storage file should contain the new user record");

        // New manager should load from file and allow login
        UserManager um2 = new UserManager();
        User logged = um2.loginUser("alice", "pw");

        assertNotNull(logged, "Login should succeed after reload");
        assertEquals("alice", logged.getUsername());
        assertEquals(User.Role.CITIZEN, logged.getRole());
        assertEquals(logged, um2.getCurrentUser(), "Current user should be set on successful login");
    }

    /**
     * Verifies that duplicate usernames are rejected.
     */
    @Test
    void testRegisterDuplicateUsernameFails() {
        UserManager um = new UserManager();

        assertTrue(um.registerUser("bob", "pw1", User.Role.CITIZEN), "First registration should succeed");
        assertFalse(um.registerUser("bob", "pw2", User.Role.CITIZEN), "Duplicate username should be rejected");
    }

    /**
     * Verifies that only one Governor can exist in the system at a time.
     */
    @Test
    void testOnlyOneGovernorAllowed() {
        UserManager um = new UserManager();

        assertTrue(um.registerUser("gov1", "pw", User.Role.GOVERNOR), "First governor should be allowed");
        assertFalse(um.registerUser("gov2", "pw", User.Role.GOVERNOR), "Second governor should be rejected");
    }

    /**
     * Verifies that registering a MinistryMember requires a non-empty ministry name.
     */
    @Test
    void testMinistryMemberRequiresMinistryName() {
        UserManager um = new UserManager();

        assertFalse(um.registerUser("m1", "pw", User.Role.MINISTRYMEMBER, null),
                "MinistryMember should be rejected when ministryName is null");
        assertFalse(um.registerUser("m2", "pw", User.Role.MINISTRYMEMBER, ""),
                "MinistryMember should be rejected when ministryName is empty");
        assertTrue(um.registerUser("m3", "pw", User.Role.MINISTRYMEMBER, "Ministry of Health"),
                "MinistryMember should be accepted with a valid ministryName");
    }

    /**
     * Verifies that the system enforces the maximum number of MinistryMembers (10).
     */
    @Test
    void testMinistryMemberLimitIsEnforced() {
        UserManager um = new UserManager();

        for (int i = 1; i <= 10; i++) {
            boolean ok = um.registerUser("mm" + i, "pw", User.Role.MINISTRYMEMBER, "Ministry of Finance");
            assertTrue(ok, "MinistryMember #" + i + " should be allowed");
        }

        assertFalse(um.registerUser("mm11", "pw", User.Role.MINISTRYMEMBER, "Ministry of Finance"),
                "11th MinistryMember should be rejected (limit is 10)");
    }

    /**
     * Verifies that login fails with an incorrect password and succeeds with the correct one.
     */
    @Test
    void testLoginValidatesPassword() {
        UserManager um = new UserManager();
        um.registerUser("charlie", "secret", User.Role.CITIZEN);

        assertNull(um.loginUser("charlie", "wrong"), "Login should fail with wrong password");
        assertNull(um.getCurrentUser(), "Current user should remain null after failed login");

        User logged = um.loginUser("charlie", "secret");
        assertNotNull(logged, "Login should succeed with correct password");
        assertEquals("charlie", logged.getUsername());
        assertEquals(User.Role.CITIZEN, logged.getRole());
        assertEquals(logged, um.getCurrentUser(), "Current user should be set after successful login");
    }

    /**
     * Verifies that {@link UserManager#logout()} clears the current user.
     */
    @Test
    void testLogoutClearsCurrentUser() {
        UserManager um = new UserManager();
        um.registerUser("dina", "pw", User.Role.CITIZEN);

        assertNotNull(um.loginUser("dina", "pw"), "Precondition: login should succeed");
        assertNotNull(um.getCurrentUser(), "Precondition: current user should be set");

        um.logout();

        assertNull(um.getCurrentUser(), "Current user should be cleared after logout");
    }
}
