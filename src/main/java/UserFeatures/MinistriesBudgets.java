package UserFeatures;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

public class MinistriesBudgets {

    private static final Map<Integer, List<String>> budgetsByYear = new HashMap<>();

    public static void loadFromResources(int year) {
        if (budgetsByYear.containsKey(year)) return;

        String resourcePath = "/NecessaryFilesAndData/BudgetReview" + year + ".txt";

        Pattern startsWith10 = Pattern.compile("^10");
        Pattern containsMinistry = Pattern.compile("Υπουργείο");

        List<String> result = new ArrayList<>();

        try (
            InputStream is = MinistriesBudgets.class.getResourceAsStream(resourcePath);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8))
        ) {
            String line;
            while ((line = reader.readLine()) != null && result.size() < 20) {
                if (startsWith10.matcher(line).find() &&
                    containsMinistry.matcher(line).find()) {
                    result.add(line);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load budgets for year " + year, e);
        }

        budgetsByYear.put(year, result);
    }

    public static List<String> getBudgets(int year) {
        return budgetsByYear.getOrDefault(year, List.of());
    }
}
