package com.ca13b.blackdroid.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.ca13b.blackdroid.BlackstarAmp;
import com.ca13b.blackdroid.Control;
import com.ca13b.blackdroid.MainActivity;
import com.ca13b.blackdroid.R;

public class ControlsFragment extends Fragment {
    private BlackstarAmp amp;
    private ControlsViewModel controlsViewModel;
    private SeekBar sbVolume;
    private SeekBar sbGain;
    Control ctrlGain;
    Control ctrlVolume;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        controlsViewModel =
                ViewModelProviders.of(this).get(ControlsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_controls, container, false);
        initializeControls(root);

        amp = ((MainActivity)getActivity()).blackstarAmp;
        ctrlGain = amp.Controls.get(2);
        ctrlVolume = amp.Controls.get(3);

        //TODO: get current settings and set sliders accordingly!


        return root;
    }

    private void initializeControls(final View root) {
        sbGain = root.findViewById(R.id.gain_slider);
        sbGain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) { progress = progresValue;}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { amp.SetControlValue(ctrlGain, (progress*ctrlGain.maxValue)/100); }

        });

        sbVolume = root.findViewById(R.id.volume_slider);
        sbVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) { progress = progresValue;}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { amp.SetControlValue(ctrlVolume, (progress*ctrlGain.maxValue)/100); }
        });
    }
}
