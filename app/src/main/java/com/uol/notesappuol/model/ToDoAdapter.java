package com.uol.notesappuol.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uol.notesappuol.R;
import com.uol.notesappuol.note.AddNoteActivity;
import com.uol.notesappuol.toDo.AddNewTask;
import com.uol.notesappuol.toDo.ToDoActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToDoAdapter  extends RecyclerView.Adapter<ToDoAdapter.MyViewHolder> {

    private List<ToDo> toDoList;
    private ToDoActivity activity;
    private FirebaseFirestore fStore;
    FirebaseAuth fAuth;
    FirebaseUser user;

    public ToDoAdapter(ToDoActivity toDoActivity, List<ToDo> toDoList){
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

    public Context getContext(){
        return activity;
    }

    public void deleteTask(int position){
        ToDo toDo = toDoList.get(position);
        fStore.collection("PrivateNotes").document(user.getUid()).collection("Tasks").document(toDo.TaskId).delete();
        toDoList.remove(position);
        notifyItemRemoved(position);
    }

    public void completed(int position){
        ToDo toDo = toDoList.get(position);
        DocumentReference docref = fStore.collection("PrivateNotes").document(user.getUid()).collection("completedTasks").document();
        Map<String,Object> task = new HashMap<>();
        task.put("task", toDo.getTask());
        task.put("dueDate", toDo.getDueDate());
        task.put("id", toDo.TaskId);
        task.put("status", toDo.getStatus());
        docref.set(task).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getContext(), "task Completed", Toast.LENGTH_SHORT).show();
            }
        });


    }

    public  void editTask(int position){
        ToDo toDo = toDoList.get(position);

        Bundle bundle = new Bundle();
        bundle.putString("task", toDo.getTask());
        bundle.putString("dueDate", toDo.getDueDate());
        bundle.putString("id", toDo.TaskId);

        AddNewTask addNewTask = new AddNewTask();
        addNewTask.setArguments(bundle);
        addNewTask.show(activity.getSupportFragmentManager(), addNewTask.getTag());

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {

        ToDo toDo = toDoList.get(position);
        holder.checkBox.setText(toDo.getTask());
        holder.dueDate.setText("Due on "+ toDo.getDueDate());

        holder.checkBox.setChecked(toBoolean(toDo.getStatus()));
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    //move to completed list
                    completed(position);
                    //delete from to_do list
                    deleteTask(position);
                }
            }
        });


    }

    private boolean toBoolean(int status){
        return status != 0;
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
