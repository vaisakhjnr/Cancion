package com.cancion.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.cancion.R;
import com.cancion.adapters.PlaylistAdapter;
import com.cancion.ui.activities.MainActivity;

public class HomeFragment extends Fragment {

    private TextView currentMoodTextView;

    private ListView playlistsView;
    private PlaylistAdapter playlistAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        playlistsView = view.findViewById(R.id.playlists_listview);
        currentMoodTextView = view.findViewById(R.id.current_mood);

        playlistAdapter = new PlaylistAdapter(getActivity(), ((MainActivity) getActivity()).playlists);
        playlistsView.setAdapter(playlistAdapter);
        switch (((MainActivity) getActivity()).mood) {
            case "sad":
                currentMoodTextView.setText("So much sadness...");
                break;
        }
    }
}
