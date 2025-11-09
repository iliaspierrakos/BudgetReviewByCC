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
                    break;
                default:
                     System.out.println("Invalid choice. Try again.");
            }
        }
    }
    
    private void registerUserUI() {
        System.out.println("\n=== Register New User ===");
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        System.out.println("Select role: ");
        System.out.println("(1) -> Citizen -- view only");
        System.out.println("(2)-> MinistryMember -- manage assigned ministry");
        System.out.println("(3)-> Governor -- full access to regional and national info");
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
                System.out.println("Enter ministry name");
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
        if (role == User.Role.MINISTRYMEMBER ? 
        userManager.registerUser(username, password, role, ministryName) :
        userManager.registerUser(username, password, role)) {
    System.out.println("Registration successful!");
}

    }
    
    private void loginUser() {
        System.out.println("\n=== Login ===");
        System.out.print("Enter username: ");
        String username = scanner.nextLine(); 
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        User user = userManager.loginUser(username,password);
        if (user != null) {
            System.out.println("Welcome, " + user.getUsername() + "! Role: " + user.getRole());
            showRoleMenu(user); 
        }
    }
    private void showRoleMenu(User user) {
            switch (user.getRole()) {
            case MINISTRYMEMBER:
                System.out.println("MinistryMember — regional access");
                break;
            case GOVERNOR:
                System.out.println("Governor — full access");
                break;
            case CITIZEN:
                System.out.println("Citizen — view only");
                break;
        }
    }
}

