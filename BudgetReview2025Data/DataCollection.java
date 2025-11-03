public static class DataCollection {
    Scanner scanner = new Scanner(System.in);
    public static string ministrySelection() {
        System.out.println("The budget of which ministry would you like to change?");
        System.out.println("Ministry of: ");
        String name = scanner.nextLine();
        return name;
    }
    public static string upOrDown() {
        System.out.println("How would you like to change this budget? Increase or Decrease? ");
        String change = scanner.nextLine();
        return change; 
    }
    public static double amount() {
        System.out.println("By how much? ");
        double amount = scanner.nextDouble();
    }
}