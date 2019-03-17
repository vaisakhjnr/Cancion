package com.cancion.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cancion.R;
import com.cancion.model.Song;

import java.util.ArrayList;
import java.util.Random;

public class SongAdapter extends BaseAdapter {

    private final Random rnd = new Random();
    private Context context;
    private ArrayList<Song> currentSongs;

    public SongAdapter(Context context, ArrayList<Song> songs) {
        this.context = context;
        this.currentSongs = songs;
    }

    @Override
    public int getCount() {
        try {
            return currentSongs.size();
        } catch (NullPointerException ignored) {
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return currentSongs.get(position);
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
            convertView = LayoutInflater.from(context).inflate(R.layout.song_item, parent, false);
            holder = new ViewHolder();
            holder.titleTextView = convertView.findViewById(R.id.song_title);
            holder.circleImageView = convertView.findViewById(R.id.colored_circle);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Song song = (Song) getItem(position);
        holder.titleTextView.setText(song.songName);
        Log.d("SongAdapter", song.songName);

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

    public void updateSongs(ArrayList<Song> newDataSet) {
        this.currentSongs = newDataSet;
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        private TextView titleTextView;
        private ImageView circleImageView;
    }
}
