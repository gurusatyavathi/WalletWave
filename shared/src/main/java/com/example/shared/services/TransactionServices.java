package com.example.shared.services;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.shared.daos.TransactionDao;
import com.example.shared.models.Transaction;
import com.example.shared.utilities.DBHelper;

import java.util.List;

public class TransactionServices {
    private final TransactionDao transactionDao;

    // Constructor to initialize TransactionServices with a TransactionDao implementation
    public TransactionServices(TransactionDao transactionDao) {
        this.transactionDao = transactionDao;
    }

    // Add a new transaction to the database
    public synchronized Boolean addTransaction(Transaction transaction, DBHelper dbHelper) {
        //return this.transactionDao.addTransaction(transaction, dbHelper);
        SQLiteDatabase db = dbHelper.getWritableDatabase(); // Ensure DB is open
        try {
            ContentValues values = new ContentValues();
            values.put(DBHelper.KEY_TYPE, transaction.getType());
            values.put(DBHelper.KEY_CATEGORY, transaction.getCategory());
            values.put(DBHelper.KEY_AMOUNT, transaction.getAmount());
            values.put(DBHelper.KEY_DATE, transaction.getDate());
            values.put(DBHelper.KEY_NOTE, transaction.getNote());

            long id = db.insert(DBHelper.TABLE_TRANSACTIONS, null, values);
            if (id != -1) {
                transaction.setId(id);
                return true;
            }
        } catch (Exception e) {
            Log.e("DB_ERROR", "Failed to insert transaction", e);
        }
        return false;
    }

    // Retrieve a list of transactions from the database
    public List<Transaction> getListOfTransactions(DBHelper dbHelper){
        return this.transactionDao.getListOfTransactions(dbHelper);
    }

    // Retrieve a list of deleted transactions  from the database
    public List<Transaction> getListOfDeletedTransactions(DBHelper dbHelper){
        return this.transactionDao.getListOfDeletedTransactions(dbHelper);
    }

    // Delete a transaction from the database
    public Boolean deleteTransaction(long transactionId, DBHelper dbHelper) {
        return this.transactionDao.deleteTransaction(transactionId, dbHelper);
    }

    // Permanently delete a transaction from the database
    public Boolean permanentDeleteTransaction(long transactionId, DBHelper dbHelper){
        return this.transactionDao.permanentDeleteTransaction(transactionId, dbHelper);
    }

    // Restore a deleted transaction in the database
    public Boolean restoreTransaction(long transactionId, DBHelper dbHelper){
        return this.transactionDao.restoreTransaction(transactionId, dbHelper);
    }

    // Update an existing transaction in the database
    public Transaction updateTransaction(Transaction transaction, DBHelper dbHelper) {
        return this.transactionDao.updateTransaction(transaction, dbHelper);
}

}