import java.util.Scanner;

public class Login {

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        
        System.out.print("Username: ");
        String username = input.nextLine();

        System.out.print("Password: ");
        String password = input.nextLine();

        
        if (username.equals("primeminister") && password.equals("1234")) {
            System.out.println("Welcome Primeminister!");
        } 
        else if (username.equals("minister") && password.equals("1234")) {
            System.out.println("Welcome " + username + "!");
        } 
        else {
            System.out.println("Error");
        }

        input.close();
    }
}