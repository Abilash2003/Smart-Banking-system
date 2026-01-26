package dao;

import model.Account;
import util.DBConnection;
import dao.TransactionDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class AccountDAO {

    TransactionDAO transactionDAO = new TransactionDAO();

    public double getBalance(int accountId) {

        String sql = "SELECT balance FROM accounts WHERE account_id = ?";
        double balance = -1;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, accountId);
            var rs = ps.executeQuery();

            if (rs.next()) {
                balance = rs.getDouble("balance");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return balance;
    }

    public boolean deposit(int accountId, double amount) {

        if (amount <= 0) {
            System.out.println("❌ Invalid amount");
            return false;
        }

        String sql = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDouble(1, amount);
            ps.setInt(2, accountId);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                transactionDAO.saveTransaction(accountId, accountId, amount, "DEPOSIT");
                return true;
            }
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean withdraw(int accountId, double amount) {

        if (amount <= 0) {
            System.out.println("❌ Invalid withdrawal amount");
            return false;
        }

        double currentBalance = getBalance(accountId);

        if (currentBalance < amount) {
            System.out.println("❌ Insufficient balance");
            return false;
        }

        String sql = "UPDATE accounts SET balance = balance - ? WHERE account_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDouble(1, amount);
            ps.setInt(2, accountId);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                transactionDAO.saveTransaction(accountId, accountId, amount, "WITHDRAW");
                return true;
            }
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean transfer(int fromAccount, int toAccount, double amount) {

        if (amount <= 0) {
            System.out.println("❌ Invalid transfer amount");
            return false;
        }

        double senderBalance = getBalance(fromAccount);

        if (senderBalance < amount) {
            System.out.println("❌ Insufficient balance for transfer");
            return false;
        }

        String debitSQL = "UPDATE accounts SET balance = balance - ? WHERE account_id = ?";
        String creditSQL = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";

        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false); // Start transaction

            try (PreparedStatement debitPs = con.prepareStatement(debitSQL);
                 PreparedStatement creditPs = con.prepareStatement(creditSQL)) {

                // Debit sender
                debitPs.setDouble(1, amount);
                debitPs.setInt(2, fromAccount);
                debitPs.executeUpdate();

                // Credit receiver
                creditPs.setDouble(1, amount);
                creditPs.setInt(2, toAccount);
                creditPs.executeUpdate();

                con.commit(); // Success

                transactionDAO.saveTransaction(fromAccount, toAccount, amount, "TRANSFER");
                return true;

            } catch (Exception e) {
                con.rollback(); // Undo if error
                e.printStackTrace();
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void createAccount(Account account) {
        String sql = "INSERT INTO accounts(user_id,balance) VALUES(?,?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, account.getUserId());
            ps.setDouble(2, account.getBalance());
            ps.executeUpdate();

            System.out.println("✅ Account Created Successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getAccountIdByUser(int userId) {

        String sql = "SELECT account_id FROM accounts WHERE user_id = ?";
        int accountId = -1;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            var rs = ps.executeQuery();

            if (rs.next()) {
                accountId = rs.getInt("account_id");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return accountId;
    }
}
