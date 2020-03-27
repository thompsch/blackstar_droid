package com.ca13b.blackdroid.ui;



import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.ca13b.blackdroid.R;

public class TunerFragment extends Fragment {

    private TunerViewModel tunerViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        tunerViewModel =
                ViewModelProviders.of(this).get(TunerViewModel.class);
        View root = inflater.inflate(R.layout.fragment_tuner, container, false);
        final TextView textView = root.findViewById(R.id.text_tuner);
        tunerViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}
