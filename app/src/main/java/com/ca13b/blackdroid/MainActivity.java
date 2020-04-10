package com.ca13b.blackdroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.ca13b.blackdroid.ui.TunerFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    public static String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    public static BlackstarAmp blackstarAmp;
    private static MainActivity instance;
    NavController navController;

    public FragmentRefreshListener getFragmentRefreshListener() {
        return fragmentRefreshListener;
    }

    public void setFragmentRefreshListener(FragmentRefreshListener fragmentRefreshListener) {
        this.fragmentRefreshListener = fragmentRefreshListener;
    }

    private FragmentRefreshListener fragmentRefreshListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_controls,
                R.id.navigation_effects,
                R.id.navigation_presets,
                R.id.navigation_tuner)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(usbReceiver, filter);

        blackstarAmp = new BlackstarAmp(this);

        instance = this;
    }


    public static MainActivity getInstance() {
        return instance;
    }

    public void SetTunerUI(final ByteBuffer packet) {
        if(getFragmentRefreshListener()!=null){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getFragmentRefreshListener().onRefresh(packet);
                }
            });
        }
    }


    BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {

                if (device != null) {
                    Log.i("BSD/MainActivity", "Broadcast receiver for device " + device.toString());
                }
            } else if (ACTION_USB_PERMISSION.equals(action)) {
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                } else {
                    Log.e("BSD/BroadcastReceiver", "Permission denied for USB device");
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)){
                Log.i("BSD/BroadcastReceiver", "Device detached");
                device = null;
            }
        }

    };

    public interface FragmentRefreshListener{
        void onRefresh(ByteBuffer buffer);
    }

    @Override
    protected void onStop() {
        unregisterReceiver(usbReceiver);
        super.onStop();
    }
}
