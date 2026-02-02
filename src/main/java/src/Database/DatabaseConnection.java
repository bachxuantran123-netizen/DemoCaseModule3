package src.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:sqlserver://localhost;"
            + "instanceName=SQLEXPRESS;"
            + "databaseName=BankDB;"
            + "encrypt=true;"
            + "trustServerCertificate=true;";

    private static final String USER = "sa";
    private static final String PASS = "123456";

    public static Connection getConnection() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Lỗi thiếu Driver!", e);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi kết nối SQL: " + e.getMessage(), e);
        }
    }
}