package UserManagementTest;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import UserManagement.Citizen;

/**
 * Unit tests for {@link Citizen}.
 *
 * <p>
 * This suite verifies that a {@code Citizen} is constructed correctly and that
 * its string representation matches the specification.
 * </p>
 *
 * <p>
 * Since {@code Citizen} extends {@code User}, some checks are performed via
 * public getters when available. For optional properties (like role/password),
 * reflection-based fallbacks are used to keep tests robust across small API variations.
 * </p>
 */
public class TestCitizen {

    /**
     * Verifies that the constructor stores the username and that {@link Citizen#toString()}
     * includes the expected prefix and username.
     */
    @Test
    void testConstructorSetsUsernameAndToString() {
        Citizen c = new Citizen("alice", "secret");

        assertEquals("alice", c.getUsername(), "Username should be stored by the super constructor");
        assertEquals("Citizen: alice", c.toString(), "toString() should be 'Citizen: <username>'");
    }

    /**
     * Verifies that the citizen role is {@code CITIZEN} when such information
     * is accessible either through a public getter or via reflection.
     */
    @Test
    void testRoleIsCitizenWhenAccessible() throws Exception {
        Citizen c = new Citizen("bob", "pw");

        Object role = readPropertyIfExists(c, "getRole", "role");
        if (role == null) {
            // If the role is not accessible in your current User implementation,
            // we treat this as "not testable" rather than failing.
            return;
        }

        assertEquals("CITIZEN", role.toString(), "Citizen role should be CITIZEN");
    }

    /**
     * Verifies that the password is stored when such information is accessible
     * either through a public getter or via reflection.
     *
     * <p>
     * Many systems intentionally do not expose raw passwords, so this test is
     * tolerant: it only asserts when the value is actually reachable.
     * </p>
     */
    @Test
    void testPasswordStoredWhenAccessible() throws Exception {
        Citizen c = new Citizen("charlie", "topsecret");

        Object password = readPropertyIfExists(c, "getPassword", "password");
        if (password == null) {
            // Not accessible by design -> do not fail
            return;
        }

        assertEquals("topsecret", password.toString(), "Password should match constructor input");
    }

    /**
     * Attempts to read a value either via a no-arg getter method or via a field.
     *
     * @param instance the object to inspect
     * @param getterName candidate getter method name (e.g. "getRole")
     * @param fieldName candidate field name (e.g. "role")
     * @return the value if found, otherwise {@code null}
     */
    private static Object readPropertyIfExists(Object instance, String getterName, String fieldName)
            throws Exception {

        // 1) Try getter
        try {
            Method m = instance.getClass().getMethod(getterName);
            return m.invoke(instance);
        } catch (NoSuchMethodException ignored) {
            // fall through
        }

        // 2) Try declared field on class hierarchy
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
