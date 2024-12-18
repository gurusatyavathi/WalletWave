package com.example.shared.daos;

import com.example.shared.models.Transaction;
import com.example.shared.utilities.DBHelper;

import java.util.List;

public interface TransactionDao {

    // Add a new transaction to the database
    Boolean addTransaction(Transaction transaction, DBHelper dbHelper);

    // Temporarily delete a transaction by marking it as deleted in the database
    Boolean deleteTransaction(long transactionId, DBHelper dbHelper);

    // Permanently delete a transaction from the database
    Boolean permanentDeleteTransaction(long transactionId, DBHelper dbHelper);

    // Restore a deleted transaction by marking it as not deleted in the database
    Boolean restoreTransaction(long transactionId, DBHelper dbHelper);

    // Update an existing transaction in the database
    Transaction updateTransaction(Transaction transaction, DBHelper dbHelper);

    List<Transaction> getListOfTransactions(DBHelper dbHelper);

    List<Transaction> getListOfDeletedTransactions(DBHelper dbHelper);
}