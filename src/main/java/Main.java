import java.sql.*;
import java.util.Scanner;

public class Main {

    private static boolean runLoop = true;
    private final static String dbUrl = "jdbc:sqlite:./";

    public static void main(String[] args) throws SQLException {

        if (args.length == 0) {
            exitWithErrorMessage("No args found");
        }
        if (args[0].equals("-filename") && args.length < 2) {
            exitWithErrorMessage("No db provided");
        }

        String url = dbUrl.concat(args[1]);
        Connection connection = connect(url);
        Scanner input = new Scanner(System.in);
        Menu userMenu = new Menu();
        BankingSystem system = new BankingSystem(connection);

        while (runLoop) {
            if (system.isLoggedIn()) {
                loggedInOperations(system, input, userMenu);
            } else {
                loggedOutOperations(system, input, userMenu);
            }
        }
        if (connection != null) connection.close();
        input.close();
    }

    public static Connection connect(String url) {
        Connection connection = null;
        Statement statement;
        try {
            connection = DriverManager.getConnection(url);
            statement = connection.createStatement();
            String createTableSql = "CREATE TABLE card (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "number TEXT NOT NULL UNIQUE," +
                    "pin TEXT NOT NULL," +
                    "balance INTEGER DEFAULT 0);";
            statement.executeUpdate(createTableSql);
            statement.close();
        } catch (SQLException e) {
            String s = e.getMessage();
            if (!s.contains("table card already exists")) {
                System.err.println(s);
            }
        }
        return connection;
    }

    public static void loggedInOperations(BankingSystem system, Scanner input, Menu userMenu) {
        userMenu.showLoggedInMenu();
        int option = input.nextInt();
        switch (option){
            case 1:
                System.out.printf("%nBalance: %d%n", system.showBalance());
                break;
            case 2:
                System.out.println(System.lineSeparator() + "Enter income:");
                int income = input.nextInt();
                system.addIncome(income);
                break;
            case 3:
                input.nextLine();
                System.out.println(System.lineSeparator() + "Enter card number:");
                String number = input.nextLine();
                String invalidTransferMessage = system.validateCardNumber(number);
                if (invalidTransferMessage != null) {
                    System.out.println(invalidTransferMessage);
                    break;
                }
                System.out.println(System.lineSeparator() + "Enter how much money you want to transfer:");
                int amount = input.nextInt();
                invalidTransferMessage = system.validateAmount(amount);
                if (invalidTransferMessage != null) {
                    System.out.println(invalidTransferMessage);
                    break;
                }
                system.doTransfer(amount, number);
                break;
            case 4:
                system.closeAccount();
                break;
            case 5:
                system.logOutOfAccount();
                break;
            case 0:
                System.out.println("Bye!");
                runLoop = false;
                break;
            default:
                System.out.println("Invalid Input");
                runLoop = false;
                break;
        }
    }

    public static void loggedOutOperations(BankingSystem system, Scanner input, Menu userMenu) {
        userMenu.showMenu();
        int option = input.nextInt();
        switch (option){
            case 1:
                System.out.println(system.createAccount());
                break;
            case 2:
                input.nextLine();
                System.out.println(System.lineSeparator() + "Enter your card number:");
                String cardNumber = input.nextLine();
                System.out.println("Enter your PIN:");
                String pin = input.nextLine();
                System.out.println(system.loginToAccount(cardNumber, pin));
                break;
            case 0:
                System.out.println("Bye!");
                runLoop = false;
                break;
            default:
                System.out.println("Invalid Input");
                runLoop = false;
                break;
        }
    }

    public static void exitWithErrorMessage(String message) {
        System.out.println(message);
        System.exit(0);
    }
}