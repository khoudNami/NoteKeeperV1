package com.zacademy.notekeeperv1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NoteRecyclerAdapter extends RecyclerView.Adapter<NoteRecyclerAdapter.ViewHolder> {

    private final Context mContext;
    private final LayoutInflater mLayoutInflater;
    private final List<NoteInfo> mNotes;

    public NoteRecyclerAdapter(Context context, List<NoteInfo> notes) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mNotes = notes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mLayoutInflater.inflate(R.layout.item_note_list,parent,false);
        return new ViewHolder(itemView);// to NoteRecyclerAdapter's pool
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            NoteInfo note = mNotes.get(position);
            holder.mTextCourse.setText(note.getCourse().getTitle());
            holder.mTextTitle.setText(note.getTitle());
    }

    @Override
    public int getItemCount() {
        return mNotes.size();
    }

            class ViewHolder extends RecyclerView.ViewHolder {

                public final TextView mTextCourse;
                public final TextView mTextTitle;

                public ViewHolder(@NonNull View itemView) {
                    super(itemView);
                    mTextCourse = itemView.findViewById(R.id.text_course);
                    mTextTitle = itemView.findViewById(R.id.text_title);
                }
            }



}

























//how do you cache references to subviews of the View to avoid unnecessary View.findViewById(int) calls