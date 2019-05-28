package com.tools.aytech.onote;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static com.tools.aytech.onote.DBOpenHelper.*;
import static com.tools.aytech.onote.NotesProvider.*;

public class LandingPage extends AppCompatActivity {

    private static final String TAG = LandingPage.class.getSimpleName();
    private static final String PACKAGE = "com.tools.aytech.digitalnoteapp";
    private static final int EDITOR_REQUEST_CODE = 1;
    public static final int ONOTE_PERMISSION_TO_READ_EXTERNAL_STORAGE = 10;
    private final static int CONTENT_SUMMARY_LENGTH = 65;
    public static boolean EXTERNAL_OPERATIONS;
    private static SharedPreferences prefs = null;
    private RecyclerView noteRecyclerView;
    private Cursor noteCursor;
    private NoteAdapter noteAdapter;
    private ArrayList<Note> noteList;
    private FloatingActionButton fab;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PACKAGE, MODE_PRIVATE);
        checkPermissions();
        setContentView(R.layout.activity_landing_page);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fab = findViewById(R.id.addNoteButton);
        fab.setOnClickListener((view) -> openEditorToInsertNewNote());

        restartLoader();
        setRecyclerViewLayout();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_landing_page, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case R.id.ACTION_CREATE_SAMPLE:
                insertSampleData();
                break;
            case R.id.ACTION_DELETE_ALL:
                deleteAllNotes();
                break;
            case R.id.ACTION_GRID_LAYOUT:
                GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
                noteRecyclerView.setLayoutManager(gridLayoutManager);
                break;
            case R.id.ACTION_LINEAR_LAYOUT:
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
                linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                noteRecyclerView.setLayoutManager(linearLayoutManager);
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String operationStatus = data.getStringExtra("status");
        if(requestCode == EDITOR_REQUEST_CODE && resultCode == RESULT_OK) {
            restartLoader();
            switch (operationStatus) {
                case "inserted":
                    infoMessage(getString(R.string.NEW_NOTE_INSERT_TEXT));
                    break;
                case "updated":
                    infoMessage(getString(R.string.NOTE_UPDATE_TEXT));
                    break;
                case "deleted":
                    infoMessage(getString(R.string.NOTE_DELETE_TEXT));
                    break;
            }
        } else if(resultCode == RESULT_CANCELED) {
            if(operationStatus.equals("discarded"))
                infoMessage(getString(R.string.NEW_NOTE_DISCARD_TEXT));
            else if(operationStatus.equals("unchanged"))
                infoMessage(getString(R.string.NOTE_UNCHANGE_TEXT));
        }
    }

    private void openEditorToInsertNewNote() {
        Intent intent = new Intent(getApplication(), NoteEditor.class);
        intent.putExtra("action",Intent.ACTION_INSERT);
        startActivityForResult(intent, EDITOR_REQUEST_CODE);
    }

    private void openEditorToUpdateNote(Note item){
        Intent intent = new Intent(LandingPage.this, NoteEditor.class);
        intent.putExtra("action",Intent.ACTION_EDIT);
        intent.putExtra("ID",item.getID());
        intent.putExtra("Priority",item.getPriority());
        if(item.getNoteTitle() != null)
            intent.putExtra("Title",item.getNoteTitle());
        if(item.getContent() != null)
            intent.putExtra("Content",item.getContent());
        if(item.getCreatedDate() != null)
            intent.putExtra("CreatedDate",item.getCreatedDate());
        if(item.getUpdatedDate() != null)
            intent.putExtra("UpdatedDate",item.getUpdatedDate());
        if(item.getColor() != null)
            intent.putExtra("Color",item.getColor());
        if(item.getImageUri() != null)
            intent.putExtra("Image",item.getImageUri().toString());
        if(item.getVideoUri() != null)
            intent.putExtra("Video",item.getVideoUri().toString());
        if(item.getSoundUri() != null)
            intent.putExtra("Sound",item.getSoundUri().toString());
        if(item.getSoundTitle() != null)
            intent.putExtra("SoundTitle",item.getSoundTitle());
        if(item.getDocUri() != null)
            intent.putExtra("Document",item.getDocUri().toString());
        startActivityForResult(intent, EDITOR_REQUEST_CODE);
    }

    private void insertNote(String noteTitle, String noteText, String noteColor) {
        Calendar currentTime = Calendar.getInstance();
        currentTime.setTimeInMillis(System.currentTimeMillis());
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(currentTime.getTime());
        ContentValues values = new ContentValues();
        values.put(NOTE_TITLE, noteTitle);
        values.put(NOTE_TEXT, noteText);
        values.put(NOTE_TEXT_SUMMARY, getSummaryOfContent(noteText));
        values.put(NOTE_COLOR, noteColor);
        values.put(NOTE_UPDATED, now);
        Uri noteUri = getContentResolver().insert(CONTENT_URI, values);
        if (noteUri != null) {
            Log.d(TAG, "Inserted Note: " + noteUri.getLastPathSegment());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void deleteAllNotes() {
        DialogInterface.OnClickListener dialogClickListener = (dialogInterface, button) -> {
            if(button == DialogInterface.BUTTON_POSITIVE){
                getContentResolver().delete(CONTENT_URI, null, null);
                restartLoader();
                infoMessage(getString(R.string.ALL_NOTES_DELETE_TEXT));
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.ARE_YOU_SURE_TEXT)
                .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                .setNegativeButton(getString(android.R.string.no), dialogClickListener)
                .show();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void insertSampleData() {
        insertNote("Alınacaklar", "- Domates\n- Makarna\n- Pilav\n- Salça\n- Yumurta\n- Un\n- Ekmek\n- Çikolata\n- Bisküvi", "#A00101");
        insertNote("Günlük","\tSevgili günlük,\n\n\tDeneme günlük yazısı. Deneme günlük yazısı. Deneme günlük yazısı. Deneme günlük yazısı. Deneme günlük yazısı. Deneme günlük yazısı. Deneme günlük yazısı. Deneme günlük yazısı. Deneme günlük yazısı. Deneme günlük yazısı.\n\n\tDeneme günlük yazısı. Deneme günlük yazısı. Deneme günlük yazısı. Deneme günlük yazısı. Deneme günlük yazısı. Deneme günlük yazısı. Deneme günlük yazısı. Deneme günlük yazısı.\n\n\tDeneme günlük yazısı. Deneme günlük yazısı. Deneme günlük yazısı. Deneme günlük yazısı. Deneme günlük yazısı.","#005900");
        insertNote("Resim Testi","Bu not resim yükleme testi yapmak için oluşturulmuştur.","#EF00BF");
        insertNote("Uzun Metin ile Resim Testi", "Bu not uzun metin ile resim testi yapmak için oluşturulmuştur. Bu not uzun metin ile resim testi yapmak için oluşturulmuştur. Bu not uzun metin ile resim testi yapmak için oluşturulmuştur. Bu not uzun metin ile resim testi yapmak için oluşturulmuştur. Bu not uzun metin ile resim testi yapmak için oluşturulmuştur. Bu not uzun metin ile resim testi yapmak için oluşturulmuştur. Bu not uzun metin ile resim testi yapmak için oluşturulmuştur. Bu not uzun metin ile resim testi yapmak için oluşturulmuştur. Bu not uzun metin ile resim testi yapmak için oluşturulmuştur. Bu not uzun metin ile resim testi yapmak için oluşturulmuştur. Bu not uzun metin ile resim testi yapmak için oluşturulmuştur. Bu not uzun metin ile resim testi yapmak için oluşturulmuştur. Bu not uzun metin ile resim testi yapmak için oluşturulmuştur. Bu not uzun metin ile resim testi yapmak için oluşturulmuştur. Bu not uzun metin ile resim testi yapmak için oluşturulmuştur. Bu not uzun metin ile resim testi yapmak için oluşturulmuştur. Bu not uzun metin ile resim testi yapmak için oluşturulmuştur. Bu not uzun metin ile resim testi yapmak için oluşturulmuştur. Bu not uzun metin ile resim testi yapmak için oluşturulmuştur. Bu not uzun metin ile resim testi yapmak için oluşturulmuştur. Bu not uzun metin ile resim testi yapmak için oluşturulmuştur. Bu not uzun metin ile resim testi yapmak için oluşturulmuştur. Bu not uzun metin ile resim testi yapmak için oluşturulmuştur. Bu not uzun metin ile resim testi yapmak için oluşturulmuştur. Bu not uzun metin ile resim testi yapmak için oluşturulmuştur. Bu not uzun metin ile resim testi yapmak için oluşturulmuştur. Bu not uzun metin ile resim testi yapmak için oluşturulmuştur. Bu not uzun metin ile resim testi yapmak için oluşturulmuştur. Bu not uzun metin ile resim testi yapmak için oluşturulmuştur. Bu not uzun metin ile resim testi yapmak için oluşturulmuştur. Bu not uzun metin ile resim testi yapmak için oluşturulmuştur. Bu not uzun metin ile resim testi yapmak için oluşturulmuştur.","#000D70");
        insertNote("Müzik Testi", "Bu not müzik testi yapmak için oluşturulmuştur.","#00C9BF");
        insertNote("Uzun Metin ile Müzik Testi", "Bu not uzun metin ile müzik testi yapmak için oluşturulmuştur. Bu not uzun metin ile müzik testi yapmak için oluşturulmuştur. Bu not uzun metin ile müzik testi yapmak için oluşturulmuştur. Bu not uzun metin ile müzik testi yapmak için oluşturulmuştur. Bu not uzun metin ile müzik testi yapmak için oluşturulmuştur. Bu not uzun metin ile müzik testi yapmak için oluşturulmuştur. Bu not uzun metin ile müzik testi yapmak için oluşturulmuştur. Bu not uzun metin ile müzik testi yapmak için oluşturulmuştur. Bu not uzun metin ile müzik testi yapmak için oluşturulmuştur. Bu not uzun metin ile müzik testi yapmak için oluşturulmuştur. Bu not uzun metin ile müzik testi yapmak için oluşturulmuştur. Bu not uzun metin ile müzik testi yapmak için oluşturulmuştur. Bu not uzun metin ile müzik testi yapmak için oluşturulmuştur. Bu not uzun metin ile müzik testi yapmak için oluşturulmuştur. Bu not uzun metin ile müzik testi yapmak için oluşturulmuştur. Bu not uzun metin ile müzik testi yapmak için oluşturulmuştur. Bu not uzun metin ile müzik testi yapmak için oluşturulmuştur. Bu not uzun metin ile müzik testi yapmak için oluşturulmuştur. Bu not uzun metin ile müzik testi yapmak için oluşturulmuştur. Bu not uzun metin ile müzik testi yapmak için oluşturulmuştur. Bu not uzun metin ile müzik testi yapmak için oluşturulmuştur. Bu not uzun metin ile müzik testi yapmak için oluşturulmuştur. Bu not uzun metin ile müzik testi yapmak için oluşturulmuştur. Bu not uzun metin ile müzik testi yapmak için oluşturulmuştur. Bu not uzun metin ile müzik testi yapmak için oluşturulmuştur. Bu not uzun metin ile müzik testi yapmak için oluşturulmuştur. Bu not uzun metin ile müzik testi yapmak için oluşturulmuştur. Bu not uzun metin ile müzik testi yapmak için oluşturulmuştur. Bu not uzun metin ile müzik testi yapmak için oluşturulmuştur. Bu not uzun metin ile müzik testi yapmak için oluşturulmuştur. Bu not uzun metin ile müzik testi yapmak için oluşturulmuştur. Bu not uzun metin ile müzik testi yapmak için oluşturulmuştur.","#FAFF00");
        insertNote("Video Testi", "Bu not video testi yapmak için oluşturulmuştur.","#C96400");
        insertNote("Uzun Metin ile Video Testi", "Bu not uzun Metin ile video testi yapmak için oluşturulmuştur. Bu not uzun Metin ile video testi yapmak için oluşturulmuştur. Bu not uzun Metin ile video testi yapmak için oluşturulmuştur. Bu not uzun Metin ile video testi yapmak için oluşturulmuştur. Bu not uzun Metin ile video testi yapmak için oluşturulmuştur. Bu not uzun Metin ile video testi yapmak için oluşturulmuştur. Bu not uzun Metin ile video testi yapmak için oluşturulmuştur. Bu not uzun Metin ile video testi yapmak için oluşturulmuştur. Bu not uzun Metin ile video testi yapmak için oluşturulmuştur. Bu not uzun Metin ile video testi yapmak için oluşturulmuştur. Bu not uzun Metin ile video testi yapmak için oluşturulmuştur. Bu not uzun Metin ile video testi yapmak için oluşturulmuştur. Bu not uzun Metin ile video testi yapmak için oluşturulmuştur. Bu not uzun Metin ile video testi yapmak için oluşturulmuştur. Bu not uzun Metin ile video testi yapmak için oluşturulmuştur. Bu not uzun Metin ile video testi yapmak için oluşturulmuştur. Bu not uzun Metin ile video testi yapmak için oluşturulmuştur. Bu not uzun Metin ile video testi yapmak için oluşturulmuştur. Bu not uzun Metin ile video testi yapmak için oluşturulmuştur. Bu not uzun Metin ile video testi yapmak için oluşturulmuştur. Bu not uzun Metin ile video testi yapmak için oluşturulmuştur. Bu not uzun Metin ile video testi yapmak için oluşturulmuştur. Bu not uzun Metin ile video testi yapmak için oluşturulmuştur. Bu not uzun Metin ile video testi yapmak için oluşturulmuştur. Bu not uzun Metin ile video testi yapmak için oluşturulmuştur. Bu not uzun Metin ile video testi yapmak için oluşturulmuştur. Bu not uzun Metin ile video testi yapmak için oluşturulmuştur. Bu not uzun Metin ile video testi yapmak için oluşturulmuştur. Bu not uzun Metin ile video testi yapmak için oluşturulmuştur. Bu not uzun Metin ile video testi yapmak için oluşturulmuştur. Bu not uzun Metin ile video testi yapmak için oluşturulmuştur. Bu not uzun Metin ile video testi yapmak için oluşturulmuştur.","#60004D");
        restartLoader();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void restartLoader() {
        noteCursor = getContentResolver().query(CONTENT_URI,null,null,null,NOTE_IMPORTANT + " DESC, " + NOTE_UPDATED + " DESC",null);
        noteList = new ArrayList<>();
        convertCursorToArrayList();

        ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) fab.getLayoutParams();
        if(noteList.size() > 8){
            p.setMargins(0,0,155,16);
            p.setMarginEnd(155);
        }
        else{
            p.setMargins(0,0,32,16);
            p.setMarginEnd(32);
        }
        fab.setLayoutParams(p);

        noteAdapter = new NoteAdapter(this, noteList, this::openEditorToUpdateNote, v -> {
            ViewGroup parent = (ViewGroup) v.getParent();
            String noteTitle = ((TextView)parent.findViewById(R.id.noteTitle)).getText().toString();
            String contentSummary = ((TextView)parent.findViewById(R.id.contentSummary)).getText().toString();
            String createdDate = ((TextView)parent.findViewById(R.id.createdDate)).getText().toString();
            String noteFilter = NOTE_TITLE +"='"+noteTitle+"'";
            noteFilter = noteFilter +" AND "+ NOTE_TEXT_SUMMARY +"='"+ contentSummary+"'";
            noteFilter = noteFilter +" AND "+ NOTE_CREATED +"='"+ createdDate+"'";
            getContentResolver().delete(CONTENT_URI,noteFilter,null);
            infoMessage(getString(R.string.NOTE_DELETE_TEXT));
            restartLoader();
        });
        noteRecyclerView = findViewById(R.id.noteRecyclerView);
        noteRecyclerView.setAdapter(noteAdapter);
    }

    private void setRecyclerViewLayout() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        noteRecyclerView.setLayoutManager(linearLayoutManager);
    }

    private void convertCursorToArrayList() {
        for(noteCursor.moveToFirst(); !noteCursor.isAfterLast(); noteCursor.moveToNext()) {
            long noteID = noteCursor.getInt(noteCursor.getColumnIndex(NOTE_ID));
            String noteTitle = noteCursor.getString(noteCursor.getColumnIndex(NOTE_TITLE));
            String content = noteCursor.getString(noteCursor.getColumnIndex(NOTE_TEXT));
            String contentSummary = noteCursor.getString(noteCursor.getColumnIndex(NOTE_TEXT_SUMMARY));
            String createdDate = noteCursor.getString(noteCursor.getColumnIndex(NOTE_CREATED));
            String updatedDate = noteCursor.getString(noteCursor.getColumnIndex(NOTE_UPDATED));
            String imageUri = noteCursor.getString(noteCursor.getColumnIndex(NOTE_IMAGE));
            String videoUri = noteCursor.getString(noteCursor.getColumnIndex(NOTE_VIDEO));
            String soundUri = noteCursor.getString(noteCursor.getColumnIndex(NOTE_SOUND));
            String soundTitle = noteCursor.getString(noteCursor.getColumnIndex(NOTE_SOUND_TITLE));
            String docUri = noteCursor.getString(noteCursor.getColumnIndex(NOTE_DOCUMENT));
            String colorHex = noteCursor.getString(noteCursor.getColumnIndex(NOTE_COLOR));
            int priority = noteCursor.getInt(noteCursor.getColumnIndex(NOTE_IMPORTANT));
            Note currentNote = new Note(noteID, noteTitle,content,createdDate);
            if(imageUri != null)
                currentNote.setImageUri(Uri.parse(imageUri));
            if(videoUri != null)
                currentNote.setVideoUri(Uri.parse(videoUri));
            if(soundUri != null){
                currentNote.setSoundUri(Uri.parse(soundUri));
                currentNote.setSoundTitle(soundTitle);
            }
            if(docUri != null)
                currentNote.setDocUri(Uri.parse(docUri));
            currentNote.setContentSummary(contentSummary);
            currentNote.setUpdatedDate(updatedDate);
            currentNote.setPriority(priority);
            currentNote.setColor(colorHex);
            noteList.add(currentNote);
        }
    }

    private void infoMessage(String text){
        Snackbar.make(findViewById(R.id.addNoteButton), text, Snackbar.LENGTH_SHORT).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void checkPermissions(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            EXTERNAL_OPERATIONS=true;
        else if(prefs.getBoolean("firstrun",true)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, ONOTE_PERMISSION_TO_READ_EXTERNAL_STORAGE);
            prefs.edit().putBoolean("firstrun",false).apply();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ONOTE_PERMISSION_TO_READ_EXTERNAL_STORAGE:
                EXTERNAL_OPERATIONS = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
            default:
                break;
        }
    }

    private String getSummaryOfContent(String content) {
        int pos = content.indexOf(10);
        if(pos != -1)
            return content.substring(0,pos)+"...";
        else if(content.length() > CONTENT_SUMMARY_LENGTH)
            return content.substring(0,CONTENT_SUMMARY_LENGTH);
        else
            return content;
    }
}
