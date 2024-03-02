package com.uol.notesappuol.note;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.uol.notesappuol.MainActivity;
import com.uol.notesappuol.R;

import java.util.HashMap;
import java.util.Map;

public class EditNoteActivity extends AppCompatActivity {

    Intent data;
    EditText editTitle, editContent;
    FirebaseFirestore fStore;
    FirebaseUser user;
    CheckBox editCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fStore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        ProgressBar progressBar = findViewById(R.id.progressBarEdit);

        data = getIntent();

        editTitle = findViewById(R.id.editNoteTitle);
        editCheck = findViewById(R.id.editCheckPublic);
        editContent = findViewById(R.id.editNoteContent);


        DocumentReference doc = fStore.collection("PrivateNotes")
                .document(user.getUid()).collection("myNotes").document(data.getStringExtra("noteId"));
        doc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot snap = task.getResult();
                    if(snap.exists()){
                        editCheck.setChecked(snap.getBoolean("published"));
                    }
                }
            }
        });


        String noteTitle = data.getStringExtra("title");
        String noteContent = data.getStringExtra("content");



        editTitle.setText(noteTitle);
        editContent.setText(noteContent);


        FloatingActionButton fab = findViewById(R.id.fabSave);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String title = editTitle.getText().toString();
                String content = editContent.getText().toString();

                if (title.isEmpty() || content.isEmpty()) {
                    Toast.makeText(EditNoteActivity.this, "cant save empty note!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                // save  note to firebase
                // Collection notes >> note, note, ...

                boolean publishNote = editCheck.isChecked();

                if(!publishNote){

                    // delete note from firebase
                    DocumentReference deleteref = fStore.collection("PublicNotes").document(data.getStringExtra(("noteId")));
                    deleteref.delete();
                }else{

                    //create document/reference document
                    DocumentReference publicref = fStore.collection("PublicNotes").document(data.getStringExtra("noteId"));
                    Map<String, Object> note = new HashMap<>();
                    note.put("title", title);
                    note.put("content", content);
                    note.put("published", publishNote);

                    publicref.update(note).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            note.put("createdBy", user.getDisplayName()); // save username
                            note.put("noteId", data.getStringExtra("noteId")); // document id
                            publicref.set(note);
                        }
                    });


                }

                //create document/reference document
                DocumentReference docref = fStore.collection("PrivateNotes").document(user.getUid()).collection("myNotes").document(data.getStringExtra("noteId"));
                Map<String, Object> note = new HashMap<>();
                note.put("title", title);
                note.put("content", content);
                note.put("published", publishNote);

                docref.update(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(EditNoteActivity.this, "Note Saved", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(v.getContext(), MainActivity.class));
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditNoteActivity.this, "Error, try again", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
                //-------------------------------------------


            }
        });

    }
}