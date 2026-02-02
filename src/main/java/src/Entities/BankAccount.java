package src.Entities;

public abstract class BankAccount {
    protected int id;
    protected String accountCode;
    protected int customerId;
    protected String ownerName;
    protected String citizenId;
    protected String creationDate;

    public BankAccount(int id, String accountCode, int customerId, String ownerName, String citizenId, String creationDate) {
        this.id = id;
        this.accountCode = accountCode;
        this.customerId = customerId;
        this.ownerName = ownerName;
        this.citizenId = citizenId;
        this.creationDate = creationDate;
    }

    public int getId() {
        return id;
    }
    public String getAccountCode() {
        return accountCode;
    }
    public int getCustomerId() {
        return customerId;
    }
    public String getOwnerName() {
        return ownerName;
    }
    public String getCitizenId() {
        return citizenId;
    }
    public String getCreationDate() {
        return creationDate;
    }
    public String getAccountType() {
        return this.getClass().getSimpleName();
    }

    public abstract String toCSV();

    @Override
    public String toString() {
        return String.format("| %-10s | %-20s | %-12s |", accountCode, ownerName, citizenId);
    }
}