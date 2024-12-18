package com.example.walletwave.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.shared.models.Transaction;
import com.example.walletwave.R;
import com.example.shared.daos.TransactionDaoImpl;
import com.example.walletwave.databinding.ActivityMainBinding;
import com.example.walletwave.databinding.FragmentHomeBinding;
import com.example.walletwave.fragments.AboutFragment;
import com.example.walletwave.fragments.DeleteFragment;
import com.example.walletwave.fragments.ExchangeFragment;
import com.example.walletwave.fragments.HomeFragment;
import com.example.walletwave.fragments.RateUsFragment;
import com.example.walletwave.fragments.SearchTransactionFragment;
import com.example.walletwave.fragments.TransactionFragment;
import com.example.shared.services.TransactionServices;
import com.example.shared.utilities.DBHelper;

import com.google.android.material.appbar.MaterialToolbar;

public class MainActivity extends AppCompatActivity{

    ActivityMainBinding mainBinding;
    private ActionBarDrawerToggle mToggle;
    private SharedPreferences shp;
    private DBHelper dbHelper;
    private TransactionServices transactionServices;
    private FragmentHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = mainBinding.getRoot();
        setContentView(view);

        // Set custom toolbar as action bar
        MaterialToolbar toolbar = findViewById(R.id.materialToolbar);
        setSupportActionBar(toolbar);
        setContentView(mainBinding.getRoot());
        // Initialize DBHelper, TransactionServices
        dbHelper = new DBHelper(this);
        transactionServices = new TransactionServices(new TransactionDaoImpl());
        setDefaultFragment();
        // Initialize UI elements
        init();
    }

    private void setDefaultFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame, new HomeFragment(transactionServices))
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    // Handle options menu item selection
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame, new SearchTransactionFragment(transactionServices))
                    .addToBackStack(null) // Add to back stack to handle back navigation
                    .commit();
            return true;
        }

        return super.onOptionsItemSelected(item) || mToggle.onOptionsItemSelected(item);
    }

    private void init() {
        setSupportActionBar(mainBinding.materialToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set up toggle for navigation drawer
        mToggle = new ActionBarDrawerToggle(this, mainBinding.drawerLayout, mainBinding.materialToolbar, R.string.navDrawerTextOpen, R.string.navDrawerTextClose);
        mainBinding.drawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        // Set up navigation view item selection listener
        mainBinding.navView.setNavigationItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_transaction) {
                fragment = new TransactionFragment(transactionServices);
            } else if (itemId == R.id.nav_about) {
                fragment = new AboutFragment();
            } else if (itemId == R.id.nav_home) {
                fragment = new HomeFragment(transactionServices);
            } else if (itemId == R.id.nav_exchange) {
                fragment = new ExchangeFragment();
            } else if (itemId == R.id.nav_rateus) {
                fragment = new RateUsFragment();
            } else if (itemId == R.id.nav_delete) {
                fragment = new DeleteFragment(transactionServices);
            }
            if (fragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame, fragment)
                        .commit();
                mainBinding.drawerLayout.closeDrawers();
                return true;
            }
            return false;
        });
    }


}