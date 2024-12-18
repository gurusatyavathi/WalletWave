package com.example.walletwavewear.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.shared.daos.TransactionDao;
import com.example.shared.daos.TransactionDaoImpl;
import com.example.shared.models.Transaction;
import com.example.shared.services.TransactionServices;
import com.example.shared.utilities.DBHelper;
import com.example.walletwavewear.R;
import com.example.walletwavewear.adapters.ViewTransactionAdapter;
import com.example.walletwavewear.databinding.ActivityViewTransactionBinding;

import java.util.List;

public class ViewTransactionActivity extends AppCompatActivity implements OnTransactionClickListener{
    private ActivityViewTransactionBinding binding;
    private TransactionServices transactionServices;
    private DBHelper dbHelper;
    private ViewTransactionAdapter adapter;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //inflate layout by view binding
        binding = ActivityViewTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //initialize database helper and transaction services
        dbHelper = DBHelper.getInstance(this);
        TransactionDao transactionDao = new TransactionDaoImpl();
        transactionServices = new TransactionServices(transactionDao);
        initializeTransactions();

        //initialize the ActivityResultLauncher
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        fetchAndUpdateTransactions();
                    }
                }
        );

    }
    //fetch and display list of transactions
    private void initializeTransactions() {
        new Thread(() -> {
            List<Transaction> transactions = transactionServices.getListOfTransactions(dbHelper);
            //switch back to UI thread
            runOnUiThread(() -> {
                //set up adapter
                adapter = new ViewTransactionAdapter(transactions);
                adapter.setOnTransactionClickListener(this);
                //set recyclerView with adapter
                binding.rvTransactions.setLayoutManager(new LinearLayoutManager(this));
                binding.rvTransactions.setAdapter(adapter);
            });
        }).start();
    }

    @Override
    public void onTransactionClick(Transaction transaction) {
        Intent intent = new Intent(ViewTransactionActivity.this, UpdateTransactionActivity.class);
        //passing transaction details to UpdateTransactionActivity
        intent.putExtra("id", transaction.getId());
        intent.putExtra("type", transaction.getType());
        intent.putExtra("category", transaction.getCategory());
        intent.putExtra("notes", transaction.getNote());
        intent.putExtra("date", transaction.getDate());
        intent.putExtra("amount", transaction.getAmount());
        activityResultLauncher.launch(intent);
    }

    private void fetchAndUpdateTransactions() {
        new Thread(() -> {
            List<Transaction> updatedTransactions = transactionServices.getListOfTransactions(dbHelper);
            runOnUiThread(() -> adapter.updateList(updatedTransactions));
        }).start();
    }
}