package com.examples.android.musicplayerexample;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * {@link Song}
 * Individual song and it's details
 */
public class Song implements Parcelable {
    // method used by Parcelable creation
    public static final Creator<Song> CREATOR
            = new Creator<Song>() {
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        public Song[] newArray(int size) {
            return new Song[size];
        }
    };
    // Album of song
    private final String mAlbum;
    // Artist of Song
    private final String mArtist;
    // Genre of song
    private final String mGenre;
    // Title of Song
    private final String mTitle;
    // Checkbox State for Various selection Needs
    private boolean mIsChecked;

    /**
     * self explanatory parameters.
     *
     * @param album  String: Title of the album containing the song
     * @param artist String: Name of the artist
     * @param genre  String: Genre of the song
     * @param title  String: Title of the song
     */
    public Song(String album, String artist, String genre, String title) {
        mAlbum = album;
        mArtist = artist;
        mGenre = genre;
        mTitle = title;
        mIsChecked = false;
    }

    // Parcelable READS, order must match WRITES
    private Song(Parcel in) {
        mAlbum = in.readString();
        mArtist = in.readString();
        mGenre = in.readString();
        mTitle = in.readString();
        mIsChecked = in.readInt() == 1;
    }

    // Getter(s):
    public String getAlbum() {
        return mAlbum;
    }

    public String getArtist() {
        return mArtist;
    }

    public String getGenre() {
        return mGenre;
    }

    public String getTitle() {
        return mTitle;
    }

    public boolean isChecked() {
        return mIsChecked;
    }

    // Setter(s)
    public void setChecked(Boolean checked) {
        mIsChecked = checked;
    }

    // required method for Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    // Parcelable WRITES, order must match READS
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mAlbum);
        dest.writeString(mArtist);
        dest.writeString(mGenre);
        dest.writeString(mTitle);
        if (mIsChecked) {
            dest.writeInt(1);
        } else {
            dest.writeInt(0);
        }
    }

}
