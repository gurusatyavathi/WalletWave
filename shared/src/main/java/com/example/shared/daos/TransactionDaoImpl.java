package com.example.shared.daos;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.shared.models.Transaction;
import com.example.shared.utilities.DBHelper;

import java.util.ArrayList;
import java.util.List;

public class TransactionDaoImpl implements TransactionDao{

    // Method to add a transaction to the database
    @Override
    public Boolean addTransaction(Transaction transaction, DBHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // ContentValues object to hold the updated values
        ContentValues values = new ContentValues();
        values.put(DBHelper.KEY_TYPE, transaction.getType());
        values.put(DBHelper.KEY_CATEGORY, transaction.getCategory());
        values.put(DBHelper.KEY_AMOUNT, transaction.getAmount());
        values.put(DBHelper.KEY_DATE, transaction.getDate());
        values.put(DBHelper.KEY_NOTE, transaction.getNote());

        // Insert the transaction into the database
        long id = db.insert(DBHelper.TABLE_TRANSACTIONS, null, values);

        // Set the generated id to the transaction object
        transaction.setId(id);



        return id != -1;
    }

    // Method to retrieve a list of transactions from the database
    public List<Transaction> getListOfTransactions(DBHelper dbHelper){
        // list to store transactions
        List<Transaction> transactionsList = new ArrayList<>();
        // readable database instance
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DBHelper.TABLE_TRANSACTIONS + " WHERE " + DBHelper.KEY_DELETE + "=" + 0;
        // Execute the query
        Cursor cursor = db.rawQuery(query, null);

        // Iterate through the result set
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.KEY_ID));
            String type = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.KEY_TYPE));
            String category = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.KEY_CATEGORY));
            int amount = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.KEY_AMOUNT));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.KEY_DATE));
            String note = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.KEY_NOTE));

            Transaction transaction = new Transaction(type, category, amount, date, note);
            transaction.setId(id);
            // Add the transaction to the list
            transactionsList.add(transaction);
        }
        // Close the cursor and database connection
        cursor.close();
        // Return the list of transactions
        return transactionsList;
    }

    // Method to retrieve a list of deleted transactions from the database
    @Override
    public List<Transaction> getListOfDeletedTransactions(DBHelper dbHelper) {
        // List to store deleted transactions
        List<Transaction> transactionsList = new ArrayList<>();

        // Get a readable database instance
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM " + DBHelper.TABLE_TRANSACTIONS + " WHERE " + DBHelper.KEY_DELETE + "=" + 1;
        // execute
        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.KEY_ID));
            String type = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.KEY_TYPE));
            String category = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.KEY_CATEGORY));
            int amount = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.KEY_AMOUNT));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.KEY_DATE));
            String note = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.KEY_NOTE));

            Transaction transaction = new Transaction(type, category, amount, date, note);
            transaction.setId(id);
            // Add the transaction to the list
            transactionsList.add(transaction);
        }
        cursor.close();
        return transactionsList;
    }

    // Method to delete a transaction temporarily from the database
    @Override
    public Boolean deleteTransaction(long transactionId, DBHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // ContentValues object to hold the updated values
        ContentValues values = new ContentValues();
        values.put(DBHelper.KEY_DELETE, 1);

        // where clause to identify the transaction to be deleted
        String whereClause = DBHelper.KEY_ID + "=" + transactionId;

        // Execute
        int rowsUpdated = db.update(DBHelper.TABLE_TRANSACTIONS, values, whereClause, null);

        // Return true if at least one row was updated, indicating a successful deletion
        return rowsUpdated > 0;
    }

    // Method to permanently delete a transaction from the database
    @Override
    public Boolean permanentDeleteTransaction(long transactionId, DBHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //where clause to identify the transaction to be permanently deleted
        String whereClause = DBHelper.KEY_ID + "=" + transactionId;
        //execute
        int rowsDeleted = db.delete(DBHelper.TABLE_TRANSACTIONS, whereClause, null);

        // Return true if at least one row was deleted, indicating a successful permanent deletion
        return rowsDeleted > 0;
    }
    // Method to restore a deleted transaction in the database
    @Override
    public Boolean restoreTransaction(long transactionId, DBHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DBHelper.KEY_DELETE, 0);

        String whereClause = DBHelper.KEY_ID + "=" + transactionId;

        int rowsUpdated = db.update(DBHelper.TABLE_TRANSACTIONS, values, whereClause, null);



        return rowsUpdated > 0;
    }

    // Method to update a transaction in the database
    @Override
    public Transaction updateTransaction(Transaction transaction, DBHelper dbHelper) {
        // writable database instance
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // ContentValues object to hold the updated values
        ContentValues values = new ContentValues();

        values.put(DBHelper.KEY_TYPE, transaction.getType());
        values.put(DBHelper.KEY_CATEGORY, transaction.getCategory());
        values.put(DBHelper.KEY_AMOUNT, transaction.getAmount());
        values.put(DBHelper.KEY_DATE, transaction.getDate());
        values.put(DBHelper.KEY_NOTE, transaction.getNote());

        // WHERE clause to specify which transaction to update
        String whereClause = DBHelper.KEY_ID + "=" + transaction.getId();

        // Update the transaction
        int rowsUpdated = db.update(DBHelper.TABLE_TRANSACTIONS, values, whereClause, null);

        // Check if the update was successful
        if (rowsUpdated > 0) {
            return transaction;
        } else {
            return null;
        }
    }
}