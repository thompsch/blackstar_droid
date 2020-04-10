package com.ca13b.blackdroid;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.util.Log;

import java.nio.ByteBuffer;

class UsbRunnable implements Runnable {

    private UsbDeviceConnection mDeviceConnection;
    private final BlackstarAmp amp;
    private final String tag = "BSD/UsbRunnable";

    private UsbEndpoint mEndpointIn;
    private final int mTimeout = 500;

    public UsbRunnable(UsbDeviceConnection mDeviceConnection, UsbEndpoint mEndpointIn, BlackstarAmp amp){
        this.mDeviceConnection = mDeviceConnection;
        this.mEndpointIn = mEndpointIn;
        this.amp = amp;
    }

    public void run() {
        while (mEndpointIn != null && mDeviceConnection != null) {
            byte[] recordIn = new byte[mEndpointIn.getMaxPacketSize()];
            int receivedLength = mDeviceConnection.bulkTransfer(mEndpointIn, recordIn,
                    recordIn.length, mTimeout);
            if (receivedLength > -1) {
                //passDataToListener(recordIn);
                Log.i(tag, parsePacket(ByteBuffer.wrap(recordIn)));
                HandleIncomingData(ByteBuffer.wrap(recordIn));
            }
        }

    }

    private void HandleIncomingData(ByteBuffer buffer) {

        switch (buffer.get(0)) {
            case 2:
                HandlePresetValues(buffer);
                break;
            case 3:
                HandleControlValues(buffer);
                break;
            case 7:
                HandleInitResponse(buffer);
                break;
            case 8:
                HandleAmpMode(buffer);
                break;
            case 9:
                HandleTunerData(buffer);
                break;
            default:
                Log.i(tag, "***UNHANDLED buffer");
                Log.i(tag, parsePacket(buffer));
                break;

        }
    }

    private void HandleAmpMode(ByteBuffer buffer) {
        switch (buffer.get(1)){
            case 3:
                Log.i(tag, "I'm in manual mode!");
                Log.i(tag, parsePacket(buffer));
                //# This packet indicates if the amp is in manual mode
                //                # or not and has the form 08 03 00 01 XX ... if XX is
                //                # 01, then the amp has been switched to manual mode,
                //                # and if it's 00, then the amp has been switched into
                //                # a preset.
                //                logger.debug('Data from amp:: manual mode: {0}'.format(packet[4]))
                //                return {'manual_mode': packet[4]}
                break;
            case 0x11:
                Log.i(tag, "I'm in tuner mode!");
                Log.i(tag, parsePacket(buffer));
                //# Packet indicates entering or leaving tuner
                //                # mode. Packet has the form 08 11 00 01 XX, where XX
                //                # is 01 if amp is in tuner mode, and 00 if amp has
                //                # left tuner mode.
                //                logger.debug('Data from amp:: tuner mode: {0}'.format(packet[4]))
                //                return {'tuner_mode': packet[4]}

                //SendData(new byte[64]);
                //TODO: SWITCH TO TUNER FRAGMENT UNLESS THIS WAS INITIATED
                // BY THE APP ITSELF
                MainActivity.getInstance().SetTunerUI(buffer);
                break;
            default:
                Log.i(tag, "The mystical 3rd packet");
                Log.i(tag, parsePacket(buffer));
                break;
        }
    }

    private void HandleTunerData(ByteBuffer buffer) {
        Log.i(tag, "I'm in tuner mode and sending note data.");
        Log.i(tag, parsePacket(buffer));
        MainActivity.getInstance().SetTunerUI(buffer);
        //I'm in tuner mode! Neato!
        //# In this case, the amp is in tuner mode and this data is
        //            # tuning data. It has the form 09 NN PP ...  If there is
        //            # no note, nn and pp are 00.  Otherwise nn indicates the
        //            # note w/in the scale from E == 01 to Eb == 0C, and pp
        //            # indicates the variance in pitch (based on A440 tuning),
        //            # from 0 (very flat) to 63 (very sharp), i.e, 0-99
        //            # decimal.  So, standard tuning is:
        //            # E  01 32 (same for low and high E strings)
        //            # A  06 32
        //            # D  0B 32
        //            # G  04 32
        //            # B  08 32

    }

    private void HandleInitResponse(ByteBuffer buffer) {
        Log.i(tag, "Init response");
        Log.i(tag, parsePacket(buffer));
    }

    private void HandleControlValues(ByteBuffer buffer) {
        Log.i(tag, "buffer(0) is 3");
        Log.i(tag, parsePacket(buffer));

        //# The 4th byte (packet[3]) specifies the subsequent number of
        //# bytes specifying a value.
        if (buffer.get(3) == 1) {
            byte id = buffer.get(1);
            byte controlValue = buffer.get(4);

            //if control == 'delay_time':
            //                    return {'delay_time_fine': value}
            //                else:
            //                    return {control: value}

            BlackstarAmp.HandleControlValueResponse(id, controlValue);
        } else if (buffer.get(3) == 2) {
            Log.i(tag, "I'm getting control values for effects");
            Log.i(tag, parsePacket(buffer));
            // these are the control values for the effects
            byte id = buffer.get(1);

            //if control == 'delay_time':
            //    value = packet[4] + 256 * packet[5]
            //elif control == 'delay_type':
            //                    delay_type = packet[4]
            //                    delay_feedback = packet[5]
            //elif control == 'reverb_type':
            //                    reverb_type = packet[4]
            //                    reverb_size = packet[5]
            //                    logger.debug('Data from amp:: reverb_type: {0} reverb_size: {1}\n'.format(
            //                        reverb_type, reverb_size))
            //                    return {'reverb_type': packet[4], 'reverb_size': packet[5]}
            //                elif control == 'mod_type':
            //                    mod_type = packet[4]
            //                    mod_segval = packet[5]


        } else if (buffer.get(3) == 0x2a) {

            Log.i(tag, "*********** HEY! I got the all controls packet!!");
            Log.i(tag, parsePacket(buffer));
            amp.SetControlsFromPacket(buffer);
            //# Then packet is a packet describing all current control
            // # settings - note that the 4th byte being 42 (0x2a)
            // # distinguishes this from a packet specifying the voice
            // # setting for which the 4th byte would be 0x01. This is
            // # the 2nd of 3 response packets to the startup packet.
            // # Conveniently the byte address for each control setting
            // # corresponds to the ID number of the control plus
            // # 3. Weird, but handy.

            //                logger.debug('All controls info packet received\n' + self._format_data(packet))
            //                settings = {}
            //                for control, id in self.controls.items():
            //                    if control == 'delay_time':
            //                        settings[control] = (
            //                            packet[id + 4] * 256) + packet[id + 3]
            //                        logger.debug('All controls data:: control: {0} value: {1}'.format(
            //                            control, settings[control]))
            //                    elif control == 'delay_time_coarse':
            //                        # Skip this one, as we already deal with it
            //                        # for the delay_time entry
            //                        pass
            //                    else:
            //                        settings[control] = packet[id + 3]
        }
    }

    private void HandlePresetValues(ByteBuffer buffer) {
        if (buffer.get(1) == 4) {
            Log.i(tag, "I'm preset name packet");
            Log.i(tag, parsePacket(buffer));
            BlackstarAmp.HandlePresetNameResponse(buffer);
        } else if (buffer.get(1) == 5) {
            //# Then packet contains settings for the preset
            // todo: parse entire packet, getting the values for each of the settings
        } else if (buffer.get(1) == 6) {
                            /*  # Then packet is indicating that the preset has been
                                # changed on the amp. This can happen if the user
                                # selects a preset with an amp button. But, this
                                # packet is also sent after the amp changes channel in
                                # response to sending a packet to change channel.*/
            byte preset = buffer.get(2);
            //todo: what to do with this info?? FIIK
        }
    }

    /*
    private final String tag = "BSD/UsbListener";

    public static BlockingQueue<byte[]> outQueue = new ArrayBlockingQueue<>(64);
    public static BlockingQueue<byte[]> inQueue = new ArrayBlockingQueue<>(64);

    UsbDeviceConnection usbConnection;
    UsbEndpoint epIn;
    UsbEndpoint epOut;
    ByteBuffer bufferIn;

    public UsbRunnable(UsbDeviceConnection usbConnection, UsbEndpoint epIn, UsbEndpoint epOut){
        this.usbConnection = usbConnection;
        this.epOut = epOut;
        this.epIn = epIn;
    }

    @Override
    public void run() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                UsbRequest requestOut = new UsbRequest();
                requestOut.initialize(usbConnection, epOut);

                while (true) {
                    //Log.i(tag, Thread.currentThread().getName());
                    try {
                        if (!outQueue.isEmpty()){

                            ByteBuffer sendBuffer = ByteBuffer.wrap(outQueue.take());
                            requestOut.queue(sendBuffer);
                            Log.i(tag, "MsgOut:" + parsePacket(sendBuffer));

                        };
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, "Producer Thread").start();

        new Thread(new Runnable() {
            @Override
            public void run() {

                UsbRequest requestIn = new UsbRequest();
                requestIn.initialize(usbConnection, epIn);
bufferIn = ByteBuffer.wrap(new byte[64]);

                while (true) {
                   // Log.i(tag, Thread.currentThread().getName() + "=> ");
                    try {requestIn.queue(bufferIn);} catch (Exception ex){}
                    if (bufferIn!=null) inQueue.add(bufferIn.array());
                    try {
                        if (!inQueue.isEmpty()) {
                            byte[] data = inQueue.take();
                            Log.i(tag, parsePacket(ByteBuffer.wrap(data)));
                        };
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, "Consumer Thread").start();
    }
*/
    private String parsePacket(ByteBuffer buffer){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 64; i++) {
            sb.append(buffer.get(i) + " ");
        }
        return sb.toString();
    }

}
