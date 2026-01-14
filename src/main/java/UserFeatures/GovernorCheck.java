package UserFeatures;

/**
 * This class operates the feature of the Governor checking the ministers proposals. The class
 * includes the viewing of the proposals' files and the governor selecting which .txt file he will
 * see each time(he will either accept or reject the proposal). After accepting the proposal,the
 * budget is updated with the changes. After rejecting the proposal, the file will be deleted from
 * the folder.
 */
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class GovernorCheck {
  Scanner scanner = new Scanner(System.in);

  public void viewProposalsNames() {
    File folder = new File("src/main/resources/NecessaryFilesAndData/ProposalsFromMinisters"); // folder
    File[] files = folder.listFiles(); // saving the files name inside the folder
    if (files != null) {
      int counter = 0;
      for (File file : files) { // enhanced for used for searching inside the File Array
        if (file.isFile()) {
          System.out.println(file.getName());
          counter++;
        }
      }
      if (!(counter == 0)) {
        System.out.println("PLease select which minister's budget changes you would like to see?");
        String ans = scanner.nextLine();
        viewProposal(ans);
      } else {
        System.out.println("Folder empty.");
      }
    } else {
      System.out.println("Folder not found or empty.");
    }
  }

  public void viewProposal(String fileName) {
    // System.out.println(fileName);
    try {
      List<String> lines = Files.readAllLines(Paths.get(
          "src/main/resources/NecessaryFilesAndData/ProposalsFromMinisters/" + fileName + ".txt"));
      for (String line : lines) {
        System.out.println(line);
      }
    } catch (Exception e) {
    }
    fileManagement(budgetChecking(), fileName);
  }

  public boolean budgetChecking() {
    System.out.println("Would you like to accept the changes?");
    String ans = scanner.nextLine();
    while (!(ans.equalsIgnoreCase("yes") || ans.equalsIgnoreCase("no"))) {
      System.out.println("Please type yes or no:");
      ans = scanner.nextLine();
    }
    if (ans.equalsIgnoreCase("yes")) {
      return true;
    } else {
      return false;
    }
  }

  public void fileManagement(boolean accepted, String fileName) {
    if (accepted == true) {
      EditHistoryList history = new EditHistoryList();
      history.applyingEdits();
    } else {
      Path filePath = Paths.get(
          "src/main/resources/NecessaryFilesAndData/ProposalsFromMinisters/" + fileName + ".txt");
      try {
        Files.deleteIfExists(filePath);
        System.out.println("File deleted successfully.");
      } catch (Exception e) {
        System.out.println("Could not delete file.");
      }
    }
  }
}
