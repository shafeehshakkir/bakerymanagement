// ManageUsersPanel.java
package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import db.DBConnection;

public class ManageUsersPanel extends JPanel {

    private JTable usersTable;
    private DefaultTableModel usersTableModel;
    private JButton deleteButton;

    public ManageUsersPanel() {
        setLayout(new BorderLayout(10, 10));

        usersTableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Username", "Role"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        usersTable = new JTable(usersTableModel);
        JScrollPane scrollPane = new JScrollPane(usersTable);
        add(scrollPane, BorderLayout.CENTER);

        deleteButton = new JButton("Delete Selected User");
        add(deleteButton, BorderLayout.SOUTH);

        deleteButton.addActionListener(e -> deleteSelectedUser());
    }

    public void loadUsers() {
        usersTableModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, username, role FROM users")) {
            while (rs.next()) {
                usersTableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("username"),
                        rs.getString("role")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading users: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedUser() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) usersTableModel.getValueAt(selectedRow, 0);
        String username = (String) usersTableModel.getValueAt(selectedRow, 2);
        String role = (String) usersTableModel.getValueAt(selectedRow, 3);

        if ("admin".equalsIgnoreCase(role)) {
            JOptionPane.showMessageDialog(this, "Cannot delete admin user.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete user \"" + username + "\"?", "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "User deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadUsers();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error deleting user: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}