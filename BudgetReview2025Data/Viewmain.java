public class ViewMain { 
    public static void main(String[] args) {
        Ministries min = new Ministries();
        MinistriesBudgets budg = new MinistriesBudgets();
        View show = new View();
        budg.budget();
        min.minlist();
        show.view();
        //System.out.println(Ministry.getCounter());
    }
}