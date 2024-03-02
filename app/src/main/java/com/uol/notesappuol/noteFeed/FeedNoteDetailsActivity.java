package com.uol.notesappuol.noteFeed;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uol.notesappuol.R;
import com.uol.notesappuol.note.AddNoteActivity;

import java.util.HashMap;
import java.util.Map;

public class FeedNoteDetailsActivity extends AppCompatActivity {

    FirebaseFirestore fstore;
    FirebaseUser user;
    Intent data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_note_details);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        data = getIntent(); // get put extra
        fstore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        //setup note
        TextView title = findViewById(R.id.feedNoteDetailsTitle);
        TextView createdBy = findViewById(R.id.noteCreatorName);
        TextView content = findViewById(R.id.feedNoteDetailsContent);
        content.setMovementMethod(new ScrollingMovementMethod());

        title.setText(data.getStringExtra("title"));
        createdBy.setText("created by: "+ data.getStringExtra("username"));
        content.setText(data.getStringExtra("content"));
        Integer code = data.getIntExtra("code",0);
        content.setBackgroundColor(getResources().getColor(code));
        //------------------------------

        FloatingActionButton fab = findViewById(R.id.fabCopyNote);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create document/reference document
                DocumentReference docref = fstore.collection("PrivateNotes").document(user.getUid()).collection("myNotes").document();
                Map<String,Object> note = new HashMap<>();
                note.put("title", title.getText());
                String text = "{created by: "+ data.getStringExtra("username") +" } "+ content.getText();
                note.put("content", text);
                note.put("published", false);
                note.put("color", code);

                docref.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(FeedNoteDetailsActivity.this, "Note Saved", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(FeedNoteDetailsActivity.this, "Error, try again", Toast.LENGTH_SHORT).show();
                    }
                });
                //-------------------------------------------
            }
        });




    }

    //back button
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


}