package api;

import com.google.gson.Gson;
import dao.AccountDAO;
import dao.TransactionDAO;
import dao.UserDAO;
import model.User;
import model.Account;

import static spark.Spark.*;

import java.util.HashMap;
import java.util.Map;

public class BankApiServer {

    static UserDAO userDAO = new UserDAO();
    static AccountDAO accountDAO = new AccountDAO();
    static TransactionDAO transactionDAO = new TransactionDAO();
    static Gson gson = new Gson();

    // Simple in-memory session (demo purpose)
    static Map<String, Integer> sessions = new HashMap<>();

    public static void main(String[] args) {

        port(8080);

        // Allow CORS (for browser UI later)
        before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Headers", "*");
            res.type("application/json");
        });

        options("/*", (req, res) -> "OK");

        System.out.println("🚀 Bank API Server started on http://localhost:8080");

        // ---------------- LOGIN ----------------
        post("/login", (req, res) -> {
            Map body = gson.fromJson(req.body(), Map.class);
            String email = (String) body.get("email");
            String password = (String) body.get("password");

            User user = userDAO.login(email, password);

            if (user == null) {
                res.status(401);
                return gson.toJson(Map.of("message", "Invalid credentials"));
            }

            int accountId = accountDAO.getAccountIdByUser(user.getId());
            String token = "TOKEN_" + System.currentTimeMillis();

            sessions.put(token, accountId);

            return gson.toJson(Map.of(
                    "token", token,
                    "name", user.getName()
            ));
        });

        // ---------------- REGISTER ----------------
        post("/register", (req, res) -> {
            Map body = gson.fromJson(req.body(), Map.class);

            String name = (String) body.get("name");
            String email = (String) body.get("email");
            String password = (String) body.get("password");

            User user = new User(name, email, password);
            int userId = userDAO.createUser(user);

            if (userId == -1) {
                res.status(400);
                return gson.toJson(Map.of("message", "User already exists"));
            }

            // Create account automatically
            Account account = new Account(userId, 0);
            accountDAO.createAccount(account);

            return gson.toJson(Map.of("message", "Registration successful"));
        });

        // ---------------- BALANCE ----------------
        get("/balance", (req, res) -> {
            int accountId = authenticate(req.headers("token"));
            double balance = accountDAO.getBalance(accountId);
            return gson.toJson(Map.of("balance", balance));
        });

        // ---------------- DEPOSIT ----------------
        post("/deposit", (req, res) -> {
            int accountId = authenticate(req.headers("token"));
            Map body = gson.fromJson(req.body(), Map.class);
            double amount = ((Number) body.get("amount")).doubleValue();

            boolean success = accountDAO.deposit(accountId, amount);
            return gson.toJson(Map.of("success", success));
        });

        // ---------------- WITHDRAW ----------------
        post("/withdraw", (req, res) -> {
            int accountId = authenticate(req.headers("token"));
            Map body = gson.fromJson(req.body(), Map.class);
            double amount = ((Number) body.get("amount")).doubleValue();

            boolean success = accountDAO.withdraw(accountId, amount);
            return gson.toJson(Map.of("success", success));
        });

        // ---------------- TRANSFER ----------------
        post("/transfer", (req, res) -> {
            int fromAccount = authenticate(req.headers("token"));
            Map body = gson.fromJson(req.body(), Map.class);

            int toAccount = ((Number) body.get("toAccount")).intValue();
            double amount = ((Number) body.get("amount")).doubleValue();

            boolean success = accountDAO.transfer(fromAccount, toAccount, amount);
            return gson.toJson(Map.of("success", success));
        });

        // ---------------- TRANSACTIONS ----------------
        get("/transactions", (req, res) -> {
            int accountId = authenticate(req.headers("token"));
            return gson.toJson(transactionDAO.getTransactions(accountId));
        });
    }

    // -------- Token validation ----------
    private static int authenticate(String token) {
        if (token == null || !sessions.containsKey(token)) {
            halt(401, "Unauthorized");
        }
        return sessions.get(token);
    }
}
