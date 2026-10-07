package model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Transaction {
    private final String id;
    private final String type;
    private final BigDecimal amount;
    private final String category;
    private final String description;
    private final LocalDate date;

    public Transaction(String id, String type, BigDecimal amount, String category,
                       String description, LocalDate date) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.date = date;
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public LocalDate getDate() { return date; }
}
