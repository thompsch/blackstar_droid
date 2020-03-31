package com.ca13b.blackdroid.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.ca13b.blackdroid.BlackstarAmp;
import com.ca13b.blackdroid.Control;
import com.ca13b.blackdroid.MainActivity;
import com.ca13b.blackdroid.R;

public class EffectsFragment extends Fragment {

    private EffectsViewModel effectsViewModel;
    private BlackstarAmp amp;
    private Spinner reverb_type_list;
    private SeekBar sbReverbSize;
    private SeekBar sbReverbLevel;
    private ImageButton reverbPowerSwitch;
    private View reverbPowerLed;

    Control ctrlReverbSize;
    Control ctrlReverbLevel;
    Control ctrlReverbType;
    Control ctrlReverbPower;

    boolean powerOn;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        effectsViewModel =
                ViewModelProviders.of(this).get(EffectsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_effects, container, false);

        initializeControls(root);

        return root;
    }


    private void initializeControls(final View root) {
        amp = ((MainActivity)getActivity()).blackstarAmp;
        ctrlReverbLevel = amp.Controls.get(32);
        ctrlReverbSize = amp.Controls.get(30);
        ctrlReverbType = amp.Controls.get(29);
        ctrlReverbPower = amp.Controls.get(17);

        reverb_type_list = root.findViewById(R.id.reverb_type_list);
        reverb_type_list.setOnItemSelectedListener(ddChange);
        sbReverbLevel = root.findViewById(R.id.reverb_level_slider);
        sbReverbLevel.setOnSeekBarChangeListener(seekBarChanged);
        sbReverbSize = root.findViewById(R.id.reverb_size_slider);
        sbReverbSize.setOnSeekBarChangeListener(seekBarChanged);
        reverbPowerLed = root.findViewById(R.id.reverb_power_led);
        reverbPowerSwitch = root.findViewById(R.id.reverb_power_switch);
        reverbPowerSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                powerOn = !powerOn;
                ctrlReverbPower.controlValue = powerOn ? 1 : 0;

                if (powerOn){
                    reverbPowerLed.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.led_on));
                } else {
                    reverbPowerLed.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.led_off));
                }

                amp.SetControlValue(ctrlReverbPower,ctrlReverbPower.controlValue);
            }
        });
        getInitialValues();
    }

    private void getInitialValues(){
        sbReverbLevel.setProgress(ctrlReverbLevel.controlValue);
        sbReverbSize.setProgress(ctrlReverbSize.controlValue);
        reverb_type_list.setSelection(ctrlReverbType.controlValue);

        powerOn = ctrlReverbPower.controlValue == 1;
        if (powerOn){
            reverbPowerLed.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.led_on));
        } else {
            reverbPowerLed.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.led_off));
        }
    }

    private AdapterView.OnItemSelectedListener ddChange = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            amp.SetControlValue(ctrlReverbType, position);
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
                case R.id.reverb_size_slider: {
                    ctrlTemp = ctrlReverbSize;
                    break;
                }
                case R.id.reverb_level_slider: {
                    ctrlTemp = ctrlReverbLevel;
                    break;
                }
            }
            if (ctrlTemp == null) return;
            ctrlTemp.controlValue = progress;
            amp.SetControlValue(ctrlTemp, (progress*ctrlTemp.maxValue)/100);
           // Toast.makeText(getContext(), String.format("I set %s to %d", ctrlTemp.controlName, progress), Toast.LENGTH_LONG).show();
        }
    };
}
