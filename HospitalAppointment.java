import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class HospitalAppointment extends JFrame {

    JTextField patientField, doctorField, dateField, timeField;
    JTable table;
    DefaultTableModel model;

    public HospitalAppointment() {

        setTitle("Hospital Appointment Management System");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel title = new JLabel(
                "Hospital Appointment Management System",
                SwingConstants.CENTER
        );
        title.setFont(new Font("Arial", Font.BOLD, 24));

        patientField = new JTextField();
        doctorField = new JTextField();
        dateField = new JTextField();
        timeField = new JTextField();

        JPanel formPanel = new JPanel(new GridLayout(2, 4, 10, 10));

        formPanel.add(new JLabel("Patient Name"));
        formPanel.add(new JLabel("Doctor Name"));
        formPanel.add(new JLabel("Date"));
        formPanel.add(new JLabel("Time"));

        formPanel.add(patientField);
        formPanel.add(doctorField);
        formPanel.add(dateField);
        formPanel.add(timeField);

        JButton bookButton = new JButton("Book");
        JButton updateButton = new JButton("Update");
        JButton cancelButton = new JButton("Cancel");
        JButton clearButton = new JButton("Clear");

        JPanel buttonPanel = new JPanel(new FlowLayout());

        buttonPanel.add(bookButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(clearButton);

        String[] columns = {
                "Patient Name",
                "Doctor Name",
                "Date",
                "Time",
                "Status"
        };

        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);

        JScrollPane scrollPane = new JScrollPane(table);

        bookButton.addActionListener(e -> {

            if (patientField.getText().isEmpty()
                    || doctorField.getText().isEmpty()
                    || dateField.getText().isEmpty()
                    || timeField.getText().isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please enter all details."
                );
                return;
            }

            model.addRow(new Object[]{
                    patientField.getText(),
                    doctorField.getText(),
                    dateField.getText(),
                    timeField.getText(),
                    "Confirmed"
            });

            JOptionPane.showMessageDialog(
                    this,
                    "Appointment booked successfully."
            );

            clearFields();
        });

        updateButton.addActionListener(e -> {

            int row = table.getSelectedRow();

            if (row == -1) {
                JOptionPane.showMessageDialog(
                        this,
                        "Select an appointment to update."
                );
                return;
            }

            model.setValueAt(patientField.getText(), row, 0);
            model.setValueAt(doctorField.getText(), row, 1);
            model.setValueAt(dateField.getText(), row, 2);
            model.setValueAt(timeField.getText(), row, 3);

            JOptionPane.showMessageDialog(
                    this,
                    "Appointment updated successfully."
            );

            clearFields();
        });

        cancelButton.addActionListener(e -> {

            int row = table.getSelectedRow();

            if (row == -1) {
                JOptionPane.showMessageDialog(
                        this,
                        "Select an appointment to cancel."
                );
                return;
            }

            model.setValueAt("Cancelled", row, 4);

            JOptionPane.showMessageDialog(
                    this,
                    "Appointment cancelled."
            );
        });

        clearButton.addActionListener(e -> clearFields());

        table.getSelectionModel().addListSelectionListener(e -> {

            int row = table.getSelectedRow();

            if (row != -1) {
                patientField.setText(
                        model.getValueAt(row, 0).toString()
                );

                doctorField.setText(
                        model.getValueAt(row, 1).toString()
                );

                dateField.setText(
                        model.getValueAt(row, 2).toString()
                );

                timeField.setText(
                        model.getValueAt(row, 3).toString()
                );
            }
        });

        setLayout(new BorderLayout(10, 10));

        add(title, BorderLayout.NORTH);

        JPanel topPanel = new JPanel(new BorderLayout());

        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);

        setVisible(true);
    }

    void clearFields() {
        patientField.setText("");
        doctorField.setText("");
        dateField.setText("");
        timeField.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new HospitalAppointment()
        );
    }
}