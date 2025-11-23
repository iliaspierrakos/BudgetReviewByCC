/**
 * Represents a Citizen in the system.
 * This class extends User and sets the role to CITIZEN.
 * 
 * Each Citizen has a username and password.
 */
public class Citizen extends User {

    /**
     * Constructor for creating a Citizen.
     * 
     * @param username the username of the citizen
     * @param password the password of the citizen
     */
    public Citizen(String username, String password) {
        super(username, password, Role.CITIZEN);
    }

    /**
     * Returns a string representation of the Citizen.
     * Includes only the username.
     * 
     * @return a string describing the Citizen
     */
    @Override
    public String toString() {
        return "Citizen: " + getUsername();
    }

    // Additional Citizen-specific methods can be added here
}
