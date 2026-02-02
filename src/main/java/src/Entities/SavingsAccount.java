package src.Entities;

public class SavingsAccount extends BankAccount {
    private double depositAmount;
    private String depositDate;
    private double interestRate;
    private int term;

    public SavingsAccount(int id, String code, int custId, String name, String citizenId, String date,
                          double amount, String depDate, double rate, int term) {
        super(id, code, custId, name, citizenId, date);
        this.depositAmount = amount;
        this.depositDate = depDate;
        this.interestRate = rate;
        this.term = term;
    }
    public double getDepositAmount() {
        return depositAmount;
    }
    public String getDepositDate() {
        return depositDate;
    }
    public double getInterestRate() {
        return interestRate;
    }
    public int getTerm() {
        return term;
    }

    @Override
    public String toCSV() {
        return id + "," + accountCode + "," + ownerName + "," + citizenId + "," + creationDate + "," +
                depositAmount + "," + depositDate + "," + interestRate + "," + term;
    }
}