
import java.util.HashMap;
import java.util.Map;

public class UserManager {
    private Map<String, User> users = new HashMap<>();

    public boolean register(String username, String password, User.Role role) {
        if (users.containsKey(username)) {
            System.out.println("Username already exists!");
            return false;
        }
        users.put(username, new User(username, password, role));
        System.out.println("Registration successful as " + role + "!");
        return true;
    }

    public User login(String username, String password) {
        if (!users.containsKey(username)) {
            System.out.println("User not found!");
            return null;
        }
        User user = users.get(username);
        if (user.getPassword().equals(password)) {
            System.out.println("Login successful! Role: " + user.getRole());
            return user;
        } else {
            System.out.println("Incorrect password!");
            return null;
        }
    }
}

