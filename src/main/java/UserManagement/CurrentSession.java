package UserManagement;

/**
 * The {@code CurrentSession} class stores information about
 * the currently logged-in user.
 *
 * <p>This class acts as a simple session manager using static access.</p>
 */
public class CurrentSession {

    /** The currently logged-in user. */
    private static User currentUser;

    /**
     * Sets the current user.
     *
     * @param user the logged-in user
     */
    public static void setUser(User user) {
        currentUser = user;
    }

    /**
     * Returns the currently logged-in user.
     *
     * @return the current user, or {@code null} if no user is logged in
     */
    public static User getUser() {
        return currentUser;
    }
}
