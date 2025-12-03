package UserFeatures;

import java.util.Scanner;
import java.util.ArrayList;

/**
 * The BulkEdit class handles bulk operations on ministry budgets.
 * It allows applying changes to all ministries or selected ones,
 * with preview functionality before confirming changes.
 */
public class BulkEdit {
    private Scanner scanner = new Scanner(System.in);
    
    /**
     * Main menu for bulk edit operations.
     * Provides options for:
     * 1. Percentage change to all ministries
     * 2. Fixed amount change to all ministries
     * 3. Changes to selected ministries
     * 4. Return to previous menu
     */
    public void bulkEditMenu() {
        System.out.println("\n*** Bulk Budget Operations ***");
        System.out.println("1. Apply percentage change to ALL ministries");
        System.out.println("2. Apply fixed amount change to ALL ministries");
        System.out.println("3. Apply change to SELECTED ministries");
        System.out.println("4. Return");
        
        int choice = scanner.nextInt();
        scanner.nextLine(); // Clear buffer
        
        switch(choice) {
            case 1:
                percentageChangeAll();
                break;
            case 2:
                fixedAmountChangeAll();
                break;
            case 3:
                selectedMinistriesEdit();
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
    private void percentageChangeAll() {
        System.out.println("Enter percentage change (e.g., -10 for 10% decrease, 5 for 5% increase):");
        double percentage = scanner.nextDouble();
        scanner.nextLine();
        
        // Validation: Cannot decrease by 100% or more
        if (percentage <= -100) {
            System.out.println("Cannot decrease by 100% or more!");
            return;
        }
        
        // Show preview with before/after comparison
        showBeforeAfterPreview(percentage, null);
        
        System.out.println("\nConfirm changes? (yes/no):");
        String confirm = scanner.nextLine();
        
        if (confirm.equalsIgnoreCase("yes")) {
            applyPercentageChange(percentage, null);
            
            // Update Edit.balance based on total change
            double totalChange = calculateTotalChange(percentage, null);
            if (totalChange < 0) { // If decrease occurred
                Edit.balance += Math.abs(totalChange);
                System.out.println("Available money for Investment updated: " + 
                    Ministry.getFormattedBudget(Edit.balance));
            } else { // If increase occurred
                if (Edit.balance >= totalChange) {
                    Edit.balance -= totalChange;
                    System.out.println("Available money for Investment updated: " + 
                        Ministry.getFormattedBudget(Edit.balance));
                } else {
                    System.out.println("Warning: Increase exceeds available balance!");
                }
            }
            
            System.out.println("✓ All budgets updated successfully!");
        } else {
            System.out.println("Operation cancelled.");
        }
    }
    
    /**
     * Applies a fixed amount change to ALL ministries with preview.
     * User can specify positive (add) or negative (subtract) amounts.
     * Validates that no ministry budget becomes negative.
     * Updates the Edit.balance accordingly.
     */
    private void fixedAmountChangeAll() {
        System.out.println("Enter fixed amount to add/subtract from each ministry:");
        System.out.println("(Use negative number for decrease, e.g., -100000)");
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
        
        // Show preview with before/after comparison
        showFixedAmountPreview(amount, null);
        
        System.out.println("\nConfirm changes? (yes/no):");
        String confirm = scanner.nextLine();
        
        if (confirm.equalsIgnoreCase("yes")) {
            applyFixedAmountChange(amount, null);
            
            // Update Edit.balance based on total change
            int ministryCount = 0;
            for (Ministry m : CreatingMinistries.ministries2026) {
                if (m != null) ministryCount++;
            }
            double totalChange = amount * ministryCount;
            
            if (amount < 0) { // Decrease
                Edit.balance += Math.abs(totalChange);
            } else { // Increase
                if (Edit.balance >= totalChange) {
                    Edit.balance -= totalChange;
                } else {
                    System.out.println("Warning: Increase exceeds available balance!");
                }
            }
            
            System.out.println("Available money for Investment: " + 
                Ministry.getFormattedBudget(Edit.balance));
            System.out.println("✓ All budgets updated successfully!");
        } else {
            System.out.println("Operation cancelled.");
        }
    }
    
    /**
     * Applies changes to SELECTED ministries.
     * User selects specific ministries by entering their numbers (comma-separated).
     * Then chooses between percentage or fixed amount change.
     * Shows preview and updates Edit.balance accordingly.
     */
    private void selectedMinistriesEdit() {
        System.out.println("\n*** Select Ministries ***");
        System.out.println("Available ministries:");
        
        // Display all ministries with numbers
        for (int i = 0; i < CreatingMinistries.ministries2026.length; i++) {
            if (CreatingMinistries.ministries2026[i] != null) {
                System.out.printf("%d. %s (Budget: %s)%n", 
                    i + 1, // Display 1-based numbering for user
                    CreatingMinistries.ministries2026[i].getMinistryName(),
                    Ministry.getFormattedBudget(CreatingMinistries.ministries2026[i].getBudget()));
            }
        }
        
        System.out.println("\nEnter ministry numbers separated by commas (e.g., 1,3,5,7):");
        String input = scanner.nextLine();
        
        // Parse the input numbers
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
            scanner.nextLine();
            
            if (percentage <= -100) {
                System.out.println("Cannot decrease by 100% or more!");
                return;
            }
            
            // Show preview
            showBeforeAfterPreview(percentage, selectedIndices);
            
            System.out.println("\nConfirm changes? (yes/no):");
            String confirm = scanner.nextLine();
            
            if (confirm.equalsIgnoreCase("yes")) {
                applyPercentageChange(percentage, selectedIndices);
                
                // Update balance
                double totalChange = calculateTotalChange(percentage, selectedIndices);
                if (totalChange < 0) {
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
                System.out.println("✓ Selected ministries updated!");
            } else {
                System.out.println("Operation cancelled.");
            }
            
        } else if (opChoice == 2) {
            // FIXED AMOUNT CHANGE for selected ministries
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
                return;
            }
            
            // Show preview
            showFixedAmountPreview(amount, selectedIndices);
            
            System.out.println("\nConfirm changes? (yes/no):");
            String confirm = scanner.nextLine();
            
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
                System.out.println("✓ Selected ministries updated!");
            } else {
                System.out.println("Operation cancelled.");
            }
        } else {
            System.out.println("Invalid operation choice.");
        }
    }
    
    /**
     * Displays a before/after preview for percentage changes.
     * Shows current budget, new budget, and the change amount for each ministry.
     * 
     * @param percentage The percentage change to apply
     * @param selectedIndices List of selected ministry indices (null = all ministries)
     */
    private void showBeforeAfterPreview(double percentage, ArrayList<Integer> selectedIndices) {
        System.out.println("\n" + "=".repeat(140));
        System.out.println(String.format("%80s", "PREVIEW - BUDGET CHANGES (Percentage: " + percentage + "%)"));
        System.out.println("=".repeat(140));
        System.out.printf("%-55s %25s %25s %25s%n", 
            "MINISTRY", "CURRENT BUDGET", "NEW BUDGET", "CHANGE");
        System.out.println("-".repeat(140));
        
        double totalCurrentBudget = 0;
        double totalNewBudget = 0;
        
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
                    
                    System.out.printf("%-55s %25s %25s %25s%n",
                        m.getMinistryName(),
                        Ministry.getFormattedBudget(oldBudget),
                        Ministry.getFormattedBudget(newBudget),
                        Ministry.getFormattedBudget(change));
                }
            }
        }
        
        // Display totals
        System.out.println("-".repeat(140));
        System.out.printf("%-55s %25s %25s %25s%n",
            "TOTAL",
            Ministry.getFormattedBudget(totalCurrentBudget),
            Ministry.getFormattedBudget(totalNewBudget),
            Ministry.getFormattedBudget(totalNewBudget - totalCurrentBudget));
        System.out.println("=".repeat(140));
    }
    
    /**
     * Displays a before/after preview for fixed amount changes.
     * Shows current budget, new budget, and the fixed change amount for each ministry.
     * 
     * @param amount The fixed amount to add/subtract
     * @param selectedIndices List of selected ministry indices (null = all ministries)
     */
    private void showFixedAmountPreview(double amount, ArrayList<Integer> selectedIndices) {
        System.out.println("\n" + "=".repeat(140));
        System.out.println(String.format("%80s", "PREVIEW - BUDGET CHANGES (Fixed Amount: " + 
            Ministry.getFormattedBudget(amount) + ")"));
        System.out.println("=".repeat(140));
        System.out.printf("%-55s %25s %25s %25s%n", 
            "MINISTRY", "CURRENT BUDGET", "NEW BUDGET", "CHANGE");
        System.out.println("-".repeat(140));
        
        double totalCurrentBudget = 0;
        double totalNewBudget = 0;
        int affectedCount = 0;
        
        for (int i = 0; i < CreatingMinistries.ministries2026.length; i++) {
            Ministry m = CreatingMinistries.ministries2026[i];
            
            if (m != null) {
                if (selectedIndices == null || selectedIndices.contains(i)) {
                    double oldBudget = m.getBudget();
                    double newBudget = oldBudget + amount;
                    
                    totalCurrentBudget += oldBudget;
                    totalNewBudget += newBudget;
                    affectedCount++;
                    
                    System.out.printf("%-55s %25s %25s %25s%n",
                        m.getMinistryName(),
                        Ministry.getFormattedBudget(oldBudget),
                        Ministry.getFormattedBudget(newBudget),
                        Ministry.getFormattedBudget(amount));
                }
            }
        }
        
        // Display totals
        System.out.println("-".repeat(140));
        System.out.printf("%-55s %25s %25s %25s%n",
            "TOTAL (" + affectedCount + " ministries)",
            Ministry.getFormattedBudget(totalCurrentBudget),
            Ministry.getFormattedBudget(totalNewBudget),
            Ministry.getFormattedBudget(amount * affectedCount));
        System.out.println("=".repeat(140));
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
                    
                    // Record the change in history
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
}