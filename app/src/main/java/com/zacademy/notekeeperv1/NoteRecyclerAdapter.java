package com.zacademy.notekeeperv1;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        View itemView = mLayoutInflater.inflate(R.layout.item_note_list, parent, false);
//        itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(mContext, "Clicked " + parent, Toast.LENGTH_LONG).show();
//            }
//        });
        return new ViewHolder(itemView);// to NoteRecyclerAdapter's pool
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        NoteInfo note = mNotes.get(position);
        holder.mTextCourse.setText(note.getCourse().getTitle());
        holder.mTextTitle.setText(note.getTitle());
        holder.mCurrentPosition = position;

//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(mContext, NoteActivity.class);
//                intent.putExtra(NoteActivity.NOTE_POSITION, position);
//                mContext.startActivity(intent);
//            }
//        });
//
//        holder.mTextCourse.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(mContext,
//                        "Clicked: " + position + "th item: " + holder.mTextCourse.getText().toString(),
//                        Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        holder.mTextTitle.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(mContext,
//                        "Clicked: " + position + "th item: " + holder.mTextTitle.getText().toString(),
//                        Toast.LENGTH_SHORT).show();
//            }
//        });

    }

    @Override
    public int getItemCount() {
        return mNotes.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView mTextCourse;
        public final TextView mTextTitle;
        public int mCurrentPosition;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextCourse = itemView.findViewById(R.id.text_course);
            mTextTitle = itemView.findViewById(R.id.text_title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, NoteActivity.class);
                    intent.putExtra(NoteActivity.NOTE_POSITION, mCurrentPosition);
                    mContext.startActivity(intent);

                }
            });

            mTextCourse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext,
                            "Clicked: " + getLayoutPosition() + "th item: " + mTextCourse.getText().toString(),
                            Toast.LENGTH_SHORT).show();
                }
            });

            mTextTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext,
                            "Clicked: " + getAdapterPosition() + "th item: " + mTextTitle.getText().toString(),
                            Toast.LENGTH_SHORT).show();
                }
            });


        }
    }


}


//how do you cache references to subviews of the View to avoid unnecessary View.findViewById(int) calls