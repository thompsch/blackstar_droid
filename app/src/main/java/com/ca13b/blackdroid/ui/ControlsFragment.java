package com.ca13b.blackdroid.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

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
    private SeekBar sbBass;
    private SeekBar sbMid;
    private SeekBar sbTreble;
    private Spinner voices;
    private SeekBar sbIsf;
    Control ctrlGain;
    Control ctrlVolume;
    Control ctrlBass;
    Control ctrlMid;
    Control ctrlTreble;
    Control ctrlVoice;
    Control ctrlIsf;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        controlsViewModel =
                ViewModelProviders.of(this).get(ControlsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_controls, container, false);
        initializeControls(root);

        amp = ((MainActivity)getActivity()).blackstarAmp;
        ctrlVoice = amp.Controls.get(1);
        ctrlGain = amp.Controls.get(2);
        ctrlVolume = amp.Controls.get(3);
        ctrlBass = amp.Controls.get(4);
        ctrlMid = amp.Controls.get(5);
        ctrlTreble = amp.Controls.get(6);
        ctrlIsf = amp.Controls.get(7);


        //TODO: get current settings and set sliders accordingly!


        return root;
    }

    private void initializeControls(final View root) {
        sbGain = root.findViewById(R.id.gain_slider);
        sbGain.setOnSeekBarChangeListener(seekBarChanged);
        sbVolume = root.findViewById(R.id.volume_slider);
        sbVolume.setOnSeekBarChangeListener(seekBarChanged);
        sbBass = root.findViewById(R.id.bass_slider);
        sbBass.setOnSeekBarChangeListener(seekBarChanged);
        sbMid = root.findViewById(R.id.middle_slider);
        sbMid.setOnSeekBarChangeListener(seekBarChanged);
        sbTreble = root.findViewById(R.id.treble_slider);
        sbTreble.setOnSeekBarChangeListener(seekBarChanged);
        voices = root.findViewById(R.id.voice_list);
        voices.setOnItemSelectedListener(ddChange);
        sbIsf = root.findViewById(R.id.isf_slider);
        sbIsf.setOnSeekBarChangeListener(seekBarChanged);
    }

    private AdapterView.OnItemSelectedListener ddChange = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            amp.SetControlValue(ctrlVoice, position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };

    private SeekBar.OnSeekBarChangeListener seekBarChanged = new SeekBar.OnSeekBarChangeListener() {
        int progress = 0;

        @Override
        public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) { progress = progresValue;}

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) { }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            Control ctrlTemp = null;

            switch (seekBar.getId()){
                case R.id.gain_slider: {
                    ctrlTemp = ctrlGain;
                    break;
                }
                case R.id.volume_slider: {
                    ctrlTemp = ctrlVolume;
                    break;
                }
                case R.id.bass_slider: {
                    ctrlTemp = ctrlBass;
                    break;
                }
                case R.id.middle_slider: {
                    ctrlTemp = ctrlMid;
                    break;
                }
                case R.id.treble_slider: {
                    ctrlTemp = ctrlTreble;
                    break;
                }
                case R.id.isf_slider: {
                    ctrlTemp = ctrlIsf;
                    break;
                }
            }
            if (ctrlTemp == null) return;
            amp.SetControlValue(ctrlTemp, (progress*ctrlTemp.maxValue)/100);
            Toast.makeText(getContext(), String.format("I set %s to %d", ctrlTemp.controlName, progress), Toast.LENGTH_LONG).show();

        }
    };
}
