package com.ca13b.blackdroid;
import java.nio.ByteBuffer;
import java.util.HashMap;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;

import android.util.Log;
import android.widget.TextView;

import com.ca13b.blackdroid.ui.PresetsFragment;

public class UsbCommunicator  {

    private Context _context;
    private PresetsFragment presetsFragment;
    private UsbDevice usbDevice;
    private UsbManager usbManager;
    private UsbDeviceConnection usbConnection;
    private UsbEndpoint epToDevice;
    private UsbEndpoint epFromDevice;

    private TextView logView;
    private static String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    
    private final String tag = "BSD/UsbCommunicator";

    public UsbCommunicator(Context context) {

       this._context = context;
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);

        _context.registerReceiver(usbReceiver, filter);

        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

        Intent intent = ((MainActivity)context).getIntent();

        if (intent != null) {
            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (device == null) {
                Log.i(tag, "Device is null; iterating instead.");
                usbDevice = iterateUsbDevices();
               // presetsFragment.LogInfo( "I should now have a device: " + String.valueOf(usbDevice != null));
            } else usbDevice = device;

            if (usbDevice == null) return;

           // presetsFragment.LogInfo( "I should now have a device, right?? " + String.valueOf(usbDevice != null));
           // presetsFragment.LogInfo( "Setting the interface. " + usbDevice.getProductName());
            UsbInterface intf = usbDevice.getInterface(0);
            usbConnection = usbManager.openDevice(usbDevice);
            usbConnection.claimInterface(intf, true);

        } else {
          Log.e(tag, "Intent is NULL");
        }
        Log.i(tag, "UsbCommunicator Xtor. usbDevice is not null = " + String.valueOf(usbDevice != null));

        if (usbDevice == null) {
            Log.e(tag, "Why is the device null here??");
        } else setUpDevice();
    }


    private void setUpDevice() {

        Log.i(tag, "setUpDevice " + usbDevice.getProductName());
        if (usbDevice.getInterfaceCount() <= 0) {
            Log.e(tag, "could not find interface");
            return;
        }
        UsbInterface intf = usbDevice.getInterface(0);
        // device should have one endpoint
        if (intf.getEndpointCount() <= 0) {
            Log.e(tag, "could not find endpoint");
            return;
        }

        for (int e = 0; e < intf.getEndpointCount(); e++) {
            UsbEndpoint ep = intf.getEndpoint(e);
            Log.i(tag, "endpoint " + e + ": " + ep.getDirection());
            if (ep.getDirection() == UsbConstants.USB_DIR_IN) {
                epFromDevice = ep;
            } else if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                epToDevice = ep;
            }
        }

        if (usbDevice != null) {
            UsbDeviceConnection connection = usbManager.openDevice(usbDevice);
            if (connection != null && connection.claimInterface(intf, true)) {
                Log.i(tag, "open SUCCESS on " + connection.toString());

                usbConnection = connection;
                Thread thread = new Thread();
                thread.start();

            } else {
                Log.i(tag, "open FAIL");
                usbConnection = null;
            }
        }
    }

    public void SendData(byte[] data) {
        synchronized (this) {
            if (usbConnection == null) {
                Log.e(tag, "mConnection is null :( ");
                return;
            }
            ByteBuffer buffer = ByteBuffer.wrap(data);
            UsbRequest request = new UsbRequest();
            request.initialize(usbConnection, epToDevice);
            int status = -1;
            request.queue(buffer);

            if (usbConnection.requestWait() == request) {
                int newStatus = buffer.get(0);
                if (newStatus != status) {
                    Log.i(tag, "got status " + newStatus);
                    status = newStatus;

                    /*
                    // Get the buffer's capacity
                    int capacity = buffer.capacity();

                    // Get the buffer's limit
                    int limit = buffer.limit();

                    // Get the buffer's position
                    int position = buffer.position();

                    Log.i(tag, "Buffer capacity: " + capacity);
                    Log.i(tag, "Buffer limit: " + limit);
                    Log.i(tag, "Buffer position: " + position);*/

                    buffer.rewind();

                    // todo: figure out what type of data we just received and send it to the
                    // correct handler

                    if (buffer.get(0) == 2) {
                        if (buffer.get(1) == 4) {
                            // I'm preset name packet
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
                    } else if (buffer.get(0) == 3) {
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
                    } else if (buffer.get(0) == 7) {
                        //# This is the first of the three response packets to the
                        //            # startup packet. At this point, I don't know what this
                        //            # packet describes. Firmware version?
                    } else if (buffer.get(0) == 8) {
                        if (buffer.get(1) == 3) {
                            //# This packet indicates if the amp is in manual mode
                            //                # or not and has the form 08 03 00 01 XX ... if XX is
                            //                # 01, then the amp has been switched to manual mode,
                            //                # and if it's 00, then the amp has been switched into
                            //                # a preset.
                            //                logger.debug('Data from amp:: manual mode: {0}'.format(packet[4]))
                            //                return {'manual_mode': packet[4]}
                        } else if (buffer.get(1) == 11) {
                            //# Packet indicates entering or leaving tuner
                            //                # mode. Packet has the form 08 11 00 01 XX, where XX
                            //                # is 01 if amp is in tuner mode, and 00 if amp has
                            //                # left tuner mode.
                            //                logger.debug('Data from amp:: tuner mode: {0}'.format(packet[4]))
                            //                return {'tuner_mode': packet[4]}
                        } else {
                            // 3rd packet.
                        }
                    } else if (buffer.get(0) == 9) {
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

                    /*while (buffer.position()<63){
                        Log.i(tag,  buffer.position() + " byte: " + buffer.get());
                    }
*/

                }
            }

            /*try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Log.e(tag, e.getMessage());
            }*/
        }
    }
    BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                    //printStatus(getString(R.string.status_removed));
                    // printDeviceDescription(device);
                    Log.i(tag, "Broadcast receiver for device " + device.toString());
                }
            }
        }
    };
    private UsbDevice iterateUsbDevices() {

        HashMap<String, UsbDevice> connectedDevices = usbManager.getDeviceList();
        if (connectedDevices.isEmpty()) {
           // presetsFragment.LogError( "No USB devices currently connected!");
        } else {
            for (UsbDevice device : connectedDevices.values()) {
               // presetsFragment.LogInfo( "Found: " + device.getProductName());
                if (device.getVendorId() == BlackstarAmp.VendorId) {
                    PendingIntent permissionIntent = PendingIntent.getBroadcast(_context, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(device, permissionIntent);
                   // presetsFragment.LogInfo( "Found the amp: " + device.getProductName());
                   // presetsFragment.LogInfo( device.getProductName());
                    UsbInterface intf = device.getInterface(0);
                    usbConnection = usbManager.openDevice(device);
                    usbConnection.claimInterface(intf, true);
                    Log.i(tag, "returning a non-null device, right? " + String.valueOf(device!=null));
                    return device;
                }
            }
        }
        return null;
    }
}


/*if (mConnection.requestWait() == request) {
                byte newStatus = buffer.get(0);
                if (newStatus != status) {
                    Log.i(tag,"got status " + newStatus);
                    status = newStatus;
                    if ((status & COMMAND_FIRE) != 0) {
                        // stop firing
                        sendCommand(COMMAND_STOP);
                    }
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            } else {
                Log.e(tag,"requestWait failed, exiting");
                break;
            }*/

