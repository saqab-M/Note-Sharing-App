package com.uol.notesappuol.noteFeed;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.uol.notesappuol.LaunchScreenActivity;
import com.uol.notesappuol.MainActivity;
import com.uol.notesappuol.R;
import com.uol.notesappuol.model.Adapter;
import com.uol.notesappuol.model.Note;
import com.uol.notesappuol.note.AddNoteActivity;
import com.uol.notesappuol.toDo.ToDoActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NoteFeedActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Intent data;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    NavigationView nav_view;
    RecyclerView noteFeed;
    Adapter adapter;
    FirebaseFirestore fStore;
    FirebaseAuth fAuth;
    FirebaseUser user;
    FirestoreRecyclerAdapter<Note, NoteViewHolder> feedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_feed);

        //show navigation menu
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fAuth = FirebaseAuth.getInstance();
        user = fAuth.getCurrentUser();
        fStore = FirebaseFirestore.getInstance();
        Query query = fStore.collection("PublicNotes")
                .orderBy("title", Query.Direction.DESCENDING);


        FirestoreRecyclerOptions<Note> allNotes = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query,Note.class).build();

        feedAdapter = new FirestoreRecyclerAdapter<Note, NoteViewHolder>(allNotes) {

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder noteViewHolder, int i, @NonNull Note note) {
                // add data to each view
                noteViewHolder.noteTitle.setText(note.getTitle());
                noteViewHolder.noteContent.setText(note.getContent());
                Integer code = note.getColor();
                noteViewHolder.cardView.setCardBackgroundColor(noteViewHolder.view.getResources().getColor(code, null));

                //when note clicked, open note page
                noteViewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(v.getContext(), FeedNoteDetailsActivity.class);
                        i.putExtra("title", note.getTitle());
                        i.putExtra("content", note.getContent());
                        i.putExtra("username", note.getCreatedBy());
                        i.putExtra("code", code);
                        v.getContext().startActivity(i);
                    }
                });
            }

            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                //create view and inflate it to recycler view
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.note_view_layout,parent,false);
                return new NoteViewHolder(view);
            }
        };

        noteFeed = findViewById(R.id.noteFeedList);

        drawerLayout = findViewById(R.id.feedDrawer);
        nav_view = findViewById(R.id.nav_view_feed);
        nav_view.setNavigationItemSelectedListener(this);
        toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();



        noteFeed.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        noteFeed.setAdapter(feedAdapter);



        View headerView = nav_view.getHeaderView(0);
        TextView navUsername = headerView.findViewById(R.id.nav_username);
        TextView navEmail = headerView.findViewById(R.id.nav_email);


        // display username and password
        data = getIntent();
        navUsername.setText(data.getStringExtra("displayUsername"));
        navEmail.setText(data.getStringExtra("displayEmail"));

    }


    // check nav menu item selected and handle selection
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        //close drawer
        drawerLayout.closeDrawer(GravityCompat.START);
        switch (item.getItemId()){
            case R.id.notes:
                startActivity(new Intent(this, MainActivity.class));
                finish();
                break;
            case R.id.addNote:
                startActivity(new Intent(this, AddNoteActivity.class));
                break;
            case R.id.logout:
                // logout
                logout();
                break;
            case R.id.notesFeed:
                // do nothing just close drawer
                break;
            case R.id.toDo:
                // to-do activity
                Intent to_do = new Intent(this, ToDoActivity.class);
                to_do.putExtra("displayUsername", data.getStringExtra("displayUsername"));
                to_do.putExtra("displayEmail",data.getStringExtra("displayEmail"));
                startActivity(to_do);
                finish();
                break;
            default:
                Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
        }


        return false;
    }

    private void logout() {

        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), LaunchScreenActivity.class));
        finish();
    }


    // display search menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu,menu);
        MenuItem item = menu.findItem(R.id.feedSearch);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(NoteFeedActivity.this, searchView.getQuery(), Toast.LENGTH_SHORT).show();
                searchFeed(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchFeed(newText);
                return false;
            }
        });


        return super.onCreateOptionsMenu(menu);
    }

    private void searchFeed(String searchTerm){
        Query query = fStore.collection("PublicNotes")
                .whereGreaterThanOrEqualTo("content", searchTerm);
                //.whereLessThan("title", searchTerm+'z');

        FirestoreRecyclerOptions<Note> allNotes = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query,Note.class).build();


        // set adapter
        feedAdapter = new FirestoreRecyclerAdapter<Note, NoteViewHolder>(allNotes) {

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder noteViewHolder, int i, @NonNull Note note) {
                // add data to each view
                noteViewHolder.noteTitle.setText(note.getTitle());
                noteViewHolder.noteContent.setText(note.getContent());
                Integer code = getRandomColor();
                noteViewHolder.cardView.setCardBackgroundColor(noteViewHolder.view.getResources().getColor(code, null));

                //when note clicked, open note page
                noteViewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(v.getContext(), FeedNoteDetailsActivity.class);
                        i.putExtra("title", note.getTitle());
                        i.putExtra("content", note.getContent());
                        i.putExtra("username", note.getCreatedBy());
                        i.putExtra("code", code);
                        v.getContext().startActivity(i);
                    }
                });
            }

            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                //create view and inflate it to recycler view
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.note_view_layout,parent,false);
                return new NoteViewHolder(view);
            }
        };
        noteFeed.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        feedAdapter.startListening();
        noteFeed.setAdapter(feedAdapter);
    }




    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.settings){
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
    //------------------------------




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

    public class NoteViewHolder extends RecyclerView.ViewHolder{
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

    @Override
    protected void onStart() {
        super.onStart();
        feedAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(feedAdapter != null){
            feedAdapter.stopListening();
        }
    }
}

