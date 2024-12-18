package com.example.walletwave.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.example.walletwave.databinding.FragmentHomeBinding;
import com.example.shared.models.Transaction;
import com.example.shared.services.TransactionServices;
import com.example.shared.utilities.DBHelper;

import java.util.ArrayList;
import java.util.Calendar;

public class HomeFragment extends Fragment {
    // Variable declarations
    FragmentHomeBinding binding;
    DBHelper dbHelper;
    TransactionServices transactionServices;
    RecyclerView recyclerView;
    TransactionAdapter adapter;
    ArrayList<Transaction> transactionsList;

    // Arrays for expense and income categories
    String[] expenseCategory = {"Shopping", "Travel", "Food", "Entertainment", "Investment", "Others"};
    String[] incomeCategory = {"Salary", "Savings", "Gift", "Coupons/CashBack", "Others"};
    Button expense, income;

    public HomeFragment(TransactionServices transactionServices) {
        this.transactionServices = transactionServices;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Initialize DBHelper
        dbHelper = new DBHelper(getContext());
        expense = binding.expenseButton;
        income = binding.IncomeAmount;

        // Initialize RecyclerView and adapter
        recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        transactionsList = fetchData();
        adapter = new TransactionAdapter(transactionsList);
        recyclerView.setAdapter(adapter);

        // Access FloatingActionButton from the Activity
        if (getActivity() != null) {
            View fab = getActivity().findViewById(R.id.fab);
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(v -> showAddTransactionDialog());
        }
        refreshUI();
    }


    // Method to refresh the UI
    @SuppressLint("NotifyDataSetChanged")
    private void refreshUI() {
        transactionsList = fetchData(); // Fetch data from database
        adapter.setTransactions(transactionsList); // Update adapter data
        adapter.notifyDataSetChanged(); // Notify adapter
        int totalExpense = updateTotalExpense(); // Update total expense
        int totalIncome = updateTotalIncome(); // Update total income
        updateNetExpense(totalExpense, totalIncome);
    }



    // Method to show add transaction dialog
    @SuppressLint("NotifyDataSetChanged")
    private void showAddTransactionDialog() {
        // Check if the context is available
        if (getContext() == null) {
            // If context is null, cannot proceed
            return;
        }

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

        // Set up the dialog
        builder.setView(dialogView);
        builder.setPositiveButton("Add", (dialog, id) -> {
            // Call the method to add transaction when Add button is clicked
            addTransaction(editTextAmount, editTextDate, radioGroup, spinner, editTextNotes);
            // Refresh UI after dialog is shown
            refreshUI();
        });
        builder.setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();


    }


    private void addTransaction(EditText editTextAmount, EditText editTextDate, RadioGroup radioGroup, Spinner spinner, EditText editTextNotes) {
        try {
            // Parse input fields to extract transaction details
            int amount = Integer.parseInt(editTextAmount.getText().toString().trim()); // Get transaction amount
            String date = editTextDate.getText().toString().trim(); // Get transaction date
            String type = ((RadioButton) radioGroup.findViewById(radioGroup.getCheckedRadioButtonId())).getText().toString(); // Get transaction type (Expense or Income)
            String category = spinner.getSelectedItem().toString(); // Get transaction category
            String notes = editTextNotes.getText().toString().trim(); // Get transaction notes

            // Create a new Transaction object with extracted details
            Transaction transaction = new Transaction(type, category, amount, date, notes);
            // Attempt to add the transaction to the database
            boolean result = transactionServices.addTransaction(transaction, dbHelper);
            if (result) {
                // If transaction is added successfully, show success message and update UI
                Toast.makeText(getContext(), "Transaction Added Successfully", Toast.LENGTH_SHORT).show();
                refreshUI();
            } else {
                // If transaction addition fails, show error message
                Toast.makeText(getContext(), "Failed to add transaction", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            // Handle NumberFormatException if input format is invalid
            Toast.makeText(getContext(), "Invalid number format", Toast.LENGTH_SHORT).show();
        }
    }

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

    private int calculateTotalExpense() {
        int totalExpense = 0;
        for (Transaction transaction : transactionsList) {
            if (transaction.getType().equalsIgnoreCase("Expense")) {
                totalExpense += transaction.getAmount();
            }
        }
        return totalExpense;
    }

    private int calculateTotalIncome() {
        int totalIncome = 0;
        for (Transaction transaction : transactionsList) {
            if (transaction.getType().equalsIgnoreCase("Income")) {
                totalIncome += transaction.getAmount();
            }
        }
        return totalIncome;
    }


    @SuppressLint("DefaultLocale")
    private int updateTotalExpense() {
        int totalExpense = calculateTotalExpense();
        expense.setText(String.format("Expense\n%d", totalExpense));
        return totalExpense;
    }

    @SuppressLint("DefaultLocale")
    private int updateTotalIncome() {
        int totalIncome = calculateTotalIncome();
        income.setText(String.format("Income\n%d", totalIncome));
        return totalIncome;
    }

    private void updateNetExpense(int totalExpense, int totalIncome) {
        int netExpense = totalIncome - totalExpense;
        String totalText = "Balance: $" + netExpense;
        binding.totalTextView.setText(totalText);
    }
    // Method to fetch data from database
    @SuppressLint("NotifyDataSetChanged")
    private ArrayList<Transaction> fetchData() {

        ArrayList<Transaction> list = new ArrayList<>();
        if (transactionServices != null) {
            list.addAll(transactionServices.getListOfTransactions(dbHelper));
            // adapter.notifyDataSetChanged();
        }

        if (list.isEmpty()) {
            // Display a message indicating no records were found
            Toast.makeText(getContext(), "No records found", Toast.LENGTH_SHORT).show();
            // You can also update the UI to display a message instead of an empty list
        } else {
            // Update the adapter with the new data
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
            // More data can be added here
            return list;

        }

    }

