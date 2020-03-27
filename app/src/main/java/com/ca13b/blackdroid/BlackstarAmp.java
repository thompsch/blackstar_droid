package com.ca13b.blackdroid;

import android.content.Context;
import android.util.Log;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

public class BlackstarAmp implements Serializable {

    public static final int VendorId = 0x27d4;
    public Dictionary<Integer, Control> Controls;
    private static HashMap<Integer, String> ampModels;
    private static String tag = "BSD/UsbCommunicator";
    private static UsbCommunicator usbCommunicator;

    public BlackstarAmp(Context context) {
        usbCommunicator = new UsbCommunicator(context);

        ampModels = new HashMap<>();
        ampModels.put(0x0001, "id-tvp");
        ampModels.put(0x0010, "id-core");

        Log.i(tag, "Calling InitializeAmp now");
        InitializeAmp();

        Controls = new Hashtable<Integer, Control>(29) {};
        Controls.put(1, new Control("voice", 0x01, 0, 5));
        Controls.put(2, new Control("gain", 0x02, 0, 127));
        Controls.put(3, new Control("volume", 0x03, 0, 127));
        Controls.put(4, new Control("bass", 0x04, 0, 127));
        Controls.put(5, new Control("middle", 0x05, 0, 127));
        Controls.put(6, new Control("treble", 0x06, 0, 127));
        Controls.put(7, new Control("isf", 0x07, 0, 127));
        Controls.put(8, new Control("tvp_value", 0x08, 0, 5));
        Controls.put(11, new Control("resonance", 0x0b, 0, 127));
        Controls.put(12, new Control("presence", 0x0c, 0, 127));
        Controls.put(13, new Control("master_volume", 0x0d, 0, 127));
        Controls.put(14, new Control("tvp_switch", 0x0e, 0, 1));
        Controls.put(15, new Control("mod_switch", 0x0f, 0, 1));
        Controls.put(16, new Control("delay_switch", 0x10, 0, 1));
        Controls.put(17, new Control("reverb_switch", 0x11, 0, 1));
        Controls.put(18, new Control("mod_type", 0x12, 0, 3));
        Controls.put(19, new Control("mod_segval", 0x13, 0, 31));
        Controls.put(20, new Control("mod_manual", 0x14, 0, 127));
        Controls.put(21, new Control("mod_level", 0x15, 0, 127));
        Controls.put(22, new Control("mod_speed", 0x16, 0, 127));
        Controls.put(23, new Control("delay_type", 0x17, 0, 3));
        Controls.put(24, new Control("delay_feedback", 0x18, 0, 31));
        Controls.put(26, new Control("delay_level", 0x1a, 0, 127));
        Controls.put(27, new Control("delay_time", 0x1b, 100, 2000));
        Controls.put(28, new Control("delay_time_coarse", 0x1c, 0, 7));
        Controls.put(29, new Control("reverb_type", 0x1d, 0, 3));
        Controls.put(30, new Control("reverb_size", 0x1e, 0, 31));
        Controls.put(32, new Control("reverb_level", 0x20, 0, 127));
        Controls.put(36, new Control("fx_focus", 0x24, 1, 3));//01 is Mod, 02 is delay, 03 is reverb.

    }

    public void InitializeAmp() {

        byte[] startupPacket = new byte[64];
        startupPacket[0] = (byte)0x81;
        startupPacket[3] = (byte)0x04;
        startupPacket[4] = (byte)0x03;
        startupPacket[5] = (byte)0x06;
        startupPacket[6] = (byte)0x02;
        startupPacket[7] = (byte)0x7a;
        usbCommunicator.SendData(startupPacket);
        Log.i(tag, "I've initialized the amp, yo: " + startupPacket.toString());

        /*data = [0x00] * 64
        data[0] = 0x81
        data[3:8] = [0x04, 0x03, 0x06, 0x02, 0x7a]*/
    }

    public List<String> GetAllPresets() {
        for (int i=0; 1<12; i++) {
            GetPresetName(i);
        }
    }

    public void GetPresetName(int preset) {
        byte[] data = new byte[64];
        data[0] = 0x02;
        data[1] = 0x04;
        data[2] = (byte) preset;
        data[3] = 0x00;
        //ByteBuffer result = usbCommunicator.SendData(data);
        /*if (result.get(0) != 0x02 && result.get(1) != 0x04) {
            presetsFragment.LogError( "I tried to get a preset name but got catshit.");
            return "Unknown";
        }
        StringBuilder sb = new StringBuilder(64);
        result.position(4);
        while (result.hasRemaining()) {
            int value = result.getInt();
            sb.append(Utils.intToASCII(value));
        }

        return sb.toString();*/

    }

    public void GetPresetValue(int preset) {
        byte[] data = new byte[64];
        data[0] = 0x02;
        data[1] = 0x05;
        data[2] = (byte) preset;
        data[3] = 0x00;
       // ByteBuffer result =  usbCommunicator.SendData(data);

        /*if (result.get(0) != 0x02 && result.get(1) != 0x05) {
          //  presetsFragment.LogError( "I tried to get a preset value but got horseshit.");
           // return -1;
        }*/

        /*while (result.position()<63) {
            presetsFragment.LogInfo( result.position() + " presetValue byte: " + result.get());
        }*/
       /* presetsFragment.LogInfo( "Preset number is " + result.get(2));
        presetsFragment.LogInfo( "Preset value packet poisiton is " + presetValuePacket.get(preset));
        presetsFragment.LogInfo( "Returning " + result.get(presetValuePacket.get(preset)));*/
       // return (int)result.get(presetValuePacket.get(preset));
    }

    public void SelectPreset(int preset) {
        byte[] data = new byte[64];
        data[0] = 0x02;
        data[1] = 0x01;
        data[2] = (byte) preset;
        data[3] = 0x00;
        //usbCommunicator.SendData(data);
    }

    public void SetControlValue(Control control, Integer value) {

        if (value < control.minValue) {
            //TODO: log warning
            value = control.minValue;
        } else if (value > control.maxValue) {
            //TODO: log warning
            value = control.maxValue;
        }

        byte[] data = new byte[64];

        if (control.controlName == "delay_time") {
            data[0] = 0x03;
            data[1] = control.controlId.byteValue();
            data[2] =  0x00;
            data[3] = 0x02;
            data[4] = (byte)(value % 256);
            data[5] = value.byteValue();
        } else {
            data[0] = 0x03;
            data[1] = control.controlId.byteValue();
            data[2] =  0x00;
            data[3] = 0x01;
            data[4] = value.byteValue();
        }

        usbCommunicator.SendData(data);
    }

    public static void HandlePresetNameResponse(ByteBuffer packet) {

         byte presetId = packet.get(2);
         StringBuilder sbPresetName = new StringBuilder(21);
         for (int i=4; i<26; i++){
             sbPresetName.append((char)packet.get(i));
         }

         //presetsFragment.LogInfo( String.format("Preset {0} is named {1}", presetId, sbPresetName.toString()));
    }

    public static void HandleControlValueResponse(byte controlId, byte controlValue){
        Log.i(tag, String.format("Control %d has a value of %d", controlId, controlValue));
    }

}
