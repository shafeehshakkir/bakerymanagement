// ManageStockPanel.java
package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import db.DBConnection;

public class ManageStockPanel extends JPanel {

    private JTable stockTable;
    private DefaultTableModel stockTableModel;
    private JButton deleteButton;

    public ManageStockPanel() {
        setLayout(new BorderLayout(10, 10));

        stockTableModel = new DefaultTableModel(new Object[]{"ID", "Item Name", "Quantity", "Price"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        stockTable = new JTable(stockTableModel);
        JScrollPane scrollPane = new JScrollPane(stockTable);
        add(scrollPane, BorderLayout.CENTER);

        deleteButton = new JButton("Delete Selected Item");
        add(deleteButton, BorderLayout.SOUTH);

        deleteButton.addActionListener(e -> deleteSelectedItem());
    }

    public void loadStock() {
        stockTableModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, item_name, quantity, price FROM stock")) {
            while (rs.next()) {
                stockTableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("item_name"),
                        rs.getInt("quantity"),
                        rs.getDouble("price")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading stock: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedItem() {
        int selectedRow = stockTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) stockTableModel.getValueAt(selectedRow, 0);
        String itemName = (String) stockTableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete item \"" + itemName + "\"?", "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM stock WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Item deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadStock();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error deleting item: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}