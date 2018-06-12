package com.examples.android.musicplayerexample;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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
 * Activity for playlist review and culling.
 * - long press on a song will return to the main activity and reset the current song to the selected song.
 * - short click toggles the checkbox for the song clicked on (Used for Culling)
 * <p>
 * Floating Action Buttons
 * - Cull selected songs from the playlist
 * - Add songs to the playlist by launching the activity Activity_AddFromLibraryList
 * - return to the Activity_Main
 */
public class Activity_Playlist extends AppCompatActivity {

    private static final int ADD_SONGS = 1;
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
    private ArrayList<Song> songsOnDevice = new ArrayList<>();
    private ArrayList<Song> tempPlaylist = new ArrayList<>();
    /**
     * Set the onItemLongClickListener
     * <p>
     * method when fired will return to the main player and
     * set the current song to the one that was long clicked.
     */
    private final AdapterView.OnItemLongClickListener mItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Intent returnValues = new Intent();
            returnValues.putExtra("SongChangeRequested", position);
            tempPlaylist.clear();
            for (int i = 0; i < adapter.getCount(); i++)
                tempPlaylist.add(adapter.getItem(i));
            returnValues.putParcelableArrayListExtra("NewSongList", tempPlaylist);
            setResult(RESULT_OK, returnValues);
            finish();
            return true; // consume the click
        }
    };
    private boolean mSortDirectionAlbum = !ASCENDING;
    private boolean mSortDirectionArtist = !ASCENDING;
    private boolean mSortDirectionGenre = !ASCENDING;
    private boolean mSortDirectionTitle = !ASCENDING;

    /**
     * Incoming INTENT lands here
     *
     * @param requestCode  what the originating activity is requesting
     * @param resultCode   status the originating activity is returning to us
     * @param returnValues the values being passed to us from the originating activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent returnValues) {
        // Check which request we're responding to
        if (requestCode == ADD_SONGS) {
            // Make sure the request was successful
            // The intent wants us to update our playlist.
            if (resultCode == RESULT_OK) {
                tempPlaylist = returnValues.getParcelableArrayListExtra("SongsToAdd");
                for (int idx = 0; idx < tempPlaylist.size(); idx++)
                    adapter.add(tempPlaylist.get(idx));
                // loop from end of playlist, looking for items selected to deselect
                Song song;
                for (int i = adapter.getCount() - 1; i > 0; i--) {
                    song = adapter.getItem(i);
                    if (Objects.requireNonNull(song).isChecked())
                        Objects.requireNonNull(adapter.getItem(i)).setChecked(false);
                }
                adapter.notifyDataSetChanged();
                listView.forceLayout();
            }
        }
    }

    private void sortPlaylist(SortField sortField, boolean ascendingSort) {
        tempPlaylist.clear();
        for (int i = 0; i < adapter.getCount(); i++)
            tempPlaylist.add(adapter.getItem(i));
        tempPlaylist = SortedSongList(getIntent().<Song>getParcelableArrayListExtra("PlayList"), sortField, ascendingSort);
        adapter.clear();
        adapter.addAll(tempPlaylist);
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
        listView.forceLayout();
    }

    // inflate the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.playlist_menu, menu);
        return true;
    }

    // handle menu commands
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_album:
                mSortDirectionAlbum = !mSortDirectionAlbum;
                sortPlaylist(SortField.ALBUM, mSortDirectionAlbum);
                Toast.makeText(this, "Sort by Album selected", Toast.LENGTH_SHORT)
                        .show();
                break;

            case R.id.sort_artist:
                mSortDirectionArtist = !mSortDirectionArtist;
                sortPlaylist(SortField.ARTIST, mSortDirectionArtist);
                break;

            case R.id.sort_genre:
                mSortDirectionGenre = !mSortDirectionGenre;
                sortPlaylist(SortField.GENRE, mSortDirectionGenre);
                break;

            case R.id.sort_title:
                mSortDirectionTitle = !mSortDirectionTitle;
                sortPlaylist(SortField.TITLE, mSortDirectionTitle);
                Toast.makeText(this, "Sort by Title selected", Toast.LENGTH_SHORT)
                        .show();
                break;

            case R.id.add_playlist:
                Intent availableListIntent = new Intent(Activity_Playlist.this, Activity_AddFromLibraryList.class);
                availableListIntent.putParcelableArrayListExtra("AvailableList", songsOnDevice);

                // build a temporary copy of the current playlist to pass to the Activity_AddFromLibraryList
                // this will be used to filter the list of available items
                tempPlaylist.clear();
                for (int i = 0; i < adapter.getCount(); i++)
                    tempPlaylist.add(adapter.getItem(i));
                availableListIntent.putParcelableArrayListExtra("PlayList", tempPlaylist);
                startActivityForResult(availableListIntent, ADD_SONGS);
                break;

            case R.id.subtract_playlist:
                // check to see if any items were selected. If so, delete them
                boolean oneOrMoreSelected = false;
                // loop from end of playlist, looking for items selected to delete
                for (int i = adapter.getCount() - 1; i >= 0; i--) {
                    if (Objects.requireNonNull(adapter.getItem(i)).isChecked()) {
                        oneOrMoreSelected = true;
                    }
                }
                if (oneOrMoreSelected) {
                    showDeleteDialog();
                } else {
                    Toast.makeText(getApplicationContext(), "You haven't selected any files\nto remove from the playlist.", Toast.LENGTH_SHORT).show();
                }
                break;

            case android.R.id.home:

                // build the new playlist to send back
                tempPlaylist.clear();
                for (int i = 0; i < adapter.getCount(); i++)
                    tempPlaylist.add(adapter.getItem(i));

                // send back the new playlist
                Intent returnValues = new Intent();
                returnValues.putParcelableArrayListExtra("NewSongList", tempPlaylist);
                setResult(RESULT_OK, returnValues);
                finish();
                break;

            default:
                break;
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        adapter = new SongAdapter(this, getIntent().<Song>getParcelableArrayListExtra("PlayList"));
        songsOnDevice = getIntent().getParcelableArrayListExtra("AvailableList");

        listView = findViewById(R.id.list_view);
        listView.setAdapter(adapter);
        listView.setClickable(true);
        listView.setOnItemClickListener(mItemClickListener);
        listView.setLongClickable(true);
        listView.setOnItemLongClickListener(mItemLongClickListener);
    }

    /**
     * Build and Show the Delete Dialog
     */
    private void showDeleteDialog() {
        AlertDialog.Builder alertDeleteDialog = new AlertDialog.Builder(Activity_Playlist.this);

        alertDeleteDialog
                .setMessage("Confirm removal of selected songs\nfrom the playlist.")
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {
                    }
                })
                .setNegativeButton("cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });

        final AlertDialog alertDialog = alertDeleteDialog.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Song song;
                // loop from END of playlist, delete selected items
                for (int i = adapter.getCount() - 1; i >= 0; i--) {
                    song = adapter.getItem(i);
                    if (Objects.requireNonNull(song).isChecked()) {
                        adapter.remove(song);
                    }
                }
                adapter.notifyDataSetChanged();
                listView.forceLayout();
                alertDialog.dismiss();
            }

        });
    }

    // Sort method
    private ArrayList<Song> SortedSongList(ArrayList<Song> songListToSort, SortField sortField, boolean ascendingSort) {
        ArrayList<IndexItem> indices = new ArrayList<>();
        ArrayList<Song> sortedList = new ArrayList<>();

        switch (sortField) {
            case TITLE:
                for (int i = 0; i < songListToSort.size(); i++)
                    indices.add(new IndexItem(songListToSort.get(i).getTitle().toLowerCase(), i));
                break;
            case ALBUM:
                for (int i = 0; i < songListToSort.size(); i++)
                    indices.add(new IndexItem(songListToSort.get(i).getAlbum().toLowerCase(), i));
                break;
            case GENRE:
                for (int i = 0; i < songListToSort.size(); i++)
                    indices.add(new IndexItem(songListToSort.get(i).getGenre().toLowerCase(), i));
                break;
            case ARTIST:
                for (int i = 0; i < songListToSort.size(); i++)
                    indices.add(new IndexItem(songListToSort.get(i).getArtist().toLowerCase(), i));
                break;
            default:
                throw new InvalidParameterException("Method SortedSongList encountered an invalid Sort Type Parameter, [" + sortField.toString() + "]");
        }

        // sort by ascending or descending order per sortDirection passed to the method
        if (ascendingSort)
            Collections.sort(indices, new Comparator<IndexItem>() {
                @Override
                public int compare(IndexItem item1, IndexItem item2) {
                    return item1.key.compareTo(item2.key);
                }
            });
        else
            Collections.sort(indices, new Comparator<IndexItem>() {
                @Override
                public int compare(IndexItem item1, IndexItem item2) {
                    return item2.key.compareTo(item1.key);
                }
            });

        // rebuild the song list based on our sorted indices
        for (int i = 0; i < songListToSort.size(); i++)
            sortedList.add(songListToSort.get(indices.get(i).value));
        return sortedList;
    }

    // Sort parameter enumerations
    public enum SortField {
        TITLE, ALBUM, ARTIST, GENRE
    }

    // Used by our sorting routines
    static class IndexItem {

        final String key;
        final int value;

        IndexItem(final String key, final int value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return "IndexItem (key=" + key + ", value=" + value + ")";
        }
    }
}
