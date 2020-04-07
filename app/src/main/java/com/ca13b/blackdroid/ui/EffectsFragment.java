package com.ca13b.blackdroid.ui;

import android.os.Bundle;
import android.os.StrictMode;
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
import com.ca13b.blackdroid.ui.Pedals.DelayPedal;
import com.ca13b.blackdroid.ui.Pedals.ModPedal;
import com.ca13b.blackdroid.ui.Pedals.ReverbPedal;

public class EffectsFragment extends Fragment {

    private EffectsViewModel effectsViewModel;
    private BlackstarAmp amp;
    private ReverbPedal reverbPedal = new ReverbPedal();
    private DelayPedal delayPedal = new DelayPedal();
    private ModPedal modPedal = new ModPedal();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        amp = new BlackstarAmp(getContext());

        effectsViewModel =
                ViewModelProviders.of(this).get(EffectsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_effects, container, false);

        reverbPedal.InitializeControls(getContext(), amp, root.findViewById(R.id.reverb_pedal), seekBarChanged);
        delayPedal.InitializeControls(getContext(), amp, root.findViewById(R.id.delay_pedal), seekBarChanged);
        modPedal.InitializeControls(getContext(), amp, root.findViewById(R.id.mod_pedal), seekBarChanged);
        return root;
    }

    public SeekBar.OnSeekBarChangeListener seekBarChanged = new SeekBar.OnSeekBarChangeListener() {
        int progress = 0;

        @Override
        public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
            progress = progresValue;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) { }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            Control ctrlTemp = null;

            switch (seekBar.getId()){
                case R.id.reverb_size_slider: {
                    ctrlTemp = reverbPedal.ctrlReverbSize;
                    break;
                }
                case R.id.reverb_level_slider: {
                    ctrlTemp = reverbPedal.ctrlReverbLevel;
                    break;
                }
                case R.id.delay_feedback_slider: {
                    ctrlTemp = delayPedal.ctrlDelayFeedback;
                    break;
                }
                case R.id.delay_level_slider: {
                    ctrlTemp = delayPedal.ctrlDelayLevel;
                    break;
                }
                case R.id.delay_time_slider: {
                    ctrlTemp = delayPedal.ctrlDelayTime;
                    break;
                }
                case R.id.mod_depth_slider: {
                    ctrlTemp = modPedal.ctrlModDepth;
                    break;
                }
                case R.id.mod_seqval_slider: {
                    ctrlTemp = modPedal.ctrlModSeqVal;
                    break;
                }
                case R.id.mod_manual_slider: {
                    ctrlTemp = modPedal.ctrlModManual;
                    break;
                }
                case R.id.mod_speed_slider: {
                    ctrlTemp = modPedal.ctrlModSpeed;
                    break;
                }
            }
            if (ctrlTemp == null) return;
            ctrlTemp.controlValue = progress;
            amp.SetControlValue(ctrlTemp, (progress*ctrlTemp.maxValue)/100);
        }
    };

    @Override
    public void onDestroy() {
        amp = null;
        reverbPedal = null;
        modPedal = null;
        delayPedal = null;
        super.onDestroy();
    }
}
