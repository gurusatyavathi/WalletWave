package com.example.walletwave.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.walletwave.R;
import com.example.walletwave.databinding.FragmentRateusBinding;

import java.util.ArrayList;
import java.util.List;

public class RateUsFragment extends Fragment {

    private FragmentRateusBinding fragmentRateusBinding = null;

    private List<String> ratingsList;
    private ArrayAdapter<String> adapter;


    public RateUsFragment() {
        //empty constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentRateusBinding = FragmentRateusBinding.inflate(inflater, container, false);

        RatingBar ratingBar = fragmentRateusBinding.ratingBar;
        EditText commentsEditText = fragmentRateusBinding.commentsEditText;
        Button submitBtn = fragmentRateusBinding.submitBtn;
        ListView listView = fragmentRateusBinding.listViewRate;
        TextView submittedRatingText = fragmentRateusBinding.submittedRatingText;
        if (getActivity() != null && isAdded()) {
            View fab = getActivity().findViewById(R.id.fab);
            if (fab != null) {
                // Make the FloatingActionButton invisible
                fab.setVisibility(View.INVISIBLE);
            }
        }

        //initialize ratings list
        ratingsList = new ArrayList<>();

        //initialize adapter for the ListView
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, ratingsList);
        listView.setAdapter(adapter);


        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

            }
        });
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submittedRatingText.setVisibility(View.VISIBLE);
                //get comments from EditText
                String comment = commentsEditText.getText().toString().trim();
                if (comment.isEmpty()) {
                    comment = "N/A";
                }
                //add rating with comment to the list
                ratingsList.add("Rating: " + ratingBar.getRating() + ", Comments: " + comment);
                //notify adapter
                adapter.notifyDataSetChanged();
                //clear fields after submission
                ratingBar.setRating(0);
                commentsEditText.setText("");

            }
        });

        return fragmentRateusBinding.getRoot();

    }
}
