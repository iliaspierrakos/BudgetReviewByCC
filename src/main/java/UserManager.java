import java.io.*;
import java.util.HashMap; //Small DataBase to store our users data
import java.util.Map; //Interface for HashMap

//This class takes care of registration, login, and user storage.
public class UserManager {

    // users.txt = a file  that stores all registered users.
    // Key: username (String)
    // Value: User object (contains username, password, role)
     private HashMapMap<String, User> users;
     private static final String nameOfFile = "users.txt";

     public UserManager() {
        users = newHashMap<>();
        loadUsersFromFile();
     }

     //Registers new users.
     public boolean register(String username, String password, User.Role role) {

        //Checks if the username already exists (in the Map).
        if (users.containsKey(username)) {
            System.out.println("Username already exists. Please choose another one.");
            return false;
        }

        //Creates new User and stores it.
        User newUser = new User(username, password, role);
        users.put(username, newUser);
        saveUsersToFile(newUser);
        System.out.println("Registration successful! Role: " + role);
        return true;
     }
     
     //The user logs in if the username & password are correct.
     public User loginUser(String username, String password) {
        //Check if the username is correct.
        if(!users.containsKey(username)) {
            System.out.println("User not found.");
            return null;
        }

        User user = users.get(username);

        //Compares the given password with the given one.
        if (user.getPassword().equals(password)) {
             System.out.println("Login successful! Welcome, " + user.getRole() + " " + username);
             return user;
        } else {
             System.out.println ("Incorrect password. Please try again."); 
             return null; 
        }
    }

    private void loadUsersFromFile() {
        File file = new File(nameOfFile);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println(" Could not create users.txt file: "+ e.getMessage());
            }
            return;
        }
    }

    try (BufferedReader br = new BufferedReader(new FileReader(nameOfFile))) {
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts =line.split(",");
            if (parts.length == 3) {
                String username = parts[0].trim();
                String password = parts[1].trim();
                String role = parts[2].trim();
                users.put(username, new User(username, password, role));
            }
        }
    } catch (ΙOException e) {
        System.out.println("⚠️ Error loading users: " + e.getMessage());

    }
    public void showAllUsers() {
        if (users.isEmpty()) {
            System.out.println("No users registered yet.");
        } else {
             System.out.println("\n=== Registered Users ===");
             for (User user : users.values()) {
                 System.out.println("- " + user);
             }
        }     
    }
}
