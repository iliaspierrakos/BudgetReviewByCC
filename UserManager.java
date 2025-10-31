import java.util.HashMap; //Small DataBase to store our users data
import java.util.Map; //Interface for HashMap

//This class takes care of registration, login, and user storage.
public class UserManager {

    // A Map that stores all users in memory.
    // Key: username (String)
    // Value: User object (contains username, password, role)
     private Map<String, User> users = new HashMap<>();

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
        System.out.println("Registration successful! Role: " + role);
        return true;
     }
     //The user logs in if the username & password are correct.
     public User login(String username, String password) {
        //Check if the username is correct.
        if(!users.containsKey(username)) {
            System.out.println("User not found.");
            return null;
        }

        User user = users.get(username);

        //Compares the given and the stored password.
        if (user.getPassword().equals(password)) {
             System.out.println("Login successful! Welcome, " + user.getUsername() + " (" + user.getRole() + ")");
             return user;
        } else {
             System.out.println ("Incorrect password. Please try again."); 
             return null; 
        }
    }
}