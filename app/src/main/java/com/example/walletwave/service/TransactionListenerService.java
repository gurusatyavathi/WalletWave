package com.example.walletwave.service;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.shared.daos.TransactionDao;
import com.example.shared.daos.TransactionDaoImpl;
import com.example.shared.models.Transaction;
import com.example.shared.services.TransactionServices;
import com.example.shared.utilities.DBHelper;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;

public class TransactionListenerService extends WearableListenerService {
    private DBHelper dbHelper;
    private TransactionServices transactionServices;

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize database and transaction services
        dbHelper = new DBHelper(this);
        TransactionDao transactionDao = new TransactionDaoImpl();
        transactionServices = new TransactionServices(transactionDao);
    }

    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if ("/transactions".equals(path)) {
                    DataMap dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                    processReceivedTransaction(dataMap);
                }
            }
        }
    }

    // Process received transaction data
    private void processReceivedTransaction(DataMap dataMap) {
        long id = dataMap.getLong("id");
        String type = dataMap.getString("type");
        String category = dataMap.getString("category");
        int amount = dataMap.getInt("amount");
        String date = dataMap.getString("date");
        String notes = dataMap.getString("notes");

        // Create a Transaction object
        Transaction transaction = new Transaction(id, type, category, amount, date, notes);

        // Save the transaction to the database
        boolean success = transactionServices.addTransaction(transaction, dbHelper);
        if (success) {
            Toast.makeText(this, "Transaction received and saved", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to save received transaction", Toast.LENGTH_SHORT).show();
        }
    }
}
