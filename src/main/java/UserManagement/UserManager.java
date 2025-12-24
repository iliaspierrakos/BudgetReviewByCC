package UserManagement;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles user registration, login and persistent storage.
 *
 * Users are stored in: project-root/data/users.txt
 */
public class UserManager {

    /* =========================
       Persistent storage path
       ========================= */
    private static final Path USERS_FILE =
            Paths.get("data", "users.txt");

    private static final int MAX_MINISTRY_MEMBERS = 10;

    private Map<String, User> users = new HashMap<>();
    private User currentUser;

    /**
     * Constructor — initializes storage and loads users.
     */
    public UserManager() {
        initStorage();
        loadUsersFromFile();
    }

    /* =========================
       Public API
       ========================= */

    public boolean registerUser(String username, String password, User.Role role) {
        return registerUser(username, password, role, null);
    }

    public boolean registerUser(
            String username,
            String password,
            User.Role role,
            String ministryName
    ) {
        if (users.containsKey(username)) {
            return false;
        }

        if (role == User.Role.GOVERNOR && existsGovernor()) {
            return false;
        }

        if (role == User.Role.MINISTRYMEMBER &&
                countMinistryMembers() >= MAX_MINISTRY_MEMBERS) {
            return false;
        }

        User newUser;

        switch (role) {
            case MINISTRYMEMBER:
                if (ministryName == null || ministryName.isEmpty()) return false;
                newUser = new MinistryMember(username, password, ministryName);
                break;
            case CITIZEN:
                newUser = new Citizen(username, password);
                break;
            case GOVERNOR:
                newUser = new Governor(username, password);
                break;
            default:
                return false;
        }

        users.put(username, newUser);
        saveUserToFile(newUser);
        return true;
    }

    public User loginUser(String username, String password) {
        User user = users.get(username);
        if (user != null && user.getPassword().equals(password)) {
            currentUser = user;
            return user;
        }
        return null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    /* =========================
       Internal helpers
       ========================= */

    private void initStorage() {
        try {
            Files.createDirectories(USERS_FILE.getParent());
            if (!Files.exists(USERS_FILE)) {
                Files.createFile(USERS_FILE);
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot initialize user storage", e);
        }
    }

    private void loadUsersFromFile() {
        try (BufferedReader br = Files.newBufferedReader(USERS_FILE)) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 3) continue;

                String username = parts[0].trim();
                String password = parts[1].trim();
                User.Role role = User.Role.valueOf(parts[2].trim());

                if (role == User.Role.MINISTRYMEMBER && parts.length == 4) {
                    users.put(username,
                            new MinistryMember(username, password, parts[3].trim()));
                } else if (role == User.Role.CITIZEN) {
                    users.put(username, new Citizen(username, password));
                } else if (role == User.Role.GOVERNOR) {
                    users.put(username, new Governor(username, password));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading users", e);
        }
    }

    private void saveUserToFile(User user) {
        try (BufferedWriter writer =
                     Files.newBufferedWriter(USERS_FILE, StandardOpenOption.APPEND)) {

            if (user instanceof MinistryMember mm) {
                writer.write(mm.getUsername() + "," +
                        mm.getPassword() + "," +
                        mm.getRole() + "," +
                        mm.getMinistryName());
            } else {
                writer.write(user.getUsername() + "," +
                        user.getPassword() + "," +
                        user.getRole());
            }
            writer.newLine();

        } catch (IOException e) {
            throw new RuntimeException("Error saving user", e);
        }
    }

    private boolean existsGovernor() {
        return users.values().stream()
                .anyMatch(u -> u.getRole() == User.Role.GOVERNOR);
    }

    private long countMinistryMembers() {
        return users.values().stream()
                .filter(u -> u.getRole() == User.Role.MINISTRYMEMBER)
                .count();
    }
}
