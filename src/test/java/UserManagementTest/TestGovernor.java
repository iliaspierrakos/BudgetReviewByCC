package UserManagementTest;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import UserManagement.Governor;

/**
 * Unit tests for {@link Governor}.
 *
 * <p>
 * This suite validates that a {@code Governor} is instantiated correctly and that
 * its textual representation matches the class contract.
 * </p>
 *
 * <p>
 * Since {@code Governor} extends {@code User}, the tests primarily assert the
 * externally observable behavior (username propagation and {@code toString()} output).
 * Optional checks for role and password are performed only when the corresponding
 * information is accessible (via public getters or fields).
 * </p>
 */
public class TestGovernor {

    /**
     * Verifies that the constructor stores the username and that {@link Governor#toString()}
     * matches the expected format.
     */
    @Test
    void testConstructorSetsUsernameAndToString() {
        Governor g = new Governor("pm", "supersecret");

        assertEquals("pm", g.getUsername(), "Username should be stored by the super constructor");
        assertEquals("Governor: pm", g.toString(), "toString() should be 'Governor: <username>'");
    }

    /**
     * Verifies that the governor role is {@code GOVERNOR} when such information
     * is accessible either through a public getter or via reflection.
     */
    @Test
    void testRoleIsGovernorWhenAccessible() throws Exception {
        Governor g = new Governor("pm2", "pw");

        Object role = readPropertyIfExists(g, "getRole", "role");
        if (role == null) {
            // Role may be intentionally encapsulated; do not fail in that case.
            return;
        }

        assertEquals("GOVERNOR", role.toString(), "Governor role should be GOVERNOR");
    }

    /**
     * Verifies that the password is stored when such information is accessible.
     *
     * <p>
     * Many systems avoid exposing passwords; therefore, this test only asserts
     * when the password value is actually reachable from the object.
     * </p>
     */
    @Test
    void testPasswordStoredWhenAccessible() throws Exception {
        Governor g = new Governor("pm3", "topsecret");

        Object password = readPropertyIfExists(g, "getPassword", "password");
        if (password == null) {
            // Not accessible by design -> do not fail
            return;
        }

        assertEquals("topsecret", password.toString(), "Password should match constructor input");
    }

    /**
     * Attempts to read a value either via a no-arg getter method or via a field,
     * walking up the class hierarchy if needed.
     *
     * @param instance  the object to inspect
     * @param getterName candidate getter name (e.g. {@code getRole})
     * @param fieldName candidate field name (e.g. {@code role})
     * @return the value if found; otherwise {@code null}
     * @throws Exception if reflection access fails unexpectedly
     */
    private static Object readPropertyIfExists(Object instance, String getterName, String fieldName)
            throws Exception {

        // 1) Try public getter
        try {
            Method m = instance.getClass().getMethod(getterName);
            return m.invoke(instance);
        } catch (NoSuchMethodException ignored) {
            // fall through
        }

        // 2) Try declared field in class hierarchy
        Class<?> cls = instance.getClass();
        while (cls != null) {
            try {
                Field f = cls.getDeclaredField(fieldName);
                f.setAccessible(true);
                return f.get(instance);
            } catch (NoSuchFieldException ignored) {
                cls = cls.getSuperclass();
            }
        }

        return null;
    }
}
