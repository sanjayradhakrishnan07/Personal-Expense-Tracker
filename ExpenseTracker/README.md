# Personal Expense Tracker

A beginner-friendly Java console application for recording personal income and expenses.

## Features
- Add income and expenses with a category, description, and date
- View, search, and delete transactions
- Calculate income, expenses, and current balance
- View expense totals by category and monthly summaries
- Save and load transactions from `data/transactions.txt`

## Requirements
- Java Development Kit (JDK) 17 or newer

## Run on Windows PowerShell
From the project folder, compile the Java files:

```powershell
javac -encoding UTF-8 -d out src\model\Transaction.java src\service\ExpenseService.java src\util\FileManager.java src\Main.java
java -cp out Main
```

Transaction data is stored locally in `data/transactions.txt`.
