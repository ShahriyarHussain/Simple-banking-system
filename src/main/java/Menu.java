import java.util.Map;

public class Menu {

    private final Map<Integer, String> loggedOutOptions = Map.of(
            1, "1. Create an account",
            2,"2. Log into account");

    private final Map<Integer, String> loggedInOptions = Map.of(
            1, "1. Balance",
            2, "2. Add income",
            3, "3. Do transfer",
            4, "4. Close account",
            5, "5. Log out");

    private final String exitMenu = "0. Exit";



    public void showMenu() {
        System.out.printf("%n%s%n%s%n%s%n",
                loggedOutOptions.get(1), loggedOutOptions.get(2), exitMenu);
    }

    public void showLoggedInMenu() {
        System.out.printf("%n%s%n%s%n%s%n%s%n%s%n%s%n",
                loggedInOptions.get(1), loggedInOptions.get(2),
                loggedInOptions.get(3), loggedInOptions.get(4),
                loggedInOptions.get(5), exitMenu);
    }
}