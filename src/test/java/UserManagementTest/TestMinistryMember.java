package UserManagementTest;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import UserManagement.MinistryMember;

/**
 * Unit tests for {@link MinistryMember}.
 *
 * <p>
 * These tests validate the user-facing behavior of {@code MinistryMember}:
 * </p>
 * <ul>
 * <li>Ministry association is stored and can be updated</li>
 * <li>Username is propagated via the {@code User} superclass</li>
 * <li>{@code toString()} includes username and ministry name</li>
 * </ul>
 *
 * <p>
 * Optional checks for role/password are performed only when those values are accessible (via getters or fields). This
 * keeps the test suite compatible with different {@code User} encapsulation strategies.
 * </p>
 */
public class TestMinistryMember {

    /**
     * Verifies that the constructor stores the username and ministry name.
     */
    @Test
    void testConstructorSetsUsernameAndMinistryName() {
        MinistryMember mm = new MinistryMember("maria", "pw", "Ministry of Health");

        assertEquals("maria", mm.getUsername(), "Username should be stored by the super constructor");
        assertEquals("Ministry of Health", mm.getMinistryName(), "Ministry name should be stored");
    }

    /**
     * Verifies that the ministry name can be updated via {@link MinistryMember#setMinistryName(String)}.
     */
    @Test
    void testSetMinistryNameUpdatesValue() {
        MinistryMember mm = new MinistryMember("nikos", "pw", "Ministry of Finance");

        mm.setMinistryName("Ministry of Education");

        assertEquals("Ministry of Education", mm.getMinistryName(),
                "setMinistryName should update the stored ministry name");
    }

    /**
     * Verifies that {@link MinistryMember#toString()} includes the username and ministry name in the expected format:
     * {@code "MinistryMember: <username> (<ministryName>)"}.
     */
    @Test
    void testToStringFormat() {
        MinistryMember mm = new MinistryMember("eva", "pw", "Ministry of Health");

        assertEquals("MinistryMember: eva (Ministry of Health)", mm.toString(),
                "toString() should include username and ministry name");
    }

    /**
     * Verifies that the role is {@code MINISTRYMEMBER} when such information is accessible either through a public
     * getter or via reflection.
     */
    @Test
    void testRoleIsMinistryMemberWhenAccessible() throws Exception {
        MinistryMember mm = new MinistryMember("u1", "pw", "Ministry of Finance");

        Object role = readPropertyIfExists(mm, "getRole", "role");
        if (role == null) {
            // Role may be intentionally encapsulated; do not fail in that case.
            return;
        }

        assertEquals("MINISTRYMEMBER", role.toString(), "Role should be MINISTRYMEMBER");
    }

    /**
     * Verifies that the password is stored when such information is accessible.
     *
     * <p>
     * In many designs, raw passwords are intentionally not exposed. Therefore, this test only asserts if the password
     * can be read.
     * </p>
     */
    @Test
    void testPasswordStoredWhenAccessible() throws Exception {
        MinistryMember mm = new MinistryMember("u2", "topsecret", "Ministry of Finance");

        Object password = readPropertyIfExists(mm, "getPassword", "password");
        if (password == null) {
            // Not accessible by design -> do not fail
            return;
        }

        assertEquals("topsecret", password.toString(), "Password should match constructor input");
    }

    /**
     * Attempts to read a value either via a no-arg getter method or via a field, walking up the class hierarchy if
     * needed.
     *
     * @param instance
     *            the object to inspect
     * @param getterName
     *            candidate getter name (e.g. {@code getRole})
     * @param fieldName
     *            candidate field name (e.g. {@code role})
     *
     * @return the value if found; otherwise {@code null}
     *
     * @throws Exception
     *             if reflection access fails unexpectedly
     */
    private static Object readPropertyIfExists(Object instance, String getterName, String fieldName) throws Exception {

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
