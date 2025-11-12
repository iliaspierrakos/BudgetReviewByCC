// The User class represents a single user in the system 
public class User {
        // The Role enum defines the possible user roles.
    public enum Role {
        GOVERNOR, // Full access — can modify the national budget
        MINISTRYMEMBER, // Limited access — can propose regional changes
        CITIZEN   // Read-only access — can only view public data
    }

    private String username;
    private String password;
    private Role role;

    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
// These methods provide read-only access to the user's information.
    public String getUsername() { 
        return username; 
    }
    public String getPassword() { 
        return password; 
    }
    public Role getRole() { 
        return role;
    }
 // Prints the version of the user
    @Override
    public String toString() {
        return username + " (" + role + ")";
    }
}