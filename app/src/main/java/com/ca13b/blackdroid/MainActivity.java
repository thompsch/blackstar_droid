package com.ca13b.blackdroid;

import android.os.Bundle;

import com.ca13b.blackdroid.ui.TunerFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    public BlackstarAmp blackstarAmp;
    private static MainActivity instance;
    NavController navController;

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

        instance = this;
    }

    public static MainActivity getInstance() {
        return instance;
    }

    public void SetTunerUI(ByteBuffer packet){

        /*NavDestination current =
                NavHostFragment.findNavController(getSupportFragmentManager()
                        .getPrimaryNavigationFragment().getFragmentManager()
                        .getFragments().get(0)).getCurrentDestination();

        TunerFragment tunerFragment =
                (TunerFragment) getSupportFragmentManager().findFragmentById(current.getId());*/

        TunerFragment.SetTunerUI(packet);
    }

}
