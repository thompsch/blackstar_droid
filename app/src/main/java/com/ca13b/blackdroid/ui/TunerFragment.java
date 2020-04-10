package com.ca13b.blackdroid.ui;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.ca13b.blackdroid.BlackstarAmp;
import com.ca13b.blackdroid.MainActivity;
import com.ca13b.blackdroid.R;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

public class TunerFragment extends Fragment {

    private BlackstarAmp amp;
    private TunerViewModel tunerViewModel;

    static View viewFlat1;
    static View viewFlat2;
    static View viewFlat3;
    static View viewFlat4;
    static View viewFlat5;
    static View viewIntune;
    static View viewSharp1;
    static View viewSharp2;
    static View viewSharp3;
    static View viewSharp4;
    static View viewSharp5;
    static TextView tvNote;

    private static HashMap<Byte, String> byteToNote = new HashMap<Byte, String>()
    {{
        put((byte)0x01, "E");
        put((byte)0x02, "F");
        put((byte)0x03, "F#");
        put((byte)0x04, "G");
        put((byte)0x05, "Ab");
        put((byte)0x06, "A");
        put((byte)0x07, "Bb");
        put((byte)0x08, "B");
        put((byte)0x09, "C");
        put((byte)0x0A, "C#");
        put((byte)0x0B, "D");
        put((byte)0x0C, "Eb");
    }};

    private static ArrayList<View> allViews;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        tunerViewModel =
                ViewModelProviders.of(this).get(TunerViewModel.class);
        View root = inflater.inflate(R.layout.fragment_tuner, container, false);
        final TextView textView = root.findViewById(R.id.text_note);
        tunerViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        amp = MainActivity.blackstarAmp;

        viewFlat1 = root.findViewById(R.id.box_flat_1);
        viewFlat2 = root.findViewById(R.id.box_flat_2);
        viewFlat3 = root.findViewById(R.id.box_flat_3);
        viewFlat4 = root.findViewById(R.id.box_flat_4);
        viewFlat5 = root.findViewById(R.id.box_flat_5);
        viewIntune = root.findViewById(R.id.box_intune);
        viewSharp1 = root.findViewById(R.id.box_sharp_1);
        viewSharp2 = root.findViewById(R.id.box_sharp_2);
        viewSharp3 = root.findViewById(R.id.box_sharp_3);
        viewSharp4 = root.findViewById(R.id.box_sharp_4);
        viewSharp5 = root.findViewById(R.id.box_sharp_5);

        tvNote = root.findViewById(R.id.text_note);

        allViews = new ArrayList<View>(){{
            add(viewIntune);
            add(viewFlat1);
            add(viewFlat2);
            add(viewFlat3);
            add(viewFlat4);
            add(viewFlat5);
            add(viewSharp1);
            add(viewSharp2);
            add(viewSharp3);
            add(viewSharp4);
            add(viewSharp5);
        }};

        amp.SwitchTunerMode(true);

        ((MainActivity)getActivity()).setFragmentRefreshListener(new MainActivity.FragmentRefreshListener() {
            @Override
            public void onRefresh(ByteBuffer buffer) {
                SetTunerUI(buffer);
            }
        });



        return root;
    }


    private void SetTunerUI(ByteBuffer packet) {
/*
            # In this case, the amp is in tuner mode and this data is
            # tuning data. It has the form 09 NN PP ...  If there is
            # no note, nn and pp are 00.  Otherwise nn indicates the
            # note w/in the scale from E == 01 to Eb == 0C, and pp
            # indicates the variance in pitch (based on A440 tuning),
            # from 0 (very flat) to 63 (very sharp), i.e, 0-99
            # decimal.  So, standard tuning is:
            # E  01 32 (same for low and high E strings)
            # A  06 32
            # D  0B 32
            # G  04 32
            # B  08 32
        note = self.tuner_note[packet[1]]
        delta = 50 - packet[2]*/

        if (packet.get(0) != 0x09){
            Log.e("BSD/TunerFragment", "Did not receive valid tuner info.");
            return;
        }

        byte noteAsByte = packet.get(1);
        int pitch_variance = packet.get(2);

        Log.i("BSD/TunerFragment", String.format("Tuner note %s pitch %s", noteAsByte, pitch_variance));

        String note = byteToNote.getOrDefault(noteAsByte, "--");

        tvNote.setText(note);

        clearAll();

        if (isBetween(pitch_variance, 0, 10)){
            viewFlat5.setBackgroundResource(R.color.tuner_0);
        }
        else if (isBetween(pitch_variance, 10, 20)){
            viewFlat4.setBackgroundResource(R.color.tuner_10);
        }
        else if (isBetween(pitch_variance, 20, 30)){
            viewFlat3.setBackgroundResource(R.color.tuner_20);
        }
        else if (isBetween(pitch_variance, 30, 40)){
            viewFlat2.setBackgroundResource(R.color.tuner_30);
        }
        else if (isBetween(pitch_variance, 40, 45)){
            viewIntune.setBackgroundResource(R.color.tuner_40);
        }
        else if (isBetween(pitch_variance, 45, 55)){
            viewIntune.setBackgroundResource(R.color.tuner_50);
        }
        else if (isBetween(pitch_variance, 55, 60)){
            viewIntune.setBackgroundResource(R.color.tuner_40);
        }
        else if (isBetween(pitch_variance, 60, 70)){
            viewSharp2.setBackgroundResource(R.color.tuner_30);
        }
        else if (isBetween(pitch_variance, 70, 80)){
            viewSharp3.setBackgroundResource(R.color.tuner_20);
        }
        else if (isBetween(pitch_variance, 80, 90)){
            viewSharp4.setBackgroundResource(R.color.tuner_10);
        }
        else if (isBetween(pitch_variance, 90, 100)){
            viewSharp5.setBackgroundResource(R.color.tuner_0);
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                clearAll();
            }
        }, 500);

    }

    private void clearAll(){
        for (View v: allViews) {
            if (v==null){
                Log.e("BSD/TunerFragment", "View is null");
                continue;
            }
            v.setBackgroundColor(Color.WHITE);
        }
    }

    private static boolean isBetween(int value, int min, int max) {
        return (value <= max && value >= min);
    }

    @Override
    public void onDestroy() {
        amp.SwitchTunerMode(false);
        super.onDestroy();
    }
}
