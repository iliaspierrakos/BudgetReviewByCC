package Main;

import java.util.Scanner;
import UserManagement.*;

public class BudgetReviewMain {
    public static void main(String[] args) {
        UserManager userManager = new UserManager();
         AuthUI authUI = new AuthUI(userManager);
        authUI.start();
    }
}
