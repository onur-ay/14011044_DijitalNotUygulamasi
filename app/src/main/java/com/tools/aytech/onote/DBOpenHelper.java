package com.tools.aytech.onote;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

public class DBOpenHelper extends SQLiteOpenHelper {

    public static final String CURRENT_TIME = "datetime('now','localtime')";
    public static final String DATABASE_NAME = "notes.db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_NOTES = "notes";
    public static final String NOTE_ID =  "_id";
    public static final String NOTE_TEXT = "noteText";
    public static final String NOTE_TEXT_SUMMARY = "noteTextSummary";
    public static final String NOTE_TITLE = "noteTitle";
    public static final String NOTE_CREATED = "noteCreated";
    public static final String NOTE_UPDATED = "noteUpdated";
    public static final String NOTE_IMAGE = "noteImage";
    public static final String NOTE_VIDEO = "noteVideo";
    public static final String NOTE_SOUND = "noteSound";
    public static final String NOTE_SOUND_TITLE = "noteSoundTitle";
    public static final String NOTE_DOCUMENT = "noteDocument";
    public static final String NOTE_IMPORTANT = "priority";
    public static final String NOTE_COLOR = "color";
    public static final String[] ALL_COLUMNS = {NOTE_ID,
            NOTE_TITLE,
            NOTE_TEXT,
            NOTE_TEXT_SUMMARY,
            NOTE_CREATED,
            NOTE_UPDATED,
            NOTE_IMAGE,
            NOTE_VIDEO,
            NOTE_SOUND,
            NOTE_SOUND_TITLE,
            NOTE_DOCUMENT,
            NOTE_IMPORTANT,
            NOTE_COLOR};

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NOTES + " (" +
                    NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    NOTE_TITLE + " TEXT, " +
                    NOTE_TEXT + " TEXT, " +
                    NOTE_TEXT_SUMMARY + " TEXT, " +
                    NOTE_IMAGE + " TEXT default null, " +
                    NOTE_VIDEO + " TEXT default null, " +
                    NOTE_SOUND + " TEXT default null, " +
                    NOTE_SOUND_TITLE + " TEXT default null, " +
                    NOTE_DOCUMENT + " TEXT default null, " +
                    NOTE_IMPORTANT + " INTEGER default 0, " +
                    NOTE_COLOR + " TEXT default '#00574B'," +
                    NOTE_UPDATED + " TEXT, " +
                    NOTE_CREATED + " TEXT default (" + CURRENT_TIME + "))";

    public DBOpenHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        onCreate(sqLiteDatabase);
    }
}
