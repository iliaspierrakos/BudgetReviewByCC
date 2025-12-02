package UserFeatures;
import java.util.Scanner;
import java.nio.file.Path;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Compare {
    public static void comparingMinistries() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please type the first of the two years that you want to compare:");
        int firstYear = validityYear();
        System.out.println("Please type the second year that you want to compare:");
        int secondYear = validityYear();
        CreatingMinistries.ministryCreation(Path.of("NecessaryFilesAndData/MinistriesBudgets" + firstYear + ".csv"));
        CreatingMinistries.ministryCreation(Path.of("NecessaryFilesAndData/MinistriesBudgets" + secondYear + ".csv"));
        FileWriter fw = null;
        PrintWriter pw = null;
        try {
            fw = new FileWriter("NecessaryFilesAndData/compare" + firstYear + "with" + secondYear +".txt", false);
            pw = new PrintWriter(fw);
            Ministry[] firstYearMinistry = View.ministryYear(firstYear);
            Ministry[] secondYearMinistry = View.ministryYear(secondYear);
            String[] budget1 = new String[20];
            int counter = 0;
            double mbudg;
            String readable;
            for (Ministry m : firstYearMinistry) {
                mbudg=m.getBudget();
                readable = Ministry.getFormattedBudget(mbudg); //Caution readble variable is String it is used only for readable print in View
                budget1[counter] = readable;
                counter++;
            }
            counter=0;
            for (Ministry m : secondYearMinistry) {
                mbudg=m.getBudget();
                readable = Ministry.getFormattedBudget(mbudg); //Caution readble variable is String it is used only for readable print in View
                pw.println(m.getMinistryName() + "| " + firstYear + " budget: " + budget1[counter] + "$ | " + secondYear  + " budget: " + readable + "$"  );
                counter++;
            }

            pw.close();
            fw.close();
            System.out.println(Files.readString(Paths.get("NecessaryFilesAndData/compare" + firstYear + "with" + secondYear +".txt")));
        } catch(IOException e) {}

    }

    public static int validityYear() {
        Scanner scanner = new Scanner(System.in);
        int selectedYear = 0;
        boolean validYear = false;
        while (!validYear) {
            System.out.println("Please select a year (2020-2026):");
            try {
                selectedYear = scanner.nextInt();
                if (selectedYear >= 2020 && selectedYear <= 2026) {
                    validYear = true;
                } else {
                    System.out.println("Invalid year. Please enter a year between 2020 and 2026.");
                }
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a valid year.");
                scanner.nextLine();
            }
        }
        return selectedYear;
    }
}