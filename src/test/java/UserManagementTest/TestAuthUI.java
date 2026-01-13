package UserManagementTest;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import UserManagement.AuthUI;
import UserManagement.User;
import UserManagement.UserManager;

/**
 * Unit tests for {@link AuthUI}.
 *
 * <p>
 * The {@link AuthUI#start()} method is not tested because it is an interactive
 * infinite-loop menu relying on live console input and exit side effects.
 * </p>
 *
 * <p>
 * Instead, these tests validate deterministic behavior of internal UI actions
 * by invoking private methods via reflection and by injecting scripted input
 * into the {@code scanner} field.
 * </p>
 */
public class TestAuthUI {

    private PrintStream originalOut;
    private ByteArrayOutputStream outBuffer;

    @BeforeEach
    void captureStdout() {
        originalOut = System.out;
        outBuffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outBuffer, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void restoreStdout() {
        System.setOut(originalOut);
    }

    /**
     * Verifies that {@code showRoleMenu(User)} prints the correct message for CITIZEN.
     */
    @Test
    void testShowRoleMenuCitizenPrintsExpectedText() throws Exception {
        AuthUI ui = new AuthUI(new UserManagerStub());
        User user = new UserStub("alice", User.Role.CITIZEN);

        invokePrivate(ui, "showRoleMenu", new Class<?>[]{User.class}, user);

        String printed = outBuffer.toString(StandardCharsets.UTF_8);
        assertTrue(printed.contains("Role: Citizen"),
                "Citizen role menu should print the Citizen access message");
    }

    /**
     * Verifies that {@code showRoleMenu(User)} prints the correct message for GOVERNOR.
     */
    @Test
    void testShowRoleMenuGovernorPrintsExpectedText() throws Exception {
        AuthUI ui = new AuthUI(new UserManagerStub());
        User user = new UserStub("pm", User.Role.GOVERNOR);

        invokePrivate(ui, "showRoleMenu", new Class<?>[]{User.class}, user);

        String printed = outBuffer.toString(StandardCharsets.UTF_8);
        assertTrue(printed.contains("Role: Governor"),
                "Governor role menu should print the Governor access message");
    }

    /**
     * Verifies that {@code showRoleMenu(User)} prints the correct message for MINISTRYMEMBER.
     */
    @Test
    void testShowRoleMenuMinistryMemberPrintsExpectedText() throws Exception {
        AuthUI ui = new AuthUI(new UserManagerStub());
        User user = new UserStub("min1", User.Role.MINISTRYMEMBER);

        invokePrivate(ui, "showRoleMenu", new Class<?>[]{User.class}, user);

        String printed = outBuffer.toString(StandardCharsets.UTF_8);
        assertTrue(printed.contains("Role: MinistryMember"),
                "MinistryMember role menu should print the regional access message");
    }

    /**
     * Verifies that {@code registerUserUI()} calls {@link UserManager#registerUser(String, String, User.Role)}
     * with the correct parameters when registering a Citizen.
     */
    @Test
    void testRegisterUserUICitizenCallsRegisterUser() throws Exception {
        UserManagerStub um = new UserManagerStub();
        AuthUI ui = new AuthUI(um);

        // username, password, roleChoice(1 citizen)
        injectScanner(ui, String.join("\n",
                "myrto",
                "pass123",
                "1"
        ) + "\n");

        invokePrivate(ui, "registerUserUI", new Class<?>[]{});

        assertEquals("myrto", um.lastUsername, "UI should pass username to UserManager");
        assertEquals("pass123", um.lastPassword, "UI should pass password to UserManager");
        assertEquals(User.Role.CITIZEN, um.lastRole, "UI should register Citizen role");
        assertNull(um.lastMinistryName, "Citizen registration should not pass a ministry name");
        assertTrue(um.registerCalled, "registerUser should be called");
    }

    /**
     * Verifies that {@code registerUserUI()} calls the 4-arg register method when registering a MinistryMember.
     */
    @Test
    void testRegisterUserUIMinistryMemberCallsRegisterUserWithMinistry() throws Exception {
        UserManagerStub um = new UserManagerStub();
        AuthUI ui = new AuthUI(um);

        // username, password, roleChoice(2 ministry member), ministryName
        injectScanner(ui, String.join("\n",
                "minUser",
                "pw",
                "2",
                "Ministry of Health"
        ) + "\n");

        invokePrivate(ui, "registerUserUI", new Class<?>[]{});

        assertEquals("minUser", um.lastUsername);
        assertEquals("pw", um.lastPassword);
        assertEquals(User.Role.MINISTRYMEMBER, um.lastRole);
        assertEquals("Ministry of Health", um.lastMinistryName);
        assertTrue(um.registerCalled, "registerUser should be called");
    }

    /**
     * Verifies that {@code loginUser()} calls {@link UserManager#loginUser(String, String)}
     * and prints a welcome message when authentication succeeds.
     */
    @Test
    void testLoginUserSuccessPrintsWelcomeAndRole() throws Exception {
        UserManagerStub um = new UserManagerStub();
        um.loginReturnUser = new UserStub("alice", User.Role.CITIZEN);

        AuthUI ui = new AuthUI(um);

        // username, password
        injectScanner(ui, String.join("\n",
                "alice",
                "pw"
        ) + "\n");

        invokePrivate(ui, "loginUser", new Class<?>[]{});

        assertEquals("alice", um.lastLoginUsername);
        assertEquals("pw", um.lastLoginPassword);
        assertTrue(um.loginCalled, "loginUser should be called");

        String printed = outBuffer.toString(StandardCharsets.UTF_8);
        assertTrue(printed.contains("Welcome, alice"), "Should print welcome message");
        assertTrue(printed.contains("Role: Citizen"), "Should print citizen role menu");
    }

    // ----------------- helpers -----------------

    private static void injectScanner(AuthUI ui, String inputScript) throws Exception {
        Field f = AuthUI.class.getDeclaredField("scanner");
        f.setAccessible(true);
        f.set(ui, new Scanner(inputScript));
    }

    private static Object invokePrivate(Object target, String methodName, Class<?>[] paramTypes, Object... args)
            throws Exception {
        Method m = target.getClass().getDeclaredMethod(methodName, paramTypes);
        m.setAccessible(true);
        return m.invoke(target, args);
    }

    /**
     * Minimal stub for UserManager used to verify interactions from AuthUI.
     */
    private static class UserManagerStub extends UserManager {
        boolean registerCalled;
        String lastUsername;
        String lastPassword;
        User.Role lastRole;
        String lastMinistryName;

        boolean loginCalled;
        String lastLoginUsername;
        String lastLoginPassword;
        User loginReturnUser;

        @Override
        public boolean registerUser(String username, String password, User.Role role) {
            registerCalled = true;
            lastUsername = username;
            lastPassword = password;
            lastRole = role;
            lastMinistryName = null;
            return true;
        }

        @Override
        public boolean registerUser(String username, String password, User.Role role, String ministryName) {
            registerCalled = true;
            lastUsername = username;
            lastPassword = password;
            lastRole = role;
            lastMinistryName = ministryName;
            return true;
        }

        @Override
        public User loginUser(String username, String password) {
            loginCalled = true;
            lastLoginUsername = username;
            lastLoginPassword = password;
            return loginReturnUser;
        }
    }

    /**
     * Minimal User stub used for role-menu printing tests.
     *
     * <p>
     * This assumes {@code User} is a concrete class in your project.
     * If {@code User} is abstract, this stub should extend it accordingly.
     * </p>
     */
    private static class UserStub extends User {
        private final String u;
        private final Role r;

        UserStub(String username, Role role) {
            super(username, "x", role);
            this.u = username;
            this.r = role;
        }

        @Override
        public String getUsername() {
            return u;
        }

        @Override
        public Role getRole() {
            return r;
        }
    }
}
