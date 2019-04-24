package com.cancion.ui.fragments;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import de.hdodenhof.circleimageview.CircleImageView;

public class HomeFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "cancion.homefragment";

    private TextView currentMoodTextView;
    private ListView playlistsView;
    private CircleImageView snappedPic;

    private ArrayList<Playlist> currentPlaylists;
    private PlaylistAdapter playlistAdapter;
    private FirebaseFirestore database;
    private ArrayList<Playlist> happy;
    private ArrayList<Playlist> sad;
    private ArrayList<Playlist> calm;
    private ArrayList<Playlist> angry;
    private ArrayList<Playlist> romantic;
    private FirebaseStorage storage;
    private MediaPlayer mediaPlayer;

    private ObservableInteger progress;

    private boolean fetched = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        playlistsView = view.findViewById(R.id.playlists_listview);
        currentMoodTextView = view.findViewById(R.id.current_mood);
        snappedPic = view.findViewById(R.id.snapped_pic);

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

        storage = FirebaseStorage.getInstance();
        mediaPlayer = new MediaPlayer();

        /*snappedPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StorageReference songReference = storage.getReferenceFromUrl("gs://cancion-musicplayer.appspot.com/calm/Glad You Came.mp3");
                Toast.makeText(getActivity(), songReference.getDownloadUrl().toString(), Toast.LENGTH_SHORT).show();
                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(Objects.requireNonNull(getActivity()), Uri.parse(songReference.getDownloadUrl().toString()));
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                    //Toast.makeText(getActivity(), "IOException", Toast.LENGTH_SHORT).show();
                }
            }
        });*/

        currentMoodTextView.setOnClickListener(this);
        currentMoodTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (fetched) {
                    if (currentMoodTextView.getText().toString().trim().contains("Calm"))
                        currentPlaylists = calm;
                    else if (currentMoodTextView.getText().toString().trim().contains("Happy"))
                        currentPlaylists = happy;
                    else if (currentMoodTextView.getText().toString().trim().contains("Sad"))
                        currentPlaylists = sad;
                    else if (currentMoodTextView.getText().toString().trim().contains("Angry"))
                        currentPlaylists = angry;
                    else currentPlaylists = null;

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
                switch (newValue) {
                    case 0:
                        fetched = false;
                        currentMoodTextView.setText("Loading");
                        currentPlaylists = null;
                        ((MainActivity) Objects.requireNonNull(getActivity())).playlists = currentPlaylists;
                        playlistAdapter.updatePlaylists(currentPlaylists);
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
                        fetched = true;
                        if (((MainActivity) getActivity()).currentEmotion.trim().equalsIgnoreCase("none"))
                            currentMoodTextView.setText("You look Emotionless");
                        else
                            currentMoodTextView.setText("You look " + ((MainActivity) getActivity()).currentEmotion);
                        if (((MainActivity) getActivity()).currentEmotion.trim().contains("Calm"))
                            currentPlaylists = calm;
                        else if (((MainActivity) getActivity()).currentEmotion.trim().contains("Happy"))
                            currentPlaylists = happy;
                        else if (((MainActivity) getActivity()).currentEmotion.trim().contains("Sad"))
                            currentPlaylists = sad;
                        else if (((MainActivity) getActivity()).currentEmotion.trim().contains("Angry"))
                            currentPlaylists = angry;
                        else if (((MainActivity) getActivity()).currentEmotion.trim().contains("Romantic"))
                            currentPlaylists = romantic;

                        ((MainActivity) Objects.requireNonNull(getActivity())).playlists = currentPlaylists;
                        playlistAdapter.updatePlaylists(currentPlaylists);

                        snappedPic.setImageBitmap(((MainActivity) getActivity()).rawColorBitmap);
                        break;
                }
            }
        });
        return view;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetchPlaylists();
        progress.set(0);
        ((MainActivity) getActivity()).inHomeFragment = true;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onClick(View v) {
        /*switch (v.getId()) {
            case R.id.current_mood:
                if (fetched) {
                    if (currentMoodTextView.getText().toString().trim().contains("Sad"))
                        currentMoodTextView.setText("You look Sad");
                    else if (currentMoodTextView.getText().toString().trim().contains("Happy"))
                        currentMoodTextView.setText("You look Happy");
                    else if (currentMoodTextView.getText().toString().trim().contains("Calm"))
                        currentMoodTextView.setText("You look Calm");
                    else if (currentMoodTextView.getText().toString().trim().contains("Angry"))
                        currentMoodTextView.setText("You look Angry");
                    else currentMoodTextView.setText("You look Emotionless");
                } else currentMoodTextView.setText("Loading");
                break;
        }*/
    }

    public void fetchPlaylists() {
        final String calmPlaylistPath = "calm_playlists";
        final String happyPlaylistPath = "happy_playlists";
        final String sadPlaylistPath = "sad_playlists";
        final String angryPlaylistPath = "angry_playlists";

        CollectionReference cdb = database.collection(calmPlaylistPath);

        cdb.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    calm = new ArrayList<>();
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        final Playlist cp = new Playlist(Objects.requireNonNull(document.getDouble("count")).intValue(), document.getString("name"));
                        CollectionReference cdbi = database.collection(calmPlaylistPath).document(document.getId()).collection("songs");
                        cdbi.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    ArrayList<Song> s = new ArrayList<>();
                                    for (QueryDocumentSnapshot song : Objects.requireNonNull(task.getResult())) {
                                        s.add(new Song(song.getString("name"), song.getString("url")));
                                    }
                                    cp.songs = s;
                                    calm.add(cp);
                                    progress.set(progress.get() + 1);
                                }
                            }
                        });
                    }
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
                        CollectionReference cdbi = database.collection(angryPlaylistPath).document(document.getId()).collection("songs");
                        cdbi.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    ArrayList<Song> s = new ArrayList<>();
                                    for (QueryDocumentSnapshot song : Objects.requireNonNull(task.getResult())) {
                                        s.add(new Song(song.getString("name"), song.getString("url")));
                                    }
                                    ap.songs = s;
                                    angry.add(ap);
                                    progress.set(progress.get() + 1);
                                }
                            }
                        });
                    }
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
                        CollectionReference cdbi = database.collection(happyPlaylistPath).document(document.getId()).collection("songs");
                        cdbi.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    ArrayList<Song> s = new ArrayList<>();
                                    for (QueryDocumentSnapshot song : Objects.requireNonNull(task.getResult())) {
                                        s.add(new Song(song.getString("name"), song.getString("url")));
                                    }
                                    hp.songs = s;
                                    happy.add(hp);
                                    progress.set(progress.get() + 1);
                                }
                            }
                        });
                    }
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
                        CollectionReference cdbi = database.collection(sadPlaylistPath).document(document.getId()).collection("songs");
                        cdbi.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    ArrayList<Song> s = new ArrayList<>();
                                    for (QueryDocumentSnapshot song : Objects.requireNonNull(task.getResult())) {
                                        s.add(new Song(song.getString("name"), song.getString("url")));
                                    }
                                    sp.songs = s;
                                    sad.add(sp);
                                    progress.set(progress.get() + 1);
                                }
                            }
                        });
                    }
                }
            }
        });
    }
}