// ViewOrdersPanel.java
package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import db.DBConnection;

public class ViewOrdersPanel extends JPanel {

    private JTable ordersTable;
    private DefaultTableModel ordersTableModel;

    public ViewOrdersPanel() {
        setLayout(new BorderLayout(10, 10));

        ordersTableModel = new DefaultTableModel(new Object[]{"Order ID", "Customer", "Item", "Quantity", "Price", "Date"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        ordersTable = new JTable(ordersTableModel);
        JScrollPane scrollPane = new JScrollPane(ordersTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void loadOrders() {
        ordersTableModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, customer_name, item, quantity, price, date FROM orders ORDER BY date DESC")) {
            while (rs.next()) {
                ordersTableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("customer_name"),
                        rs.getString("item"),
                        rs.getInt("quantity"),
                        rs.getDouble("price"),
                        rs.getString("date")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading orders: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}