import java.util.HashMap;
import java.util.Map;
public class UserManager {
    private Map<String, User> users = new HashMap<>();
    public boolean register(String username, String password, User.Role role) {
        if (!users.containsKey(username)) {
            System.out.println("User not found.");
            return null;
        }
        User user =users.get(username);
    }
    if (user.getPassword().equals(password)) {
        System.out.println(" Login successful! Welcome, " + user.getUsername() + " (" + user.getRole() + ")");
    }
}
