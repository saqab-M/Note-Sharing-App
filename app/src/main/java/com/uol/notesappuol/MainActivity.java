package com.uol.notesappuol;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.uol.notesappuol.model.Adapter;
import com.uol.notesappuol.model.Note;
import com.uol.notesappuol.note.AddNoteActivity;
import com.uol.notesappuol.note.EditNoteActivity;
import com.uol.notesappuol.note.NoteDetailsActivity;
import com.uol.notesappuol.noteFeed.NoteFeedActivity;
import com.uol.notesappuol.toDo.ToDoActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // set variables for components
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    NavigationView nav_view;
    RecyclerView noteList;
    Adapter adapter;
    FirebaseFirestore fStore;
    FirestoreRecyclerAdapter<Note, NoteViewHolder> noteAdapter;
    FirebaseAuth fAuth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //show navigation menu
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fAuth = FirebaseAuth.getInstance();
        user = fAuth.getCurrentUser();

        //get data from database
        fStore = FirebaseFirestore.getInstance();

        // query notes >> uuid >> mynotes >> all notes
        Query query = fStore.collection("PrivateNotes")
                .document(user.getUid()).collection("myNotes").orderBy("title", Query.Direction.DESCENDING);
        // Note class
        FirestoreRecyclerOptions<Note> allNotes = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query,Note.class).build();
        noteAdapter = new FirestoreRecyclerAdapter<Note, NoteViewHolder>(allNotes) {
            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.note_view_layout,parent,false);
                return new NoteViewHolder(view);
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder noteViewHolder, @SuppressLint("RecyclerView") int i, @NonNull Note note) {
                // add data to each view
                noteViewHolder.noteTitle.setText(note.getTitle());
                noteViewHolder.noteContent.setText(note.getContent());
                //Integer code = getRandomColor();
                Integer code = note.getColor();
                noteViewHolder.cardView.setCardBackgroundColor(noteViewHolder.view.getResources().getColor(code, null));
                String docId = noteAdapter.getSnapshots().getSnapshot(i).getId();

                //when note clicked, open note page
                noteViewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(v.getContext(), NoteDetailsActivity.class);
                        i.putExtra("title", note.getTitle());
                        i.putExtra("content", note.getContent());
                        i.putExtra("code", code);
                        i.putExtra("noteId",docId);
                        v.getContext().startActivity(i);
                    }
                });


                //pop up options menu
                ImageView menuIcon = noteViewHolder.view.findViewById(R.id.menuIcon);
                menuIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final String docId = noteAdapter.getSnapshots().getSnapshot(i).getId();

                        PopupMenu menu = new PopupMenu(v.getContext(),v);
                        menu.setGravity(Gravity.END);
                        //menu options
                        menu.getMenu().add("Edit").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                //redirect to edit activity
                                Intent i = new Intent(v.getContext(), EditNoteActivity.class);
                                i.putExtra("title", note.getTitle());
                                i.putExtra("content",note.getContent());
                                i.putExtra("noteId",docId);

                                startActivity(i);
                                return false;
                            }
                        });

                        menu.getMenu().add("Delete").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                // delete note from firebase
                                DocumentReference docref = fStore.collection("PrivateNotes").document(user.getUid())
                                        .collection("myNotes").document(docId);
                                docref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(MainActivity.this, "Note Deleted", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MainActivity.this, "Error, try again", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                // delete note from public database
                                DocumentReference deleteref = fStore.collection("PublicNotes").document(docId);
                                deleteref.delete();

                                return false;
                            }
                        });

                        //show menu
                        menu.show();

                    }
                });
                //----------------------------------------------



            }
        };

        //assign components
        noteList = findViewById(R.id.noteList);
        drawerLayout = findViewById(R.id.drawer);
        nav_view = findViewById(R.id.nav_view);
        nav_view.setNavigationItemSelectedListener(this);

        toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();

        noteList.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        noteList.setAdapter(noteAdapter);

        View headerView = nav_view.getHeaderView(0);
        TextView navUsername = headerView.findViewById(R.id.nav_username);
        TextView navEmail = headerView.findViewById(R.id.nav_email);


        // display username and password
        navUsername.setText(user.getDisplayName());
        navEmail.setText(user.getEmail());


        // floating button
        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), AddNoteActivity.class));
            }
        });

    }


    // check nav menu item selected and handle selection
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        //close drawer
        drawerLayout.closeDrawer(GravityCompat.START);
        switch (item.getItemId()){
            case R.id.notes:
                //refresh notes page
                startActivity(new Intent(this, MainActivity.class));
                finish();
                break;
            case R.id.addNote:
                // add note activity
                startActivity(new Intent(this, AddNoteActivity.class));
                break;
            case R.id.notesFeed:
                // note feed activity
                Intent feed = new Intent(this, NoteFeedActivity.class);
                feed.putExtra("displayUsername", user.getDisplayName());
                feed.putExtra("displayEmail",user.getEmail());
                startActivity(feed);
                finish();
                break;
            case R.id.toDo:
                // to-do activity
                Intent to_do = new Intent(this, ToDoActivity.class);
                to_do.putExtra("displayUsername", user.getDisplayName());
                to_do.putExtra("displayEmail",user.getEmail());
                startActivity(to_do);
                finish();
                break;
            case R.id.logout:
                // logout
                logout();
                break;

            default:
                Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void logout() {

        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(),LaunchScreenActivity.class));
        finish();
    }



    // display options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.settings){
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
    //------------------------------


    public class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView noteTitle, noteContent;
        View view;
        CardView cardView;

        public NoteViewHolder(@NonNull View itemView) {

            super(itemView);
            noteTitle = itemView.findViewById(R.id.titles);
            noteContent = itemView.findViewById(R.id.content);
            cardView = itemView.findViewById(R.id.noteCard);
            view = itemView;
        }
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
    protected void onStart() {
        super.onStart();
        noteAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(noteAdapter != null){
            noteAdapter.stopListening();
        }
    }
}