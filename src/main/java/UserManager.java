import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * The UserManager class handles user registration, login,
 * and persistent storage of users in a local text file (users.txt).
 *
 * Each line in users.txt follows the format:
 * username,password,role
 */
public class UserManager {

    private static final String nameOfFile = "users.txt";
    private Map<String, User> users = new HashMap<>();

    public UserManager() {
        loadUsersFromFile();
    }

    // Overload για Citizen και Governor
    public boolean registerUser(String username, String password, User.Role role) {
        return registerUser(username, password, role, null);
    }

    // Main register method (handle MinistryMember)
    public boolean registerUser(String username, String password, User.Role role, String ministryName) {
        if (users.containsKey(username)) {
            System.out.println("Username already exists!");
            return false;
        }

        User newUser;

        if (role == User.Role.MINISTRYMEMBER) {

            if (ministryName == null || ministryName.isEmpty()) {
                System.out.println("Error: Ministry name is required for MinistryMember!");
                return false;
            }

            newUser = new MinistryMember(username, password, ministryName);

        } else {
            newUser = new User(username, password, role);
        }

        users.put(username, newUser);
        saveUserToFile(newUser);
        System.out.println("User registered successfully!");
        return true;
    }


    // Login
    public User loginUser(String username, String password) {
        User user = users.get(username);

        if (user != null && user.getPassword().equals(password)) {
            System.out.println("Login successful! Welcome, " + user.getUsername() + " (" + user.getRole() + ")");
            return user;
        }

        System.out.println("Invalid username or password!");
        return null;
    }


    // Φόρτωμα χρηστών από αρχείο
    private void loadUsersFromFile() {
        File file = new File(nameOfFile);

        if (!file.exists()) {
            System.out.println("No existing user file found. A new one will be created upon registration.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(nameOfFile))) {

            String line;

            while ((line = br.readLine()) != null) {

                String[] parts = line.split(",");

                if (parts.length < 3) continue;

                String username = parts[0].trim();
                String password = parts[1].trim();
                String roleString = parts[2].trim();

                User.Role role = User.Role.valueOf(roleString.toUpperCase());

                if (role == User.Role.MINISTRYMEMBER && parts.length == 4) {
                    String ministryName = parts[3].trim();
                    users.put(username, new MinistryMember(username, password, ministryName));
                } else {
                    users.put(username, new User(username, password, role));
                }
            }

        } catch (IOException e) {
            System.out.println("Error reading users file: " + e.getMessage());
        }
    }


    // Αποθήκευση χρήστη στο αρχείο
    private void saveUserToFile(User user) {
        try (FileWriter writer = new FileWriter(nameOfFile, true)) {

            if (user.getRole() == User.Role.MINISTRYMEMBER) {
                MinistryMember mm = (MinistryMember) user;
                writer.write(
                    mm.getUsername() + "," +
                    mm.getPassword() + "," +
                    mm.getRole() + "," +
                    mm.getMinistryName() + "\n"
                );
            } else {
                writer.write(
                    user.getUsername() + "," +
                    user.getPassword() + "," +
                    user.getRole() + "\n"
                );
            }

        } catch (IOException e) {
            System.out.println("Error saving user: " + e.getMessage());
        }
    }
}

