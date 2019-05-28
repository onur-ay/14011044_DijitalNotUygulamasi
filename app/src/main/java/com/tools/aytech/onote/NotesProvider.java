package com.tools.aytech.onote;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import static com.tools.aytech.onote.DBOpenHelper.*;

public class NotesProvider extends ContentProvider {

    public static final String AUTHORITY = "com.tools.aytech.digitalnoteapp.notesprovider";
    public static final String BASE_PATH = "notes";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    public static final int NOTES = 1;
    public static final int NOTES_ID = 2;

    public SQLiteDatabase database;

    public static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static final String CONTENT_ITEM_TYPE = "Note";

    static {
        uriMatcher.addURI(AUTHORITY, BASE_PATH, NOTES);
        uriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", NOTES_ID);
    }

    @Override
    public boolean onCreate() {
        DBOpenHelper helper = new DBOpenHelper(getContext());
        database = helper.getWritableDatabase();
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {
        if(uriMatcher.match(uri) == NOTES_ID) {
            s = NOTE_ID + "=" + uri.getLastPathSegment();
        }
        return database.query(TABLE_NOTES, ALL_COLUMNS, s, null, null, null, NOTE_IMPORTANT + " DESC, " + NOTE_UPDATED + " DESC");
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        long id = database.insert(TABLE_NOTES, null, contentValues);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return database.delete(TABLE_NOTES, s, strings);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return database.update(TABLE_NOTES, contentValues, s, strings);
    }
}
