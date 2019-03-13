package com.cancion.ui.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.cancion.R;
import com.cancion.model.Playlist;
import com.cancion.ui.fragments.HomeFragment;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public ArrayList<Playlist> playlists = new ArrayList<>();

    private HomeFragment homeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        homeFragment = new HomeFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.main_frag_container, homeFragment).commit();
    }
}