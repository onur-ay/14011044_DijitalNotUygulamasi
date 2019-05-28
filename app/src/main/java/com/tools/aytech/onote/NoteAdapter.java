package com.tools.aytech.onote;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.MyViewHolder> {
    private final OnItemClickListener listener;
    private final View.OnClickListener buttonListener;
    private ArrayList<Note> noteList;
    private LayoutInflater inflater;
    private MyViewHolder holder;
    private long idToDelete;

    public NoteAdapter(Context context, ArrayList<Note> noteList, OnItemClickListener listener, View.OnClickListener buttonListener) {
        this.noteList = noteList;
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
        this.buttonListener = buttonListener;
    }

    public long getIdToDelete() {
        return idToDelete;
    }

    public void setIdToDelete(long idToDelete) {
        this.idToDelete = idToDelete;
    }

    public MyViewHolder getHolder() {
        return holder;
    }

    public void setHolder(MyViewHolder holder) {
        this.holder = holder;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = this.inflater.inflate(R.layout.note_item_card, parent, false);
        this.holder = new MyViewHolder(view);
        return this.holder;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Note selectedNote = noteList.get(position);
        holder.setData(selectedNote);
        holder.bind(selectedNote, listener);
        holder.deleteNoteButton.setOnClickListener(buttonListener);
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView noteTitle, contentSummary, createdDate;
        ImageView attachmentIncluded;
        ImageView highImportance;
        ImageView noteColor;
        Button deleteNoteButton;

        public MyViewHolder(View itemView){
            super(itemView);
            this.noteTitle = itemView.findViewById(R.id.noteTitle);
            this.contentSummary = itemView.findViewById(R.id.contentSummary);
            this.createdDate = itemView.findViewById(R.id.createdDate);
            this.deleteNoteButton = itemView.findViewById(R.id.deleteNoteButton);
            this.attachmentIncluded = itemView.findViewById(R.id.attachmentIncluded);
            this.highImportance = itemView.findViewById(R.id.highImportance);
            this.noteColor = itemView.findViewById(R.id.noteColor);
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        public void setData(Note selectedNote) {
            this.noteTitle.setText(selectedNote.getNoteTitle());
            this.contentSummary.setText(selectedNote.getContentSummary());
            this.createdDate.setText(selectedNote.getCreatedDate());
            int attachmentVisibility = (selectedNote.getImageUri() != null || selectedNote.getVideoUri() != null || selectedNote.getSoundUri() != null || selectedNote.getDocUri() != null) ? VISIBLE : GONE;
            this.attachmentIncluded.setVisibility(attachmentVisibility);
            int priorityVisibility = (selectedNote.getPriority() == 1) ? VISIBLE : GONE;
            this.highImportance.setVisibility(priorityVisibility);
            this.noteColor.setBackground(new ColorDrawable(Color.parseColor(selectedNote.getColor())));
        }

        @Override
        public void onClick(View view) {

        }

        public void bind(final Note item, final OnItemClickListener listener){
            itemView.setOnClickListener((view) -> listener.onItemClick(item));
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Note item);
    }

    public interface OnClickListener {
        void onClick(Note item);
    }
}
