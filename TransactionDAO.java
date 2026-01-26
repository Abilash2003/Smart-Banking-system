package dao;

import model.Transaction;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    public void saveTransaction(int from, int to, double amount, String type) {

        String sql = "INSERT INTO transactions(from_account, to_account, amount, type) VALUES (?,?,?,?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, from);
            ps.setInt(2, to);
            ps.setDouble(3, amount);
            ps.setString(4, type);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Transaction> getTransactions(int accountId) {

        List<Transaction> list = new ArrayList<>();

        String sql = """
            SELECT * FROM transactions
            WHERE from_account = ? OR to_account = ?
            ORDER BY txn_time DESC
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, accountId);
            ps.setInt(2, accountId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Transaction t = new Transaction();
                t.setId(rs.getInt("txn_id"));
                t.setFromAccount(rs.getInt("from_account"));
                t.setToAccount(rs.getInt("to_account"));
                t.setAmount(rs.getDouble("amount"));
                t.setType(rs.getString("type"));
                t.setTime(rs.getTimestamp("txn_time"));
                list.add(t);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
