import java.io.File;

public class FileFinder {
    public static void main(String[] args) {
        
        File file = new File("C:\\cygwin64\\home\\mirto\\BudgetReviewByCC\\Κρατικός_Προυπολογισμός.txt");
       
        if (file.exists()) {
            System.out.println("This file exists!");
        }
        else {
            System.out.println("This file doesn't exist");
        }
    }

}
 