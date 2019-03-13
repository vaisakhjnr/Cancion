package com.cancion.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.cancion.R;
import com.cancion.adapters.PlaylistAdapter;
import com.cancion.model.Playlist;
import com.cancion.ui.activities.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class HomeFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "cancion.homefragment";

    private TextView currentMoodTextView;
    public static ListView playlistsView;

    private ArrayList<Playlist> currentPlaylists;
    private PlaylistAdapter playlistAdapter;
    private FirebaseFirestore database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        playlistsView = view.findViewById(R.id.playlists_listview);
        currentMoodTextView = view.findViewById(R.id.current_mood);

        database = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        database.setFirestoreSettings(settings);

        playlistAdapter = new PlaylistAdapter(getActivity(), currentPlaylists);
        playlistsView.setAdapter(playlistAdapter);
        playlistsView.setClipToPadding(false);
        playlistsView.setClipChildren(false);
        playlistsView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                for (int i = 0; i < playlistsView.getChildCount(); i++) {
                    playlistsView.getChildAt(i).invalidate();
                }
            }
        });

        currentMoodTextView.setOnClickListener(this);
        currentMoodTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                fetchPlaylist(currentMoodTextView.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        currentMoodTextView.setText("Happy");
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.current_mood:
                if (currentMoodTextView.getText().toString().trim().equalsIgnoreCase("Sad"))
                    currentMoodTextView.setText("Happy");
                else if (currentMoodTextView.getText().toString().trim().equalsIgnoreCase("Happy"))
                    currentMoodTextView.setText("Calm");
                else if (currentMoodTextView.getText().toString().trim().equalsIgnoreCase("Calm"))
                    currentMoodTextView.setText("Angry");
                else if (currentMoodTextView.getText().toString().trim().equalsIgnoreCase("Angry"))
                    currentMoodTextView.setText("Romantic");
                else if (currentMoodTextView.getText().toString().trim().equalsIgnoreCase("Romantic"))
                    currentMoodTextView.setText("Sad");
                break;
        }
    }

    public void fetchPlaylist(String what) {
        String basePlaylistPath = what.trim().toLowerCase() + "_playlists";
        CollectionReference databaseCurrentMood = database.collection(basePlaylistPath);
        databaseCurrentMood.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    currentPlaylists = new ArrayList<>();
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        Log.d(TAG, document.getData().toString());
                        currentPlaylists.add(new Playlist(Objects.requireNonNull(document.getDouble("count")).intValue(), document.getString("name")));
                    }
                    ((MainActivity) Objects.requireNonNull(getActivity())).playlists = currentPlaylists;
                    playlistAdapter.updatePlaylists(currentPlaylists);
                }
            }
        });
    }
}
