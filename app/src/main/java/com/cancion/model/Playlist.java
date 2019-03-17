package com.cancion.model;

import java.util.ArrayList;

public class Playlist {

    public int noOfTracks;
    public String playlistTitle;
    public ArrayList<Song> songs;

    public Playlist(int noOfTracks, String playlistTitle) {
        this.noOfTracks = noOfTracks;
        this.playlistTitle = playlistTitle;
    }
}
