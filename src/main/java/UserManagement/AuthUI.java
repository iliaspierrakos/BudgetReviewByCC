package UserManagement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import UserFeatures.ClearHistory;

/**
 * The {@code AuthUI} class provides a console-based user interface
 * for authentication and role selection.
 *
 * <p>Users can register, log in, and access role-specific functionality.
 * The class also handles system cleanup upon application exit.</p>
 */
public class AuthUI {

    /** Manages user registration and authentication logic. */
    private UserManager userManager;

    /** Scanner used for console input. */
    private Scanner scanner;

    /**
     * Constructs the authentication UI.
     *
     * @param userManager the user manager handling authentication logic
     */
    public AuthUI(UserManager userManager) {
        this.userManager = userManager;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Starts the authentication menu loop.
     */
    public void start() {
        boolean on = true;

        while (on) {
            System.out.println("\n=== PRIME MINISTER FOR A DAY ===");
            System.out.println("(1) -> Register");
            System.out.println("(2) -> Login");
            System.out.println("(3) -> Exit");
            System.out.print("Choose an option: ");

            String usersChoice = scanner.nextLine();

            switch (usersChoice) {
                case "1":
                    registerUserUI();
                    break;
                case "2":
                    loginUser();
                    break;
                case "3":
                    on = false;
                    System.out.println("Goodbye!");
                    cleanupFiles();
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    /**
     * Handles user registration input and logic.
     */
    private void registerUserUI() {
        System.out.println("\n=== Register New User ===");
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        System.out.println("Select role:");
        System.out.println("(1) -> Citizen");
        System.out.println("(2) -> MinistryMember");
        System.out.println("(3) -> Governor");
        System.out.print("Choice: ");

        String roleChoice = scanner.nextLine();
        String ministryName = null;
        User.Role role;

        switch (roleChoice) {
            case "1":
                role = User.Role.CITIZEN;
                break;
            case "2":
                role = User.Role.MINISTRYMEMBER;
                System.out.print("Enter ministry name: ");
                ministryName = scanner.nextLine();
                if (ministryName.isEmpty()) {
                    System.out.println("Ministry name cannot be empty!");
                    return;
                }
                break;
            case "3":
                role = User.Role.GOVERNOR;
                break;
            default:
                System.out.println("Invalid choice!");
                return;
        }

        boolean success = (role == User.Role.MINISTRYMEMBER)
            ? userManager.registerUser(username, password, role, ministryName)
            : userManager.registerUser(username, password, role);

        if (success) {
            System.out.println("Registration successful!");
        }
    }

    /**
     * Handles user login input and authentication.
     */
    private void loginUser() {
        System.out.println("\n=== Login ===");
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        User user = userManager.loginUser(username, password);
        if (user != null) {
            System.out.println("Welcome, " + user.getUsername());
            showRoleMenu(user);
        }
    }

    /**
     * Displays role-specific access information.
     *
     * @param user the logged-in user
     */
    private void showRoleMenu(User user) {
        switch (user.getRole()) {
            case MINISTRYMEMBER:
                System.out.println("Role: MinistryMember — regional access");
                break;
            case GOVERNOR:
                System.out.println("Role: Governor — full access");
                break;
            case CITIZEN:
                System.out.println("Role: Citizen — view only");
                break;
        }
    }

    /**
     * Clears temporary and history files on application exit.
     */
    private void cleanupFiles() {
        ClearHistory.clearFile(Path.of("NecessaryFilesAndData/edithistory.txt"));

        for (int i = 2020; i <= 2026; i++) {
            ClearHistory.clearFile(Path.of("NecessaryFilesAndData/MinistriesBudgets" + i + ".csv"));
            ClearHistory.clearFile(Path.of("NecessaryFilesAndData/view" + i + ".txt"));

            for (int j = 2020; j <= 2026; j++) {
                try {
                    Files.deleteIfExists(
                        Paths.get("NecessaryFilesAndData/compare" + i + "with" + j + ".txt")
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
