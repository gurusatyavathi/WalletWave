package com.example.walletwavewear.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.walletwavewear.R;

import com.example.walletwavewear.databinding.ActivityTransactionBinding;
import com.example.shared.models.Transaction;
import com.example.shared.utilities.DBHelper;
import com.example.shared.daos.TransactionDao;
import com.example.shared.daos.TransactionDaoImpl;
import com.example.shared.services.TransactionServices;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TransactionActivity extends AppCompatActivity implements DataClient.OnDataChangedListener{
    ActivityTransactionBinding transactionBinding;
    private Calendar myCalendar;
    private DBHelper dbHelper;
    private DataClient dataClient;
    private TransactionServices transactionServices;
    private String[] expenseCategory = {"Shopping", "Travel", "Food", "Entertainment", "Investment", "Others"};
    private String[] incomeCategory = {"Salary", "Savings", "Gift", "Coupons/CashBack", "Others"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //inflate layout by view binding
        transactionBinding = ActivityTransactionBinding.inflate(getLayoutInflater());
        View view = transactionBinding.getRoot();
        setContentView(view);
        //initialize listeners
        setupListeners();
        setupSpinner();
        //initialize database helper and DAO
        dbHelper = new DBHelper(getApplicationContext());
        TransactionDao transactionDao = new TransactionDaoImpl();
        transactionServices = new TransactionServices(transactionDao);
        myCalendar = Calendar.getInstance();

        //initialize DataClient
        dataClient = Wearable.getDataClient(this);

    }

    //setting up listeners
    private void setupListeners() {
        transactionBinding.addDate.setOnClickListener(v -> showDatePicker());
        transactionBinding.btnAdd.setOnClickListener(v -> addTransaction());
        transactionBinding.radioGroupTransactionType.setOnCheckedChangeListener((group, checkedId) -> updateCategorySpinner());
    }

    //spinner for categories
    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, expenseCategory);
        adapter.setDropDownViewResource(R.layout.spinner_item); // Use the same custom layout for dropdown
        transactionBinding.spinnerCategory.setAdapter(adapter);
    }

    //update spinner on selection
    private void updateCategorySpinner() {
        String[] categories = transactionBinding.radioButtonExpense.isChecked() ? expenseCategory : incomeCategory;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, categories);
        adapter.setDropDownViewResource(R.layout.spinner_item); // Use custom layout
        transactionBinding.spinnerCategory.setAdapter(adapter);
    }

    //display date picker dialog
    private void showDatePicker() {
        new DatePickerDialog(TransactionActivity.this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    //update date on selection
    private DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }

    };

    //update UI with selected date
    private void updateLabel() {
        String myFormat = "MM/dd/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        transactionBinding.addDate.setText(sdf.format(myCalendar.getTime()));
    }

    //add transaction
    private void addTransaction() {
        if (!validateInputs()) {
            return;
        }
        //input values
        String amountStr = transactionBinding.addAmount.getText().toString();
        String date = transactionBinding.addDate.getText().toString();
        String notes = transactionBinding.addNotes.getText().toString();
        String category = transactionBinding.spinnerCategory.getSelectedItem().toString();
        String type = transactionBinding.radioButtonExpense.isChecked() ? "Expense" : "Income";

        //creating transaction object
        int amount = Integer.parseInt(amountStr);
        Transaction transaction = new Transaction(type, category, amount, date, notes);
        //add transaction to SQL
        Boolean success = transactionServices.addTransaction(transaction, dbHelper);
        if (success) {
            Toast.makeText(this, "Transaction added successfully", Toast.LENGTH_SHORT).show();
            sendTransactionToMobile(transaction);
            clearFields();
            finish();
        } else {
            Toast.makeText(this, "Failed to add transaction", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(TransactionActivity.this, "Transaction sent to mobile", Toast.LENGTH_SHORT).show());
    }

    //validate user inputs
    private boolean validateInputs() {
        String amountStr = transactionBinding.addAmount.getText().toString();
        String date = transactionBinding.addDate.getText().toString();
        String category = transactionBinding.spinnerCategory.getSelectedItem().toString();

        //check for empty fields
        if (amountStr.isEmpty() || date.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        //validate amount
        try {
            Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    //clear input fields after transaction success
    private void clearFields() {
        transactionBinding.addAmount.setText("");
        transactionBinding.addDate.setText("");
        transactionBinding.addNotes.setText("");
        transactionBinding.radioGroupTransactionType.clearCheck();
        transactionBinding.spinnerCategory.setSelection(0);
    }
    //remove listeners
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dataClient != null) {
            dataClient.removeListener(this);
        }
    }

    //data change from data layer API
    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEventBuffer) {
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if ("/transactions".equals(path)) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    DataMap dataMap = dataMapItem.getDataMap();
                    processReceivedTransaction(dataMap);
                }
            }
        }
    }

    //process received transaction from mobile
    private void processReceivedTransaction(DataMap dataMap) {
        //get transaction details from DataMap
        long id = dataMap.getLong("id");
        String type = dataMap.getString("type");
        String category = dataMap.getString("category");
        int amount = dataMap.getInt("amount");
        String date = dataMap.getString("date");
        String notes = dataMap.getString("notes");
        //create transaction object
        Transaction transaction = new Transaction((int) id, type, category, amount, date, notes);
        //add transaction to SQL
        Boolean success = transactionServices.addTransaction(transaction, dbHelper);
        if (success) {
            Toast.makeText(this, "Received and saved transaction from mobile", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to save received transaction", Toast.LENGTH_SHORT).show();
        }
    }
}