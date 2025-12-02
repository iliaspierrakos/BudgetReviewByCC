package UserFeatures;
import java.util.Scanner;
import UserManagement.User;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

public class Propose {
    public static void editProposal(User user) {
        if (user.getRole() == User.Role.GOVERNOR || user.getRole() == User.Role.CITIZEN) {
            return;
        }
        Scanner scanner = new Scanner(System.in);
        FileWriter fw = null;
        PrintWriter pw = null;
        try {
            fw = new FileWriter("NecessaryFilesAndData/" + user.getUsername() + " .txt", true);
            pw = new PrintWriter(fw);

            pw.close();
            fw.close();
        } catch(IOException e) {}

    }
}