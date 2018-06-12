package com.examples.android.musicplayerexample;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

class SongAdapter extends ArrayAdapter<Song> {

    public SongAdapter(Activity context, ArrayList<Song> songs) {
        super(context, 0, songs);
    }

    /**
     * Provides a view for an AdapterView (ListView, GridView, etc.)
     *
     * @param position    The position in the recycler_view of data that should be displayed in the
     *                    recycler_view item view.
     * @param convertView The recycled view to populate.
     * @param parent      The parent ViewGroup that is used for inflation.
     * @return The View for the position in the AdapterView.
     */
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // Reusing view? If not, inflate view
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }

        // Get the Song object located at this position in the recycler_view
        Song currentSong = getItem(position);

        // if it isn't null then place values into the view
        if (currentSong == null) throw new AssertionError();
        else {
            TextView titleTextView = listItemView.findViewById(R.id.title_text_view);
            titleTextView.setText(currentSong.getTitle());

            TextView descriptionTextView = listItemView.findViewById(R.id.description_text_view);
            descriptionTextView.setText(String.format("%s by %s", currentSong.getAlbum(), currentSong.getArtist()));

            ImageView checkBox = listItemView.findViewById(R.id.selected_checkbox);
            if (currentSong.isChecked())
                checkBox.setImageResource(R.drawable.ic_check_box_checked);
            else
                checkBox.setImageResource(R.drawable.ic_check_box_unchecked);
        }
        // Return the view so that it can be shown in the ListView
        return listItemView;
    }

}
