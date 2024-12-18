package com.example.walletwavewear.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.walletwavewear.R;
import com.example.walletwavewear.databinding.ActivityExchangeBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExchangeActivity extends AppCompatActivity {

    private ActivityExchangeBinding binding; // ViewBinding instance
    private RequestQueue requestQueue;
    private Map<String, String> countryToCurrencyMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize View Binding
        binding = ActivityExchangeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize the RequestQueue and map for currency
        requestQueue = Volley.newRequestQueue(this);
        countryToCurrencyMap = new HashMap<>();

        // Initialize the spinner and set its data
        initSpinner();
    }

    private void initSpinner() {
        String url = "https://restcountries.com/v3.1/all";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        List<String> countryList = new ArrayList<>();

                        for (int i = 0; i < response.length(); i++) {
                            JSONObject countryObject = response.getJSONObject(i);
                            String countryName = countryObject.getJSONObject("name").getString("common");

                            JSONObject currencies = countryObject.optJSONObject("currencies");
                            if (currencies != null) {
                                String currencyCode = currencies.keys().next();
                                countryList.add(countryName);
                                countryToCurrencyMap.put(countryName, currencyCode);
                            }
                        }

                        // Use View Binding to set the adapter
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_spinner_item, countryList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.spnCurrency.setAdapter(adapter);

                        binding.spnCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                String selectedCountry = (String) parent.getItemAtPosition(position);
                                String currencyCode = countryToCurrencyMap.get(selectedCountry);

                                if (currencyCode != null) {
                                    fetchExchangeRate(currencyCode);
                                } else {
                                    binding.txtExchangeRate.setText(getString(R.string.currency_not_available_for) + selectedCountry);
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                // No action needed
                            }
                        });
                    } catch (JSONException e) {
                        Log.e("JSON_ERROR", "Error parsing country data: " + e.getMessage());
                    }
                },
                error -> Log.e("API_ERROR", "Failed to fetch countries: " + error.getMessage()));

        requestQueue.add(jsonArrayRequest);
    }

    // Fetch exchange rate based on the selected currency
    private void fetchExchangeRate(String currencyCode) {
        String url = "https://api.exchangerate-api.com/v4/latest/CAD";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject rates = response.getJSONObject("rates");
                        if (rates.has(currencyCode)) {
                            double exchangeRate = rates.getDouble(currencyCode);
                            String formattedRate = String.format("Exchange Rate: %.2f %s", exchangeRate, currencyCode);
                            binding.txtExchangeRate.setText(formattedRate);
                        } else {
                            binding.txtExchangeRate.setText("Rate not available for " + currencyCode);
                        }
                    } catch (JSONException e) {
                        Log.e("JSON_ERROR", "Error parsing exchange rate: " + e.getMessage());
                    }
                },
                error -> Log.e("API_ERROR", "Failed to fetch exchange rate: " + error.getMessage()));

        requestQueue.add(jsonObjectRequest);
    }

    @Override
    // Cancel the request queue when the activity is destroyed
    protected void onDestroy() {
        super.onDestroy();
        if (requestQueue != null) {
            requestQueue.cancelAll(request -> true);
        }
    }
}
