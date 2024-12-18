package com.example.walletwave.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.walletwave.R;
import com.example.walletwave.adapters.TransactionAdapter;
import com.example.shared.daos.TransactionDaoImpl;
import com.example.walletwave.databinding.FragmentSearchTransactionBinding;
import com.example.shared.models.Transaction;
import com.example.shared.services.TransactionServices;
import com.example.shared.utilities.DBHelper;

import java.util.ArrayList;
import java.util.List;

public class SearchTransactionFragment extends Fragment {
    FragmentSearchTransactionBinding binding;
    TransactionAdapter adapter;
    List<Transaction> allTransactions;
    TransactionServices transactionServices;

    public SearchTransactionFragment(TransactionServices transactionServices) {
        this.transactionServices = transactionServices;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchTransactionBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        //accessing floating add button
        if (getActivity() != null) {
            View fab = getActivity().findViewById(R.id.fab);
            fab.setVisibility(View.VISIBLE);
        }

        //initialize RecyclerView
        binding.searchTransactionRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        allTransactions = new ArrayList<>();
        adapter = new TransactionAdapter((ArrayList<Transaction>) allTransactions);
        binding.searchTransactionRecycler.setAdapter(adapter);

        //initialize TransactionServices
        transactionServices = new TransactionServices(new TransactionDaoImpl());
        binding.radioGroupTransactionType.check(R.id.radioButtonExpense);
        updateSpinnerAdapter("Expense");
        //setting up radio button listener
        binding.radioGroupTransactionType.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedRadioButton = group.findViewById(checkedId);
            if (selectedRadioButton != null) {
                String selectedTransactionType = selectedRadioButton.getText().toString();
                updateSpinnerAdapter(selectedTransactionType);
            }
        });

        //search button click
        binding.searchTransaction.setOnClickListener(v -> performSearch());

        return view;
    }

    //updating category spinner
    private void updateSpinnerAdapter(String selectedTransactionType) {
        ArrayAdapter<String> adapter;
        String[] categories;
        //toggle between expense and income categories
        if (selectedTransactionType.equals("Expense")) {
            categories = new String[]{"Shopping", "Travel", "Food", "Entertainment", "Investment", "Others"};
        } else {
            categories = new String[]{"Salary", "Savings", "Gift", "Coupons/CashBack", "Others" };
        }
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        binding.spinnerCategory.setAdapter(adapter);
    }

    //search transactions
    private void performSearch() {
        //getting type and category
        String type;
        RadioButton selectedRadioButton = binding.radioGroupTransactionType.findViewById(binding.radioGroupTransactionType.getCheckedRadioButtonId());
        if (selectedRadioButton != null) {
            type = selectedRadioButton.getText().toString();
        } else {
            // Default to Expense if no radio button is selected
            type = "Expense";
        }
        String category = binding.spinnerCategory.getSelectedItem().toString();

        Log.d("SearchTransaction", "Type: " + type + ", Category: " + category);
        //retrieve complete list of transactions
        DBHelper dbHelper = new DBHelper(getContext());
        allTransactions = transactionServices.getListOfTransactions(dbHelper);
        //filter transactions based on type and category
        List<Transaction> filteredTransactions = filterTransactions(allTransactions, type, category);
        binding.noResults.setVisibility(filteredTransactions.isEmpty() ? View.VISIBLE : View.GONE);
        //converting filtered transactions to ArrayList
        ArrayList<Transaction> filteredTransactionsArrayList = new ArrayList<>(filteredTransactions);
        //updating RecyclerView with the filtered transactions
        adapter = new TransactionAdapter(filteredTransactionsArrayList);
        binding.searchTransactionRecycler.setAdapter(adapter);
    }
    //filtering out transactions
    private List<Transaction> filterTransactions(List<Transaction> transactions, String type, String category) {
        //list for filtered transactions
        List<Transaction> filteredTransactions = new ArrayList<>();
        //loop to check for matched transactions
        for (Transaction transaction : transactions) {
            //check for transaction type
            if ((type.equals("Expense") && transaction.getType().equals("Expense"))
                    || (type.equals("Income") && transaction.getType().equals("Income"))) {
                //check for transaction category
                if (category.isEmpty() || transaction.getCategory().equalsIgnoreCase(category)) {
                    //add transactions to list
                    filteredTransactions.add(transaction);
                }
            }
        }
        return filteredTransactions;
    }
}
