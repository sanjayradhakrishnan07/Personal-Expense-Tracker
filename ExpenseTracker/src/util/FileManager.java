package util;

import model.Transaction;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class FileManager {
    private final Path filePath = Path.of("data", "transactions.txt");

    public List<Transaction> loadTransactions() throws IOException {
        List<Transaction> transactions = new ArrayList<>();
        if (!Files.exists(filePath)) return transactions;

        for (String line : Files.readAllLines(filePath, StandardCharsets.UTF_8)) {
            if (line.isBlank()) continue;
            try {
                String[] parts = line.split("\\|", -1);
                transactions.add(new Transaction(
                    parts[0], decode(parts[1]), new BigDecimal(parts[2]),
                    decode(parts[3]), decode(parts[4]), LocalDate.parse(parts[5])
                ));
            } catch (RuntimeException ex) {
                System.out.println("Skipped one damaged data line.");
            }
        }
        return transactions;
    }

    public void saveTransactions(List<Transaction> transactions) throws IOException {
        Files.createDirectories(filePath.getParent());
        List<String> lines = new ArrayList<>();
        for (Transaction t : transactions) {
            lines.add(t.getId() + "|" + encode(t.getType()) + "|" + t.getAmount()
                + "|" + encode(t.getCategory()) + "|" + encode(t.getDescription())
                + "|" + t.getDate());
        }
        Files.write(filePath, lines, StandardCharsets.UTF_8);
    }

    private String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String value) {
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }
}
