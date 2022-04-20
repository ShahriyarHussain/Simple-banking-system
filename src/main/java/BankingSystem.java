import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Random;

public class BankingSystem {

    private final Random random = new Random();
    private final int randomUpperLimit = 10;
    private boolean loggedIn = false;
    private String currentUserNumber;
    private final Connection connection;

    public BankingSystem(Connection connection) {
        this.connection = connection;
    }

    public String createAccount() {
        String number = generateCardNumber();
        String pin = generatePin();
        while (checkIfCardAlreadyExists(number)) {
            number = generateCardNumber();
        }
        addAccountToDb(number, pin);
        return String.format(
                "%nYour card has been created %nYour card number: %n%s%nYour card PIN: %n%s",
                number, pin);
    }

    public String loginToAccount(String number, String pin) {
        Map<String, String> infoMap = findNumberAndPinIfExists(number);
        if (infoMap == null || infoMap.get("number").isEmpty() || !infoMap.get("pin").equals(pin)) {
            return System.lineSeparator() + "Wrong card number or PIN!";
        }
        loggedIn = true;
        currentUserNumber = infoMap.get("number");
        return System.lineSeparator() + "You have successfully logged in!";
    }

    public Integer showBalance() {
        return findBalanceByNumber();
    }

    public void addIncome(int providedIncome) {
        addBalanceToAccount(providedIncome);
    }

    public void doTransfer(int amount, String number) {
        initiateTransfer(amount, number);
    }

    public void closeAccount() {
        deleteAccount();
        logOutOfAccount();
    }

    private void deleteAccount() {
        try (Statement statement = connection.createStatement()) {
            String deleteAccountSql = "DELETE FROM card " +
                    "WHERE number = " + currentUserNumber + ";";
            statement.executeUpdate(deleteAccountSql);
            System.out.println("The account has been closed!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void logOutOfAccount() {
        currentUserNumber = null;
        loggedIn = false;
    }

    public String validateCardNumber(String number) {
        if (isNotValidCard(number)) {
            return "Probably you made a mistake in the card number. Please try again!";
        } else if (!checkIfCardAlreadyExists(number)) {
            return "Such a card does not exist.";
        }
        return null;
    }

    public String validateAmount(int amount) {
        int balance = findBalanceByNumber();
        if (balance < amount) {
            return "Not enough money!";
        }
        return null;
    }

    private void initiateTransfer(int amount, String number) {
        try (Statement statement = connection.createStatement()) {
            connection.setAutoCommit(false);
            String senderSql = "UPDATE card " +
                    "SET balance = balance - " + amount + " " +
                    "WHERE number = " + currentUserNumber + ";";
            String receiverSql = "UPDATE card " +
                    "SET balance = balance + " + amount + " " +
                    "WHERE number = " + number + ";";
            statement.executeUpdate(senderSql);
            statement.executeUpdate(receiverSql);
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int findBalanceByNumber() {
        int balance = 0;
        try {
            Statement statement = connection.createStatement();
            String findBalanceSql = "SELECT balance " +
                    "FROM card " +
                    "WHERE number = " + currentUserNumber + ";";
            balance = statement.executeQuery(findBalanceSql).getInt("balance");
            statement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return balance;
    }

    private void addBalanceToAccount(int income) {
        try (Statement statement = connection.createStatement()) {
            String addBalanceSql = "UPDATE card " +
                    "SET balance = balance + " + income + " " +
                    "WHERE number = " + currentUserNumber + ";";
            statement.executeUpdate(addBalanceSql);
            System.out.println("Income was added!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean checkIfCardAlreadyExists(String cardNumber) {
        int matches = 0;
        try (Statement statement = connection.createStatement()) {
            String findCardSql = "SELECT COUNT(number) as number " +
                    "FROM card " +
                    "WHERE number = " + cardNumber + "; ";
            matches = statement.executeQuery(findCardSql).getInt("number");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return matches != 0;
    }

    private void addAccountToDb(String number, String pin) {
        try {
            Statement statement = connection.createStatement();
            String cardInsertSql = "INSERT INTO card (number, pin) " +
                    "VALUES (" + number + "," + pin + ");";
            statement.executeUpdate(cardInsertSql);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Map<String, String> findNumberAndPinIfExists(String number) {
        Map<String, String> infoMap = null;
        try (Statement statement = connection.createStatement()){
            String findCardAndPinSql = "SELECT pin " +
                    "FROM card " +
                    "WHERE number = " + number + ";";
            String pin =  statement.executeQuery(findCardAndPinSql).getString("pin");
            if (pin != null) {
                infoMap = Map.of(
                        "number", number,
                        "pin", pin);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return infoMap;
    }

    private String generatePin() {
        StringBuilder pin = new StringBuilder();
        pin.append(random.nextInt(randomUpperLimit - 1) + 1);
        for (int i = 0; i < 3; i++) {
            pin.append(random.nextInt(randomUpperLimit));
        }
        return pin.toString();
    }

    private String generateCardNumber() {
        StringBuilder cardNumber = new StringBuilder();
        addMilAndBin(cardNumber);
        addAccountNumber(cardNumber);
        addCheckSum(cardNumber);
        return cardNumber.toString();
    }

    private void addMilAndBin(StringBuilder sb) {
        final String cardBeginWith = "4";
        final String binWithoutMil = "00000";
        sb.append(cardBeginWith).append(binWithoutMil);
    }

    private void addAccountNumber(StringBuilder sb) {
        for (int i = 0; i < 9; i++) {
            sb.append(random.nextInt(randomUpperLimit));
        }
    }

    private void addCheckSum(StringBuilder sb) {
        int[] cardNumberWithoutChecksum = convertStringToIntArray(sb.toString());
        multiplyOddDigitsByTwo(cardNumberWithoutChecksum);
        subtractNinesToNumbersOverNine(cardNumberWithoutChecksum);
        int total = addAllNumbers(cardNumberWithoutChecksum);
        int checksum = findCheckSum(total);
        sb.append(checksum);
    }

    private int findCheckSum(int total) {
        if (total % 10 == 0) return 0;
        return (10 - (total % 10)) % 10;
    }

    private boolean isNotValidCard(String cardNumber) {
        int[] cardNumberArray = convertStringToIntArray(cardNumber);
        int checksum = cardNumberArray[cardNumberArray.length - 1];
        cardNumberArray[cardNumberArray.length - 1] = 0;
        multiplyOddDigitsByTwo(cardNumberArray);
        subtractNinesToNumbersOverNine(cardNumberArray);
        int sum = addAllNumbers(cardNumberArray);
        return (sum + checksum) % 10 != 0;
    }

    private int addAllNumbers(int[] cardNumberWithoutChecksum) {
        int sum = 0;
        for (int i: cardNumberWithoutChecksum) {
            sum += i;
        }
        return sum;
    }

    private void subtractNinesToNumbersOverNine(int[] cardNumberWithoutChecksum) {
        for (int i = 0; i < cardNumberWithoutChecksum.length; i++) {
            if (cardNumberWithoutChecksum[i] > 9) {
                cardNumberWithoutChecksum[i] -= 9;
            }
        }
    }

    private void multiplyOddDigitsByTwo(int[] cardNumberWithoutChecksum) {
        for (int i = 0; i < cardNumberWithoutChecksum.length; i++) {
            if ((i+1) % 2 != 0) {
                cardNumberWithoutChecksum[i] *= 2;
            }
        }
    }

    private int[] convertStringToIntArray(String cardNumber) {
        int [] array = new int[cardNumber.length()];
        for (int i = 0; i < cardNumber.length(); i++) {
            array[i] = cardNumber.charAt(i) - 48;
        }
        return array;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

}