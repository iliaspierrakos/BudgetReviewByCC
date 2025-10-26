import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.io.BufferedWriter;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class LargePdfTextExtractor {
    public static void main(String[] args) {
        // Χρήση File απευθείας
        File pdfFile = new File("C:\\Users\\mirto\\OneDrive\\Υπολογιστής\\Κρατικός-Προϋπολογισμός-2025_ΟΕ.pdf");
        String outputFile = "BudgetReview2025.txt";

        try (PDDocument document = Loader.loadPDF(pdfFile)) {

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(1);
            stripper.setEndPage(document.getNumberOfPages());

            String text = stripper.getText(document);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
                writer.write(text);
            }

            System.out.println("Το κείμενο του PDF σώθηκε στο: " + outputFile);

        } catch (IOException e) {
            System.err.println("Σφάλμα κατά την ανάγνωση ή εξαγωγή PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }
}



