package com.ca13b.blackdroid;
import java.nio.ByteBuffer;
import java.util.HashMap;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.ca13b.blackdroid.ui.PresetsFragment;

public class UsbCommunicator {

    private Object lock = new Object();

    private Context _context;
    private PresetsFragment presetsFragment;
    private UsbDevice usbDevice;
    private UsbManager usbManager;
    private UsbDeviceConnection usbConnection;
    private UsbEndpoint epToDevice;
    private UsbEndpoint epFromDevice;

    private final String tag = "BSD/UsbCommunicator";
    private final BlackstarAmp amp;

    public UsbCommunicator(Context context, BlackstarAmp amp) {
        this.amp = amp;
        this._context = context;
        if (usbDevice == null) setUpDevice();
    }

    public void setUpDevice() {
        usbManager = (UsbManager) _context.getSystemService(Context.USB_SERVICE);

        Intent intent = ((MainActivity) _context).getIntent();

        if (intent != null) {
            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (device == null) {
                Log.i(tag, "Device is null; iterating instead.");
                usbDevice = iterateUsbDevices();
                if (usbDevice == null){
                    Toast.makeText(_context, "No Blackstar amp is connected!", Toast.LENGTH_LONG).show();
                    return;
                }
            } else usbDevice = device;

            if (usbDevice == null) {
                Log.e(tag, "No USB device found!");
                return;
            }
            UsbInterface intf = usbDevice.getInterface(0);
            usbConnection = usbManager.openDevice(usbDevice);
            usbConnection.claimInterface(intf, true);

            for (int e = 0; e < intf.getEndpointCount(); e++) {
                UsbEndpoint ep = intf.getEndpoint(e);
                Log.i(tag, "endpoint " + e + ": " + ep.getDirection());
                if (ep.getDirection() == UsbConstants.USB_DIR_IN) {
                    epFromDevice = ep;
                } else if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                    epToDevice = ep;
                }
            }

            UsbRunnable runnable = new UsbRunnable(usbConnection, epFromDevice, amp);
            MainActivity.usbReceiverThread = new Thread(runnable);
            MainActivity.usbReceiverThread.start();
        }
    }

    public void SendData(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);

        ByteBuffer bufferIn = ByteBuffer.wrap(new byte[64]);

        Log.i(tag, "Sending packet of length " + data.length);
        Log.i(tag, "Sending data: " + parsePacket(buffer));

        if (usbConnection == null) {
            Log.e(tag, "mConnection is null :( ");
            return;
        }

        UsbRequest requestOut = new UsbRequest();
        requestOut.initialize(usbConnection, epToDevice);
        requestOut.queue(buffer);

        bufferIn.rewind();
        Log.i(tag, "Buffer in: " + parsePacket(bufferIn));
    }

    private UsbDevice iterateUsbDevices() {

        HashMap<String, UsbDevice> connectedDevices = usbManager.getDeviceList();
        if (connectedDevices.isEmpty()) {
           Log.e(tag, "No USB devices currently connected!");
        } else {
            for (UsbDevice device : connectedDevices.values()) {
                if (device.getVendorId() == BlackstarAmp.VendorId) {
                    PendingIntent permissionIntent = PendingIntent.getBroadcast(_context, 0, new Intent(MainActivity.ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(device, permissionIntent);
                    UsbInterface intf = device.getInterface(0);
                    usbConnection = usbManager.openDevice(device);
                    usbConnection.claimInterface(intf, true);
                    return device;
                }
            }
        }
        return null;
    }

    private String parsePacket(ByteBuffer buffer){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 64; i++) {
            sb.append(buffer.get(i) + " ");
        }
        return sb.toString();
    }

    @Override
    protected void finalize() throws Throwable {
        MainActivity.usbReceiverThread.interrupt();
        super.finalize();
    }
}