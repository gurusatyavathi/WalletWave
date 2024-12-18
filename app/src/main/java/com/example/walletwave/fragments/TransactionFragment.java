package com.example.walletwave.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.walletwave.R;
import com.example.walletwave.adapters.TransactionAdapter;
import com.example.walletwave.databinding.FragmentTransactionBinding;
import com.example.shared.models.Transaction;
import com.example.shared.services.TransactionServices;
import com.example.shared.utilities.DBHelper;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class TransactionFragment extends Fragment {

    private TransactionAdapter adapter;
    private ArrayList<Transaction> transactionsList;
    private FragmentTransactionBinding fragmentTransactionBinding = null;
    private DBHelper dbHelper;
    private DataClient dataClient;
    //categories dropdown arrays
    String[] expenseCategory = {"Shopping", "Travel", "Food", "Entertainment", "Investment", "Others"};
    String[] incomeCategory = {"Salary", "Savings", "Gift", "Coupons/CashBack", "Others"};
    private final TransactionServices transactionServices;

    //constructor
    public TransactionFragment(TransactionServices transactionServices) {
        this.transactionServices = transactionServices;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //inflating layout for fragment
        fragmentTransactionBinding = FragmentTransactionBinding.inflate(inflater, container, false);
        return fragmentTransactionBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //initialize DBHelper
        dbHelper = new DBHelper(getContext());
        //setting up RecyclerView
        RecyclerView recyclerView = fragmentTransactionBinding.transaction;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        //fetching and displaying transactions
        transactionsList = fetchData();
        Collections.reverse(transactionsList);
        adapter = new TransactionAdapter(transactionsList);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(this::showUpdateDialog);

        //accessing floating add button
        if (getActivity() != null) {
            View fab = getActivity().findViewById(R.id.fab);
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(v -> showAddTransactionDialog());
        }
        refreshUI();
    }

    //refresh UI after data changes
    @SuppressLint("NotifyDataSetChanged")
    private void refreshUI() {
        transactionsList = fetchData(); // Fetch data from database
        adapter.setTransactions(transactionsList); // Update adapter data
        adapter.notifyDataSetChanged(); // Notify adapter

    }

    //dialog for updating transaction
    @SuppressLint("NotifyDataSetChanged")
    private void showUpdateDialog(final int position) {
        Transaction transactionUpdate = transactionsList.get(position);
        //creating dialog with custom fragment_update_transaction layout
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.fragment_update_transaction, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        //initialize dialog view
        EditText updateAmountEditText = dialogView.findViewById(R.id.updateAmount);
        EditText updateDateEditText = dialogView.findViewById(R.id.updateDate);
        updateDateEditText.setOnClickListener(v -> showDatePicker(updateDateEditText));
        RadioGroup radioGroupTransactionType = dialogView.findViewById(R.id.radioGroupTransactionType);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
        EditText updateNotesEditText = dialogView.findViewById(R.id.updateNotes);

        //prepopulate dialog fields with transaction details
        updateAmountEditText.setText(String.valueOf(transactionUpdate.getAmount()));
        updateDateEditText.setText(transactionUpdate.getDate());
        //setting up radio group listener
        radioGroupTransactionType.setOnCheckedChangeListener((group, checkedId) -> {
            ArrayAdapter<String> categoryAdapter;
            if (checkedId == R.id.radioButtonExpense) {
                categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, expenseCategory);
            } else {
                categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, incomeCategory);
            }
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
            spinnerCategory.setAdapter(categoryAdapter);
        });

        if (transactionUpdate.getType().equals("Expense")) {
            radioGroupTransactionType.check(R.id.radioButtonExpense);
        } else {
            radioGroupTransactionType.check(R.id.radioButtonIncome);
        }
        ArrayAdapter<String> adapters;
        if (radioGroupTransactionType.getCheckedRadioButtonId() == R.id.radioButtonExpense) {
            adapters = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, expenseCategory);
        } else {
            adapters = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, incomeCategory);
        }
        adapters.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinnerCategory.setAdapter(adapters);
        spinnerCategory.setSelection(adapters.getPosition(transactionUpdate.getCategory()));
        updateNotesEditText.setText(transactionUpdate.getNote());

        builder.setView(dialogView);
        //positive button for updating transaction
        builder.setPositiveButton("Update", (dialog, which) -> {
            if (updateAmountEditText.getText().toString().isEmpty() ||
                    updateDateEditText.getText().toString().isEmpty() ||
                    radioGroupTransactionType.getCheckedRadioButtonId() == -1 ||
                    spinnerCategory.getSelectedItem() == null
            ) {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            //update transaction with updates values
            Transaction updatedTransaction = new Transaction(
                    (int) transactionUpdate.getId(),
                    //populate with updated data from dialog fields
                    radioGroupTransactionType.getCheckedRadioButtonId() == R.id.radioButtonExpense ? "Expense" : "Income",
                    spinnerCategory.getSelectedItem().toString(),
                    Integer.parseInt(updateAmountEditText.getText().toString()),
                    updateDateEditText.getText().toString(),
                    updateNotesEditText.getText().toString()
            );
            boolean isUpdated = transactionServices.updateTransaction(updatedTransaction, dbHelper) != null;
            if (isUpdated) {
                transactionsList.set(position, updatedTransaction);
                refreshUI();
                Toast.makeText(getContext(), "Transaction updated Successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to Update transaction", Toast.LENGTH_SHORT).show();
            }
        });
        //negative button for deleting transaction
        builder.setNegativeButton("Delete", (dialog, which) -> {
            //deleting transaction
            boolean isDeleted = transactionServices.deleteTransaction(transactionUpdate.getId(), dbHelper);
            if (isDeleted) {
                transactionsList.remove(position);
                refreshUI();
                Toast.makeText(getContext(), "Transaction Deleted Successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to Deleted transaction", Toast.LENGTH_SHORT).show();
            }
        });
        //neutral button for cancel
        builder.setNeutralButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        refreshUI();
    }

    //fetch transactions from database
    @SuppressLint("NotifyDataSetChanged")
    private ArrayList<Transaction> fetchData() {
        ArrayList<Transaction> list = new ArrayList<>();
        if (transactionServices != null) {
            list.addAll(transactionServices.getListOfTransactions(dbHelper));
        }
        if (list.isEmpty()) {
            //toast message for no records
            Toast.makeText(getContext(), "No records found", Toast.LENGTH_SHORT).show();
        } else {
            //update the adapter with new data
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
        return list;
    }

    //add transaction to database
    @SuppressLint("NotifyDataSetChanged")
    private void showAddTransactionDialog() {
        // Inflate the layout for the dialog
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.fragment_add_transaction, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        // Initialize views in the dialog
        EditText editTextAmount = dialogView.findViewById(R.id.addAmount);
        EditText editTextDate = dialogView.findViewById(R.id.addDate);
        editTextDate.setOnClickListener(v -> showDatePicker(editTextDate));
        RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroupTransactionType);
        Spinner spinner = dialogView.findViewById(R.id.spinnerCategory);
        // Set up the spinner adapter based on the selected transaction type
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedRadioButton = dialogView.findViewById(checkedId);
            String selectedTransactionType = selectedRadioButton.getText().toString();
            ArrayAdapter<String> adapter;
            if (selectedTransactionType.equals("Expense")) {
                adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, expenseCategory);
            } else {
                adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, incomeCategory);
            }
            adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
            spinner.setAdapter(adapter);
        });

        EditText editTextNotes = dialogView.findViewById(R.id.addNotes);
        builder.setView(dialogView);
        //positive button for adding transaction
        builder.setPositiveButton("Add", (dialog, id) -> {
            addTransaction(editTextAmount, editTextDate, radioGroup, spinner, editTextNotes);
            refreshUI();
        });
        //negative button for canceling transaction
        builder.setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void addTransaction(EditText editTextAmount, EditText editTextDate, RadioGroup radioGroup, Spinner spinner, EditText editTextNotes) {
        try {
            // Parse input fields to extract transaction details
            int amount = Integer.parseInt(editTextAmount.getText().toString().trim());
            String date = editTextDate.getText().toString().trim();
            String type = ((RadioButton) radioGroup.findViewById(radioGroup.getCheckedRadioButtonId())).getText().toString();
            String category = spinner.getSelectedItem().toString();
            String notes = editTextNotes.getText().toString().trim();
            // Create a new Transaction object with extracted details
            Transaction transaction = new Transaction(type, category, amount, date, notes);
            // Attempt to add the transaction to the database
            boolean result = transactionServices.addTransaction(transaction, dbHelper);
            if (result) {
                // If transaction is added successfully, show success message
                Toast.makeText(getContext(), "Transaction Added Successfully", Toast.LENGTH_SHORT).show();
                transactionsList = fetchData();

            } else {
                // If transaction addition fails, show error message
                Toast.makeText(getContext(), "Failed to add transaction", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            // Handle NumberFormatException if input format is invalid
            Toast.makeText(getContext(), "Invalid number format", Toast.LENGTH_SHORT).show();
        }
    }

    //show date picker dialog
    private void showDatePicker(EditText editTextDate) {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String dateString = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                    editTextDate.setText(dateString);
                }, year, month, day);
        datePickerDialog.show();
    }
}
