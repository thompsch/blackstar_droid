package com.ca13b.blackdroid.ui.Pedals;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;

import androidx.core.content.ContextCompat;

import com.ca13b.blackdroid.BlackstarAmp;
import com.ca13b.blackdroid.Control;
import com.ca13b.blackdroid.R;

public class ReverbPedal {

    private Spinner reverb_type_list;
    private SeekBar sbReverbSize;
    private SeekBar sbReverbLevel;
    private ImageButton reverbPowerSwitch;
    private View reverbPowerLed;
    public Control ctrlReverbSize;
    public Control ctrlReverbLevel;
    public Control ctrlReverbType;
    public Control ctrlReverbPower;
    boolean reverbPowerOn;
    BlackstarAmp amp;

    public void InitializeControls(final Context context,
                                   final BlackstarAmp amp,
                                   final View root,
                                   final SeekBar.OnSeekBarChangeListener seekBarChanged) {

        this.amp = amp;

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

                reverbPowerOn = !reverbPowerOn;
                ctrlReverbPower.controlValue = reverbPowerOn ? 1 : 0;

                if (reverbPowerOn) {
                    reverbPowerLed.setBackground(ContextCompat.getDrawable(context, R.drawable.led_on));
                } else {
                    reverbPowerLed.setBackground(ContextCompat.getDrawable(context, R.drawable.led_off));
                }

                amp.SetControlValue(ctrlReverbPower, ctrlReverbPower.controlValue);
            }
        });
        getInitialValues(context);
    }

    private void getInitialValues(Context context){
        sbReverbLevel.setProgress(ctrlReverbLevel.controlValue);
        sbReverbSize.setProgress(ctrlReverbSize.controlValue);
        reverb_type_list.setSelection(ctrlReverbType.controlValue);

        reverbPowerOn = ctrlReverbPower.controlValue == 1;
        if (reverbPowerOn){
            reverbPowerLed.setBackground(ContextCompat.getDrawable(context, R.drawable.led_on));
        } else {
            reverbPowerLed.setBackground(ContextCompat.getDrawable(context, R.drawable.led_off));
        }
    }

    private AdapterView.OnItemSelectedListener ddChange = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            ctrlReverbType.controlValue = position;
            amp.SetControlValue(ctrlReverbType, position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };
}
