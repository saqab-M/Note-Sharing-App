package com.uol.notesappuol.model;

import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.uol.notesappuol.note.NoteDetailsActivity;
import com.uol.notesappuol.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    //dummy data
    List<String> titles;
    List<String> content;

    public Adapter(List<String> titles, List<String> content){
        this.titles = titles;
        this.content = content;

    }

    @NonNull
    @Override
    public Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //create view and inflate it to recycler view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_view_layout,parent,false);
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull Adapter.ViewHolder holder, int position) {
        // add data to each view
        holder.noteTitle.setText(titles.get(position));
        holder.noteContent.setText(content.get(position));
        Integer code = getRandomColor();
        holder.cardView.setCardBackgroundColor(holder.view.getResources().getColor(code, null));

        //when note clicked, open note page
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), NoteDetailsActivity.class);
                i.putExtra("title", titles.get(position));
                i.putExtra("content", content.get(position));
                i.putExtra("code", code);
                v.getContext().startActivity(i);
            }
        });

    }

    private int getRandomColor() {
        List<Integer> colorCode = new ArrayList<>();
        colorCode.add(R.color.yellow);
        colorCode.add(R.color.lightGreen);
        colorCode.add(R.color.skyblue);
        colorCode.add(R.color.pink);
        colorCode.add(R.color.lightPurple);

        Random randomColor = new Random();
        int number = randomColor.nextInt(colorCode.size());

        return colorCode.get(number);
    }

    @Override
    public int getItemCount() {
        return titles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView noteTitle, noteContent;
        View view;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            noteTitle = itemView.findViewById(R.id.titles);
            noteContent = itemView.findViewById(R.id.content);
            cardView = itemView.findViewById(R.id.noteCard);
            view = itemView;
        }
    }
}
