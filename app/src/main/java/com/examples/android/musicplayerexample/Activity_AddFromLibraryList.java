package com.examples.android.musicplayerexample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

/**
 * Activity for selecting available songs to add to the playlist.
 * - short click toggles the checkbox for the song clicked
 * - songs selected will be added to the playlist with a return Intent
 * when the user selects the back key/button
 */
public class Activity_AddFromLibraryList extends AppCompatActivity {

    private final ArrayList<String> artistFilterList = new ArrayList<>();
    private final ArrayList<String> albumFilterList = new ArrayList<>();
    private final ArrayList<String> genreFilterList = new ArrayList<>();
    private final boolean ASCENDING = true;
    private ListView listView;
    private SongAdapter adapter;
    /**
     * Set the onItemClickListener
     * <p>
     * method will cause the selected list item to
     * toggle checked/unchecked it's checked state
     */
    private final AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            assert adapter.getItem(position) != null;
            Objects.requireNonNull(adapter.getItem(position)).setChecked(!Objects.requireNonNull(adapter.getItem(position)).isChecked());
            adapter.notifyDataSetChanged();
            listView.forceLayout();
        }
    };
    private ArrayList<Song> availableSongList = new ArrayList<>();
    private ArrayList<Song> masterAvailableSongArrayList = new ArrayList<>();
    private ArrayList<Song> existingPlaylist = new ArrayList<>();
    private boolean mSortDirectionAlbum = !ASCENDING;
    private boolean mSortDirectionArtist = !ASCENDING;
    private boolean mSortDirectionGenre = !ASCENDING;
    private boolean mSortDirectionTitle = !ASCENDING;

    /**
     * This method will sort the song list of available songs based upon the parameters passed
     *
     * @param sortField     which field to sort by
     * @param ascendingSort which direction to sort
     */
    private void sortPlaylist(MasterSortField sortField, boolean ascendingSort) {
        availableSongList = MasterSortedSongList(availableSongList, sortField, ascendingSort);
        adapter.clear();
        adapter.addAll(availableSongList);
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
        listView.forceLayout();
    }

    // inflate the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.addfromlibrarylist_menu, menu);
        return true;
    }

    // handle the selections from the menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_album:
                mSortDirectionAlbum = !mSortDirectionAlbum;
                sortPlaylist(MasterSortField.ALBUM, mSortDirectionAlbum);
                Toast.makeText(this, "Sort by Album selected", Toast.LENGTH_SHORT)
                        .show();
                break;

            case R.id.sort_artist:
                mSortDirectionArtist = !mSortDirectionArtist;
                sortPlaylist(MasterSortField.ARTIST, mSortDirectionArtist);
                break;

            case R.id.sort_genre:
                mSortDirectionGenre = !mSortDirectionGenre;
                sortPlaylist(MasterSortField.GENRE, mSortDirectionGenre);
                break;

            case R.id.sort_title:
                mSortDirectionTitle = !mSortDirectionTitle;
                sortPlaylist(MasterSortField.TITLE, mSortDirectionTitle);
                Toast.makeText(this, "Sort by Title selected", Toast.LENGTH_SHORT)
                        .show();
                break;

            case android.R.id.home:
                // build the new playlist
                ArrayList<Song> songsToAdd = new ArrayList<>();
                Intent returnValues = new Intent();
                for (int i = availableSongList.size() - 1; i >= 0; i--) {
                    if (Objects.requireNonNull(adapter.getItem(i)).isChecked())
                        songsToAdd.add(adapter.getItem(i));
                }
                // store the results to return to the calling activity
                returnValues.putParcelableArrayListExtra("SongsToAdd", songsToAdd);
                setResult(RESULT_OK, returnValues);
                finish(); // close this activity and return to preview activity (if there is any)
                break;
            default:
                break;
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addfromlibrarylist);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // retrieve the master list of songs from the calling intent. This list will stay the same
        masterAvailableSongArrayList = getIntent().getParcelableArrayListExtra("AvailableList");
        // duplicate the list into our adapter for viewing and additions
        adapter = new SongAdapter(this, masterAvailableSongArrayList);
        // retrieve the current playlist from the calling intent
        existingPlaylist = getIntent().getParcelableArrayListExtra("PlayList");

        discoverFilterPossibilities();
        buildAvailableSongList();

        listView = findViewById(R.id.list_view);
        listView.setAdapter(adapter);
        listView.setClickable(true);
        listView.setOnItemClickListener(mItemClickListener);
    }

    // extract filter possibilities from masterAvailableSongList
    private void discoverFilterPossibilities() {
        Song song;
        for (int i = 0; i < masterAvailableSongArrayList.size(); i++) {
            song = masterAvailableSongArrayList.get(i);
            if (!genreFilterList.contains(song.getGenre())) genreFilterList.add(song.getGenre());
            if (!albumFilterList.contains(song.getAlbum())) albumFilterList.add(song.getAlbum());
            if (!artistFilterList.contains(song.getArtist()))
                artistFilterList.add(song.getArtist());
        }
    }

    private void buildAvailableSongList() {
        availableSongList = masterAvailableSongArrayList;

        // Filter out items already in the current playlist
        boolean matchFound;
        // loop from END of playlist, delete selected items
        for (int i = availableSongList.size() - 1; i >= 0; i--) {
            Song song = adapter.getItem(i);
            matchFound = false;
            for (int idx = 0; idx < existingPlaylist.size(); idx++) {
                if (existingPlaylist.get(idx).getTitle().equals(Objects.requireNonNull(song).getTitle()) &&
                        existingPlaylist.get(idx).getArtist().equals(song.getArtist()) &&
                        existingPlaylist.get(idx).getAlbum().equals(song.getAlbum()) &&
                        existingPlaylist.get(idx).getGenre().equals(song.getGenre())
                        ) {
                    matchFound = true;
                    break;
                }
            }
            if (matchFound) {
                adapter.remove(song);
            }
        }
    }

    // Internal sort method
    private ArrayList<Song> MasterSortedSongList(ArrayList<Song> songListToSort, MasterSortField sortField, boolean ascendingSort) {
        ArrayList<MasterIndexItem> indices = new ArrayList<>();
        ArrayList<Song> sortedList = new ArrayList<>();

        switch (sortField) {
            case TITLE:
                for (int i = 0; i < songListToSort.size(); i++)
                    indices.add(new MasterIndexItem(songListToSort.get(i).getTitle().toLowerCase(), i));
                break;
            case ALBUM:
                for (int i = 0; i < songListToSort.size(); i++)
                    indices.add(new MasterIndexItem(songListToSort.get(i).getAlbum().toLowerCase(), i));
                break;
            case GENRE:
                for (int i = 0; i < songListToSort.size(); i++)
                    indices.add(new MasterIndexItem(songListToSort.get(i).getGenre().toLowerCase(), i));
                break;
            case ARTIST:
                for (int i = 0; i < songListToSort.size(); i++)
                    indices.add(new MasterIndexItem(songListToSort.get(i).getArtist().toLowerCase(), i));
                break;
            default:
                throw new InvalidParameterException("Method SortedSongList encountered an invalid Sort Type Parameter, [" + sortField.toString() + "]");
        }

        // sort by ascending or descending order per sortDirection passed to the method
        if (ascendingSort)
            Collections.sort(indices, new Comparator<MasterIndexItem>() {
                @Override
                public int compare(MasterIndexItem item1, MasterIndexItem item2) {
                    return item1.key.compareTo(item2.key);
                }
            });
        else
            Collections.sort(indices, new Comparator<MasterIndexItem>() {
                @Override
                public int compare(MasterIndexItem item1, MasterIndexItem item2) {
                    return item2.key.compareTo(item1.key);
                }
            });

        // rebuild the song list based on our sorted indices
        for (int i = 0; i < songListToSort.size(); i++)
            sortedList.add(songListToSort.get(indices.get(i).value));
        return sortedList;
    }

    /**
     * The possible sort types we will offer
     */
    private enum MasterSortField {
        TITLE, ALBUM, ARTIST, GENRE
    }

    // Used by our sorting routines
    static class MasterIndexItem {

        final String key;
        final int value;

        MasterIndexItem(final String key, final int value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return "IndexItem (key=" + key + ", value=" + value + ")";
        }
    }
}
