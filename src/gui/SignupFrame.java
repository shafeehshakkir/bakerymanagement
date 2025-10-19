// SignupFrame.java
package gui;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import db.DBConnection;

public class SignupFrame extends JFrame {

    public SignupFrame() {
        setTitle("Signup - Sweet Corner");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(9, 1, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Create Account"));

        JTextField nameField = new JTextField();
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JPasswordField confirmPasswordField = new JPasswordField();
        JButton signupButton = new JButton("Signup");
        JButton backButton = new JButton("Back to Login");
        JLabel messageLabel = new JLabel("", SwingConstants.CENTER);

        formPanel.add(new JLabel("Full Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Password:"));
        formPanel.add(passwordField);
        formPanel.add(new JLabel("Confirm Password:"));
        formPanel.add(confirmPasswordField);
        formPanel.add(signupButton);

        add(formPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(backButton, BorderLayout.WEST);
        bottomPanel.add(messageLabel, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);

        signupButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            if (name.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                messageLabel.setForeground(Color.RED);
                messageLabel.setText("Please fill all fields.");
                return;
            }

            if (!password.equals(confirmPassword)) {
                messageLabel.setForeground(Color.RED);
                messageLabel.setText("Passwords do not match.");
                return;
            }

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) AS count FROM users WHERE username = ?")) {
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt("count") > 0) {
                    messageLabel.setForeground(Color.RED);
                    messageLabel.setText("Username already exists.");
                    return;
                }

                try (PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO users (name, username, password, role) VALUES (?, ?, ?, 'customer')")) {
                    insertStmt.setString(1, name);
                    insertStmt.setString(2, username);
                    insertStmt.setString(3, password);
                    insertStmt.executeUpdate();

                    messageLabel.setForeground(new Color(34, 139, 34));
                    messageLabel.setText("Signup successful! Redirecting to login...");

                    Timer timer = new Timer(1500, ev -> {
                        dispose();
                        new SweetCornerLogin().setVisible(true);
                    });
                    timer.setRepeats(false);
                    timer.start();
                }
            } catch (SQLException ex) {
                messageLabel.setForeground(Color.RED);
                messageLabel.setText("DB error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        backButton.addActionListener(e -> {
            dispose();
            new SweetCornerLogin().setVisible(true);
        });
    }
}