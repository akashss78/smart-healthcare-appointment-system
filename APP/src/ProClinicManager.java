/*
 * =================================================================================
 * Pro Clinic Manager - Modern UI Version (SYNCHRONIZED WITH FINAL DB SCHEMA)
 * MODIFIED WITH ABHA INTEGRATION FEATURES
 * =================================================================================
 */

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Vector;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.TimePicker;

public class ProClinicManager {

    public static void main(String[] args) {
        Path uploadPath = Paths.get("clinic_uploads");
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectory(uploadPath);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Could not create storage directory for uploads.", "Startup Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("FlatLaf initialization failed. Using default theme.");
        }
        SwingUtilities.invokeLater(() -> {
            try {
                new LoginChoiceFrame().setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error loading application: " + e.getMessage(), "Startup Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    // ====================== MODERN BUTTON ======================
    public static class ModernButton extends JButton {
        private final Color startColor = new Color(0x4CAF50);
        private final Color endColor = new Color(0x2E7D32);
        public ModernButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 16));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, startColor, 0, getHeight(), endColor));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            if (getModel().isRollover()) {
                g2.setColor(new Color(255, 255, 255, 50));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
            super.paintComponent(g2);
            g2.dispose();
        }
    }

    // ====================== THEME TOGGLE ======================
    public static class ThemeManager {
        private static boolean isDarkMode = true;
        public static void toggleTheme() {
            try {
                if (isDarkMode) UIManager.setLookAndFeel(new FlatLightLaf());
                else UIManager.setLookAndFeel(new FlatDarculaLaf());
                isDarkMode = !isDarkMode;
                for (Window window : Window.getWindows()) SwingUtilities.updateComponentTreeUI(window);
            } catch (Exception ex) { ex.printStackTrace(); }
        }
        public static boolean isDarkMode() { return isDarkMode; }
    }

    // ====================== DATABASE ======================
    private static class DatabaseManager {
        private static final String DB_URL = "jdbc:mysql://localhost:3306/clinic_management";
        private static final String DB_USER = "root";
        private static final String DB_PASSWORD = "@Ak070707";
        public static Connection getConnection() throws SQLException {
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        }
    }

    // ====================== MODELS ======================
    private static class User {
        int id; String username; String role;
        public User(int id, String username, String role) { this.id = id; this.username = username; this.role = role; }
    }
    private static class Doctor {
        int id; int userId; String name; String specialty;
        @Override public String toString() { return name + " - " + specialty; }
    }
    private static class Patient { int id; int userId; String name; }

    // ====================== SESSION ======================
    private static class SessionManager {
        private static User currentUser;
        public static void login(User user) { currentUser = user; }
        public static void logout() { currentUser = null; }
        public static User getCurrentUser() { return currentUser; }
        public static boolean hasRole(String... roles) {
            if (currentUser == null) return false;
            for (String r : roles) if (currentUser.role.equalsIgnoreCase(r)) return true;
            return false;
        }
    }

    // ====================== LOGIN SCREENS ======================
    public static class LoginChoiceFrame extends JFrame {
        public LoginChoiceFrame() {
            setTitle("SMART HEALTHCARE APPOINTMENT SYSTEM");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); setSize(700, 450);
            setLocationRelativeTo(null); setLayout(new BorderLayout(10, 10));
            JLabel titleLabel = new JLabel("Welcome! Select Your Role to Login", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
            titleLabel.setBorder(new EmptyBorder(40, 0, 20, 0)); add(titleLabel, BorderLayout.NORTH);
            JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 30, 20));
            buttonPanel.setBorder(new EmptyBorder(20, 50, 80, 50));
            buttonPanel.add(createRoleButton("Patient", e -> { new RoleLoginFrame("Patient").setVisible(true); dispose(); }));
            buttonPanel.add(createRoleButton("Doctor", e -> { new RoleLoginFrame("Doctor").setVisible(true); dispose(); }));
            buttonPanel.add(createRoleButton("Admin", e -> { new AdminLoginFrame().setVisible(true); dispose(); }));
            add(buttonPanel, BorderLayout.CENTER);
            ModernButton themeToggle = new ModernButton(ThemeManager.isDarkMode() ? "Light Mode" : "Dark Mode");
            themeToggle.setFont(new Font("Segoe UI", Font.BOLD, 14));
            themeToggle.addActionListener(e -> { ThemeManager.toggleTheme(); themeToggle.setText(ThemeManager.isDarkMode() ? "Light Mode" : "Dark Mode"); });
            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottomPanel.setBorder(new EmptyBorder(0, 0, 10, 10)); bottomPanel.add(themeToggle);
            add(bottomPanel, BorderLayout.SOUTH);
        }
        private ModernButton createRoleButton(String text, ActionListener action) {
            ModernButton button = new ModernButton(text); button.addActionListener(action); button.setFont(new Font("Segoe UI", Font.BOLD, 20)); return button;
        }
    }
    public static class AdminLoginFrame extends JFrame {
        public AdminLoginFrame() {
            setTitle("Admin Login"); setSize(400, 250); setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); setLayout(new BorderLayout(10, 10));
            add(new JLabel("Admin Login", SwingConstants.CENTER), BorderLayout.NORTH);
            final JPasswordField passwordField = new JPasswordField();
            passwordField.setHorizontalAlignment(SwingConstants.CENTER);
            final JTextField usernameField = new JTextField("Admin");
            usernameField.setHorizontalAlignment(SwingConstants.CENTER);

            JPanel centerPanel = new JPanel(new GridLayout(2,1, 5, 5));
            centerPanel.add(new JLabel("Username:", SwingConstants.CENTER));
            centerPanel.add(usernameField);
            centerPanel.add(new JLabel("Password:", SwingConstants.CENTER));
            centerPanel.add(passwordField);
            add(centerPanel, BorderLayout.CENTER);

            ModernButton loginButton = new ModernButton("Login");
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); buttonPanel.add(loginButton);
            add(buttonPanel, BorderLayout.SOUTH);
            loginButton.addActionListener(e -> {
                try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ? AND role = 'admin'")) {
                    pstmt.setString(1, usernameField.getText());
                    pstmt.setString(2, new String(passwordField.getPassword()));
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        SessionManager.login(new User(rs.getInt("id"), rs.getString("username"), "admin"));
                        new MainApplicationFrame().setVisible(true);
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(this, "Incorrect Credentials.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Login Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }
    public static class RoleLoginFrame extends JFrame {
        public RoleLoginFrame(final String role) {
            setTitle(role + " Portal"); setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(450, "Patient".equals(role) ? 550 : 350); setLocationRelativeTo(null);
            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.addTab("Login", createLoginPanel(role));
            if ("Patient".equals(role)) { tabbedPane.addTab("Register", createPatientRegisterPanel()); }
            add(tabbedPane);
        }
        private JPanel createLoginPanel(final String role) {
            JPanel panel = new JPanel(new GridBagLayout()); GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5); gbc.fill = GridBagConstraints.HORIZONTAL;
            final JComboBox<Doctor> doctorCombo = new JComboBox<>();
            final JTextField usernameField = new JTextField(20);
            final JPasswordField passwordField = new JPasswordField(20);
            if ("Doctor".equals(role)) {
                gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Doctor:"), gbc);
                gbc.gridx = 1; panel.add(doctorCombo, gbc); loadDoctorsIntoComboBox(doctorCombo);
            } else {
                gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Username:"), gbc);
                gbc.gridx = 1; panel.add(usernameField, gbc);
            }
            gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Password:"), gbc);
            gbc.gridx = 1; panel.add(passwordField, gbc);
            ModernButton loginButton = new ModernButton("Login");
            gbc.gridx = 1; gbc.gridy = 2; panel.add(loginButton, gbc);
            loginButton.addActionListener(e -> {
                String username = usernameField.getText();
                if ("Doctor".equals(role) && doctorCombo.getSelectedItem() != null) {
                    Doctor selectedDoctor = (Doctor) doctorCombo.getSelectedItem();
                    username = "dr_" + selectedDoctor.name.toLowerCase().replace(" ", "");
                }
                performLogin(username, new String(passwordField.getPassword()));
            });
            return panel;
        }
        private JPanel createPatientRegisterPanel() {
            JPanel panel = new JPanel(new GridBagLayout()); GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5); gbc.fill = GridBagConstraints.HORIZONTAL;
            final JTextField regUser = new JTextField(), regName = new JTextField(), regPhone = new JTextField(), regAbhaId = new JTextField();
            final JPasswordField regPass = new JPasswordField(); final DatePicker regDob = new DatePicker();
            gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Username:"), gbc); gbc.gridx = 1; panel.add(regUser, gbc);
            gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Password:"), gbc); gbc.gridx = 1; panel.add(regPass, gbc);
            gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Full Name:"), gbc); gbc.gridx = 1; panel.add(regName, gbc);
            gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Date of Birth:"), gbc); gbc.gridx = 1; panel.add(regDob, gbc);
            gbc.gridx = 0; gbc.gridy = 4; panel.add(new JLabel("Phone:"), gbc); gbc.gridx = 1; panel.add(regPhone, gbc);
            gbc.gridx = 0; gbc.gridy = 5; panel.add(new JLabel("ABHA ID (Optional):"), gbc); gbc.gridx = 1; panel.add(regAbhaId, gbc);
            ModernButton registerButton = new ModernButton("Register");
            gbc.gridx = 1; gbc.gridy = 6; panel.add(registerButton, gbc);
            registerButton.addActionListener(e -> {
                String abhaId = regAbhaId.getText();
                if (abhaId != null && !abhaId.trim().isEmpty()) {
                    int choice = JOptionPane.showConfirmDialog(this,
                            "Do you want to link this ABHA ID to your account?\nThis is a one-time process.",
                            "Confirm ABHA Linking", JOptionPane.YES_NO_OPTION);
                    if (choice == JOptionPane.YES_OPTION) {
                        performRegistration(regUser.getText(), new String(regPass.getPassword()), regName.getText(), regDob.getDate(), regPhone.getText(), abhaId);
                    }
                } else {
                     performRegistration(regUser.getText(), new String(regPass.getPassword()), regName.getText(), regDob.getDate(), regPhone.getText(), null);
                }
            });
            return panel;
        }
        private void performLogin(String username, String password) {
            String sql = "SELECT id, username, role FROM users WHERE username = ? AND password = ?";
            try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username); pstmt.setString(2, password); ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    SessionManager.login(new User(rs.getInt("id"), rs.getString("username"), rs.getString("role")));
                    new MainApplicationFrame().setVisible(true); dispose();
                } else { JOptionPane.showMessageDialog(this, "Invalid Credentials.", "Login Failed", JOptionPane.ERROR_MESSAGE); }
            } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
        }
        private void performRegistration(String username, String password, String name, LocalDate dob, String phone, String abhaId) {
            if (username.isEmpty() || password.isEmpty() || name.isEmpty() || dob == null) {
                JOptionPane.showMessageDialog(this, "Username, Password, Name, and DoB fields must be filled out.", "Validation Error", JOptionPane.ERROR_MESSAGE); return;
            }
            try (Connection conn = DatabaseManager.getConnection()) {
                conn.setAutoCommit(false); int userId;
                String userSql = "INSERT INTO users (username, password, role) VALUES (?, ?, 'patient')";
                try (PreparedStatement userPstmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                    userPstmt.setString(1, username); userPstmt.setString(2, password); userPstmt.executeUpdate();
                    ResultSet rs = userPstmt.getGeneratedKeys(); rs.next(); userId = rs.getInt(1);
                }
                // NOTE: Assumes an 'abha_id' VARCHAR column exists in the 'patients' table.
                // ALTER TABLE patients ADD COLUMN abha_id VARCHAR(255) NULL;
                String patientSql = "INSERT INTO patients (user_id, name, dob, phone, abha_id) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement patientPstmt = conn.prepareStatement(patientSql)) {
                    patientPstmt.setInt(1, userId); patientPstmt.setString(2, name);
                    patientPstmt.setDate(3, Date.valueOf(dob)); patientPstmt.setString(4, phone);
                    if (abhaId != null && !abhaId.trim().isEmpty()) {
                       patientPstmt.setString(5, abhaId);
                    } else {
                       patientPstmt.setNull(5, Types.VARCHAR);
                    }
                    patientPstmt.executeUpdate();
                }
                conn.commit();
                JOptionPane.showMessageDialog(this, "Registration successful! Please login.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Registration failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        private void loadDoctorsIntoComboBox(JComboBox<Doctor> doctorCombo) {
            try (Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM doctors ORDER BY name")) {
                while (rs.next()) {
                    Doctor d = new Doctor(); d.id = rs.getInt("id"); d.userId = rs.getInt("user_id"); d.name = rs.getString("name"); d.specialty = rs.getString("specialty");
                    doctorCombo.addItem(d);
                }
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // ====================== MAIN DASHBOARD ======================
    public static class MainApplicationFrame extends JFrame {
        public MainApplicationFrame() {
            User user = SessionManager.getCurrentUser();
            setTitle("Pro Clinic Manager - " + (user != null ? user.role.toUpperCase() : ""));
            setSize(1200, 700); setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null); setLayout(new BorderLayout());
            JLabel welcomeLabel = new JLabel("Welcome, " + (user != null ? user.username : ""), SwingConstants.CENTER);
            welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24)); welcomeLabel.setBorder(new EmptyBorder(10,0,10,0));
            JPanel mainPanel = SessionManager.hasRole("patient") ? new PatientDashboardPanel() :
                                 SessionManager.hasRole("doctor") ? new DoctorDashboardPanel() : new AdminDashboardPanel();
            add(welcomeLabel, BorderLayout.NORTH); add(mainPanel, BorderLayout.CENTER);
            ModernButton logoutButton = new ModernButton("Logout");
            logoutButton.addActionListener(e -> { SessionManager.logout(); new LoginChoiceFrame().setVisible(true); dispose(); });
            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottomPanel.add(logoutButton); add(bottomPanel, BorderLayout.SOUTH);
        }
    }

    // ====================== DASHBOARD PANELS ======================
    public static class PatientDashboardPanel extends JPanel {
        private final AppointmentsViewPanel appointmentsView;
        public PatientDashboardPanel() {
            setLayout(new BorderLayout());
            JTabbedPane tabbedPane = new JTabbedPane();
            JPanel appointmentsOuterPanel = new JPanel(new BorderLayout());
            ModernButton bookAppointmentButton = new ModernButton("Book New Appointment");
            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            topPanel.setBorder(new EmptyBorder(10,0,10,0));
            topPanel.add(bookAppointmentButton);
            appointmentsOuterPanel.add(topPanel, BorderLayout.NORTH);
            appointmentsView = new AppointmentsViewPanel();
            appointmentsOuterPanel.add(appointmentsView, BorderLayout.CENTER);
            bookAppointmentButton.addActionListener(e -> new AppointmentBookingDialog(this).setVisible(true));
            tabbedPane.addTab("My Appointments", appointmentsOuterPanel);
            tabbedPane.addTab("My Medical Records", new PatientRecordsPanel());
            add(tabbedPane, BorderLayout.CENTER);
        }
        public void refreshAppointments() { appointmentsView.refreshData(); }
    }
    public static class DoctorDashboardPanel extends JPanel {
        public DoctorDashboardPanel() {
            setLayout(new BorderLayout());
            AppointmentsViewPanel appointmentsPanel = new AppointmentsViewPanel();
            JTable appointmentsTable = appointmentsPanel.getTable();
            appointmentsTable.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int selectedRow = appointmentsTable.getSelectedRow();
                        if (selectedRow >= 0) {
                            int appointmentId = (int) appointmentsTable.getModel().getValueAt(selectedRow, 0);
                            String patientName = (String) appointmentsTable.getModel().getValueAt(selectedRow, 1);
                            int patientId = (int) appointmentsTable.getModel().getValueAt(selectedRow, 5); // Hidden patient_id column
                            new PatientHistoryDialog((JFrame) SwingUtilities.getWindowAncestor(DoctorDashboardPanel.this),
                                    patientId, patientName, appointmentId).setVisible(true);
                        }
                    }
                }
            });
            add(appointmentsPanel, BorderLayout.CENTER);
        }
    }
    public static class AdminDashboardPanel extends JPanel {
        public AdminDashboardPanel() { setLayout(new BorderLayout()); add(new AppointmentsViewPanel(), BorderLayout.CENTER); }
    }

    // ====================== PATIENT RECORDS VIEW ======================
    public static class PatientRecordsPanel extends JPanel {
        private final DefaultTableModel recordsTableModel;
        private final DefaultTableModel reportsTableModel;
        private int currentPatientId = -1;
        private JLabel abhaInfoLabel;


        public PatientRecordsPanel() {
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(10, 10, 10, 10));

            abhaInfoLabel = new JLabel("ABHA ID: Checking...", SwingConstants.LEFT);
            abhaInfoLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            abhaInfoLabel.setBorder(new EmptyBorder(0, 5, 10, 0));
            add(abhaInfoLabel, BorderLayout.NORTH);


            recordsTableModel = new DefaultTableModel(new String[]{"Date", "Doctor", "Notes"}, 0){
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            JTable recordsTable = new JTable(recordsTableModel);
            JScrollPane recordsScrollPane = new JScrollPane(recordsTable);
            recordsScrollPane.setBorder(new TitledBorder("Medical History"));
            reportsTableModel = new DefaultTableModel(new String[]{"Uploaded On", "Report Name", "File Path"}, 0){
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            JTable reportsTable = new JTable(reportsTableModel);
            reportsTable.removeColumn(reportsTable.getColumnModel().getColumn(2)); // Hide file path

            JButton viewReportButton = new JButton("View Selected Report");
            JPanel reportsPanel = new JPanel(new BorderLayout(5, 5));
            reportsPanel.setBorder(new TitledBorder("Scans & Reports (from ABHA)"));
            reportsPanel.add(new JScrollPane(reportsTable), BorderLayout.CENTER);
            reportsPanel.add(viewReportButton, BorderLayout.SOUTH);
            JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, recordsScrollPane, reportsPanel);
            splitPane.setResizeWeight(0.6);
            add(splitPane, BorderLayout.CENTER);
            viewReportButton.addActionListener(e -> {
                int selectedRow = reportsTable.getSelectedRow();
                if (selectedRow >= 0) {
                    // Correctly get file path from the hidden column in the model
                    String filePath = (String) reportsTableModel.getValueAt(selectedRow, 2);
                    try {
                        Desktop.getDesktop().open(new File(filePath));
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(this, "Could not open file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Please select a report to view.", "No Selection", JOptionPane.WARNING_MESSAGE);
                }
            });
            loadPatientData();
        }

        private void loadPatientData() {
            // Updated SQL to also fetch abha_id
            try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement("SELECT id, abha_id FROM patients WHERE user_id = ?")) {
                pstmt.setInt(1, SessionManager.getCurrentUser().id);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    this.currentPatientId = rs.getInt("id");
                    String abhaId = rs.getString("abha_id");
                    if(abhaId != null && !abhaId.isEmpty()){
                       abhaInfoLabel.setText("Linked ABHA ID: " + abhaId);
                    } else {
                       abhaInfoLabel.setText("ABHA ID: Not Linked");
                    }
                    loadRecords();
                    loadReports();
                }
            } catch (SQLException ex) { ex.printStackTrace(); }
        }

        private void loadRecords() {
            recordsTableModel.setRowCount(0);
            String sql = "SELECT a.appointment_datetime, d.name, a.notes FROM appointments a JOIN doctors d ON a.doctor_id = d.id WHERE a.patient_id = ? ORDER BY a.appointment_datetime DESC";
            try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, currentPatientId);
                ResultSet rs = pstmt.executeQuery();
                while(rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getTimestamp("appointment_datetime").toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                    row.add(rs.getString("name"));
                    row.add(rs.getString("notes"));
                    recordsTableModel.addRow(row);
                }
            } catch (SQLException ex) { ex.printStackTrace(); }
        }

        private void loadReports() {
            reportsTableModel.setRowCount(0);
            String sql = "SELECT mr.uploaded_at, mr.report_name, mr.file_path FROM medical_reports mr JOIN appointments a ON mr.appointment_id = a.id WHERE a.patient_id = ? ORDER BY mr.uploaded_at DESC";
            try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, currentPatientId);
                ResultSet rs = pstmt.executeQuery();
                while(rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getTimestamp("uploaded_at").toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                    row.add("[ABHA] " + rs.getString("report_name")); // Add ABHA prefix
                    row.add(rs.getString("file_path")); // Hidden column
                    reportsTableModel.addRow(row);
                }
            } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }

    // ====================== APPOINTMENTS VIEW ======================
    public static class AppointmentsViewPanel extends JPanel {
        private final DefaultTableModel tableModel;
        private final JTable table;

        public AppointmentsViewPanel() {
            setLayout(new BorderLayout());
            tableModel = new DefaultTableModel(new String[]{"ID", "Patient", "Doctor", "Date & Time", "Status", "PatientID", "Notes"}, 0){
                   @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            table = new JTable(tableModel);
            table.removeColumn(table.getColumnModel().getColumn(6));
            table.removeColumn(table.getColumnModel().getColumn(5));
            table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            table.setRowHeight(25);
            table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
            add(new JScrollPane(table), BorderLayout.CENTER);
            refreshData();
        }
        public JTable getTable() { return table; }

        public void refreshData() {
            tableModel.setRowCount(0);
            String sql = "SELECT a.id, p.name as patient_name, d.name as doctor_name, a.appointment_datetime, a.status, p.id as patient_id, a.notes FROM appointments a JOIN patients p ON a.patient_id = p.id JOIN doctors d ON a.doctor_id = d.id";
            User currentUser = SessionManager.getCurrentUser();
            if (SessionManager.hasRole("patient")) sql += " WHERE p.user_id = " + currentUser.id;
            else if (SessionManager.hasRole("doctor")) sql += " WHERE d.user_id = " + currentUser.id;
            sql += " ORDER BY a.appointment_datetime DESC";
            try (Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getInt("id"));
                    row.add(rs.getString("patient_name"));
                    row.add(rs.getString("doctor_name"));
                    row.add(rs.getTimestamp("appointment_datetime").toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                    row.add(rs.getString("status"));
                    row.add(rs.getInt("patient_id"));
                    row.add(rs.getString("notes"));
                    tableModel.addRow(row);
                }
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // ====================== BOOKING DIALOG ======================
    public static class AppointmentBookingDialog extends JDialog {
        private final PatientDashboardPanel parentPanel;
        public AppointmentBookingDialog(PatientDashboardPanel parent) {
            this.parentPanel = parent; setTitle("Book an Appointment"); setModal(true);
            setSize(500, 300); setLocationRelativeTo(parent); setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(10, 10, 10, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL; final JComboBox<Doctor> doctorCombo = new JComboBox<>();
            final DatePicker datePicker = new DatePicker(); final TimePicker timePicker = new TimePicker();
            gbc.gridx = 0; gbc.gridy = 0; add(new JLabel("Select Doctor:"), gbc);
            gbc.gridx = 1; gbc.weightx = 1.0; add(doctorCombo, gbc);
            gbc.gridx = 0; gbc.gridy = 1; add(new JLabel("Select Date:"), gbc);
            gbc.gridx = 1; add(datePicker, gbc);
            gbc.gridx = 0; gbc.gridy = 2; add(new JLabel("Select Time:"), gbc);
            gbc.gridx = 1; add(timePicker, gbc);
            ModernButton bookButton = new ModernButton("Book"); gbc.gridx = 1; gbc.gridy = 3; add(bookButton, gbc);
            loadDoctorsIntoComboBox(doctorCombo);
            bookButton.addActionListener(e -> {
                Doctor selectedDoctor = (Doctor) doctorCombo.getSelectedItem();
                LocalDate selectedDate = datePicker.getDate(); LocalTime selectedTime = timePicker.getTime();
                if (selectedDoctor == null || selectedDate == null || selectedTime == null) {
                    JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE); return;
                }
                LocalDateTime appointmentDateTime = LocalDateTime.of(selectedDate, selectedTime); int patientId = -1;
                try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement("SELECT id FROM patients WHERE user_id = ?")) {
                    pstmt.setInt(1, SessionManager.getCurrentUser().id); ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) patientId = rs.getInt(1);
                } catch (SQLException ex) { ex.printStackTrace(); }
                if (patientId == -1) { JOptionPane.showMessageDialog(this, "Could not find patient profile.", "Error", JOptionPane.ERROR_MESSAGE); return; }
                String sql = "INSERT INTO appointments (patient_id, doctor_id, appointment_datetime) VALUES (?, ?, ?)";
                try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, patientId); pstmt.setInt(2, selectedDoctor.id);
                    pstmt.setTimestamp(3, Timestamp.valueOf(appointmentDateTime)); pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Appointment booked successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    parentPanel.refreshAppointments(); dispose();
                } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Booking failed: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE); }
            });
        }
        private void loadDoctorsIntoComboBox(JComboBox<Doctor> doctorCombo) {
            try (Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM doctors ORDER BY name")) {
                while (rs.next()) {
                    Doctor d = new Doctor(); d.id = rs.getInt("id"); d.userId = rs.getInt("user_id"); d.name = rs.getString("name"); d.specialty = rs.getString("specialty");
                    doctorCombo.addItem(d);
                }
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // ====================== PATIENT HISTORY DIALOG (FOR DOCTOR) ======================
    public static class PatientHistoryDialog extends JDialog {
        private final int patientId;
        private final int appointmentId;
        private final JTextArea notesArea;
        private final DefaultTableModel reportsTableModel;

        public PatientHistoryDialog(JFrame owner, int patientId, String patientName, int appointmentId) {
            super(owner, "Medical File: " + patientName, true);
            this.patientId = patientId;
            this.appointmentId = appointmentId;
            setSize(800, 600); setLocationRelativeTo(owner);
            setLayout(new BorderLayout(10, 10));
            JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            splitPane.setResizeWeight(0.5);
            JPanel topPanel = new JPanel(new GridLayout(1, 2, 10, 10));
            notesArea = new JTextArea(10, 30);
            JPanel notesPanel = new JPanel(new BorderLayout());
            notesPanel.setBorder(new TitledBorder("Diagnosis & Prescription Notes"));
            notesPanel.add(new JScrollPane(notesArea), BorderLayout.CENTER);
            topPanel.add(notesPanel);
            topPanel.add(createReportsPanel());
            splitPane.setTopComponent(topPanel);
            splitPane.setBottomComponent(createHistoryPanel());
            add(splitPane, BorderLayout.CENTER);
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            ModernButton saveButton = new ModernButton("Save Changes");
            ModernButton closeButton = new ModernButton("Close");
            buttonPanel.add(saveButton);
            buttonPanel.add(closeButton);
            add(buttonPanel, BorderLayout.SOUTH);
            closeButton.addActionListener(e -> dispose());
            saveButton.addActionListener(e -> saveChanges());
            reportsTableModel = (DefaultTableModel)((JTable)((JScrollPane)((JPanel)topPanel.getComponent(1)).getComponent(0)).getViewport().getView()).getModel();
            loadInitialData();
            loadReports();
        }

        private JPanel createReportsPanel() {
            JPanel panel = new JPanel(new BorderLayout(5, 5));
            panel.setBorder(new TitledBorder("Uploaded Reports"));
            DefaultTableModel model = new DefaultTableModel(new String[]{"Date", "Name", "File Path"}, 0){
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            JTable reportsTable = new JTable(model);
            reportsTable.removeColumn(reportsTable.getColumnModel().getColumn(2)); // Hide file path
            panel.add(new JScrollPane(reportsTable), BorderLayout.CENTER);
            JButton uploadButton = new JButton("Upload New Report");
            uploadButton.addActionListener(e -> uploadReport());
            panel.add(uploadButton, BorderLayout.SOUTH);
            return panel;
        }

        private JPanel createHistoryPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(new TitledBorder("Patient Appointment History"));
            DefaultTableModel historyModel = new DefaultTableModel(new String[]{"Date", "Doctor", "Status"}, 0);
            JTable historyTable = new JTable(historyModel);
            panel.add(new JScrollPane(historyTable), BorderLayout.CENTER);
            String sql = "SELECT a.appointment_datetime, d.name, a.status FROM appointments a JOIN doctors d ON a.doctor_id = d.id WHERE a.patient_id = ? ORDER BY a.appointment_datetime DESC";
            try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, patientId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    historyModel.addRow(new Object[]{
                        rs.getTimestamp("appointment_datetime").toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE),
                        rs.getString("name"),
                        rs.getString("status")
                    });
                }
            } catch (SQLException ex) { ex.printStackTrace(); }
            return panel;
        }

        private void loadInitialData() {
            String sql = "SELECT notes FROM appointments WHERE id = ?";
            try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, appointmentId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    notesArea.setText(rs.getString("notes"));
                }
            } catch (SQLException ex) { ex.printStackTrace(); }
        }

        private void loadReports() {
            reportsTableModel.setRowCount(0);
            String sql = "SELECT uploaded_at, report_name, file_path FROM medical_reports WHERE appointment_id = ? ORDER BY uploaded_at DESC";
            try(Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, appointmentId);
                ResultSet rs = pstmt.executeQuery();
                while(rs.next()) {
                    reportsTableModel.addRow(new Object[]{
                        rs.getTimestamp("uploaded_at").toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE),
                        rs.getString("report_name"),
                        rs.getString("file_path") // hidden
                    });
                }
            } catch (SQLException ex) { ex.printStackTrace(); }
        }

        private void saveChanges() {
            String sql = "UPDATE appointments SET notes = ?, status = 'Completed' WHERE id = ?";
            try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, notesArea.getText());
                pstmt.setInt(2, appointmentId);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Record updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Failed to update record: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void uploadReport() {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                Path destFolder = Paths.get("clinic_uploads");
                String newFileName = patientId + "_" + appointmentId + "_" + System.currentTimeMillis() + "_" + selectedFile.getName();
                Path destPath = destFolder.resolve(newFileName);
                try {
                    Files.copy(selectedFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
                    String sql = "INSERT INTO medical_reports (appointment_id, report_name, file_path) VALUES (?, ?, ?)";
                    try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setInt(1, appointmentId);
                        pstmt.setString(2, selectedFile.getName());
                        pstmt.setString(3, destPath.toAbsolutePath().toString());
                        pstmt.executeUpdate();
                        JOptionPane.showMessageDialog(this, "Report uploaded successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadReports(); // Refresh the list
                    }
                } catch (IOException | SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Report upload failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}