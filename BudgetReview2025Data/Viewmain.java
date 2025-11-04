import java.util.Scanner;
public class ViewMain {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Ministries min = new Ministries();
        MinistriesBudgets budg = new MinistriesBudgets();
        budg.budget();
        min.minlist();
        System.out.println("Do you want to :");
        System.out.println("1.View");
        System.out.println("2.Edit");
        int number = scanner.nextInt();
        String answer = "no";
        switch (number) {
            case 1:
                View.view(); //Run View
                break;
            case 2:
                do {
                Edit obj = new Edit();
                obj.collectData(); // Need Object!!!!!!!
                answer = scanner.nextLine();
                }
              while (answer.equalsIgnoreCase("yes"));
                break;
            default:
                System.out.println("Invalid");
                break;
        }
    }//System.out.println(Ministry.getCounter());
}