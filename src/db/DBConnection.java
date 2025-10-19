// DBConnection.java
package db;

import java.sql.*;

public class DBConnection {

    private static final String URL = "jdbc:sqlite:bakery.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initialize() {
        try {
            Class.forName("org.sqlite.JDBC");

            try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

                String ordersTable = "CREATE TABLE IF NOT EXISTS orders (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "customer_name TEXT," +
                        "item TEXT," +
                        "quantity INTEGER," +
                        "price REAL," +
                        "date TEXT" +
                        ");";

                String stockTable = "CREATE TABLE IF NOT EXISTS stock (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "item_name TEXT UNIQUE," +
                        "quantity INTEGER," +
                        "price REAL" +
                        ");";

                String usersTable = "CREATE TABLE IF NOT EXISTS users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT," +
                        "username TEXT UNIQUE," +
                        "password TEXT," +
                        "role TEXT" +
                        ");";

                stmt.execute(ordersTable);
                stmt.execute(stockTable);
                stmt.execute(usersTable);

                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM users;");
                if (rs.next() && rs.getInt("count") == 0) {
                    String insertAdmin = "INSERT INTO users (name, username, password, role) VALUES ('Admin User', 'admin', 'admin123', 'admin');";
                    String insertCustomer = "INSERT INTO users (name, username, password, role) VALUES ('Customer User', 'customer', 'cust123', 'customer');";
                    stmt.execute(insertAdmin);
                    stmt.execute(insertCustomer);
                }

                // Insert some default stock if empty
                rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM stock;");
                if (rs.next() && rs.getInt("count") == 0) {
                    String insertStock1 = "INSERT INTO stock (item_name, quantity, price) VALUES ('Chocolate Cake', 10, 15.0);";
                    String insertStock2 = "INSERT INTO stock (item_name, quantity, price) VALUES ('Cupcake', 20, 3.0);";
                    String insertStock3 = "INSERT INTO stock (item_name, quantity, price) VALUES ('Croissant', 15, 4.5);";
                    stmt.execute(insertStock1);
                    stmt.execute(insertStock2);
                    stmt.execute(insertStock3);
                }

                System.out.println("✅ Database initialized successfully!");

            }

        } catch (ClassNotFoundException e) {
            System.out.println("❌ SQLite JDBC driver not found: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("❌ DB Initialization Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}