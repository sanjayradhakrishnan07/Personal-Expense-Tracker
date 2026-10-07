import model.Transaction;
import service.ExpenseService;
import util.FileManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Main {
    private static final Color NAVY = new Color(25, 45, 72);
    private static final Color BLUE = new Color(43, 105, 190);
    private static final Color BACKGROUND = new Color(245, 247, 250);
    private static final Color GREEN = new Color(28, 130, 91);
    private static final Color RED = new Color(190, 69, 69);

    private final FileManager fileManager = new FileManager();
    private final ExpenseService expenseService;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JLabel incomeValue = new JLabel();
    private final JLabel expenseValue = new JLabel();
    private final JLabel balanceValue = new JLabel();
    private final JTextField searchField = new JTextField(22);

    private Main() throws IOException {
        expenseService = new ExpenseService(fileManager.loadTransactions());
        tableModel = new DefaultTableModel(new Object[]{"ID", "TYPE", "AMOUNT", "CATEGORY", "DATE", "DESCRIPTION"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        buildWindow();
        refresh();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { new Main(); }
            catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Could not load saved transactions:\n" + ex.getMessage(), "File error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void buildWindow() {
        JFrame frame = new JFrame("Personal Expense Tracker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 720);
        frame.setMinimumSize(new Dimension(850, 600));
        frame.setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(BACKGROUND);
        root.setBorder(new EmptyBorder(24, 28, 24, 28));
        frame.setContentPane(root);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Personal Expense Tracker");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(NAVY);
        JLabel subtitle = new JLabel("Keep track of your income and spending");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(103, 116, 132));
        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 4));
        titles.setOpaque(false);
        titles.add(title);
        titles.add(subtitle);
        header.add(titles, BorderLayout.WEST);
        root.add(header, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setOpaque(false);
        root.add(content, BorderLayout.CENTER);

        JPanel summary = new JPanel(new GridLayout(1, 3, 14, 0));
        summary.setOpaque(false);
        summary.add(summaryCard("TOTAL INCOME", incomeValue, GREEN));
        summary.add(summaryCard("TOTAL EXPENSES", expenseValue, RED));
        summary.add(summaryCard("CURRENT BALANCE", balanceValue, BLUE));
        content.add(summary, BorderLayout.NORTH);

        JPanel transactionsPanel = new JPanel(new BorderLayout(0, 12));
        transactionsPanel.setBackground(Color.WHITE);
        transactionsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 231, 237)), new EmptyBorder(16, 16, 16, 16)));
        content.add(transactionsPanel, BorderLayout.CENTER);

        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);
        JLabel sectionTitle = new JLabel("Transactions");
        sectionTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        sectionTitle.setForeground(NAVY);
        toolbar.add(sectionTitle, BorderLayout.WEST);
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchPanel.setOpaque(false);
        searchField.setToolTipText("Search by ID, type, category, or description");
        JButton searchButton = button("Search", BLUE);
        searchButton.addActionListener(e -> searchTransactions());
        JButton allButton = button("Show all", new Color(100, 113, 130));
        allButton.addActionListener(e -> { searchField.setText(""); refresh(); });
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(allButton);
        toolbar.add(searchPanel, BorderLayout.EAST);
        transactionsPanel.add(toolbar, BorderLayout.NORTH);

        table.setRowHeight(32);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(239, 243, 248));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.getColumnModel().getColumn(0).setPreferredWidth(245);
        table.getColumnModel().getColumn(1).setPreferredWidth(85);
        transactionsPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 0));
        actions.setOpaque(false);
        JButton addIncome = button("+ Add income", GREEN);
        addIncome.addActionListener(e -> addTransaction("Income"));
        JButton addExpense = button("+ Add expense", RED);
        addExpense.addActionListener(e -> addTransaction("Expense"));
        JButton delete = button("Delete selected", new Color(100, 113, 130));
        delete.addActionListener(e -> deleteSelected());
        JButton categories = button("Category summary", new Color(100, 113, 130));
        categories.addActionListener(e -> showCategories());
        JButton monthly = button("Monthly summary", new Color(100, 113, 130));
        monthly.addActionListener(e -> showMonthly());
        actions.add(addIncome);
        actions.add(addExpense);
        actions.add(delete);
        actions.add(categories);
        actions.add(monthly);
        transactionsPanel.add(actions, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private JPanel summaryCard(String label, JLabel value, Color color) {
        JPanel card = new JPanel(new GridLayout(2, 1, 0, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 231, 237)), new EmptyBorder(16, 18, 16, 18)));
        JLabel heading = new JLabel(label);
        heading.setFont(new Font("SansSerif", Font.BOLD, 11));
        heading.setForeground(new Color(112, 123, 137));
        value.setFont(new Font("SansSerif", Font.BOLD, 23));
        value.setForeground(color);
        card.add(heading);
        card.add(value);
        return card;
    }

    private JButton button(String label, Color color) {
        JButton button = new JButton(label);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(9, 13, 9, 13));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void addTransaction(String type) {
        JTextField amountField = new JTextField();
        JTextField categoryField = new JTextField();
        JTextField descriptionField = new JTextField();
        JTextField dateField = new JTextField(LocalDate.now().toString());
        Object[] fields = {"Amount in rupees:", amountField, "Category:", categoryField,
                "Description:", descriptionField, "Date (YYYY-MM-DD):", dateField};
        while (true) {
            int result = JOptionPane.showConfirmDialog(null, fields, "Add " + type, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.OK_OPTION) return;
            try {
                BigDecimal amount = new BigDecimal(amountField.getText().trim());
                if (amount.signum() <= 0) throw new IllegalArgumentException("Amount must be greater than zero.");
                String category = categoryField.getText().trim();
                if (category.isEmpty()) throw new IllegalArgumentException("Please enter a category.");
                LocalDate date = LocalDate.parse(dateField.getText().trim());
                String id = UUID.randomUUID().toString().toUpperCase();
                expenseService.addTransaction(new Transaction(id, type, amount.setScale(2, RoundingMode.HALF_UP),
                        category, descriptionField.getText().trim(), date));
                saveData();
                refresh();
                return;
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Enter a valid amount, such as 500 or 125.50.", "Check amount", JOptionPane.WARNING_MESSAGE);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(null, "Enter a real date in YYYY-MM-DD format, such as 2026-10-07.", "Check date", JOptionPane.WARNING_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Check details", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void searchTransactions() {
        String query = searchField.getText().trim();
        List<Transaction> results = query.isEmpty() ? expenseService.getTransactions() : expenseService.search(query);
        displayRows(results);
    }

    private void deleteSelected() {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(null, "Select a transaction row first.", "Nothing selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String id = String.valueOf(table.getValueAt(table.convertRowIndexToModel(selected), 0));
        int confirm = JOptionPane.showConfirmDialog(null, "Delete the selected transaction?", "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            expenseService.deleteTransaction(id);
            saveData();
            refresh();
        }
    }

    private void showCategories() {
        Map<String, BigDecimal> totals = expenseService.expensesByCategory();
        if (totals.isEmpty()) { JOptionPane.showMessageDialog(null, "No expenses recorded yet.", "Category summary", JOptionPane.INFORMATION_MESSAGE); return; }
        StringBuilder text = new StringBuilder("Expense totals by category\n\n");
        totals.forEach((category, amount) -> text.append(category).append("    ").append(money(amount)).append('\n'));
        JOptionPane.showMessageDialog(null, text.toString(), "Category summary", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showMonthly() {
        String input = JOptionPane.showInputDialog(null, "Enter month as YYYY-MM:", YearMonth.now().toString());
        if (input == null) return;
        try {
            YearMonth month = YearMonth.parse(input.trim());
            BigDecimal income = BigDecimal.ZERO;
            BigDecimal expenses = BigDecimal.ZERO;
            for (Transaction t : expenseService.forMonth(month)) {
                if (t.getType().equals("Income")) income = income.add(t.getAmount());
                else expenses = expenses.add(t.getAmount());
            }
            String text = "Summary for " + month + "\n\nIncome: " + money(income)
                    + "\nExpenses: " + money(expenses) + "\nBalance: " + money(income.subtract(expenses));
            JOptionPane.showMessageDialog(null, text, "Monthly summary", JOptionPane.INFORMATION_MESSAGE);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(null, "Use YYYY-MM, for example 2026-10.", "Check month", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void refresh() {
        displayRows(expenseService.getTransactions());
        incomeValue.setText(money(expenseService.total("Income")));
        expenseValue.setText(money(expenseService.total("Expense")));
        balanceValue.setText(money(expenseService.total("Income").subtract(expenseService.total("Expense"))));
    }

    private void displayRows(List<Transaction> transactions) {
        tableModel.setRowCount(0);
        for (Transaction t : transactions) {
            tableModel.addRow(new Object[]{t.getId(), t.getType(), money(t.getAmount()), t.getCategory(), t.getDate(), t.getDescription()});
        }
    }

    private String money(BigDecimal amount) {
        return "₹" + amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private void saveData() {
        try { fileManager.saveTransactions(expenseService.getTransactions()); }
        catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Could not save transaction data:\n" + ex.getMessage(), "File error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
