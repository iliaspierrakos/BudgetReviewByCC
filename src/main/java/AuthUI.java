import java.util.Scanner;

public class AuthUI {
    private UserManager userManager;
    private Scanner scanner; 

    public AuthUI(UserManager userManager) {
        this.userManager = userManager;
        this.scanner = new Scanner(System.in);
    }
    public void start() {
        boolean on = true;
        
        while(on) {
            System.out.println("\n=== PRIME MINISTER FOR A DAY ===");
            System.out.println("(1) -> Register");
            System.out.println("(2) ->Login");
            System.out.println("(3) -> Exit");
            System.out.print("Choose an option: ");
            String usersChoise = scanner.nextLine();

            switch (usersChoise) {
                case "1":
                    registerUser();
                    break;
                case "2": 
                    loginUser();
                    break;
                case "3":
                    on = false;
                    System.out.println("Exit");
                    break;
                default:
                     System.out.println("Invalid choice. Try again.");
            }
        }
    }
    
    private void registerUser() {
        System.out.println("\n=== Register New User ===");
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        System.out.println("Select role: ");
        System.out.println("(1) -> Minister");
        System.out.println("(2)-> Governor");
        System.out.println("(3)-> Citizen");
        System.out.print("Choice: ");
        String roleChoise = scanner.nextLine();

        User.Role role;
        switch (roleChoise) {
            case "1":
                role = User.Role.MINISTER;
                break;
            case "2":
                role = User.Role.GOVERNOR;
                break;
            default:
                role = User.Role.CITIZEN;    
        }
        userManager.register(username, password, role);
    }
    
    private void loginUser() {
        System.out.println("\n=== Login ===");
        System.out.print("Enter username: ");
        String username = scanner.nextLine(); 
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        User user = userManager.login(username,password);
        if (user != null) {
            System.out.println("Welcome, " + user.getUsername() + "! Role: " + user.getRole());
            showRoleMenu(user); 
        }
    }
    private void showRoleMenu(User user) {
            switch (user.getRole()) {
            case MINISTER:
                System.out.println("Minister — full access");
                break;
            case GOVERNOR:
                System.out.println("Governor — regional access");
                break;
            case CITIZEN:
                System.out.println("Citizen — view only");
                break;
        }
    }
}
