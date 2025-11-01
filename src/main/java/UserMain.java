public class UserMain {
    public static void main(String[] args) {
        UserManager userManager = new UserManager();
        AuthUI authUI = new AuthUI(userManager);
        authUI.start();
    }
}
