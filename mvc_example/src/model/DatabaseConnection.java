package model;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DatabaseConnection - Quản lý kết nối JDBC đến Supabase
 * Dùng Singleton Pattern: chỉ tạo 1 kết nối duy nhất trong toàn bộ app
 */
public class DatabaseConnection {

    private static Connection connection = null;

    // Thông tin kết nối đọc từ file config.properties
    private static String DB_URL;
    private static String DB_USER;
    private static String DB_PASSWORD;

    // Nạp cấu hình khi class được load
    static {
        loadConfig();
    }

    /**
     * Đọc thông tin kết nối từ file config.properties
     */
    private static void loadConfig() {
        try (InputStream input = DatabaseConnection.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {

            if (input == null) {
                System.err.println("Không tìm thấy file config.properties!");
                return;
            }

            Properties props = new Properties();
            props.load(input);

            String host     = props.getProperty("db.host");
            String port     = props.getProperty("db.port", "5432");
            String name     = props.getProperty("db.name", "postgres");
            DB_USER         = props.getProperty("db.user", "postgres");
            DB_PASSWORD     = props.getProperty("db.password");

            // Ghép URL kết nối JDBC
            // Thêm ssl=true&sslmode=require vì Supabase bắt buộc dùng SSL
            DB_URL = "jdbc:postgresql://" + host + ":" + port + "/" + name
                    + "?ssl=true&sslmode=require";

            System.out.println("Đã nạp cấu hình kết nối: " + host);

        } catch (IOException e) {
            System.err.println("Lỗi đọc file config: " + e.getMessage());
        }
    }

    /**
     * Lấy kết nối database (Singleton)
     * Nếu chưa có kết nối hoặc kết nối bị đứt thì tạo mới
     *
     * @return Connection đến Supabase
     * @throws SQLException nếu kết nối thất bại
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Nạp PostgreSQL JDBC Driver
                Class.forName("org.postgresql.Driver");

                System.out.println("Đang kết nối đến Supabase...");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                System.out.println("Kết nối thành công!");

            } catch (ClassNotFoundException e) {
                throw new SQLException("Không tìm thấy PostgreSQL JDBC Driver. " +
                        "Hãy thêm file postgresql.jar vào project!", e);
            }
        }
        return connection;
    }

    /**
     * Đóng kết nối database (gọi khi tắt ứng dụng)
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Đã đóng kết nối database.");
            } catch (SQLException e) {
                System.err.println("Lỗi đóng kết nối: " + e.getMessage());
            }
        }
    }

    /**
     * Kiểm tra kết nối có đang hoạt động không
     *
     * @return true nếu kết nối OK, false nếu không
     */
    public static boolean isConnected() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(3);
        } catch (SQLException e) {
            return false;
        }
    }
}
