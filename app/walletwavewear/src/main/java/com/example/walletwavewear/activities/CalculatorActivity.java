package com.example.walletwavewear.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.walletwavewear.R;
import com.example.walletwavewear.databinding.ActivityCalculatorBinding;

public class CalculatorActivity extends AppCompatActivity {
    private ActivityCalculatorBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCalculatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up click listener for btnCalculateTip
        binding.btnCalculateTip.setOnClickListener(view -> {
            String billAmountStr = binding.edtBillAmount.getText().toString();
            String tipPercentageStr = binding.edtTipPercentage.getText().toString();
            String numPeopleStr = binding.edtNumPeople.getText().toString();

            if (!billAmountStr.isEmpty() && !tipPercentageStr.isEmpty() && !numPeopleStr.isEmpty()) {
                double billAmount = Double.parseDouble(billAmountStr);
                double tipPercentage = Double.parseDouble(tipPercentageStr);
                int numPeople = Integer.parseInt(numPeopleStr);

                double tipAmount = (billAmount * tipPercentage) / 100;
                double totalAmount = billAmount + tipAmount;
                double amountPerPerson = totalAmount / numPeople;

                binding.txtTipResult.setText(String.format("Tip Amount: $%.2f", tipAmount));
                binding.txtEachPersonPays.setText(String.format("Each Person Pays: $%.2f", amountPerPerson));
            } else {
                binding.txtTipResult.setText(R.string.please_enter_valid_inputs);
                binding.txtEachPersonPays.setText("");
            }
        });

    }
}
