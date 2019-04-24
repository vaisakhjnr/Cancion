package com.cancion.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cancion.R;
import com.cancion.model.Song;

import java.util.ArrayList;
import java.util.Random;

import androidx.annotation.NonNull;

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
                holder.titleTextView.setBackground(context.getDrawable(R.drawable.blue_rect));
                break;
            case 1:
                holder.titleTextView.setBackground(context.getDrawable(R.drawable.green_rect));
                break;
            case 2:
                holder.titleTextView.setBackground(context.getDrawable(R.drawable.red_rect));
                break;
            case 3:
                holder.titleTextView.setBackground(context.getDrawable(R.drawable.violet_rect));
                break;
            case 4:
                holder.titleTextView.setBackground(context.getDrawable(R.drawable.yellow_rect));
                break;
            default:
                holder.titleTextView.setBackground(context.getDrawable(R.drawable.blue_rect));
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
    }
}
