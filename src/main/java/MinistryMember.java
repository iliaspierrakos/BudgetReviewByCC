 /**
 * Represents a member of a Ministry in the system.
 * This class extends User and adds ministry-specific information.
 * 
 * Each MinistryMember has a username, password, role (MINISTRYMEMBER),
 * and is associated with a specific ministry.
 */

public class MinistryMember extends User {

    /** The name of the ministry this member belongs to */
    private String ministryName;
    
    /**
     * Constructor for creating a MinistryMember.
     * 
     * @param username      the username of the user
     * @param password      the password of the user
     * @param ministryName  the name of the ministry this member belongs to
     */
public MinistryMember(String username, String password, String ministryName) {
    super(username, password, Role.MINISTRYMEMBER);
    this.ministryName = ministryName;
}
/**
     * Returns the name of the ministry this member belongs to.
     * 
     * @return the ministry name
     */
public String getMinistryName() {
    return ministryName;
}
/**
     * Updates the name of the ministry this member belongs to.
     * 
     * @param ministryName the new ministry name
     */
    public void setMinistryName(String ministryName) {
        this.ministryName = ministryName;
    }


//Additional methods for ministries' members......
//.....//

/**
     * Returns a string representation of the MinistryMember.
     * Includes username and ministry name.
     * 
     * @return a string describing the MinistryMember
     * 
    */    
@Override
public String toString() {
    return "MinistryMember: " + getUsername() + " (" + ministryName + ")";
}
}
