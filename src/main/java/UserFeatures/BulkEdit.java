package UserFeatures;

import java.util.Scanner;
import java.util.ArrayList;

/**
 * The BulkEdit class handles bulk operations on ministry budgets.
 * It allows applying changes to all ministries or selected ones,
 */
public class BulkEdit {
    private Scanner scanner = new Scanner(System.in);
    
    public void bulkEditMenu() {
        System.out.println("\nWhat do you want to do? ");
        System.out.println("1. Apply percentage change to ALL ministries");
        System.out.println("2. Apply fixed amount change to ALL ministries");
        System.out.println("3. Apply change to SELECTED ministries");
        System.out.println("4. Return");
        
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        switch(choice) {
            case 1:
                percentageChangeAll(1, choice);
                break;
            case 2:
                fixedAmountChangeAll(1, choice);
                break;
            case 3:
                selectedMinistriesEdit(1, choice);
                break;
            case 4:
                return;
            default:
                System.out.println("Invalid option");
        }
    }
    
    /**
     * Applies a percentage change to ALL ministries with preview.
     * User can specify positive (increase) or negative (decrease) percentages.
     * Shows a detailed preview before confirming changes.
     * Updates the Edit.balance accordingly.
     */
    private void percentageChangeAll(int x, int choice) {
        if (x == 1) {
            System.out.println("Enter percentage change (positive to increase, negative to decrease):");
            System.out.println("*** Note: Increase must be smaller than the balance ***");
        } else if (x == 2) {
            System.out.println( "Now, your available balance is " + Ministry.getFormattedBudget(Edit.balance));
            System.out.println("So let's try again. Insert percentage change");
        }
        double percentage = scanner.nextDouble();
        scanner.nextLine();
        // Restriction: Cannot decrease by 100% or more
        if (percentage <= -100) {
            System.out.println("Cannot decrease by 100% or more!");
            return;
        }
        double totalChange = calculateTotalChange(percentage, null);
        if (percentage > 0 && totalChange > Edit.balance) {
            System.out.println("Not enough available money! You have to decrease.");
            necessaryDecrease(choice);       
        } else {
            // Show preview with before/after comparison
            showBeforeAfterPreview(percentage, null);
            
            System.out.println("\nConfirm changes? (yes/no):");
            String confirm = scanner.nextLine();
            confirm = Ministry.yesOrNo(confirm);
            
            if (confirm.equalsIgnoreCase("yes")) {
                applyPercentageChange(percentage, null);
                
                // Update Edit.balance based on total change
                totalChange = calculateTotalChange(percentage, null);
                if (totalChange < 0) { // If decrease occurred
                    Edit.balance += Math.abs(totalChange);
                    System.out.println("Available money for Investment updated: " + Ministry.getFormattedBudget(Edit.balance));
                } else { // If increase occurred
                    if (Edit.balance >= totalChange) {
                        Edit.balance -= totalChange;
                        System.out.println("Available money for Investment updated: " + Ministry.getFormattedBudget(Edit.balance));
                    }
                }
                
                System.out.println("All budgets updated successfully!");
            } else {
                System.out.println("Operation cancelled.");
            }
            System.out.println(Edit.balance);
        }
    }
    
    /**
     * Applies a fixed amount change to ALL ministries with preview.
     * User can specify positive (add) or negative (subtract) amounts.
     * Validates that no ministry budget becomes negative.
     * Updates the Edit.balance accordingly.
     */
    private void fixedAmountChangeAll(int x, int choice) {
        if (x == 1) {
            System.out.println("Enter fixed amount to add/subtract from each ministry:");
            System.out.println("(Use negative number for decrease)");
        } else {
            System.out.println("Great. Now the available money is " + Ministry.getFormattedBudget(Edit.balance));
            System.out.println("So let's try again. Insert amount.");
        }    
        double amount = scanner.nextDouble();
        scanner.nextLine();
        
        // Validation: Check if any budget would become negative
        boolean wouldCauseNegative = false;
        for (Ministry m : CreatingMinistries.ministries2026) {
            if (m != null && m.getBudget() + amount < 0) {
                System.out.println("Error: This would make " + m.getMinistryName() + " budget negative!");
                System.out.println("Current budget: " + Ministry.getFormattedBudget(m.getBudget()));
                System.out.println("Attempted change: " + Ministry.getFormattedBudget(amount));
                wouldCauseNegative = true;
            }
        }
        
        if (wouldCauseNegative) {
            return;
        }
        // Update Edit.balance based on total change
        int ministryCount = 0;
        for (Ministry m : CreatingMinistries.ministries2026) {
            if (m != null) ministryCount++;
        }
        double totalChange = amount * ministryCount;
        if (totalChange > Edit.balance) {
            necessaryDecrease(choice);
        } else {

            // Show preview with before/after comparison
            showFixedAmountPreview(amount, null);
            
            System.out.println("\nConfirm changes? (yes/no):");
            String confirm = scanner.nextLine();
            confirm = Ministry.yesOrNo(confirm);
            
            if (confirm.equalsIgnoreCase("yes")) {
                applyFixedAmountChange(amount, null);

                if (amount < 0) { // Decrease
                    Edit.balance += Math.abs(totalChange);
                } else { // Increase
                    if (Edit.balance >= totalChange) {
                        Edit.balance -= totalChange;
                    } 
                }
                
                System.out.println("Available money for Investment: " + 
                    Ministry.getFormattedBudget(Edit.balance));
                System.out.println("All budgets updated successfully!");
            } else {
                System.out.println("Operation cancelled.");
            }
        }    
    }
    
    /**
     * Applies changes to SELECTED ministries.
     * User selects specific ministries by entering their numbers (comma-separated).
     * Then chooses between percentage or fixed amount change.
     * Shows preview and updates Edit.balance accordingly.
     */
    private void selectedMinistriesEdit(int x, int choice) {
        if (x == 1) {
            System.out.println("\n*** Select Ministries ***");
            System.out.println("Available ministries:");
        } else {
            System.out.println("Successful decrease. Available money now is " + Ministry.getFormattedBudget(Edit.balance)+ ".");
            System.out.println("Let's try again. Select from these ministries: ");
        }
        Ministry.displayListOfMinistries();
        
        System.out.println("\nEnter ministry numbers separated by commas (e.g., 1,3,5,7):");
        String input = scanner.nextLine();
        ArrayList<Integer> selectedIndices = new ArrayList<>(); 
        selectedIndices = fillingListWithIndex(input);
        // Check if any valid ministries were selected
        if (selectedIndices.isEmpty()) {
            System.out.println("No valid ministries selected. Operation cancelled.");
            return;
        }
        
        // Display selected ministries
        System.out.println("\nYou selected " + selectedIndices.size() + " ministries:");
        int counter = 0;
        for (int idx : selectedIndices) {
            System.out.println("- " + CreatingMinistries.ministries2026[idx].getMinistryName());
            counter++;        
        }
        
        // Choose operation type
        System.out.println("\nChoose operation:");
        System.out.println("1. Apply percentage change");
        System.out.println("2. Apply fixed amount change");
        int opChoice = scanner.nextInt();
        
        scanner.nextLine();
        
        if (opChoice == 1) {
            // PERCENTAGE CHANGE for selected ministries
            System.out.println("Enter percentage change:");
            double percentage = scanner.nextDouble();
            scanner.nextLine();
            
            if (percentage <= -100) {
                System.out.println("Cannot decrease by 100% or more!");
                return;
            }
            double totalChange = calculateTotalChange(percentage, selectedIndices);
            if (totalChange > Edit.balance){
                System.out.println("Decrease necessary.");
                necessaryDecrease(choice);
            } else {
                // Show preview
                executionSelectedPercentage(percentage, selectedIndices);
            }
        } else if (opChoice == 2) {
            // fixed amount change for selected ministries
            System.out.println("Enter fixed amount change per ministry:");
            double amount = scanner.nextDouble();
            scanner.nextLine();
            
            // Validation: Check for negative budgets
            boolean wouldCauseNegative = false;
            for (int idx : selectedIndices) {
                Ministry m = CreatingMinistries.ministries2026[idx];
                if (m.getBudget() + amount < 0) {
                    System.out.println("Error: This would make " + m.getMinistryName() + " budget negative!");
                    wouldCauseNegative = true;
                }
            }
            if (wouldCauseNegative) {
                amount = smallerNegative(amount, selectedIndices);
            }
            double totalChange = counter * amount;
            if (totalChange > Edit.balance){
                System.out.println("Decrease necessary.");
                necessaryDecrease(choice);
            } else {
            executionSelectedFixed(amount, selectedIndices);
            }
        } else {
            System.out.println("Invalid operation choice.");
        }
    }
    
    /**
     * Displays a before/after preview for percentage changes.
     * @param percentage The percentage change to apply
     * @param selectedIndices List of selected ministry indices (null = all ministries)
     */
    private void showBeforeAfterPreview(double percentage, ArrayList<Integer> selectedIndices) {
        StringBuilder sb = new StringBuilder();
        
        //  HEADER 
        TableUtils.appendSeparator(sb, 140, '=');
        TableUtils.appendTitle(sb, "PREVIEW - BUDGET CHANGES (Percentage: " + percentage + "%)", 140);
        TableUtils.appendSeparator(sb, 140, '=');
        
        //  COLUMN HEADERS 
        TableUtils.appendTableRowCustom(sb, 55, 25, "MINISTRY", "CURRENT BUDGET", "NEW BUDGET", "CHANGE");
        TableUtils.appendSeparator(sb, 140, '-');
        
        double totalCurrentBudget = 0;
        double totalNewBudget = 0;
        
        //  DATA ROWS 
        for (int i = 0; i < CreatingMinistries.ministries2026.length; i++) {
            Ministry m = CreatingMinistries.ministries2026[i];
            
            if (m != null) {
                // If selectedIndices == null, show all ministries
                // If selectedIndices != null, show only selected ones
                if (selectedIndices == null || selectedIndices.contains(i)) {
                    double oldBudget = m.getBudget();
                    double newBudget = oldBudget * (1 + percentage / 100.0);
                    double change = newBudget - oldBudget;
                    
                    totalCurrentBudget += oldBudget;
                    totalNewBudget += newBudget;
                    
                    TableUtils.appendTableRowCustom(sb, 55, 25,
                        m.getMinistryName(),
                        Ministry.getFormattedBudget(oldBudget),
                        Ministry.getFormattedBudget(newBudget),
                        Ministry.getFormattedBudget(change));
                }
            }
        }
        
        //  FOOTER
        TableUtils.appendSeparator(sb, 140, '-');
        TableUtils.appendTableRowCustom(sb, 55, 25,
            "TOTAL",
            Ministry.getFormattedBudget(totalCurrentBudget),
            Ministry.getFormattedBudget(totalNewBudget),
            Ministry.getFormattedBudget(totalNewBudget - totalCurrentBudget));
        TableUtils.appendSeparator(sb, 140, '=');
        
        // Display the preview
        System.out.println(sb.toString());
    }
    
    /**
     * Displays a before/after preview for fixed amount changes.
     * Shows current budget, new budget, and the fixed change amount for each ministry.
     
     * @param amount The fixed amount to add/subtract
     * @param selectedIndices List of selected ministry indices (null = all ministries)
     */
    private void showFixedAmountPreview(double amount, ArrayList<Integer> selectedIndices) {
        StringBuilder sb = new StringBuilder();
        
        //  HEADER 
        TableUtils.appendSeparator(sb, 140, '=');
        TableUtils.appendTitle(sb, "PREVIEW - BUDGET CHANGES (Fixed Amount: " + Ministry.getFormattedBudget(amount) + ")", 140);
        TableUtils.appendSeparator(sb, 140, '=');
        
        //  COLUMN HEADERS 
        TableUtils.appendTableRowCustom(sb, 55, 25, "MINISTRY", "CURRENT BUDGET", "NEW BUDGET", "CHANGE");
        TableUtils.appendSeparator(sb, 140, '-');
        
        double totalCurrentBudget = 0;
        double totalNewBudget = 0;
        int affectedCount = 0;
        
        //  DATA ROWS 
        for (int i = 0; i < CreatingMinistries.ministries2026.length; i++) {
            Ministry m = CreatingMinistries.ministries2026[i];
            
            if (m != null) {
                if (selectedIndices == null || selectedIndices.contains(i)) {
                    double oldBudget = m.getBudget();
                    double newBudget = oldBudget + amount;
                    
                    totalCurrentBudget += oldBudget;
                    totalNewBudget += newBudget;
                    affectedCount++;
                    
                    TableUtils.appendTableRowCustom(sb, 55, 25,
                        m.getMinistryName(),
                        Ministry.getFormattedBudget(oldBudget),
                        Ministry.getFormattedBudget(newBudget),
                        Ministry.getFormattedBudget(amount));
                }
            }
        }
        
        //  FOOTER 
        TableUtils.appendSeparator(sb, 140, '-');
        TableUtils.appendTableRowCustom(sb, 55, 25,
            "TOTAL (" + affectedCount + " ministries)",
            Ministry.getFormattedBudget(totalCurrentBudget),
            Ministry.getFormattedBudget(totalNewBudget),
            Ministry.getFormattedBudget(amount * affectedCount));
        TableUtils.appendSeparator(sb, 140, '=');
        
        // Display the preview
        System.out.println(sb.toString());
    }
    
    /**
     * Applies percentage change to ministries and records changes in history.
     * 
     * @param percentage The percentage change to apply
     * @param selectedIndices List of selected ministry indices (null = apply to all)
     */
    private void applyPercentageChange(double percentage, ArrayList<Integer> selectedIndices) {
        for (int i = 0; i < CreatingMinistries.ministries2026.length; i++) {
            Ministry m = CreatingMinistries.ministries2026[i];
            
            if (m != null) {
                if (selectedIndices == null || selectedIndices.contains(i)) {
                    double oldBudget = m.getBudget();
                    double newBudget = oldBudget * (1 + percentage / 100.0);
                    EditHistory.historyOfEdit(m.getMinistryName(), oldBudget, newBudget);
                    m.setBudget(newBudget);
                }
            }
        }
    }
    
    /**
     * Applies fixed amount change to ministries and records changes in history.
     * 
     * @param amount The amount to add/subtract from each ministry
     * @param selectedIndices List of selected ministry indices (null = apply to all)
     */
    private void applyFixedAmountChange(double amount, ArrayList<Integer> selectedIndices) {
        for (int i = 0; i < CreatingMinistries.ministries2026.length; i++) {
            Ministry m = CreatingMinistries.ministries2026[i];
            
            if (m != null) {
                if (selectedIndices == null || selectedIndices.contains(i)) {
                    double oldBudget = m.getBudget();
                    double newBudget = oldBudget + amount;
                    
                    // Record the change in history
                    EditHistory.historyOfEdit(m.getMinistryName(), oldBudget, newBudget);
                    m.setBudget(newBudget);
                }
            }
        }
    }
    
    /**
     * Calculates the total budget change resulting from a percentage operation.
     * Used to update the Edit.balance appropriately.
     * 
     * @param percentage The percentage change
     * @param selectedIndices List of selected ministry indices (null = all ministries)
     * @return Total change amount (negative for decrease, positive for increase)
     */
    private double calculateTotalChange(double percentage, ArrayList<Integer> selectedIndices) {
        double totalChange = 0;
        
        for (int i = 0; i < CreatingMinistries.ministries2026.length; i++) {
            Ministry m = CreatingMinistries.ministries2026[i];
            
            if (m != null) {
                if (selectedIndices == null || selectedIndices.contains(i)) {
                    double oldBudget = m.getBudget();
                    double change = oldBudget * (percentage / 100.0);
                    totalChange += change;
                }
            }
        }
        
        return totalChange;
    }
    public void necessaryDecrease(int choice) {
        System.out.println("Select which ministries' budget you want to decrease: ");
        Ministry.displayListOfMinistries();
        System.out.println("\nEnter ministry numbers separated by commas (e.g., 1,3,5,7):");
        String input = scanner.nextLine();
        fillingListWithIndex(input);
        ArrayList<Integer> selectedIndices = new ArrayList<>(); 
        selectedIndices = fillingListWithIndex(input);
        // Check if any valid ministries were selected
        if (selectedIndices.isEmpty()) {
            System.out.println("No valid ministries selected. Operation cancelled.");
            return;
        }
        
        // Display selected ministries
        System.out.println("\nYou selected " + selectedIndices.size() + " ministries:");
        for (int idx : selectedIndices) {
            System.out.println("- " + CreatingMinistries.ministries2026[idx].getMinistryName());
        }
        
        // Choose operation type
        System.out.println("\nChoose operation:");
        System.out.println("1. Apply percentage change");
        System.out.println("2. Apply fixed amount change");
        int opChoice = scanner.nextInt();
        
        scanner.nextLine();
        
        if (opChoice == 1) {
            // PERCENTAGE CHANGE for selected ministries
            System.out.println("Enter percentage change:");
            double percentage = scanner.nextDouble();
            percentage = validateForDecrease(percentage);
            scanner.nextLine();
            
            if (percentage <= -100) {
                System.out.println("Cannot decrease by 100% or more!");
                while (percentage <= -100) {
                percentage = scanner.nextDouble();
                percentage = validateForDecrease(percentage);
                }            
            }
            executionSelectedPercentage(percentage, selectedIndices);
        } else if (opChoice == 2) {
            // fixed amount change for selected ministries
            System.out.println("Enter fixed amount change per ministry:");
            double amount = scanner.nextDouble();
            amount = validateForDecrease(amount); 
            scanner.nextLine();
            
            // Validation: Check for negative budgets
            boolean wouldCauseNegative = false;
            for (int idx : selectedIndices) {
                Ministry m = CreatingMinistries.ministries2026[idx];
                if (m.getBudget() + amount < 0) {
                    System.out.println("Error: This would make " + m.getMinistryName() + " budget negative!");
                    wouldCauseNegative = true;
                }
            }
            
            if (wouldCauseNegative) {
                amount = smallerNegative(amount, selectedIndices);
            } 
            executionSelectedFixed(amount, selectedIndices);

        } else {
            while (opChoice != 1 && opChoice != 2) {
                System.out.println("Invalid operation choice.");
                opChoice = scanner.nextInt();
            }
        }
        switch(choice) {
            case 1:
                percentageChangeAll(2, choice);
                break;
            case 2:
                fixedAmountChangeAll(2, choice);
                break;
            case 3:
                selectedMinistriesEdit(2, choice);
                break;
            case 4:
                return;
            default:
                System.out.println("Invalid option");
        }
    }

    public ArrayList<Integer> fillingListWithIndex(String input) {
        ArrayList<Integer> selectedIndices = new ArrayList<>();
        String[] parts = input.split(",");
        
        for (String s : parts) {
            try {
                int userNumber = Integer.parseInt(s.trim());
                int index = userNumber - 1; // Convert from 1-based to 0-based indexing
                
                // Validate the index
                if (index >= 0 && index < 20 && CreatingMinistries.ministries2026[index] != null) {
                    selectedIndices.add(index);
                } else {
                    System.out.println("Ignoring invalid number: " + userNumber);
                }
            } catch (NumberFormatException e) {
                System.out.println("Ignoring invalid input: " + s);
            }
        }
        return selectedIndices;
    }
    public double validateForDecrease(double number) {
        while (number >= 0) {
            System.out.println("Decrease Necessary. Insert negative number");
            number = scanner.nextDouble();
        }
        return number;
    }

    public void executionSelectedPercentage(double percentage, ArrayList<Integer> selectedIndices) {
        showBeforeAfterPreview(percentage, selectedIndices);
            
        System.out.println("\nConfirm changes? (yes/no):");
        String confirm = scanner.nextLine();
        confirm = Ministry.yesOrNo(confirm);
                    
        if (confirm.equalsIgnoreCase("yes")) {
            applyPercentageChange(percentage, selectedIndices);
                
            // Update balance
            double totalChange = calculateTotalChange(percentage, selectedIndices);
            if (totalChange < 0) {
                Edit.balance += Math.abs(totalChange);
            } else {
                Edit.balance -= totalChange;
            }
            
            System.out.println("Selected ministries updated!");
        } else {
            System.out.println("Operation cancelled.");
        }
    }
    public double smallerNegative(double amount,ArrayList<Integer> selectedIndices ) {
        boolean wouldCauseNegative = true;
        while (wouldCauseNegative == true || amount >= 0) {
            // Validation: Check for negative budgets
            amount = scanner.nextDouble();
            if ( amount >= 0 ) {
                System.out.println("Insert non-positive.");
                continue;
            }
            wouldCauseNegative = false;
            for (int idx : selectedIndices) {
                Ministry m = CreatingMinistries.ministries2026[idx];
                if (m.getBudget() + amount < 0) {
                    System.out.println("Error: This would make " + m.getMinistryName() + " budget negative!");
                    wouldCauseNegative = true;
                }
            }
        }
        return amount;

    }
    public void executionSelectedFixed(double amount, ArrayList<Integer> selectedIndices) {
        // Show preview
        showFixedAmountPreview(amount, selectedIndices);    
        System.out.println("\nConfirm changes? (yes/no):");
        String confirm = scanner.nextLine();
        confirm = Ministry.yesOrNo(confirm);
            
        if (confirm.equalsIgnoreCase("yes")) {
            applyFixedAmountChange(amount, selectedIndices);
                
            // Update balance
            double totalChange = amount * selectedIndices.size();
            if (amount < 0) {
                Edit.balance += Math.abs(totalChange);
            } else {
                if (Edit.balance >= totalChange) {
                    Edit.balance -= totalChange;
                } else {
                    System.out.println("Warning: Increase exceeds available balance!");
                }
            }
                
            System.out.println("Available money for Investment: " + 
                Ministry.getFormattedBudget(Edit.balance));
            System.out.println("Selected ministries updated!");
        } else {
            System.out.println("Operation cancelled.");
        }
    }

}

