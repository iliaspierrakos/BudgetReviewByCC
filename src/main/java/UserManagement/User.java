package UserManagement;

/**
 * The {@code User} class represents a single system user.
 *
 * <p>Each user has a username, password, and assigned role that
 * determines their level of access.</p>
 */
public class User {

    /**
     * Defines the possible roles a user can have in the system.
     */
    public enum Role {
        /** Full access — can modify national budgets. */
        GOVERNOR,

        /** Limited access — can propose ministry-specific changes. */
        MINISTRYMEMBER,

        /** Read-only access — can only view public data. */
        CITIZEN
    }

    private String username;
    private String password;
    private Role role;

    /**
     * Constructs a new user.
     *
     * @param username the user's username
     * @param password the user's password
     * @param role the user's role
     */
    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    /** @return the username */
    public String getUsername() {
        return username;
    }

    /** @return the password */
    public String getPassword() {
        return password;
    }

    /** @return the user's role */
    public Role getRole() {
        return role;
    }

    /**
     * Returns a string representation of the user.
     *
     * @return formatted user description
     */
    @Override
    public String toString() {
        return username + " (" + role + ")";
    }
}
