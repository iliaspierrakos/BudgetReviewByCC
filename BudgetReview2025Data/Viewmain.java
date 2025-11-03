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
        if (number==1)  {
            View.view();//Run View
        } else if (number==2) {
            int a=1;// Need Object!!!!!!!
        } else {
            System.out.println("Invalid");
        }


        //System.out.println(Ministry.getCounter());
    }
}