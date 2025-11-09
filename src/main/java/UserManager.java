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

    // File path for saving and loading users
    private static final String nameOfFile = "users.txt";

    // Stores users in memory (key = username)
    private Map<String, User> users = new HashMap<>();

    /**
     * Constructor – loads users from the file when the program starts.
     */
    public UserManager() {
        loadUsersFromFile();
    }

    
    //Registers a new user if the username does not already exist.
    public boolean registerUser(String username, String password, User.Role role) {
        if (users.containsKey(username)) {
            System.out.println("Username already exists!");
            return false;
        }
        User newUser = new User(username, password, role);
        users.put(username, newUser);
        saveUserToFile(newUser);
        System.out.println("User registered successfully!");
        return true;
    }

    
    //Logs in a user by verifying username and password.
    public User loginUser(String username, String password) {
        User user = users.get(username);

        if (user != null && user.getPassword().equals(password)) {
            System.out.println(" Login successful! Welcome, " + user.getUsername() + " (" + user.getRole() + ")");
            return user;
        } else {
            System.out.println("Invalid username or password!");
            return null;
        }
    }

    /**
     * Reads all saved users from the users.txt file
     * and loads them into the HashMap.
     */
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
                if (parts.length == 3) {
                    String username = parts[0].trim();
                    String password = parts[1].trim();
                    String role = parts[2].trim();
                    users.put(username, new User(username, password,User.Role.valueOf(role.trim().toUpperCase())));
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading users file: " + e.getMessage());
        }
    }
    //Saves a single user's data to the file.
    private void saveUserToFile(User user) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(nameOfFile,true))) {
            bw.write(user.getUsername() + "," + user.getPassword() + "," +user.getRole().name());
            bw.newLine();
        } catch (IOException e) {
            System.out.println(" Error writing user to file: " + e.getMessage());
        }    
    }
}    
