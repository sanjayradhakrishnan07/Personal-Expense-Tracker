package service;

import model.Transaction;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ExpenseService {
    private final List<Transaction> transactions;

    public ExpenseService(List<Transaction> savedTransactions) {
        transactions = new ArrayList<>(savedTransactions);
    }

    public List<Transaction> getTransactions() { return new ArrayList<>(transactions); }
    public void addTransaction(Transaction transaction) { transactions.add(transaction); }

    public boolean deleteTransaction(String id) {
        return transactions.removeIf(t -> t.getId().equalsIgnoreCase(id));
    }

    public List<Transaction> search(String query) {
        List<Transaction> matches = new ArrayList<>();
        String text = query.toLowerCase();
        for (Transaction t : transactions) {
            if (t.getId().toLowerCase().contains(text)
                    || t.getType().toLowerCase().contains(text)
                    || t.getCategory().toLowerCase().contains(text)
                    || t.getDescription().toLowerCase().contains(text)) {
                matches.add(t);
            }
        }
        return matches;
    }

    public BigDecimal total(String type) {
        BigDecimal result = BigDecimal.ZERO;
        for (Transaction t : transactions) {
            if (t.getType().equals(type)) result = result.add(t.getAmount());
        }
        return result;
    }

    public Map<String, BigDecimal> expensesByCategory() {
        Map<String, BigDecimal> totals = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Transaction t : transactions) {
            if (t.getType().equals("Expense")) {
                totals.merge(t.getCategory(), t.getAmount(), BigDecimal::add);
            }
        }
        return totals;
    }

    public List<Transaction> forMonth(YearMonth month) {
        List<Transaction> matches = new ArrayList<>();
        for (Transaction t : transactions) {
            if (YearMonth.from(t.getDate()).equals(month)) matches.add(t);
        }
        return matches;
    }
}
