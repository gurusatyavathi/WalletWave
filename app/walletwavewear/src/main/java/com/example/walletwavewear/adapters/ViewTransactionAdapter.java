package com.example.walletwavewear.adapters;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shared.models.Transaction;
import com.example.walletwavewear.activities.MainActivity;
import com.example.walletwavewear.activities.UpdateTransactionActivity;
import com.example.walletwavewear.activities.ViewTransactionActivity;
import com.example.walletwavewear.databinding.ActivityViewTransactionListBinding;

import java.util.List;

public class ViewTransactionAdapter extends RecyclerView.Adapter<ViewTransactionAdapter.ViewTransactionViewHolder>{
    private final List<Transaction> transactions;
    private ViewTransactionActivity listener;

    public ViewTransactionAdapter(List<Transaction> transactions) {
        this.transactions = transactions;
    }


    @NonNull
    @Override
    public ViewTransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        ActivityViewTransactionListBinding binding = ActivityViewTransactionListBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewTransactionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewTransactionAdapter.ViewTransactionViewHolder holder, int position) {
        //retrieve transaction
        Transaction transaction = transactions.get(position);
        //bind data to views
        holder.binding.tvCategory.setText(transaction.getCategory());
        holder.binding.tvNotes.setText(transaction.getNote());
        holder.binding.tvDate.setText(transaction.getDate());

        //amount is converted to double and format it
        double amount = Double.parseDouble(String.valueOf(transaction.getAmount()));
        String amountText = transaction.getType().equalsIgnoreCase("Income")
                ? "+" + String.format("$%.2f", amount)
                : "-" + String.format("$%.2f", amount);
        holder.binding.tvAmount.setText(amountText);

        //change text color based on transaction type
        if ("Expense".equalsIgnoreCase(transaction.getType())) {
            holder.binding.tvAmount.setTextColor(Color.RED);
        } else {
            holder.binding.tvAmount.setTextColor(Color.GREEN);
        }

        //setting click listener to inflate UpdateTransactionActivity
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onTransactionClick(transaction);
            Intent intent = new Intent(holder.itemView.getContext(), UpdateTransactionActivity.class);
            intent.putExtra("id", transaction.getId());
            intent.putExtra("category", transaction.getCategory());
            intent.putExtra("notes", transaction.getNote());
            intent.putExtra("date", transaction.getDate());
            intent.putExtra("amount", transaction.getAmount());
            intent.putExtra("type", transaction.getType());
            ((MainActivity) holder.itemView.getContext()).startActivityForResult(intent, 100);
        });
    }

    //return number of transactions
    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public void setOnTransactionClickListener(ViewTransactionActivity listener) {
        this.listener = listener;
    }

    //updatedTransactions with new list
    public void updateList(List<Transaction> updatedTransactions) {
        this.transactions.clear();
        this.transactions.addAll(updatedTransactions);
        notifyDataSetChanged();
    }

    //view holder for RecyclerView item.
    public static class ViewTransactionViewHolder extends RecyclerView.ViewHolder {
        final ActivityViewTransactionListBinding binding;

        public ViewTransactionViewHolder(@NonNull ActivityViewTransactionListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
