package com.uol.notesappuol.note;

import android.os.Bundle;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uol.notesappuol.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AddNoteActivity extends AppCompatActivity {

    FirebaseFirestore fstore;
    EditText noteTitle, noteContent;
    ProgressBar progressBarSave;
    FirebaseUser user;
    CheckBox checkPublish;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true); // back button

        fstore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        noteTitle = findViewById(R.id.addNoteTitle);
        noteContent = findViewById(R.id.addNoteContent);
        progressBarSave = findViewById(R.id.progressBarSave);
        checkPublish = findViewById(R.id.checkPublic);



        FloatingActionButton fab = findViewById(R.id.fabEdit);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = noteTitle.getText().toString();
                String content = noteContent.getText().toString();

                if( title.isEmpty() || content.isEmpty()){
                    Toast.makeText(AddNoteActivity.this, "cant save empty note!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBarSave.setVisibility(View.VISIBLE);

                // save  note to firebase
                // Collection notes >> note, note, ...

                // check if checkbox is checked
                boolean publishNote = checkPublish.isChecked();

                //get random color
                Integer colorCode = getRandomColor();

                //create document/reference document
                DocumentReference docref = fstore.collection("PrivateNotes").document(user.getUid()).collection("myNotes").document();
                Map<String,Object> note = new HashMap<>();
                note.put("title", title);
                note.put("content", content);
                note.put("published", publishNote);
                note.put("color", colorCode);

                docref.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(AddNoteActivity.this, "Note Saved", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddNoteActivity.this, "Error, try again", Toast.LENGTH_SHORT).show();
                        progressBarSave.setVisibility(View.INVISIBLE);
                    }
                });

                if(publishNote){
                    // save note to public collection
                    DocumentReference docrefPublic = fstore.collection("PublicNotes").document(docref.getId());
                    note = new HashMap<>();
                    note.put("title", title);
                    note.put("content", content);
                    note.put("published", publishNote);
                    note.put("createdBy", user.getDisplayName()); // save username
                    note.put("noteId", docref.getId()); // document id
                    note.put("color", colorCode);


                    docrefPublic.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(AddNoteActivity.this, "Note Published", Toast.LENGTH_SHORT).show();
                            onBackPressed();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AddNoteActivity.this, "Error, try again", Toast.LENGTH_SHORT).show();
                            progressBarSave.setVisibility(View.INVISIBLE);
                        }
                    });
                    //-------------------------------------------
                }else{
                    Toast.makeText(AddNoteActivity.this, "private Note", Toast.LENGTH_SHORT).show();
                }


                //-------------------------------------------


            }
        });

    }


    //close menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflate = getMenuInflater();
        inflate.inflate(R.menu.close_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.close){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
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


    //-----------------------------------
}