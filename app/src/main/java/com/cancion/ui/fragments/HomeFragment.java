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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.cancion.R;
import com.cancion.adapters.PlaylistAdapter;
import com.cancion.model.Playlist;
import com.cancion.model.Song;
import com.cancion.ui.activities.MainActivity;
import com.cancion.util.ObservableInteger;
import com.cancion.util.OnIntegerChangeListener;
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
    private ListView playlistsView;

    private ArrayList<Playlist> currentPlaylists;
    private PlaylistAdapter playlistAdapter;
    private FirebaseFirestore database;
    private ArrayList<Playlist> happy;
    private ArrayList<Playlist> sad;
    private ArrayList<Playlist> calm;
    private ArrayList<Playlist> angry;
    private ArrayList<Playlist> romantic;

    private ObservableInteger progress;

    private boolean fetched = false;

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

        playlistsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((MainActivity) Objects.requireNonNull(getActivity())).onPlaylistSelected(currentPlaylists.get(position));
            }
        });

        currentMoodTextView.setOnClickListener(this);
        currentMoodTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (fetched) {
                    if (currentMoodTextView.getText().toString().trim().equalsIgnoreCase("Calm"))
                        currentPlaylists = calm;
                    else if (currentMoodTextView.getText().toString().trim().equalsIgnoreCase("Happy"))
                        currentPlaylists = happy;
                    else if (currentMoodTextView.getText().toString().trim().equalsIgnoreCase("Sad"))
                        currentPlaylists = sad;
                    else if (currentMoodTextView.getText().toString().trim().equalsIgnoreCase("Angry"))
                        currentPlaylists = angry;
                    else if (currentMoodTextView.getText().toString().trim().equalsIgnoreCase("Romantic"))
                        currentPlaylists = romantic;

                    ((MainActivity) Objects.requireNonNull(getActivity())).playlists = currentPlaylists;
                    playlistAdapter.updatePlaylists(currentPlaylists);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        progress = new ObservableInteger();
        progress.setOnIntegerChangeListener(new OnIntegerChangeListener() {
            @Override
            public void onIntegerChanged(int newValue) {
                Log.d(TAG, "Progress: " + newValue);
                switch (newValue) {
                    case 0:
                        fetched = false;
                        currentMoodTextView.setText("Loading");
                        break;
                    case 1:
                        fetched = false;
                        break;
                    case 2:
                        fetched = false;
                        break;
                    case 3:
                        fetched = false;
                        break;
                    case 4:
                        fetched = false;
                        break;
                    case 5:
                        fetched = true;
                        currentMoodTextView.setText("Loaded. Tap to continue");
                        break;
                }
            }
        });
        progress.set(0);
        fetchPlaylists();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.current_mood:
                if (fetched) {
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
                    else currentMoodTextView.setText("Happy");
                } else currentMoodTextView.setText("Loading");
                break;
        }
    }

    public void fetchPlaylists() {
        String calmPlaylistPath = "calm_playlists";
        String happyPlaylistPath = "happy_playlists";
        String sadPlaylistPath = "sad_playlists";
        String angryPlaylistPath = "angry_playlists";
        String romanticPlaylistPath = "romantic_playlists";

        CollectionReference cdb = database.collection(calmPlaylistPath);

        cdb.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    calm = new ArrayList<>();
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        final Playlist cp = new Playlist(Objects.requireNonNull(document.getDouble("count")).intValue(), document.getString("name"));
                        document.getReference().getFirestore().collection("songs").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    ArrayList<Song> s = new ArrayList<>();
                                    for (QueryDocumentSnapshot song : Objects.requireNonNull(task.getResult())) {
                                        s.add(new Song(song.getString("name"), song.getString("url")));
                                        Log.d(TAG, "song: " + song.getString("name"));
                                    }
                                    cp.songs = s;
                                    calm.add(cp);
                                } else Log.d(TAG, "failed");
                            }
                        });
                    }
                    progress.set(progress.get() + 1);
                }
            }
        });

        CollectionReference adb = database.collection(angryPlaylistPath);

        adb.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    angry = new ArrayList<>();
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        final Playlist ap = new Playlist(Objects.requireNonNull(document.getDouble("count")).intValue(), document.getString("name"));
                        document.getReference().getFirestore().collection("songs").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    ArrayList<Song> s = new ArrayList<>();
                                    for (QueryDocumentSnapshot song : Objects.requireNonNull(task.getResult())) {
                                        s.add(new Song(song.getString("name"), song.getString("url")));
                                        Log.d(TAG, "song: " + song.getString("name"));
                                    }
                                    ap.songs = s;
                                    angry.add(ap);
                                }
                            }
                        });
                    }
                    progress.set(progress.get() + 1);
                }
            }
        });

        CollectionReference hdb = database.collection(happyPlaylistPath);

        hdb.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    happy = new ArrayList<>();
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        final Playlist hp = new Playlist(Objects.requireNonNull(document.getDouble("count")).intValue(), document.getString("name"));
                        document.getReference().getFirestore().collection("songs").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    ArrayList<Song> s = new ArrayList<>();
                                    for (QueryDocumentSnapshot song : Objects.requireNonNull(task.getResult())) {
                                        s.add(new Song(song.getString("name"), song.getString("url")));
                                        Log.d(TAG, "song: " + song.getString("name"));
                                    }
                                    hp.songs = s;
                                    happy.add(hp);
                                }
                            }
                        });
                    }
                    progress.set(progress.get() + 1);
                }
            }
        });

        CollectionReference sdb = database.collection(sadPlaylistPath);

        sdb.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    sad = new ArrayList<>();
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        final Playlist sp = new Playlist(Objects.requireNonNull(document.getDouble("count")).intValue(), document.getString("name"));
                        document.getReference().getFirestore().collection("songs").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    ArrayList<Song> s = new ArrayList<>();
                                    for (QueryDocumentSnapshot song : Objects.requireNonNull(task.getResult())) {
                                        s.add(new Song(song.getString("name"), song.getString("url")));
                                        Log.d(TAG, "song: " + song.getString("name"));
                                    }
                                    sp.songs = s;
                                    sad.add(sp);
                                }
                            }
                        });
                    }
                    progress.set(progress.get() + 1);
                }
            }
        });

        CollectionReference rdb = database.collection(romanticPlaylistPath);

        rdb.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    romantic = new ArrayList<>();
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        final Playlist rp = new Playlist(Objects.requireNonNull(document.getDouble("count")).intValue(), document.getString("name"));
                        document.getReference().getFirestore().collection("songs").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    ArrayList<Song> s = new ArrayList<>();
                                    for (QueryDocumentSnapshot song : Objects.requireNonNull(task.getResult())) {
                                        s.add(new Song(song.getString("name"), song.getString("url")));
                                        Log.d(TAG, "song: " + song.getString("name"));
                                    }
                                    rp.songs = s;
                                    romantic.add(rp);
                                }
                            }
                        });
                    }
                    progress.set(progress.get() + 1);
                }
            }
        });
    }
}