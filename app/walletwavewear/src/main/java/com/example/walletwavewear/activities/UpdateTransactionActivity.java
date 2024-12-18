package com.example.walletwavewear.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.shared.daos.TransactionDaoImpl;
import com.example.shared.models.Transaction;
import com.example.shared.utilities.DBHelper;
import com.example.shared.services.TransactionServices;
import com.example.walletwavewear.R;
import com.example.walletwavewear.databinding.ActivityUpdateTransactionBinding;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class UpdateTransactionActivity extends AppCompatActivity {
    private ActivityUpdateTransactionBinding binding;
    private TransactionServices transactionServices;
    private DBHelper dbHelper;
    private DataClient dataClient;
    private Transaction transactionUpdate;
    private String[] expenseCategories = {"Food", "Transport", "Shopping", "Bills", "Health"};
    private String[] incomeCategories = {"Salary", "Business", "Investments", "Gifts"};


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //inflate layout by view binding
        binding = ActivityUpdateTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //set up database helpers and services
        dbHelper = new DBHelper(this);
        TransactionDaoImpl transactionDao = new TransactionDaoImpl();
        transactionServices = new TransactionServices(transactionDao);

        //receive data from Intent
        long transactionId = getIntent().getLongExtra("id", -1);
        String transactionType = getIntent().getStringExtra("type");
        String transactionCategory = getIntent().getStringExtra("category");
        int transactionAmount = getIntent().getIntExtra("amount", 0);
        String transactionDate = getIntent().getStringExtra("date");
        String transactionNote = getIntent().getStringExtra("notes");
        transactionUpdate = new Transaction(transactionId, transactionType, transactionCategory, transactionAmount, transactionDate, transactionNote);

        //prepopulate fields with data
        binding.updateAmount.setText(String.valueOf(transactionAmount));
        binding.updateNotes.setText(transactionNote);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy", Locale.US);
        String formattedDate = convertToDateFormat(transactionDate, sdf);
        binding.updateDate.setText(formattedDate);

        binding.spinnerCategory.setSelection(getCategoryIndex(transactionUpdate.getCategory(), transactionUpdate.getType()));

        if ("Expense".equalsIgnoreCase(transactionType)) {
            binding.radioGroupTransactionType.check(R.id.radioButtonExpense);
        } else {
            binding.radioGroupTransactionType.check(R.id.radioButtonIncome);
        }

        setupListeners();
    }
    //changing date format
    private String convertToDateFormat(String originalDate, SimpleDateFormat sdf) {
        try {
            Calendar calendar = Calendar.getInstance();
            String[] dateParts = originalDate.split("-");
            calendar.set(Calendar.YEAR, Integer.parseInt(dateParts[0]));
            calendar.set(Calendar.MONTH, Integer.parseInt(dateParts[1]) - 1);
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateParts[2]));
            return sdf.format(calendar.getTime());
        } catch (Exception e) {
            e.printStackTrace();
            return originalDate;
        }
    }

    //getting category adapter spinner
    private ArrayAdapter<String> getCategoryAdapter(String type) {
        String[] categories = "Expense".equalsIgnoreCase(type) ? expenseCategories : incomeCategories;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        return adapter;
    }

    private void setupListeners() {
        //set initial spinner adapter
        ArrayAdapter<String> adapter = getCategoryAdapter(transactionUpdate.getType());
        binding.spinnerCategory.setAdapter(adapter);

        binding.radioGroupTransactionType.setOnCheckedChangeListener((group, checkedId) -> {
            String type = (checkedId == R.id.radioButtonExpense) ? "Expense" : "Income";
            ArrayAdapter<String> newAdapter = getCategoryAdapter(type);
            binding.spinnerCategory.setAdapter(newAdapter);
        });

        //listeners for update, delete and update date
        binding.updateDate.setOnClickListener(v -> showDatePickerDialog());
        binding.btnUpdate.setOnClickListener(v -> updateTransaction());
        binding.btnDelete.setOnClickListener(v -> deleteTransaction());
    }

    //get category index with expense or income
    private int getCategoryIndex(String category, String type) {
        String[] categories = "Expense".equalsIgnoreCase(type) ? expenseCategories : incomeCategories;
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equalsIgnoreCase(category)) return i;
        }
        return 0;
    }

    //date picker dialog for date
    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            Calendar selectedDateCalendar = Calendar.getInstance();
            selectedDateCalendar.set(year1, month1, dayOfMonth);

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy", Locale.US);
            String formattedDate = sdf.format(selectedDateCalendar.getTime());
            binding.updateDate.setText(formattedDate);
        }, year, month, day);

        datePickerDialog.show();
    }

    //update transaction on changes
    private void updateTransaction() {
        if (transactionUpdate == null) {
            Toast.makeText(this, "Transaction data is unavailable!", Toast.LENGTH_SHORT).show();
            return;
        }
        String amountText = binding.updateAmount.getText().toString();
        String date = binding.updateDate.getText().toString();
        int selectedTypeId = binding.radioGroupTransactionType.getCheckedRadioButtonId();
        String category = binding.spinnerCategory.getSelectedItem().toString();
        String notes = binding.updateNotes.getText().toString();

        if (amountText.isEmpty() || date.isEmpty() || selectedTypeId == -1 || category.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String type = (selectedTypeId == R.id.radioButtonExpense) ? "Expense" : "Income";
        int amount = Integer.parseInt(amountText);
        //new transaction object
        Transaction updatedTransaction = new Transaction(
                transactionUpdate.getId(),
                type,
                category,
                amount,
                date,
                notes
        );
        //pass new transaction object to database for update
        Transaction result = transactionServices.updateTransaction(updatedTransaction, dbHelper);

        if (result != null) {
            Toast.makeText(this, "Transaction updated successfully", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            sendTransactionToMobile(result);
            finish();
        } else {
            Toast.makeText(this, "Failed to update transaction", Toast.LENGTH_SHORT).show();
        }
    }

    //soft delete
    private void deleteTransaction() {
        boolean isDeleted = transactionServices.deleteTransaction(transactionUpdate.getId(), dbHelper);
        if (isDeleted) {
            Toast.makeText(this, "Transaction deleted successfully", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Failed to delete transaction", Toast.LENGTH_SHORT).show();
        }
    }

    //sync transaction to mobile with data layer API
    private void sendTransactionToMobile(Transaction transaction) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/transactions");
        DataMap dataMap = putDataMapReq.getDataMap();
        //get transaction details into DataMap
        dataMap.putLong("id", transaction.getId());
        dataMap.putString("type", transaction.getType());
        dataMap.putString("category", transaction.getCategory());
        dataMap.putInt("amount", transaction.getAmount());
        dataMap.putString("date", transaction.getDate());
        dataMap.putString("notes", transaction.getNote());
        //create request and sent it
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        putDataReq.setUrgent();

        Task<DataItem> putDataTask = dataClient.putDataItem(putDataReq);
        putDataTask.addOnSuccessListener(dataItem ->
                Toast.makeText(UpdateTransactionActivity.this, "Transaction sent to mobile", Toast.LENGTH_SHORT).show());
    }
}