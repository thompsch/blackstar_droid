package com.ca13b.blackdroid;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.ca13b.blackdroid.ui.TunerFragment;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;

public class BlackstarAmp implements Serializable {

    public static final int VendorId = 0x27d4;
    public Dictionary<Integer, Control> Controls;
    private static HashMap<Integer, String> ampModels;
    private static String tag = "BSD/UsbCommunicator";
    private static UsbCommunicator usbCommunicator;
    public boolean isInitialized;
    public int retryCount;
    public final int RETRY_MAX = 5;
    private Context _context;

    public BlackstarAmp(Context context) {

        usbCommunicator = new UsbCommunicator(context,this);
        _context = context;

        retryCount = 0;

        ampModels = new HashMap<>();
        ampModels.put(0x0001, "id-tvp");
        ampModels.put(0x0010, "id-core");

        Controls = new Hashtable<Integer, Control>(309) {};
        Controls.put(1, new Control("voice", 0x01, 0, 5));
        Controls.put(2, new Control("gain", 0x02, 0, 127));
        Controls.put(3, new Control("volume", 0x03, 0, 127));
        Controls.put(4, new Control("bass", 0x04, 0, 127));
        Controls.put(5, new Control("middle", 0x05, 0, 127));
        Controls.put(6, new Control("treble", 0x06, 0, 127));
        Controls.put(7, new Control("isf", 0x07, 0, 127));
        Controls.put(8, new Control("tvp_value", 0x08, 0, 5));
        Controls.put(10, new Control("mod_abspos", 0x0a, 0, 127));
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


        Log.i(tag, "Calling InitializeAmp now");

        if (usbCommunicator!=null) InitializeAmp();
    }

    public void InitializeAmp(){
        if (!isInitialized && retryCount <= RETRY_MAX) {

            retryCount ++;
            byte[] startupPacket = new byte[64];

            startupPacket[0] = (byte) (0x81 & 0xFF);
            startupPacket[3] = 0x04;
            startupPacket[4] = 0x03;
            startupPacket[5] = 0x09;
            startupPacket[6] = 0x01;
            startupPacket[7] = (byte) (0xFF & 0xFF);

            usbCommunicator.SendData(startupPacket);
            Log.i(tag, "I've initialized the amp, yo: ");
        }
    }

    public void SetControlsFromPacket(ByteBuffer packet){

        isInitialized = true;
        retryCount = 0;

        //TODO: preset_number
        //ps.preset_number = packet[2]

        for (int c= 1; c< this.Controls.size(); c++) {
            Control ctrl = this.Controls.get(c);

            if (ctrl != null) {
                byte controlValue = packet.get(c + 3);

                if (controlValue <  ctrl.minValue || controlValue > ctrl.maxValue) {
                    Log.e(tag, String.format("Control value is out of bounds! %s, %s", ctrl.controlName, controlValue));
                } else {
                    this.Controls.get(c).controlValue = (int)controlValue;
                    Log.i(tag, String.format("Setting %s to %s", ctrl.controlName, controlValue));
                }
            }
        }

        this.Controls.get(27).controlValue = (256 * packet.get(31)) + packet.get(30);

        /*# The delay time setting is specifed with two bytes,
        # packet[30] and packet[31]. With the delay set to the minimum
        # value, packet[30,31]=[0x64, 0x00], and with the delay time
        # set to maximum packet[30,31]=[0xD0, 0x07]. Somewhere in the
        # middle, packet[30,31]=[0xF4, 0x03]. So, it seems packet[31]
        # is some coarse multiplier, and packet[31] is a finer
        # delineation. According to blackstar the minimum delay is 100
        # ms, and the maximum delay is 2s. So, [0x64, 0x00] = 100ms
        # makes sense. So, the actual delay in ms is:
        # delay = (packet[31] * 256 + packet[30])
        # delay_time_1 = packet[30]  # 00-FF
        # ps.delay_time_2 = packet[31]  # 00-07
        ps.preset_number = packet[2]

        ps.voice = packet[4]  # 00-05
        ps.gain = packet[5]  # 00-7F
        ps.volume = packet[6]  # 00-7F
        ps.bass = packet[7]  # 00-7F
        ps.middle = packet[8]  # 00-7F
        ps.treble = packet[9]  # 00-7F
        ps.isf = packet[10]  # 00-7F
        ps.tvp_switch = packet[17]  # 00 or 01
        ps.tvp_valve = packet[11]  # 00-05

        ps.reverb_switch = packet[20]  # 00 or 01
        ps.reverb_type = packet[32]  # 00-03
        ps.reverb_size = packet[33]  # 00-1F, segval
        # There is a point of confusion here. Adjusting reverb level
        # alters packet[35], but also packet[12]. However, adjusting
        # modulation level changes only packet[12]. So we assume that
        # packet[35] is reverb level, packet[12] is modulation level,
        # and that a firmware bug is changing packet[12] when reverb
        # level is changed. Will be interesting to see if this changes
        # with a later firmware.
        ps.reverb_level = packet[35]  # 00-7F

        ps.delay_switch = packet[19]  # 00 or 01
        ps.delay_type = packet[26]  # 00-03
        ps.delay_feedback = packet[27]  # 00-1F, segval
        ps.delay_level = packet[29]  # 00-7F
        # The delay time setting is specifed with two bytes,
        # packet[30] and packet[31]. With the delay set to the minimum
        # value, packet[30,31]=[0x64, 0x00], and with the delay time
        # set to maximum packet[30,31]=[0xD0, 0x07]. Somewhere in the
        # middle, packet[30,31]=[0xF4, 0x03]. So, it seems packet[31]
        # is some coarse multiplier, and packet[31] is a finer
        # delineation. According to blackstar the minimum delay is 100
        # ms, and the maximum delay is 2s. So, [0x64, 0x00] = 100ms
        # makes sense. So, the actual delay in ms is:
        # delay = (packet[31] * 256 + packet[30])
        # delay_time_1 = packet[30]  # 00-FF
        # ps.delay_time_2 = packet[31]  # 00-07
        ps.delay_time = 256 * packet[31] + packet[30]

        ps.mod_switch = packet[18]  # 00 or 01
        ps.mod_type = packet[21]  # 00-03
        ps.mod_segval = packet[22]  # 00-1F
        ps.mod_level = packet[12]  # 00-7F
        ps.mod_speed = packet[25]  # 00-7F

        # The 'manual' control is exposed via Insider, but doesn't
        # seem to be available from an amp front panel setting, and
        # applies only to the Flanger modulation type.
                ps.mod_manual = packet[23]  # 00-7F - used only for Flanger

        # This next setting is weird, it seems to reflect the absolute
        # position of the segmented selection knowb when selection
        # modulation type and segment value. It takes values between
        # 00-1F in the "1" segment, 20-3F in the "2" segment, 30-4F
        # when in the "3" segment and 40-5F when in the "4" segment.
        // ps.mod_abspos = packet[13]

        # This denotes which efect has "focus" (to use the term in the
        # blackstar manual) i.e. is being controlled by the level,
        # type and tap controls. This is the effect which has the
        # green LED lit on the front panel. 01 is Mod, 02 is delay, 03
        # is reverb.
        ps.effect_focus = packet[39]*/
    }

    public void SetControlsFromFile(){
        //TODO~~~
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

    public void SwitchTunerMode(Boolean on) {

        byte[] data = new byte[64];
        data[0] = 0x08;
        data[1] = (byte)0x11;
        data[2] =  0x00;
        data[3] = 0x01;
        data[4] = on ? (byte)0x01 : (byte)0x00;

        usbCommunicator.SendData(data);
    }
    public void SetControlValue(Control control, Integer value) {

        if (value < control.minValue) {
            Log.e(tag, String.format("Control %s cannot have a value of %s", control.controlName, value));
            control.controlValue = control.minValue;
        } else if (value > control.maxValue) {
            Log.e(tag, String.format("Control %s cannot have a value of %s", control.controlName, value));
            control.controlValue = control.maxValue;
        } else control.controlValue = value;

        byte[] data = new byte[64];

        if (control.controlName == "delay_time") {
            data[0] = 0x03;
            data[1] = control.controlId.byteValue();
            data[2] =  0x00;
            data[3] = 0x02;
            data[4] = (byte)(control.controlValue % 256);
            data[5] = control.controlValue.byteValue();
        } else {
            data[0] = 0x03;
            data[1] = control.controlId.byteValue();
            data[2] =  0x00;
            data[3] = 0x01;
            data[4] = control.controlValue.byteValue();
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
