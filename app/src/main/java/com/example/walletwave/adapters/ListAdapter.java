package com.example.walletwave.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.walletwave.R;
import com.example.shared.models.Transaction;

import java.util.List;

public class ListAdapter extends BaseAdapter {

    Context context;
    List<Transaction> transactionList;
    LayoutInflater inflater;
    public ListAdapter(Context context, List<Transaction> list){
        this.context =context;
        this.transactionList = list;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return transactionList.size();
    }

    @Override
    public Object getItem(int position) {
        return transactionList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.fragment_transaction_item, parent, false);
        }
        // Retrieve the Transaction object for the current position
        Transaction transaction = transactionList.get(position);

        TextView cat = (TextView) convertView.findViewById(R.id.transactionCategory);
        TextView note = (TextView) convertView.findViewById(R.id.transactionNotes);
        TextView date = (TextView) convertView.findViewById(R.id.transactionDate);
        TextView amountText = (TextView) convertView.findViewById(R.id.amountText);

        // Set the text of TextViews with the corresponding data from the Transaction object
        cat.setText(transaction.getCategory());
        note.setText(transaction.getNote());
        date.setText(transaction.getDate());

        // Adjusting the amount text and color based on transaction type
        int amount = transaction.getAmount();
        if (transaction.getType().equals("Income")) {
            amountText.setText(String.format("+ $%d", amount));
            amountText.setTextColor(Color.GREEN);
        } else if (transaction.getType().equals("Expense")) {
            amountText.setText(String.format("- $%d", amount));
            amountText.setTextColor(Color.RED);
        }

        return convertView;
}

}