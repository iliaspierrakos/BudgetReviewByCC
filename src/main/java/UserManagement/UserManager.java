package UserManagement;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * The UserManager class handles user registration, login,
 * and persistent storage of users in a local text file (users.txt).
 *
 * Each line in users.txt follows the format:
 * username,password,role[,ministryName]
 *
 * Constraints:
 * - Only one Governor (Prime Minister) is allowed.
 * - Maximum number of MinistryMembers is limited (MAX_MINISTRY_MEMBERS).
 */
public class UserManager {

    /** File path for saving/loading users */
    private static final String nameOfFile = "NecessaryFilesAndData/users.txt";

    /** Maximum allowed MinistryMembers */
    private static final int MAX_MINISTRY_MEMBERS = 10;

    /** Stores users in memory (key = username) */
    private Map<String, User> users = new HashMap<>();

    /**
     * Constructor — loads users from file on initialization.
     */
    public UserManager() {
        loadUsersFromFile();
    }

    /**
     * Registers a user (overload for Citizen and Governor).
     *
     * @param username the username
     * @param password the password
     * @param role     the role of the user
     * @return true if registration successful, false otherwise
     */
    public boolean registerUser(String username, String password, User.Role role) {
        return registerUser(username, password, role, null);
    }

    /**
     * Registers a user with optional ministry name (for MinistryMember).
     *
     * @param username      the username
     * @param password      the password
     * @param role          the role of the user
     * @param ministryName  the ministry name (required if role is MINISTRYMEMBER)
     * @return true if registration successful, false otherwise
     */
    public boolean registerUser(String username, String password, User.Role role, String ministryName) {
        if (users.containsKey(username)) {
            System.out.println("Username already exists!");
            return false;
        }

        // Restriction: Only one Governor
        if (role == User.Role.GOVERNOR && existsGovernor()) {
            System.out.println("There is already a Governor! Only ONE is allowed.");
            return false;
        }

        // Restriction: Maximum MinistryMembers
        if (role == User.Role.MINISTRYMEMBER && countMinistryMembers() >= MAX_MINISTRY_MEMBERS) {
            System.out.println("Maximum number of MinistryMembers reached!");
            return false;
        }

        User newUser;

        switch (role) {
            case MINISTRYMEMBER:
                if (ministryName == null || ministryName.isEmpty()) {
                    System.out.println("Error: Ministry name is required for MinistryMember!");
                    return false;
                }
                newUser = new MinistryMember(username, password, ministryName);
                break;

            case CITIZEN:
                newUser = new Citizen(username, password);
                break;

            case GOVERNOR:
                newUser = new Governor(username, password);
                break;

            default:
                System.out.println("Invalid role!");
                return false;
        }

        users.put(username, newUser);
        saveUserToFile(newUser);
        System.out.println("User registered successfully!");
        return true;
    }

    /**
     * Logs in a user by verifying username and password.
     *
     * @param username the username
     * @param password the password
     * @return the User object if login successful, null otherwise
     */
    public User loginUser(String username, String password) {
        User user = users.get(username);

        if (user != null && user.getPassword().equals(password)) {
            System.out.println("Login successful! ");
            return user;
        }

        System.out.println("Invalid username or password!");
        return null;
    }

    /**
     * Checks if a Governor already exists.
     *
     * @return true if a Governor exists, false otherwise
     */
    private boolean existsGovernor() {
        return users.values().stream().anyMatch(u -> u.getRole() == User.Role.GOVERNOR);
    }

    /**
     * Counts the number of MinistryMembers currently registered.
     *
     * @return the number of MinistryMembers
     */
    private long countMinistryMembers() {
        return users.values().stream().filter(u -> u.getRole() == User.Role.MINISTRYMEMBER).count();
    }

    /**
     * Loads users from the file into memory.
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

                if (parts.length < 3) continue;

                String username = parts[0].trim();
                String password = parts[1].trim();
                String roleString = parts[2].trim();

                User.Role role = User.Role.valueOf(roleString.toUpperCase());

                if (role == User.Role.MINISTRYMEMBER && parts.length == 4) {
                    String ministryName = parts[3].trim();
                    users.put(username, new MinistryMember(username, password, ministryName));
                } else if (role == User.Role.CITIZEN) {
                    users.put(username, new Citizen(username, password));
                } else if (role == User.Role.GOVERNOR) {
                    users.put(username, new Governor(username, password));
                } else {
                    users.put(username, new User(username, password, role));
                }
            }

        } catch (IOException e) {
            System.out.println("Error reading users file: " + e.getMessage());
        }
    }

    /**
     * Saves a single user's data to the file.
     *
     * @param user the user to save
     */
    private void saveUserToFile(User user) {
        try (FileWriter writer = new FileWriter(nameOfFile, true)) {
            if (user.getRole() == User.Role.MINISTRYMEMBER) {
                MinistryMember mm = (MinistryMember) user;
                writer.write(mm.getUsername() + "," + mm.getPassword() + "," + mm.getRole() + "," + mm.getMinistryName() + "\n");
            } else {
                writer.write(user.getUsername() + "," + user.getPassword() + "," + user.getRole() + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error saving user: " + e.getMessage());
        }
    }
}