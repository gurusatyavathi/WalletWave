package com.example.walletwave.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.walletwave.R;
import com.example.walletwave.databinding.FragmentDeleteBinding;
import com.example.walletwave.databinding.FragmentExchangeBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ExchangeFragment extends Fragment implements AdapterView.OnItemClickListener {

    FragmentExchangeBinding fragmentExchangeBinding;
    String country;
    private RequestQueue requestQueue;

    public ExchangeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        fragmentExchangeBinding = FragmentExchangeBinding.inflate(inflater, container, false);

        if (getActivity() != null && isAdded()) {
            View fab = getActivity().findViewById(R.id.fab);
            if (fab != null) {
                // Make the FloatingActionButton invisible
                fab.setVisibility(View.INVISIBLE);
            }
        }

        initSpinner();
        return fragmentExchangeBinding.getRoot();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Volley request queue
        requestQueue = Volley.newRequestQueue(requireContext());
    }


    private void initSpinner() {
        String url = "https://restcountries.com/v3.1/all";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // Lists to hold country names and map them to currency codes and flag URLs
                        List<String> countryList = new ArrayList<>();
                        Map<String, String> countryToCurrencyMap = new HashMap<>();
                        Map<String, String> countryToFlagMap = new HashMap<>();

                        // Parse the JSON array
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject countryObject = response.getJSONObject(i);

                            // Extract country name
                            String countryName = countryObject.getJSONObject("name").getString("common");

                            // Extract currency code (if available)
                            JSONObject currencies = countryObject.optJSONObject("currencies");
                            if (currencies != null && currencies.keys().hasNext()) {
                                String currencyCode = currencies.keys().next();
                                countryList.add(countryName);
                                countryToCurrencyMap.put(countryName, currencyCode);
                            }

                            // Extract flag URL
                            String flagUrl = countryObject.getJSONObject("flags").getString("png");
                            countryToFlagMap.put(countryName, flagUrl);
                        }

                        countryList.sort(String::compareToIgnoreCase);

                        // Set the adapter for the Spinner
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                                android.R.layout.simple_spinner_item, countryList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        fragmentExchangeBinding.spnCurrency.setAdapter(adapter);

                        // Handle item selection in the Spinner
                        fragmentExchangeBinding.spnCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                String selectedCountry = parent.getItemAtPosition(position).toString();
                                String currencyCode = countryToCurrencyMap.get(selectedCountry);
                                String flagUrl = countryToFlagMap.get(selectedCountry);

                                // Fetch exchange rate for the selected currency
                                if (currencyCode != null) {
                                    fetchExchangeRate(currencyCode);
                                } else {
                                    fragmentExchangeBinding.txtExchangeRate.setText("Currency not available for " + selectedCountry);
                                }

                                // Load the flag into the ImageView
                                if (flagUrl != null) {
                                    loadFlag(flagUrl);
                                } else {
                                    fragmentExchangeBinding.countryFlag.setImageResource(R.drawable.default_flag); // Use a placeholder if flag URL is missing
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                // Do nothing
                            }
                        });

                    } catch (JSONException e) {
                        Log.e("JSON_ERROR", "Error parsing country data: " + e.getMessage());
                    }
                },
                error -> Log.e("API_ERROR", "Failed to fetch countries: " + error.getMessage()));

        // Add the request to the Volley request queue
        requestQueue.add(jsonArrayRequest);
    }

    private void loadFlag(String flagUrl) {
        Glide.with(this)
                .load(flagUrl)
                .placeholder(R.drawable.default_flag) // Optional placeholder while loading
                .error(R.drawable.error_flag) // Optional error placeholder
                .into(fragmentExchangeBinding.countryFlag);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    private void fetchExchangeRate(String currencyCode) {
        // Example API for exchange rate (replace with actual API)
        String url = "https://api.exchangerate-api.com/v4/latest/CAD";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // Parse exchange rates
                        JSONObject rates = response.getJSONObject("rates");
                        if (rates.has(currencyCode)) {
                            double exchangeRate = rates.getDouble(currencyCode);
                            String formattedRate = String.format("%.2f", exchangeRate);

                            // Display the exchange rate
                            fragmentExchangeBinding.txtExchangeRate.setText("Exchange Rate: " + formattedRate + currencyCode);
                        } else {
                            fragmentExchangeBinding.txtExchangeRate.setText("Rate not available for " + currencyCode);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.e("API_ERROR", "Failed to fetch exchange rate: " + error.getMessage()));

        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (requestQueue != null) {
            requestQueue.cancelAll(request -> true);
        }
        fragmentExchangeBinding = null;
    }
}