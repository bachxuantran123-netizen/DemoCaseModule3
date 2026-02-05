package src.DAO;

import src.Database.DatabaseConnection;
import src.Entities.BankAccount;
import src.Entities.PaymentAccount;
import src.Entities.SavingsAccount;
import src.Entities.ActivityLog;
import src.Exceptions.NotFoundBankAccountException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {
    private BankAccount mapRowToAccount(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String code = rs.getString("account_code");
        String date = rs.getString("creation_date");
        String type = rs.getString("type");
        int custId = rs.getInt("customer_id");
        String name = rs.getString("full_name");
        String citizenId = rs.getString("citizen_id");

        if ("SavingsAccount".equalsIgnoreCase(type) || "SAVINGS".equalsIgnoreCase(type)) {
            return new SavingsAccount(id, code, custId, name, citizenId, date,
                    rs.getDouble("deposit_amount"),
                    rs.getString("deposit_date"),
                    rs.getDouble("interest_rate"),
                    rs.getInt("term"));
        } else {
            return new PaymentAccount(id, code, custId, name, citizenId, date,
                    rs.getString("card_number"),
                    rs.getDouble("balance"));
        }
    }

    public List<BankAccount> getAllAccounts() {
        List<BankAccount> list = new ArrayList<>();
        String sql = "SELECT a.*, c.full_name, c.citizen_id " +
                "FROM Accounts a " +
                "JOIN Customers c ON a.customer_id = c.customer_id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToAccount(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<BankAccount> searchAccounts(String keyword) {
        List<BankAccount> list = new ArrayList<>();
        String sql = "SELECT a.*, c.full_name, c.citizen_id FROM Accounts a " +
                "JOIN Customers c ON a.customer_id = c.customer_id " +
                "WHERE a.account_code LIKE ? OR c.full_name LIKE ? OR c.citizen_id LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String query = "%" + keyword + "%";
            pstmt.setString(1, query);
            pstmt.setString(2, query);
            pstmt.setString(3, query);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRowToAccount(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countTotalAccounts() {
        String sql = "SELECT COUNT(*) FROM Accounts";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public List<BankAccount> getAccountsByPage(int pageIndex, int pageSize) {
        List<BankAccount> list = new ArrayList<>();
        int offset = (pageIndex - 1) * pageSize;
        if (offset < 0) offset = 0; // An toàn

        String sql = "SELECT a.*, c.full_name, c.citizen_id FROM Accounts a " +
                "JOIN Customers c ON a.customer_id = c.customer_id " +
                "ORDER BY a.creation_date DESC " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, offset);
            ps.setInt(2, pageSize);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRowToAccount(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean addAccount(BankAccount acc, String currentAdmin) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            int customerId = -1;
            String checkCust = "SELECT customer_id FROM Customers WHERE citizen_id = ?";
            try (PreparedStatement psCheck = conn.prepareStatement(checkCust)) {
                psCheck.setString(1, acc.getCitizenId());
                ResultSet rsCheck = psCheck.executeQuery();
                if (rsCheck.next()) {
                    customerId = rsCheck.getInt("customer_id");
                } else {
                    String insertCust = "INSERT INTO Customers (citizen_id, full_name) VALUES (?, ?)";
                    try (PreparedStatement psInsCust = conn.prepareStatement(insertCust, Statement.RETURN_GENERATED_KEYS)) {
                        psInsCust.setString(1, acc.getCitizenId());
                        psInsCust.setString(2, acc.getOwnerName());
                        psInsCust.executeUpdate();
                        ResultSet rsKey = psInsCust.getGeneratedKeys();
                        if (rsKey.next()) customerId = rsKey.getInt(1);
                    }
                }
            }

            String sqlAcc = "INSERT INTO Accounts (account_code, customer_id, creation_date, type, " +
                    "deposit_amount, deposit_date, interest_rate, term, card_number, balance) " +
                    "VALUES (?, ?, GETDATE(), ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement psAcc = conn.prepareStatement(sqlAcc)) {
                psAcc.setString(1, acc.getAccountCode());
                psAcc.setInt(2, customerId);

                if (acc instanceof SavingsAccount) {
                    SavingsAccount sa = (SavingsAccount) acc;
                    psAcc.setString(3, "SavingsAccount");
                    psAcc.setDouble(4, sa.getDepositAmount());
                    psAcc.setDate(5, new java.sql.Date(System.currentTimeMillis()));
                    psAcc.setDouble(6, sa.getInterestRate());
                    psAcc.setInt(7, sa.getTerm());
                    psAcc.setNull(8, Types.VARCHAR);
                    psAcc.setNull(9, Types.DOUBLE);
                } else {
                    PaymentAccount pa = (PaymentAccount) acc;
                    psAcc.setString(3, "PaymentAccount");
                    psAcc.setNull(4, Types.DOUBLE);
                    psAcc.setNull(5, Types.DATE);
                    psAcc.setNull(6, Types.DOUBLE);
                    psAcc.setNull(7, Types.INTEGER);
                    psAcc.setString(8, pa.getCardNumber());
                    psAcc.setDouble(9, pa.getBalance());
                }
                psAcc.executeUpdate();
            }

            logActivity(conn, currentAdmin, "Thêm tài khoản mới " + acc.getAccountCode());

            conn.commit();
            return true;
        } catch (Exception e) {
            try {
                if (conn != null)
                    conn.rollback();
            } catch (SQLException ex) {}
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (Exception e) {}
        }
    }

    public boolean updateAccount(BankAccount acc, String currentAdmin) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            int customerId = -1;

            String checkSql = "SELECT customer_id FROM Customers WHERE citizen_id = ?";
            try (PreparedStatement pCheck = conn.prepareStatement(checkSql)) {
                pCheck.setString(1, acc.getCitizenId());
                ResultSet rs = pCheck.executeQuery();
                if (rs.next()) {
                    customerId = rs.getInt("customer_id");
                }
            }

            if (customerId != -1) {
                String updateName = "UPDATE Customers SET full_name = ? WHERE customer_id = ?";
                try (PreparedStatement pUpd = conn.prepareStatement(updateName)) {
                    pUpd.setString(1, acc.getOwnerName());
                    pUpd.setInt(2, customerId);
                    pUpd.executeUpdate();
                }
            } else {
                String insertCust = "INSERT INTO Customers (citizen_id, full_name) VALUES (?, ?)";
                try (PreparedStatement pIns = conn.prepareStatement(insertCust, Statement.RETURN_GENERATED_KEYS)) {
                    pIns.setString(1, acc.getCitizenId());
                    pIns.setString(2, acc.getOwnerName());
                    pIns.executeUpdate();
                    ResultSet rsKey = pIns.getGeneratedKeys();
                    if (rsKey.next()) {
                        customerId = rsKey.getInt(1);
                    }
                }
            }

            String sqlAcc = "UPDATE Accounts SET customer_id=?, deposit_amount=?, interest_rate=?, term=?, card_number=?, balance=? WHERE account_code=?";

            try (PreparedStatement pstmt = conn.prepareStatement(sqlAcc)) {
                if (acc instanceof SavingsAccount) {
                    SavingsAccount sa = (SavingsAccount) acc;
                    pstmt.setDouble(1, sa.getDepositAmount());
                    pstmt.setDouble(2, sa.getInterestRate());
                    pstmt.setInt(3, sa.getTerm());
                    pstmt.setNull(4, Types.VARCHAR);
                    pstmt.setNull(5, Types.DOUBLE);
                } else {
                    PaymentAccount pa = (PaymentAccount) acc;
                    pstmt.setNull(1, Types.DOUBLE);
                    pstmt.setNull(2, Types.DOUBLE);
                    pstmt.setNull(3, Types.INTEGER);
                    pstmt.setString(4, pa.getCardNumber());
                    pstmt.setDouble(5, pa.getBalance());
                }
                pstmt.setString(6, acc.getAccountCode());
                pstmt.executeUpdate();
            }

            logActivity(conn, currentAdmin, "Cập nhật tài khoản " + acc.getAccountCode());
            conn.commit();
            return true;
        } catch (Exception e) {
            try {
                if (conn != null)
                    conn.rollback();
            } catch (SQLException ex) {}
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) 
                    conn.close();
            } catch (Exception e) {}
        }
    }

    public void deleteAccount(String code, String currentAdmin) throws NotFoundBankAccountException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String sqlDel = "DELETE FROM Accounts WHERE account_code = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlDel)) {
                pstmt.setString(1, code);
                if (pstmt.executeUpdate() == 0) throw new NotFoundBankAccountException("Không tìm thấy tài khoản!");
            }

            logActivity(conn, currentAdmin, "Đã xóa tài khoản " + code);
            conn.commit();
        } catch (SQLException e) {
            try {
                if (conn != null)
                    conn.rollback();
            } catch (SQLException ex) {}
            e.printStackTrace();
        } finally {
            try {
                if (conn != null)
                    conn.close();
            } catch (Exception e) {}
        }
    }

    private void logActivity(Connection conn, String user, String action) throws SQLException {
        String sql = "INSERT INTO ActivityLogs (username, action, log_time) VALUES (?, ?, GETDATE())";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user);
            ps.setString(2, action);
            ps.executeUpdate();
        }
    }

    public boolean isCodeExist(String code) {
        String sql = "SELECT COUNT(*) FROM Accounts WHERE account_code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public BankAccount getAccountByCode(String code) {
        String sql = "SELECT a.*, c.full_name, c.citizen_id FROM Accounts a " +
                "JOIN Customers c ON a.customer_id = c.customer_id WHERE a.account_code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapRowToAccount(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int countTotalLogs() {
        String sql = "SELECT COUNT(*) FROM ActivityLogs";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<ActivityLog> getLogsByPage(int pageIndex, int pageSize) {
        List<ActivityLog> logs = new ArrayList<>();
        int offset = (pageIndex - 1) * pageSize;
        if (offset < 0) offset = 0;

        String sql = "SELECT log_id, username, action, log_time FROM ActivityLogs " +
                "ORDER BY log_time DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, offset);
            ps.setInt(2, pageSize);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                logs.add(new ActivityLog(
                        rs.getInt("log_id"),
                        rs.getString("username"),
                        rs.getString("action"),
                        rs.getTimestamp("log_time")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }
}