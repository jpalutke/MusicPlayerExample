package com.examples.android.musicplayerexample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class Activity_Main extends AppCompatActivity {
    private static final int PLAYLIST_MODIFY = 1;
    private static final int STATE_PLAYING = 200;
    private static final int STATE_PAUSED = 201;
    private static final int STATE_STOPPED = 202;
    // Initialize the randomNumberClass for our getRandom function
    private final Random randomNumberClass = new Random();
    private final ArrayList<Song> songLibrary = new ArrayList<>();
    private int playlistPosition;
    private ImageView viewPlayButton;
    private ImageView imageViewNowPlayingAlbumArt;
    private TextView textViewNowPlayingSongTitle;
    private TextView textViewNowPlayingArtist;
    private TextView textViewNowPlayingAlbum;
    private SnapTimer snap;
    private SeekBar seekBar;
    private boolean thisIsFirstSongPlayed = true;
    private int mPlayerState = STATE_PAUSED;
    private ArrayList<Song> songsPlaylist = new ArrayList<>();

    /**
     * PLAY BUTTON onClickListener
     */
    private final View.OnClickListener play_button_onClickListener = new View.OnClickListener() {
        public void onClick(final View v) {
            switch (mPlayerState) {
                case STATE_PLAYING: {
                    setPlayerState(STATE_PAUSED);
                    snap.stop();
                    break;
                }
                case STATE_STOPPED: {
                    if (playlistPosition >= 0 && playlistPosition <= songsPlaylist.size()) {
                        playCurrentSong(false);
                        setPlayerState(STATE_PLAYING);
                        snap.start();
                    }
                    break;
                }
                case STATE_PAUSED: {
                    playCurrentSong(true);
                }
            }
        }
    };
    private boolean Shuffle = false;
    /**
     * LEFT Button onClickListener
     */
    private final View.OnClickListener previous_button_onClickListener = new View.OnClickListener() {
        public void onClick(final View v) {
            previousSong();
        }
    };
    /**
     * RIGHT Button onClickListener
     */
    private final View.OnClickListener next_button_onClickListener = new View.OnClickListener() {
        public void onClick(final View v) {
            nextSong();
        }
    };

    /**
     * Swap the Play/Pause buttons as determined by the specified state and
     * store the specified status to mPlayerState variable
     *
     * @param state [STATE_PAUSED/STATE_PLAYING/STATE_STOPPED]
     */
    private void setPlayerState(int state) {
        mPlayerState = state;
        if (state == STATE_PLAYING) {
            viewPlayButton.setImageResource(R.drawable.ic_pause);
            snap.start();
        } else {
            if (snap.isActive())
                snap.stop();
            viewPlayButton.setImageResource(R.drawable.ic_play);
        }
    }

    /**
     * getRandom returns a random int from 0 to upperBound-1
     */
    private int getRandom(int upperBound) {
        return randomNumberClass.nextInt(upperBound);
    }

    // display current song. Would implement the actual media play here.
    private void playCurrentSong(Boolean resumeFromPause) {
        if (!resumeFromPause)
            seekBar.setProgress(0);
        if (snap.isInactive())
            snap.start();
        if (songsPlaylist.size() >= playlistPosition) {
            if (thisIsFirstSongPlayed) {
                thisIsFirstSongPlayed = false;
                imageViewNowPlayingAlbumArt.setVisibility(View.VISIBLE);
            }

            imageViewNowPlayingAlbumArt.setImageResource(R.drawable.ic_music_note);
            textViewNowPlayingArtist.setText(songsPlaylist.get(playlistPosition).getArtist());
            textViewNowPlayingSongTitle.setText(songsPlaylist.get(playlistPosition).getTitle());
            textViewNowPlayingAlbum.setText(songsPlaylist.get(playlistPosition).getAlbum());
            textViewNowPlayingSongTitle.invalidate();
            textViewNowPlayingArtist.invalidate();
            imageViewNowPlayingAlbumArt.invalidate();
            setPlayerState(STATE_PLAYING);
        }
    }

    /**
     * Return INTENT lands here
     *
     * @param requestCode  what the originating activity is requesting
     * @param resultCode   status the originating activity is returning to us
     * @param returnValues the values being passed to us from the originating activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent returnValues) {
        // Check which request we're responding to
        if (requestCode == PLAYLIST_MODIFY) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // retrieve the edited playlist
                songsPlaylist.clear();
                songsPlaylist = returnValues.getParcelableArrayListExtra("NewSongList");
                int newPlayListPosition = returnValues.getIntExtra("SongChangeRequested", -1);
                if (newPlayListPosition != -1) {
                    playlistPosition = newPlayListPosition;
                    playCurrentSong(false);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        // initialize the proper shuffle icon to the menu
        if (Shuffle) {
            menu.findItem(R.id.shuffle_toggle).setIcon(R.drawable.ic_shuffle_black_24dp);
            menu.findItem(R.id.shuffle_toggle).setTitle("Shuffle is ON");
        } else {
            menu.findItem(R.id.shuffle_toggle).setIcon(R.drawable.ic_no_shuffle_black_24dp);
            menu.findItem(R.id.shuffle_toggle).setTitle("Shuffle is OFF");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_playlist:
                launchPlaylist();
                break;
            case R.id.shuffle_toggle:
                Shuffle = !Shuffle;
                if (!Shuffle) {
                    item.setIcon(R.drawable.ic_no_shuffle_black_24dp);
                    Toast.makeText(this, "Shuffle Off", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    item.setIcon(R.drawable.ic_shuffle_black_24dp);
                    Toast.makeText(this, "Shuffle On", Toast.LENGTH_SHORT)
                            .show();
                }
            default:
                break;
        }
        return true;
    }

    /**
     * onCREATE method
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        snap = new SnapTimer(50, false);
        snap.setCustomObjectListener(new SnapTimer.SnapTimerListener() {
            @Override
            public void onTick() {
                // Has song ended?
                if (seekBar.getProgress() == seekBar.getMax()) {
                    seekBar.setProgress(0);
                    snap.stop();
                    nextSong();
                } else {
                    seekBar.setProgress(seekBar.getProgress() + 1);
                }
            }
        });

        viewPlayButton = findViewById(R.id.button_play);
        imageViewNowPlayingAlbumArt = findViewById(R.id.album_art);
        textViewNowPlayingSongTitle = findViewById(R.id.song_title_text);
        textViewNowPlayingArtist = findViewById(R.id.artist_text);
        textViewNowPlayingAlbum = findViewById(R.id.album_text);
        seekBar = findViewById(R.id.seekBar);
        setPlayerState(STATE_PAUSED);

        findViewById(R.id.button_play).setOnClickListener(play_button_onClickListener);
        findViewById(R.id.button_previous).setOnClickListener(previous_button_onClickListener);
        findViewById(R.id.button_next).setOnClickListener(next_button_onClickListener);

        // populate songLibrary with 75 songs
        discoverDeviceMedia();

        // populate a playlist with 5 songs
        // we know songLibrary has more than 3 songs since we populated the media beyond that
        // make sure song 1 is in the playlist for testing purposes
        int[] chosenSongs = new int[5];
        chosenSongs[0] = 0;
        songsPlaylist.add(songLibrary.get(chosenSongs[0]));

        boolean songAlreadyChosen;
        for (int count = 1; count < 5; count++) {
            chosenSongs[count] = getRandom(songLibrary.size());
            songAlreadyChosen = true;
            // pick random songs until one is found that is not already chosen
            while (songAlreadyChosen) {
                chosenSongs[count] = getRandom(songLibrary.size());
                songAlreadyChosen = false;
                for (int idx = 0; idx < count; idx++) {
                    if (chosenSongs[idx] == chosenSongs[count])
                        songAlreadyChosen = true;
                    if (songAlreadyChosen) break;
                }
            }
            // add it to the playlist
            songsPlaylist.add(songLibrary.get(chosenSongs[count]));
        }

    }

    // populate 75 available songs into the songLibrary list
    private void discoverDeviceMedia() {
        int counter = 0;
        for (int artist = 1; artist <= 5; artist++)
            for (int album = 1; album <= 3; album++)
                for (int title = 1; title <= 5; title++) {
                    counter++;
                    songLibrary.add(new Song("Album " + album, "Artist " + artist, "Genre " + getRandom(4) + 1, "Song " + counter));
                }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("shuffle", Shuffle);
        outState.putInt("play_list_position", playlistPosition);
        outState.putParcelableArrayList("PlayList", songsPlaylist);
        outState.putInt("mPlayerState", mPlayerState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Shuffle = savedInstanceState.getBoolean("shuffle");
        playlistPosition = savedInstanceState.getInt("play_list_position");
        songsPlaylist = savedInstanceState.getParcelableArrayList("PlayList");
        mPlayerState = savedInstanceState.getInt("mPlayerState");
        setPlayerState(mPlayerState);
    }

    // play the previous song in the playlist.
    // if shuffle, then randomly pick next song
    private void previousSong() {
        if (!Shuffle) {
            // sequential mode, are there prior songs in playlist?
            if (playlistPosition > 0) {
                // yes, play prior song in playlist
                playlistPosition -= 1;
                playCurrentSong(false);
            } else {
                // no
                // are there any other songs in playlist?
                if (songsPlaylist.size() > playlistPosition) {
                    // yes, play last song in playlist
                    playlistPosition = songsPlaylist.size() - 1;
                    playCurrentSong(false);
                }
            }
        } else {
            // shuffle mode, are there any other songs in the playlist?
            if (songsPlaylist.size() > 1) {
                // yes, randomly select a song until the selected song is not the current song
                int trialIndex = getRandom(songsPlaylist.size());
                while (trialIndex == playlistPosition) {
                    trialIndex = getRandom(songsPlaylist.size());
                }
                playlistPosition = trialIndex;
                playCurrentSong(false);
            }
        }
    }

    // advances to the next song to be played
    // if shuffle, then randomly pick next song
    private void nextSong() {
        if (!Shuffle) {
            // sequential mode, are there more songs in playlist?
            if (playlistPosition < songsPlaylist.size() - 1) {
                // yes, play next song in playlist
                playlistPosition += 1;
                playCurrentSong(false);
            } else {
                // no
                // are there any other songs in playlist?
                if (songsPlaylist.size() > 0) {
                    // yes, play first song in playlist
                    playlistPosition = 0;
                    playCurrentSong(false);
                }
            }
        } else {
            // shuffle mode, are there any other songs in the playlist?
            if (songsPlaylist.size() > 1) {
                // yes, randomly select a song until the selected song is not the current song
                int trialIndex = getRandom(songsPlaylist.size());
                while (trialIndex == playlistPosition) {
                    trialIndex = getRandom(songsPlaylist.size());
                }
                playlistPosition = trialIndex;
                playCurrentSong(false);
            } else {
                setPlayerState(STATE_PAUSED);
            }
        }
    }

    // start Playlist activity
    private void launchPlaylist() {
        Intent playlistIntent = new Intent(Activity_Main.this, Activity_Playlist.class);
        playlistIntent.putParcelableArrayListExtra("PlayList", songsPlaylist);
        playlistIntent.putParcelableArrayListExtra("AvailableList", songLibrary);
        startActivityForResult(playlistIntent, PLAYLIST_MODIFY);
    }

}

