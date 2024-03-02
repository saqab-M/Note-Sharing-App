package com.uol.notesappuol.model;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uol.notesappuol.R;
import com.uol.notesappuol.toDo.ToDoActivity;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CompToDoAdapter extends RecyclerView.Adapter<CompToDoAdapter.MyViewHolder> {

    private List<ToDo> toDoList;
    private ToDoActivity activity;
    private FirebaseFirestore fStore;
    FirebaseAuth fAuth;
    FirebaseUser user;

    public CompToDoAdapter(ToDoActivity toDoActivity, List<ToDo> toDoList){
        this.toDoList = toDoList;
        activity = toDoActivity;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.card_task, parent, false);
        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        user = fAuth.getCurrentUser();

        return new MyViewHolder(view);
    }


    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {


        Date currentTime = Calendar.getInstance().getTime();
        ToDo toDo = toDoList.get(position);
        holder.checkBox.setVisibility(View.GONE);
        holder.dueDate.setText(toDo.getTask());

    }

    @Override
    public int getItemCount() {
        return toDoList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView dueDate;
        public CheckBox checkBox;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            dueDate = itemView.findViewById(R.id.dueDateText);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }
}
