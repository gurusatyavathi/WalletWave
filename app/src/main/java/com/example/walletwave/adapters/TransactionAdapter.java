package com.example.walletwave.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.walletwave.R;
import com.example.shared.models.Transaction;

import java.util.ArrayList;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private final ArrayList<Transaction> transactions;
    private OnItemClickListener mListener;

    // Interface for item click events
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    // Method to set the listener for item clicks
    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    // Constructor
    public TransactionAdapter(ArrayList<Transaction> transactions) {
        this.transactions = transactions;
    }
    // Method to update the list of transactions
    public void setTransactions(ArrayList<Transaction> transactions) {
        this.transactions.clear();
        this.transactions.addAll(transactions);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for a transaction item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_transaction_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get transaction
        Transaction transaction = transactions.get(position);
        // Set transaction
        holder.transactionCategory.setText(transaction.getCategory());
        holder.transactionNotes.setText(transaction.getNote());
        holder.transactionDate.setText(transaction.getDate());

        // Adjusting the amount text and color based on transaction type
        int amount = transaction.getAmount();
        // if income add + and green color
        if (transaction.getType().equals("Income")) {
            holder.amountText.setText(String.format("+ $%d", amount));
            holder.amountText.setTextColor(holder.itemView.getResources().getColor(R.color.green));
        }
        // if expense add - and red color
        else if (transaction.getType().equals("Expense")) {
            holder.amountText.setText(String.format("- $%d", amount));
            holder.amountText.setTextColor(holder.itemView.getResources().getColor(R.color.red));
        }

        // Set click listener for the item view
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get adapter position of clicked item
                int clickedPosition = holder.getAdapterPosition();
                if (mListener != null && clickedPosition != RecyclerView.NO_POSITION) {
                    // Invoke onItemClick method of the listener
                    mListener.onItemClick(clickedPosition);
                }
            }
        });
    }

    // Method to get the number of items in the list
    @Override
    public int getItemCount() {
        return transactions.size();
    }

    // ViewHolder class to hold references to UI elements of each item view
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView transactionCategory, transactionNotes, transactionDate, amountText;

        // Constructor to initialize UI elements
        public ViewHolder(View itemView) {
            super(itemView);
            transactionCategory = itemView.findViewById(R.id.transactionCategory);
            transactionNotes = itemView.findViewById(R.id.transactionNotes);
            transactionDate = itemView.findViewById(R.id.transactionDate);
            amountText = itemView.findViewById(R.id.amountText);
        }
    }
}
