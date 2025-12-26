package UserManagement;
/**
 * This is a class that contains static methods for having 
 * easy access to the user object.
 */

public class CurrentSession {

    private static User currentUser;

    public static void setUser(User user) {
        currentUser = user;
    }

    public static User getUser() {
        return currentUser;
    }

    public static void clear() {
        currentUser = null;
    }
}
