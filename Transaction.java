package model;

import java.sql.Timestamp;

public class Transaction {

    private int id;
    private int fromAccount;
    private int toAccount;
    private double amount;
    private String type;
    private Timestamp time;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getFromAccount() { return fromAccount; }
    public void setFromAccount(int fromAccount) { this.fromAccount = fromAccount; }

    public int getToAccount() { return toAccount; }
    public void setToAccount(int toAccount) { this.toAccount = toAccount; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Timestamp getTime() { return time; }
    public void setTime(Timestamp time) { this.time = time; }
}
