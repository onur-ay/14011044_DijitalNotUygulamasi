package com.tools.aytech.onote;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;
import com.bumptech.glide.Glide;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
//import com.nbsp.materialfilepicker.MaterialFilePicker;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;
import java.util.regex.Pattern;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.tools.aytech.onote.DBOpenHelper.*;
import static com.tools.aytech.onote.LandingPage.EXTERNAL_OPERATIONS;
import static com.tools.aytech.onote.NotesProvider.*;

public class NoteEditor extends AppCompatActivity {

    private final static String DEFAULT_NOTE_COLOR = "#00574B";
    private final static int CONTENT_SUMMARY_LENGTH = 65;
    private final static int MEDIA_PIC = 1;
    private final static int MEDIA_VID = 2;
    private final static int MEDIA_MUS = 3;
    private final static int MEDIA_DOC = 4;
    private static boolean IS_PLAYING_VIDEO = false;
    private static boolean IS_PLAYING_SOUND = false;

    Note currentNote;
    private String action;
    private SeekBar noteVideoSeekbar;
    private SeekBar noteSoundSeekbar;
    private Button playPauseNoteVideoButton;
    private Button playPauseNoteSoundButton;
    private TextView noteLastModifiedDateText;
    private TextView noteCreatedDateText;
    private TextView noteSoundTitle;
    private Button deleteVideoAttachmentButton;
    private Button deleteImageAttachmentButton;
    private Button deleteSoundAttachmentButton;
    private ImageButton noteDocument;
    private EditText noteTitleEditText;
    private EditText noteContentEditText;
    private ImageView noteImageView;
    private RelativeLayout videoViewCard;
    private LinearLayout noteVideoView;
    private VideoView noteVideo;
    private MediaPlayer noteSound;
    private String oldText;
    private String oldTitle;
    private Uri oldImage;
    private Uri newImage;
    private Uri oldVideo;
    private Uri newVideo;
    private Uri oldSound;
    private Uri newSound;
    private Uri oldDoc;
    private Uri newDoc;
    private String oldSoundTitle;
    private String newSoundTitle;
    private String oldColor;
    private String newColor;
    private boolean oldImportant;
    private boolean newImportant;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);
        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_attach);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setOverflowIcon(drawable);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        noteTitleEditText = findViewById(R.id.noteTitleEditText);
        noteContentEditText = findViewById(R.id.noteContentEditText);
        noteImageView = findViewById(R.id.noteImageView);
        videoViewCard = findViewById(R.id.videoCardView);
        noteVideoView = findViewById(R.id.noteVideoView);
        noteVideo = findViewById(R.id.noteVideo);
        noteSound = new MediaPlayer();
        noteDocument = findViewById(R.id.noteDocument);
        noteSoundTitle = findViewById(R.id.noteSoundTitle);
        playPauseNoteVideoButton = findViewById(R.id.playPauseNoteVideoButton);
        playPauseNoteSoundButton = findViewById(R.id.playPauseNoteSoundButton);
        deleteImageAttachmentButton = findViewById(R.id.deleteImageAttachmentButton);
        deleteSoundAttachmentButton = findViewById(R.id.deleteSoundAttachmentButton);
        deleteVideoAttachmentButton = findViewById(R.id.deleteVideoAttachmentButton);
        noteVideoSeekbar = findViewById(R.id.noteVideoSeekBar);
        noteSoundSeekbar = findViewById(R.id.noteSoundSeekBar);
        deleteImageAttachmentButton.setOnClickListener(v -> {
            newImage = null;
            findViewById(R.id.imageCardView).setVisibility(GONE);
            noteImageView.setImageResource(0);
        });
        deleteVideoAttachmentButton.setOnClickListener(v -> {
            newVideo = null;
            findViewById(R.id.videoCardView).setVisibility(GONE);
            noteVideo.setBackgroundResource(0);
        });
        deleteSoundAttachmentButton.setOnClickListener(v -> {
            newSound = null;
            findViewById(R.id.soundCardView).setVisibility(GONE);
            noteSound.reset();
        });
        noteVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onPrepared(MediaPlayer mp) {
                noteVideo.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, mp.getVideoHeight()));
                noteVideoSeekbar.setMax(noteVideo.getDuration());
                noteVideoSeekbar.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        noteVideoSeekbar.setProgress(noteVideo.getCurrentPosition());
                        if(noteVideo.isPlaying())
                            noteVideoSeekbar.postDelayed(this, 0);
                        else
                            noteVideo.seekTo(noteVideoSeekbar.getProgress());
                    }
                }, 0);
                playPauseNoteVideoButton.setOnClickListener(v -> {
                    if(IS_PLAYING_VIDEO){
                        noteVideo.pause();
                        IS_PLAYING_VIDEO = false;
                        playPauseNoteVideoButton.setBackground(getResources().getDrawable(R.drawable.ic_play));
                    } else {
                        if (noteVideoSeekbar.getMax() == noteVideoSeekbar.getProgress()){
                            noteVideoSeekbar.setProgress(0);
                            noteVideo.seekTo(0);
                        } else
                            noteVideo.start();
                        noteVideoSeekbar.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                noteVideoSeekbar.setProgress(noteVideo.getCurrentPosition());
                                if(noteVideo.isPlaying()){
                                    noteVideoSeekbar.postDelayed(this, 0);
                                }
                            }
                        }, 0);
                        IS_PLAYING_VIDEO = true;
                        playPauseNoteVideoButton.setBackground(getResources().getDrawable(R.drawable.ic_pause));
                    }
                });
                noteVideoSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if(fromUser){
                            if(seekBar.getProgress() == seekBar.getMax()){
                                seekBar.setProgress(0);
                                noteVideo.seekTo(0);
                            } else
                                noteVideo.seekTo(progress);
                            if(IS_PLAYING_VIDEO)
                                noteVideo.start();
                        } else if(seekBar.getProgress() == seekBar.getMax()){
                            seekBar.setProgress(0);
                            noteVideo.seekTo(0);
                            playPauseNoteVideoButton.setBackground(getResources().getDrawable(R.drawable.ic_play));
                            IS_PLAYING_VIDEO=false;
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        (new Runnable() {
                            @Override
                            public void run() {
                                seekBar.setProgress(seekBar.getProgress());
                            }
                        }).run();
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        noteVideo.seekTo(seekBar.getProgress());
                    }
                });
            }
        });
        noteSound.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                noteSoundSeekbar.setMax(noteSound.getDuration());
                noteSoundSeekbar.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        noteSoundSeekbar.setProgress(noteSound.getCurrentPosition());
                        if(noteSound.isPlaying())
                            noteSoundSeekbar.postDelayed(this,0);
                        else
                            noteSound.seekTo(noteSoundSeekbar.getProgress());
                    }
                },0);
                playPauseNoteSoundButton.setOnClickListener(v -> {
                    if(IS_PLAYING_SOUND){
                        noteSound.pause();
                        IS_PLAYING_SOUND = false;
                        playPauseNoteSoundButton.setBackground(getResources().getDrawable(R.drawable.ic_play));
                    } else {
                        if (noteSoundSeekbar.getMax() == noteSoundSeekbar.getProgress()){
                            noteSoundSeekbar.setProgress(0);
                            noteSound.seekTo(0);
                        } else
                            noteSound.start();
                        noteSoundSeekbar.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                noteSoundSeekbar.setProgress(noteSound.getCurrentPosition());
                                if(noteSound.isPlaying()){
                                    noteSoundSeekbar.postDelayed(this, 0);
                                }
                            }
                        }, 0);
                        IS_PLAYING_SOUND = true;
                        playPauseNoteSoundButton.setBackground(getResources().getDrawable(R.drawable.ic_pause));
                    }
                });
                noteSoundSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if(fromUser){
                            if(seekBar.getProgress() == seekBar.getMax()){
                                seekBar.setProgress(0);
                                noteSound.seekTo(0);
                            } else
                                noteSound.seekTo(progress);
                            if(IS_PLAYING_SOUND)
                                noteSound.start();
                        } else if(seekBar.getMax()-seekBar.getProgress()<5){
                            seekBar.setProgress(0);
                            noteSound.seekTo(0);
                            playPauseNoteSoundButton.setBackground(getResources().getDrawable(R.drawable.ic_play));
                            IS_PLAYING_SOUND=false;
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        (new Runnable() {
                            @Override
                            public void run() {
                                seekBar.setProgress(seekBar.getProgress());
                            }
                        }).run();
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        noteSound.seekTo(seekBar.getProgress());
                    }
                });
            }
        });
        noteLastModifiedDateText = findViewById(R.id.noteLastModifiedDateText);
        noteCreatedDateText = findViewById(R.id.noteCreatedDateText);
        Intent incomingIntent = getIntent();
        this.action = incomingIntent.getStringExtra("action");
        currentNote = decodeIntent(incomingIntent);

        if(action.equals(Intent.ACTION_INSERT)) {
            setTitle(R.string.NEW_NOTE_ACTIVITY_TITLE);
        } else if(action.equals(Intent.ACTION_EDIT)) {
            setTitle(R.string.UPDATE_NOTE_ACTIVITY_TITLE);
            oldText = currentNote.getContent();
            oldTitle = currentNote.getNoteTitle();
            oldImage = currentNote.getImageUri();
            newImage = currentNote.getImageUri();
            oldVideo = currentNote.getVideoUri();
            newVideo = currentNote.getVideoUri();
            oldSound = currentNote.getSoundUri();
            newSound = currentNote.getSoundUri();
            oldSoundTitle = currentNote.getSoundTitle();
            newSoundTitle = currentNote.getSoundTitle();
            oldImportant = currentNote.getPriority() == 1;
            newImportant = currentNote.getPriority() == 1;
            if(oldImage != null){
                findViewById(R.id.imageCardView).setVisibility(VISIBLE);
                Glide.with(getApplicationContext()).load(oldImage).into(noteImageView);
            }
            else
                findViewById(R.id.imageCardView).setVisibility(GONE);
            if(oldVideo != null){
                findViewById(R.id.videoCardView).setVisibility(VISIBLE);
                noteVideo.setVideoURI(oldVideo);
            }
            else
                findViewById(R.id.videoCardView).setVisibility(GONE);
            if(oldSound != null){
                try {
                    findViewById(R.id.soundCardView).setVisibility(VISIBLE);
                    noteSoundTitle.setText(oldSoundTitle);
                    noteSound.setDataSource(this, oldSound);
                    noteSound.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else
                findViewById(R.id.soundCardView).setVisibility(GONE);
            oldColor = currentNote.getColor();
            newColor = currentNote.getColor();
            if(oldTitle.equals("(Untitled)"))
                noteTitleEditText.setText(null);
            else
                noteTitleEditText.setText(oldTitle);
            noteContentEditText.setText(oldText);
            noteLastModifiedDateText.setText(String.format("Last Modified Date  :  %s", currentNote.getUpdatedDate()));
            noteCreatedDateText.setText(String.format("Created Date           :  %s", currentNote.getCreatedDate()));
        }
    }

    @Override
    public void onBackPressed() {
        if(action.equals(Intent.ACTION_EDIT))
            discardEditing("unchanged");
        else if(action.equals(Intent.ACTION_INSERT))
            discardEditing("discarded");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note_editor, menu);
        if(oldImportant)
            menu.findItem(R.id.ACTION_MAKE_IMPORTANT).setIcon(getResources().getDrawable(R.drawable.ic_high_importance_light));
        else
            menu.findItem(R.id.ACTION_MAKE_IMPORTANT).setIcon(null);
        setExternalOperationsAccordingToPermissions(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int button = item.getItemId();
        switch(button) {
            case R.id.ACTION_MAKE_IMPORTANT:
                makeNoteImportant(item);
                break;
            case R.id.ACTION_SAVE_NOTE:
                finishEditingAction();
                break;
            case R.id.ACTION_ATTACH_PICTURE_NOTE:
                attachPictureToNote();
                break;
            case R.id.ACTION_ATTACH_VIDEO_NOTE:
                attachVideoToNote();
                break;
            case R.id.ACTION_ATTACH_SOUND_NOTE:
                attachSoundToNote();
                break;
            case R.id.ACTION_ATTACH_DOC_NOTE:
                attachDocToNote();
                break;
            case R.id.ACTION_SET_COLOR:
                setColorOfNote();
                break;
            case R.id.ACTION_SET_REMINDER:
                //setReminderOfNote();
                break;
            case android.R.id.home:
                if(action.equals(Intent.ACTION_EDIT))
                    discardEditing("unchanged");
                else if(action.equals(Intent.ACTION_INSERT))
                    discardEditing("discarded");
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case MEDIA_PIC:
                if(resultCode == RESULT_OK && null != data){
                    newImage = data.getData();
                    if(newImage != null){
                        Glide.with(getApplicationContext()).load(newImage).into(noteImageView);
                        if(findViewById(R.id.imageCardView).getVisibility() != VISIBLE)
                            findViewById(R.id.imageCardView).setVisibility(VISIBLE);
                    }
                }
                break;
            case MEDIA_VID:
                if(resultCode == RESULT_OK && null != data){
                    newVideo = data.getData();
                    if(newVideo != null){
                        noteVideo.setVideoURI(newVideo);
                        if(findViewById(R.id.videoCardView).getVisibility() != VISIBLE)
                            findViewById(R.id.videoCardView).setVisibility(VISIBLE);
                    }
                }
                break;
            case MEDIA_MUS:
                if(resultCode == RESULT_OK && null != data){
                    String path = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
                    newSoundTitle = path.substring(path.lastIndexOf('/')+1, path.length());
                    newSound = Uri.fromFile(new File(path));
                    if(newSound != null){
                        try {
                            noteSoundTitle.setText(newSoundTitle);
                            noteSound.setDataSource(this, newSound);
                            noteSound.prepare();
                            if(findViewById(R.id.soundCardView).getVisibility() != VISIBLE)
                                findViewById(R.id.soundCardView).setVisibility(VISIBLE);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case MEDIA_DOC:
                if(resultCode == RESULT_OK && null != data){
                    String path = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
                    String fileType = path.substring(path.lastIndexOf('.')+1, path.length()).substring(0,3);
                    newDoc = Uri.fromFile(new File(path));
                    if(newDoc != null){
                        noteDocument.setBackgroundResource(getResources().getIdentifier("/drawable/ic_"+fileType,"id", this.getPackageName()));
                        noteDocument.setOnClickListener(v -> {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            if (Build.VERSION.SDK_INT >= 24){
                                Uri tempUri = FileProvider.getUriForFile(
                                        NoteEditor.this,
                                        NoteEditor.this.getPackageName() + ".fileprovider",
                                        new File(path));
                                intent.setDataAndType(tempUri, "application/"+fileType);
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            } else{
                                intent.setDataAndType(newDoc, "application/"+fileType);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            }
                            startActivity(intent);
                        });
                        if(findViewById(R.id.documentCardView).getVisibility() != VISIBLE)
                            findViewById(R.id.documentCardView).setVisibility(VISIBLE);
                    }
                }
                break;
        }
    }


    private void setColorOfNote() {
        ColorPickerDialogBuilder
                .with(this)
                .setTitle("Choose color")
                .initialColor(Color.parseColor(DEFAULT_NOTE_COLOR))
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setOnColorSelectedListener(selectedColor -> {})
                .setPositiveButton("ok", (dialog, selectedColor, allColors) -> setColor(selectedColor))
                .setNegativeButton("cancel", (dialog, which) -> {})
                .build()
                .show();
    }

    private void setColor(int color) {
        newColor = String.format("#%06X", (0xFFFFFF & color));
    }

    private void makeNoteImportant(MenuItem item){
        newImportant = !newImportant;
        if(newImportant)
            item.setIcon(getResources().getDrawable(R.drawable.ic_high_importance_light));
        else
            item.setIcon(null);
    }

    private Note decodeIntent(Intent incomingIntent) {
        Note incomingNote = new Note();
        if(incomingIntent.hasExtra("ID"))
            incomingNote.setID(incomingIntent.getLongExtra("ID",0));
        if(incomingIntent.hasExtra("Priority"))
            incomingNote.setPriority(incomingIntent.getIntExtra("Priority",0));
        if(incomingIntent.hasExtra("Title"))
            incomingNote.setNoteTitle(incomingIntent.getStringExtra("Title"));
        if(incomingIntent.hasExtra("Content"))
            incomingNote.setContent(incomingIntent.getStringExtra("Content"));
        if(incomingIntent.hasExtra("CreatedDate"))
            incomingNote.setCreatedDate(incomingIntent.getStringExtra("CreatedDate"));
        if(incomingIntent.hasExtra("UpdatedDate"))
            incomingNote.setUpdatedDate(incomingIntent.getStringExtra("UpdatedDate"));
        if(incomingIntent.hasExtra("Color"))
            incomingNote.setColor(incomingIntent.getStringExtra("Color"));
        if(incomingIntent.hasExtra("Image"))
            incomingNote.setImageUri(Uri.parse(incomingIntent.getStringExtra("Image")));
        if(incomingIntent.hasExtra("Video"))
            incomingNote.setVideoUri(Uri.parse(incomingIntent.getStringExtra("Video")));
        if(incomingIntent.hasExtra("Sound"))
            incomingNote.setSoundUri(Uri.parse(incomingIntent.getStringExtra("Sound")));
        if(incomingIntent.hasExtra("SoundTitle"))
            incomingNote.setSoundTitle(incomingIntent.getStringExtra("SoundTitle"));
        if(incomingIntent.hasExtra("Document"))
            incomingNote.setDocUri(Uri.parse(incomingIntent.getStringExtra("Document")));
        return incomingNote;
    }

    private void attachSoundToNote() {
        startActivityForResult(new MaterialFilePicker()
                .withActivity(this)
                .withRequestCode(1)
                .withFilter(Pattern.compile(".*\\.(mp3|wav|ogg|m4a|flac|aac)$")).withTitle("Pick a sound").withCloseMenu(true).getIntent(),MEDIA_MUS);
        setResult(RESULT_OK);
    }

    private void attachVideoToNote() {
        Intent uploadVideoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(uploadVideoIntent, MEDIA_VID);
        setResult(RESULT_OK);
    }

    private void attachPictureToNote() {
        Intent uploadImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(uploadImageIntent, MEDIA_PIC);
        setResult(RESULT_OK);
    }

    private void attachDocToNote() {
        startActivityForResult(new MaterialFilePicker()
                .withActivity(this)
                .withRequestCode(1)
                .withFilter(Pattern.compile(".*\\.(pdf|ppt|pptx|doc|docx|xls|xlsx|txt)$"))
                .withTitle("Pick a document")
                .withCloseMenu(true)
                .getIntent(),MEDIA_DOC);
        setResult(RESULT_OK);
    }

    private void finishEditingAction(){
        String noteTitle = noteTitleEditText.getText().toString().trim();
        String noteContent = noteContentEditText.getText().toString().trim();

        switch(action) {
            case Intent.ACTION_INSERT:
                if(noteContent.length() == 0 && noteImageView.getVisibility() == GONE) {
                    discardEditing("discarded");
                } else {
                    insertNewNote(noteTitle, noteContent);
                }
                break;
            case Intent.ACTION_EDIT:
                if(noteContent.length() == 0 && noteImageView.getVisibility() == GONE) {
                    deleteNote();
                } else if(oldText.equals(noteContent) && oldTitle.equals(noteTitle) && (newImage == oldImage) && (newVideo == oldVideo) && (newSound == oldSound) && oldImportant == newImportant && oldColor.equals(newColor)) {
                    discardEditing("unchanged");
                } else {
                    updateNote(noteTitle, noteContent);
                }
        }
    }

    private void deleteNote() {
        getContentResolver().delete(CONTENT_URI,String.format(NOTE_ID+"=%s",currentNote.getID()),null);
        applyEditing("deleted");
    }

    @SuppressLint("SimpleDateFormat")
    private void updateNote(String noteTitle, String noteText){
        ContentValues values = new ContentValues();
        Calendar currentTime = Calendar.getInstance();
        currentTime.setTimeInMillis(System.currentTimeMillis());
        values.put(NOTE_TITLE, noteTitle);
        values.put(NOTE_TEXT, noteText);
        values.put(NOTE_TEXT_SUMMARY, getSummaryOfContent(noteText));
        if(newImage != null)
            values.put(NOTE_IMAGE, newImage.toString());
        else
            values.putNull(NOTE_IMAGE);
        if(newVideo != null)
            values.put(NOTE_VIDEO, newVideo.toString());
        else
            values.putNull(NOTE_VIDEO);
        if(newSound!= null){
            values.put(NOTE_SOUND_TITLE, newSoundTitle);
            values.put(NOTE_SOUND, newSound.toString());
        }
        else{
            values.putNull(NOTE_SOUND_TITLE);
            values.putNull(NOTE_SOUND);
        }
        if(newImportant)
            values.put(NOTE_IMPORTANT, 1);
        else
            values.put(NOTE_IMPORTANT, 0);
        values.put(NOTE_COLOR, newColor);
        values.put(NOTE_CREATED, currentNote.getCreatedDate());
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(currentTime.getTime());
        values.put(NOTE_UPDATED, now);
        getContentResolver().update(CONTENT_URI, values, String.format(NOTE_ID+"=%s",currentNote.getID()), null);
        applyEditing("updated");
        currentNote.setNoteTitle(noteTitle);
        currentNote.setContent(noteText);
        currentNote.setUpdatedDate(now);
        currentNote.setImageUri(newImage);
        currentNote.setVideoUri(newVideo);
        currentNote.setSoundUri(newSound);
        currentNote.setSoundTitle(newSoundTitle);
        currentNote.setColor(newColor);
        if(newImportant)
            currentNote.setPriority(1);
        else
            currentNote.setPriority(0);
    }

    private void insertNewNote(String noteTitle, String noteContent) {
        ContentValues values = new ContentValues();
        values.put(NOTE_TEXT, noteContent);
        values.put(NOTE_TEXT_SUMMARY, getSummaryOfContent(noteContent));
        if(noteTitle != null){
            if(noteTitle.isEmpty())
                values.put(NOTE_TITLE, "(Untitled)");
            else
                values.put(NOTE_TITLE, noteTitle);
        }
        if(newImage != null)
            values.put(NOTE_IMAGE, newImage.toString());
        if(newVideo != null)
            values.put(NOTE_VIDEO, newVideo.toString());
        if(newSound != null)
            values.put(NOTE_SOUND, newSound.toString());
        if(newImportant)
            values.put(NOTE_IMPORTANT, 1);
        else
            values.put(NOTE_IMPORTANT, 0);
        getContentResolver().insert(CONTENT_URI, values);
        applyEditing("inserted");
    }

    private void discardEditing(String status) {
        setResult(RESULT_CANCELED, new Intent(this, LandingPage.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).putExtra("status",status));
        finish();
    }

    private void applyEditing(String status) {
        setResult(RESULT_OK, new Intent(this, LandingPage.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).putExtra("status",status));
        finish();
    }

    private void setExternalOperationsAccordingToPermissions(Menu menu){
        MenuItem picture = menu.findItem(R.id.ACTION_ATTACH_PICTURE_NOTE);
        MenuItem video = menu.findItem(R.id.ACTION_ATTACH_VIDEO_NOTE);
        MenuItem sound = menu.findItem(R.id.ACTION_ATTACH_SOUND_NOTE);
        MenuItem doc = menu.findItem(R.id.ACTION_ATTACH_DOC_NOTE);
        picture.setEnabled(EXTERNAL_OPERATIONS);
        video.setEnabled(EXTERNAL_OPERATIONS);
        sound.setEnabled(EXTERNAL_OPERATIONS);
        doc.setEnabled(EXTERNAL_OPERATIONS);
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
