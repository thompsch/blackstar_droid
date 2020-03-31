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

public class DelayPedal {
    private Spinner delay_type_list;
    private SeekBar sbDelayFeedback;
    private SeekBar sbDelayLevel;
    private SeekBar sbDelayTime;
    private ImageButton delayPowerSwitch;
    private View delayPowerLed;
    public Control ctrlDelayFeedback;
    public Control ctrlDelayLevel;
    public Control ctrlDelayTime;
    public Control ctrlDelayType;
    public Control ctrlDelayPower;
    boolean delayPowerOn;

    BlackstarAmp amp;

    public void InitializeControls(final Context context,
                                   final BlackstarAmp amp,
                                   final View root,
                                   final SeekBar.OnSeekBarChangeListener seekBarChanged) {

        this.amp = amp;
        this.delayPowerOn = false;

        ctrlDelayPower = amp.Controls.get(16);
        ctrlDelayType = amp.Controls.get(23);
        ctrlDelayFeedback = amp.Controls.get(24);
        ctrlDelayLevel = amp.Controls.get(26);
        ctrlDelayTime = amp.Controls.get(27);

        delay_type_list = root.findViewById(R.id.delay_type_list);
        delay_type_list.setOnItemSelectedListener(ddChange);
        sbDelayLevel = root.findViewById(R.id.delay_level_slider);
        sbDelayLevel.setOnSeekBarChangeListener(seekBarChanged);
        sbDelayFeedback = root.findViewById(R.id.delay_feedback_slider);
        sbDelayFeedback.setOnSeekBarChangeListener(seekBarChanged);
        sbDelayTime = root.findViewById(R.id.delay_time_slider);
        sbDelayTime.setOnSeekBarChangeListener(seekBarChanged);
        delayPowerLed = root.findViewById(R.id.delay_power_led);
        delayPowerSwitch = root.findViewById(R.id.delay_power_switch);
        delayPowerSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                delayPowerOn = !delayPowerOn;
                ctrlDelayPower.controlValue = delayPowerOn ? 1 : 0;

                if (delayPowerOn) {
                    delayPowerLed.setBackground(ContextCompat.getDrawable(context, R.drawable.led_on));
                } else {
                    delayPowerLed.setBackground(ContextCompat.getDrawable(context, R.drawable.led_off));
                }

                amp.SetControlValue(ctrlDelayPower, ctrlDelayPower.controlValue);
            }
        });

        getInitialValues(context);

    }


    private void getInitialValues(Context context){
        sbDelayLevel.setProgress(ctrlDelayLevel.controlValue);
        sbDelayFeedback.setProgress(ctrlDelayFeedback.controlValue);
        sbDelayTime.setProgress(ctrlDelayTime.controlValue);
        delay_type_list.setSelection(ctrlDelayType.controlValue);

        delayPowerOn = ctrlDelayPower.controlValue == 1;

        if (delayPowerOn) {
            delayPowerLed.setBackground(ContextCompat.getDrawable(context, R.drawable.led_on));
        } else {
            delayPowerLed.setBackground(ContextCompat.getDrawable(context, R.drawable.led_off));
        }
    }

    private AdapterView.OnItemSelectedListener ddChange = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            ctrlDelayType.controlValue = position;
            amp.SetControlValue(ctrlDelayType, position);

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };
}
