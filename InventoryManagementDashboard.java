import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class InventoryManagementDashboard extends JFrame {

    JTextField idField, nameField, qtyField, priceField, searchField;
    JTable table;
    DefaultTableModel model;

    public InventoryManagementDashboard() {

        setTitle("Inventory Management Dashboard");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);


        JLabel title = new JLabel("Inventory Management Dashboard",
                SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));


        JPanel formPanel = new JPanel(new GridLayout(2, 5, 10, 10));

        idField = new JTextField();
        nameField = new JTextField();
        qtyField = new JTextField();
        priceField = new JTextField();

        formPanel.add(new JLabel("Product ID"));
        formPanel.add(new JLabel("Product Name"));
        formPanel.add(new JLabel("Quantity"));
        formPanel.add(new JLabel("Price"));
        formPanel.add(new JLabel(""));

        formPanel.add(idField);
        formPanel.add(nameField);
        formPanel.add(qtyField);
        formPanel.add(priceField);

        JButton addButton = new JButton("Add");
        formPanel.add(addButton);


        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
        JButton clearButton = new JButton("Clear");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);


        searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");

        JPanel searchPanel = new JPanel();
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);


        String[] columns = {"Product ID", "Product Name", "Quantity", "Price"};

        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);

        JScrollPane scrollPane = new JScrollPane(table);


        addButton.addActionListener(e -> {

            if (idField.getText().isEmpty() ||
                    nameField.getText().isEmpty() ||
                    qtyField.getText().isEmpty() ||
                    priceField.getText().isEmpty()) {

                JOptionPane.showMessageDialog(this,
                        "Please enter all product details.");
                return;
            }

            model.addRow(new Object[]{
                    idField.getText(),
                    nameField.getText(),
                    qtyField.getText(),
                    priceField.getText()
            });

            clearFields();
        });


        updateButton.addActionListener(e -> {

            int row = table.getSelectedRow();

            if (row == -1) {
                JOptionPane.showMessageDialog(this,
                        "Select a product to update.");
                return;
            }

            model.setValueAt(idField.getText(), row, 0);
            model.setValueAt(nameField.getText(), row, 1);
            model.setValueAt(qtyField.getText(), row, 2);
            model.setValueAt(priceField.getText(), row, 3);

            clearFields();
        });


        deleteButton.addActionListener(e -> {

            int row = table.getSelectedRow();

            if (row == -1) {
                JOptionPane.showMessageDialog(this,
                        "Select a product to delete.");
                return;
            }

            model.removeRow(row);
            clearFields();
        });


        clearButton.addActionListener(e -> clearFields());

        // Select table row
        table.getSelectionModel().addListSelectionListener(e -> {

            int row = table.getSelectedRow();

            if (row != -1) {
                idField.setText(model.getValueAt(row, 0).toString());
                nameField.setText(model.getValueAt(row, 1).toString());
                qtyField.setText(model.getValueAt(row, 2).toString());
                priceField.setText(model.getValueAt(row, 3).toString());
            }
        });

        // Search
        searchButton.addActionListener(e -> {

            String search = searchField.getText().toLowerCase();

            for (int i = 0; i < model.getRowCount(); i++) {

                String name = model.getValueAt(i, 1)
                        .toString().toLowerCase();

                if (name.contains(search)) {
                    table.setRowSelectionInterval(i, i);
                    return;
                }
            }

            JOptionPane.showMessageDialog(this,
                    "Product not found.");
        });
        setLayout(new BorderLayout(10, 10));

        add(title, BorderLayout.NORTH);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);
        add(searchPanel, BorderLayout.PAGE_END);

        setVisible(true);
    }

    void clearFields() {
        idField.setText("");
        nameField.setText("");
        qtyField.setText("");
        priceField.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new InventoryManagementDashboard());
    }
}