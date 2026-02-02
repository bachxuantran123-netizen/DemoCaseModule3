package src.Entities;

public class PaymentAccount extends BankAccount {
    private String cardNumber;
    private double balance;

    public PaymentAccount(int id, String code, int custId, String name, String citizenId, String date,
                          String cardNum, double balance) {
        super(id, code, custId, name, citizenId, date);
        this.cardNumber = cardNum;
        this.balance = balance;
    }

    public String getCardNumber() {
        return cardNumber;
    }
    public double getBalance() {
        return balance;
    }

    @Override
    public String toCSV() {
        return id + "," + accountCode + "," + ownerName + "," + citizenId + "," + creationDate + "," +
                cardNumber + "," + balance;
    }
}