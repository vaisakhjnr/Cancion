package com.cancion.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cancion.R;
import com.cancion.model.Playlist;

import java.util.ArrayList;
import java.util.Random;

import androidx.annotation.NonNull;

public class PlaylistAdapter extends BaseAdapter {

    private final Random rnd = new Random();
    private Context context;
    private ArrayList<Playlist> currentPlaylists;

    public PlaylistAdapter(Context context, ArrayList<Playlist> playlists) {
        this.context = context;
        this.currentPlaylists = playlists;
    }

    @Override
    public int getCount() {
        try {
            return currentPlaylists.size();
        } catch (NullPointerException ignored) {
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return currentPlaylists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.playlist_item, parent, false);
            holder = new ViewHolder();
            holder.titleTextView = convertView.findViewById(R.id.playlist_title);
            holder.detailTextView = convertView.findViewById(R.id.playlist_details);
            holder.circleImageView = convertView.findViewById(R.id.colored_circle);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Playlist playlist = (Playlist) getItem(position);
        holder.titleTextView.setText(playlist.playlistTitle);
        holder.detailTextView.setText(playlist.noOfTracks + " songs");
        int random = rnd.nextInt(5);
        switch (random) {
            case 0:
                holder.circleImageView.setImageResource(R.drawable.blue_circle);
                break;
            case 1:
                holder.circleImageView.setImageResource(R.drawable.green_circle);
                break;
            case 2:
                holder.circleImageView.setImageResource(R.drawable.red_circle);
                break;
            case 3:
                holder.circleImageView.setImageResource(R.drawable.violet_circle);
                break;
            case 4:
                holder.circleImageView.setImageResource(R.drawable.yellow_circle);
                break;
            default:
                holder.circleImageView.setImageResource(R.drawable.blue_circle);
                break;
        }

        return convertView;
    }

    private static class ViewHolder {
        private TextView titleTextView;
        private TextView detailTextView;
        private ImageView circleImageView;
    }

    public void updatePlaylists(ArrayList<Playlist> newDataSet) {
        this.currentPlaylists = newDataSet;
        notifyDataSetChanged();
    }
}
