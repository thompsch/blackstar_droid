package com.ca13b.blackdroid.ui.Pedals;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.ca13b.blackdroid.BlackstarAmp;
import com.ca13b.blackdroid.Control;
import com.ca13b.blackdroid.R;

public class ModPedal {

    private Spinner mod_type_list;
    private SeekBar sbModSeqVal;
    private SeekBar sbModDepth;
    private SeekBar sbModSpeed;
    private SeekBar sbModManual;

    private ImageButton modPowerSwitch;
    private View modPowerLed;
    public Control ctrlModType;
    public Control ctrlModSeqVal;
    public Control ctrlModDepth;
    public Control ctrlModSpeed;
    public Control ctrlModManual;
    public Control ctrlModPower;

    public TextView seqval_label;
    public TextView manual_label;
    public TextView pedal_label;

    boolean modPowerOn;
    BlackstarAmp amp;

    public void InitializeControls(final Context context,
                                         final BlackstarAmp amp,
                                         final View root,
                                         final SeekBar.OnSeekBarChangeListener seekBarChanged) {

        this.amp = amp;
        this.modPowerOn = false;

        ctrlModType = amp.Controls.get(18);
        ctrlModDepth = amp.Controls.get(21);
        ctrlModSeqVal = amp.Controls.get(19);
        ctrlModSpeed = amp.Controls.get(22);
        ctrlModManual = amp.Controls.get(20);
        ctrlModPower = amp.Controls.get(15);

        seqval_label = root.findViewById(R.id.seqval_label);
        pedal_label = root.findViewById(R.id.pedal_label);
        manual_label = root.findViewById(R.id.manual_label);

        mod_type_list = root.findViewById(R.id.mod_type_list);
        mod_type_list.setOnItemSelectedListener(ddChange);

        sbModSeqVal = root.findViewById(R.id.mod_seqval_slider);
        sbModSeqVal.setOnSeekBarChangeListener(seekBarChanged);

        sbModDepth = root.findViewById(R.id.mod_depth_slider);
        sbModDepth.setOnSeekBarChangeListener(seekBarChanged);

        sbModSpeed = root.findViewById(R.id.mod_speed_slider);
        sbModSpeed.setOnSeekBarChangeListener(seekBarChanged);

        sbModManual = root.findViewById(R.id.mod_manual_slider);
        sbModManual.setOnSeekBarChangeListener(seekBarChanged);

        modPowerLed = root.findViewById(R.id.mod_power_led);

        modPowerSwitch = root.findViewById(R.id.mod_power_switch);
        modPowerSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                modPowerOn = !modPowerOn;
                ctrlModPower.controlValue = modPowerOn ? 1 : 0;

                if (modPowerOn) {
                    modPowerLed.setBackground(ContextCompat.getDrawable(context, R.drawable.led_on));
                } else {
                    modPowerLed.setBackground(ContextCompat.getDrawable(context, R.drawable.led_off));
                }

                amp.SetControlValue(ctrlModPower, ctrlModPower.controlValue);
            }
        });
        getInitialValues(context);
    }

    private void getInitialValues(Context context){
        sbModDepth.setProgress(ctrlModDepth.controlValue);
        sbModSeqVal.setProgress(ctrlModSeqVal.controlValue);
        sbModManual.setProgress(ctrlModManual.controlValue);
        sbModSpeed.setProgress(ctrlModSpeed.controlValue);
        mod_type_list.setSelection(ctrlModType.controlValue);

        modPowerOn = ctrlModPower.controlValue == 1;
        if (modPowerOn){
            modPowerLed.setBackground(ContextCompat.getDrawable(context, R.drawable.led_on));
        } else {
            modPowerLed.setBackground(ContextCompat.getDrawable(context, R.drawable.led_off));
        }
    }

    private AdapterView.OnItemSelectedListener ddChange = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            //ctrlModType.controlValue = position;
            
            switch (ctrlModType.controlValue){
                case 0:
                    pedal_label.setText("PHASER");
                    seqval_label.setText("MIX");
                    manual_label.setVisibility(View.INVISIBLE);
                    sbModManual.setVisibility(View.INVISIBLE);
                    break;
                case 1:
                    pedal_label.setText("FLANGER");
                    seqval_label.setText("FEEDBACK");
                    manual_label.setVisibility(View.VISIBLE);
                    sbModManual.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    pedal_label.setText("CHORUS");
                    seqval_label.setText("MIX");
                    manual_label.setVisibility(View.INVISIBLE);
                    sbModManual.setVisibility(View.INVISIBLE);
                    break;
                case 3:
                    pedal_label.setText("TREMOLO");
                    seqval_label.setText("PITCH");
                    manual_label.setVisibility(View.INVISIBLE);
                    sbModManual.setVisibility(View.INVISIBLE);
                    break;
            }

            amp.SetControlValue(ctrlModType, position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };
}
