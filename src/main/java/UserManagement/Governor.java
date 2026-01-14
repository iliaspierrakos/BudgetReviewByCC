package UserManagement;

/**
 * Represents the Governor (Prime Minister) in the system. This class extends User and sets the role
 * to GOVERNOR.
 *
 * A Governor has full access to regional and national data.
 */
public class Governor extends User {

  /**
   * Constructor for creating a Governor.
   *
   * @param username the username of the Governor
   * @param password the password of the Governor
   */
  public Governor(String username, String password) {
    super(username, password, Role.GOVERNOR);
  }

  /**
   * Returns a string representation of the Governor. Includes only the username.
   *
   * @return a string describing the Governor
   *
   */
  @Override
  public String toString() {
    return "Governor: " + getUsername();
  }

  // Additional Governor-specific methods can be added here,
  // e.g., approve budgets, manage ministries, etc.
}
