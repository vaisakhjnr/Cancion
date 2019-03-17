package com.cancion.ui.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.cancion.R;
import com.cancion.model.Playlist;
import com.cancion.ui.fragments.HomeFragment;
import com.cancion.ui.fragments.PlayerFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "cancion.homefragment";

    public ArrayList<Playlist> playlists = new ArrayList<>();

    private HomeFragment homeFragment;
    public Playlist currentPlaylist;
    private PlayerFragment playerFragment;

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    public void onPlaylistSelected(Playlist playlist) {
        getSupportFragmentManager().beginTransaction().replace(R.id.main_frag_container, playerFragment).commit();
        this.currentPlaylist = playlist;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth = FirebaseAuth.getInstance();
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInAnonymously:success");
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            user = currentUser;
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            user = null;
                        }
                    }
                });

        homeFragment = new HomeFragment();
        playerFragment = new PlayerFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.main_frag_container, homeFragment).commit();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        user = currentUser;
    }
}