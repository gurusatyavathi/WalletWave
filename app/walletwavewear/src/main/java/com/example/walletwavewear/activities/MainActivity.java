package com.example.walletwavewear.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.walletwavewear.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {
    ActivityMainBinding mainBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //inflate layout by view binding
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = mainBinding.getRoot();
        setContentView(view);

        mainBinding.btnAddExpense.setOnClickListener(v -> {
            //intent to move to TransactionActivity
            Intent intent = new Intent(MainActivity.this, TransactionActivity.class);
            startActivity(intent);
        });

        mainBinding.btnCurrencyExchange.setOnClickListener(v -> {
            //intent to move to ExchangeActivity
            Intent intent = new Intent(MainActivity.this, ExchangeActivity.class);
            startActivity(intent);
        });

        mainBinding.btnCalculator.setOnClickListener(v -> {
            //intent to move to CalculatorActivity
            Intent intent = new Intent(MainActivity.this, CalculatorActivity.class);
            startActivity(intent);
        });

        mainBinding.btnViewTransaction.setOnClickListener(v -> {
            //intent to move to ViewTransactionActivity
            Intent intent = new Intent(MainActivity.this, ViewTransactionActivity.class);
            startActivity(intent);
        });
    }

}
