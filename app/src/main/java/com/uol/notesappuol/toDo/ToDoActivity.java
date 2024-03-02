package com.uol.notesappuol.toDo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.uol.notesappuol.LaunchScreenActivity;
import com.uol.notesappuol.MainActivity;
import com.uol.notesappuol.R;
import com.uol.notesappuol.model.CompToDoAdapter;
import com.uol.notesappuol.model.ToDo;
import com.uol.notesappuol.model.ToDoAdapter;
import com.uol.notesappuol.note.AddNoteActivity;
import com.uol.notesappuol.noteFeed.NoteFeedActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ToDoActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Intent data;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    NavigationView nav_view;
    RecyclerView recyclerView;
    RecyclerView compView;
    FloatingActionButton fab;
    FirebaseFirestore fStore;
    FirebaseAuth fAuth;
    FirebaseUser user;
    ToDoAdapter adapter;
    CompToDoAdapter compAdapter;

    List<ToDo> list;
    List<ToDo> compList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do);

        //show navigation menu
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fAuth = FirebaseAuth.getInstance();
        user = fAuth.getCurrentUser();

        drawerLayout = findViewById(R.id.toDoDrawer);
        nav_view = findViewById(R.id.nav_view_todo);
        nav_view.setNavigationItemSelectedListener(this);
        toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();

        recyclerView = findViewById(R.id.todo_list);
        compView = findViewById(R.id.todo_list_complete);
        fab = findViewById(R.id.fabAddTask);
        fStore = FirebaseFirestore.getInstance();

        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(ToDoActivity.this));
        compView.setHasFixedSize(false);
        compView.setLayoutManager(new LinearLayoutManager(ToDoActivity.this));


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNewTask.newInstance().show(getSupportFragmentManager(),AddNewTask.TAG);
            }
        });

        list = new ArrayList<>();
        adapter = new ToDoAdapter(ToDoActivity.this, list);

        compList = new ArrayList<>();
        compAdapter = new CompToDoAdapter(ToDoActivity.this, compList);


        recyclerView.setAdapter(adapter);
        compView.setAdapter(compAdapter);
        showData();

        View headerView = nav_view.getHeaderView(0);
        TextView navUsername = headerView.findViewById(R.id.nav_username);
        TextView navEmail = headerView.findViewById(R.id.nav_email);

        // display username and password
        data = getIntent();
        navUsername.setText(data.getStringExtra("displayUsername"));
        navEmail.setText(data.getStringExtra("displayEmail"));

    }

    private void showData(){
        fStore.collection("PrivateNotes").document(user.getUid()).collection("Tasks").orderBy("time", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                for(DocumentChange documentChange : value.getDocumentChanges()){
                    if(documentChange.getType() == DocumentChange.Type.ADDED){
                        String id = documentChange.getDocument().getId();
                        ToDo model = documentChange.getDocument().toObject(ToDo.class).withId(id);

                        list.add(model);
                        adapter.notifyDataSetChanged();

                    }
                }
                Collections.reverse(list);


            }
        });
        fStore.collection("PrivateNotes").document(user.getUid()).collection("completedTasks").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                for(DocumentChange documentChange : value.getDocumentChanges()){
                    if(documentChange.getType() == DocumentChange.Type.ADDED){
                        String id = documentChange.getDocument().getId();
                        ToDo compModel = documentChange.getDocument().toObject(ToDo.class).withId(id);
                        //Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();

                        compList.add(compModel);
                        compAdapter.notifyDataSetChanged();

                    }
                }
                Collections.reverse(compList);


            }
        });

    }

    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        //close drawer
        drawerLayout.closeDrawer(GravityCompat.START);
        switch (item.getItemId()){
            case R.id.notes:
                // my notes activity
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
                feed.putExtra("displayUsername", data.getStringExtra("displayUsername"));
                feed.putExtra("displayEmail",data.getStringExtra("displayEmail"));
                startActivity(feed);
                finish();
                break;
            case R.id.toDo:
                // reload to-do activity
                startActivity(new Intent(this, ToDoActivity.class));
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
        startActivity(new Intent(getApplicationContext(), LaunchScreenActivity.class));
        finish();
    }


}