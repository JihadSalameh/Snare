package com.example.snare.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.snare.Entities.Note;
import com.example.snare.Entities.PinnedLocations;
import com.example.snare.Entities.Reminder;
import com.example.snare.R;
import com.example.snare.adapters.NotesAdapter;
import com.example.snare.dao.NotesDataBase;
import com.example.snare.dao.PinnedLocationsDataBase;
import com.example.snare.dao.ReminderDataBase;
import com.example.snare.listeners.NotesListeners;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NotesActivity extends AppCompatActivity implements NotesListeners {

    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    public NavigationView navigationView;

    private DatabaseReference userRef;
    private FirebaseAuth auth;
    private FirebaseUser user;

    private RecyclerView noteRecycleView;
    private ImageView imageAddImage;
    private ImageView imageAddNote;
    private ImageView imageAddNoteMain;
    private List<Note> noteList;
    private NotesAdapter notesAdapter;
    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    private static final int REQUEST_CODE_SHOW_NOTE = 3;
    private static final int REQUEST_CODE_SELECT_IMAGE = 4;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 5;
    private int onClickPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        ///////just to get the token to test notifications
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if(task.isComplete()){
                GetToken(task);
            }
        });

        setActivity();

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        fillNavDrawer();
        navOnClickAction();
    }

    private void GetToken(Task<String> task) {
        String token = task.getResult();

        Map<String, Object> update = new HashMap<>();
        update.put("token", token);
        userRef.updateChildren(update);

        System.out.println("token = " + token);
    }

    private void navOnClickAction() {
        navigationView.setNavigationItemSelectedListener(item -> {
            if(item.getTitle().toString().equals("Logout")) {
                logout();
            } else if(item.getTitle().toString().equals("Profile")) {
                startActivity(new Intent(NotesActivity.this, ProfileActivity.class));
            } else if(item.getTitle().toString().equals("Friends")) {
                startActivity(new Intent(NotesActivity.this, FriendsActivity.class));
            } else if(item.getTitle().toString().equals("Shouts")) {
                startActivity(new Intent(NotesActivity.this, ShoutsActivity.class));
                finish();
            } else if(item.getTitle().toString().equals("Reminders")) {
                startActivity(new Intent(NotesActivity.this, ReminderActivity.class));
                finish();
            } else if(item.getTitle().toString().equals("Notifications")) {
                startActivity(new Intent(NotesActivity.this, NotificationsActivity.class));
            } else if(item.getTitle().toString().equals("Pinned Locations")) {
                startActivity(new Intent(NotesActivity.this, PinnedLocationsActivity.class));
            }

            return true;
        });
    }

    private void setActivity() {
        initializeActivity();
        setListeners();
        setRecycleView();
        getAllNotes(REQUEST_CODE_SHOW_NOTE, false);
    }

    private void initializeActivity() {
        drawerLayout = findViewById(R.id.my_drawer_layout);
        navigationView = findViewById(R.id.nav_menu);
        noteRecycleView = findViewById(R.id.noteRecycleView);
        imageAddImage = findViewById(R.id.imageAddImage);
        imageAddNote = findViewById(R.id.imageAddNote);
        imageAddNoteMain = findViewById(R.id.imageAddNoteMain);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
    }

    private void setListeners() {
        setImageAddNoteMainListener();
        setImageAddImageListener();
        setImageAddNoteListener();
    }

    private void setImageAddNoteMainListener() {
        imageAddNoteMain.setOnClickListener(view -> startActivityForResult(new Intent(getApplicationContext(), CreateActivity.class), REQUEST_CODE_ADD_NOTE));
    }

    private void getAllNotes(int requestCode, boolean isNoteDeleted) {

        if(isNetworkAvailable(getApplicationContext())) {
            FirebaseNotes firebaseNotes = new FirebaseNotes();
            firebaseNotes.getAllNotes(new FirebaseNotes.NotesCallback() {

                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onNotesRetrieved(List<Note> notes) {
                    if (notes != null) {
                        if (requestCode == REQUEST_CODE_SHOW_NOTE) {
                            noteList.addAll(notes);
                            notesAdapter.notifyDataSetChanged();
                        } else if (requestCode == REQUEST_CODE_ADD_NOTE) {

                            noteList.add(0, notes.get(0));
                            notesAdapter.notifyItemInserted(0);
                            noteRecycleView.smoothScrollToPosition(0);
                        } else if (requestCode == REQUEST_CODE_UPDATE_NOTE) {
                            noteList.remove(onClickPosition);
                            if (isNoteDeleted) {
                                notesAdapter.notifyItemRemoved(onClickPosition);
                            } else {
                                noteList.add(onClickPosition, notes.get(onClickPosition));
                                notesAdapter.notifyItemChanged(onClickPosition);
                            }
                        }
                    }
                }

                @Override
                public void onNotesRetrieveError(String error) {
                    System.out.println(error);
                }

            });

            return;
        }

        class GetNotesTask extends AsyncTask<Void, Void, List<Note>> {

            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NotesDataBase.getDatabase(getApplicationContext()).noteDao().getAllNotes();
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);

                if(requestCode == REQUEST_CODE_SHOW_NOTE) {
                    noteList.addAll(notes);
                    notesAdapter.notifyDataSetChanged();
                } else if(requestCode == REQUEST_CODE_ADD_NOTE) {
                    noteList.add(0, notes.get(0));
                    notesAdapter.notifyItemInserted(0);
                    noteRecycleView.smoothScrollToPosition(0);
                } else if(requestCode == REQUEST_CODE_UPDATE_NOTE) {
                    noteList.remove(onClickPosition);
                    if(isNoteDeleted) {
                        notesAdapter.notifyItemRemoved(onClickPosition);
                    } else {
                        noteList.add(onClickPosition, notes.get(onClickPosition));
                        notesAdapter.notifyItemChanged(onClickPosition);
                    }
                }
            }
        }

        new GetNotesTask().execute();
    }

    public  boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void setRecycleView() {
        noteRecycleView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        noteList = new ArrayList<>();
        notesAdapter = new NotesAdapter(noteList, this);
        noteRecycleView.setAdapter(notesAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE_ADD_NOTE & resultCode == RESULT_OK) {
            getAllNotes(REQUEST_CODE_ADD_NOTE, false);
        } else if(requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK) {
            if(data != null) {
                getAllNotes(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted", false));
            }
        } else if(requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            // Get the selected image's URI
            assert data != null;
            Uri selectedImageUri = data.getData();
            if(selectedImageUri != null) {
                try {
                    String selectedImagePath = getPathFromUri(selectedImageUri);
                    Intent intent = new Intent(getApplicationContext(), CreateActivity.class);
                    intent.putExtra("isFromQuickActionsBar",true);
                    intent.putExtra("quickActionBarType","image");
                    intent.putExtra("imagePath",selectedImagePath);
                    startActivityForResult(intent,REQUEST_CODE_ADD_NOTE);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "no image selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onNoteClick(Note note, int position) {
        onClickPosition = position;
        Intent intent = new Intent(getApplicationContext(), CreateActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("note", note);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);

    }

    private void setImageAddImageListener() {
        imageAddImage.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(NotesActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_STORAGE_PERMISSION);
            } else {
                selectImage();
            }
        });
    }

    @SuppressLint("IntentReset")
    private void selectImage() {
        // Create an Intent to open the image picker
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        if(intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }

    @SuppressLint("Range")
    private String getPathFromUri(Uri imageUri) {
        String filePath = null;
        // Get the file path from the Uri
        Cursor cursor = getContentResolver().query(imageUri, null, null, null, null);
        if(cursor != null) {
            cursor.moveToFirst();
            filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            cursor.close();
        }

        return filePath;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                Toast.makeText(this, "permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setImageAddNoteListener() {
        imageAddNote.setOnClickListener(view -> startActivityForResult(new Intent(getApplicationContext(), CreateActivity.class), REQUEST_CODE_ADD_NOTE));
    }

    private void fillNavDrawer() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(Objects.requireNonNull(user).getUid());
        databaseReference.get().addOnSuccessListener(snapshot -> {
            ImageView imageView = findViewById(R.id.profileImg);
            TextView name = findViewById(R.id.nameTxt);
            TextView email = findViewById(R.id.emailTxtNav);

            Picasso.get().load(snapshot.child("profilePic").getValue(String.class)).into(imageView);
            name.setText(snapshot.child("name").getValue(String.class));
            email.setText(user.getEmail());
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        fillNavDrawer();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        auth.signOut();

        //delete all tables
        deleteDatabase("notes_db");
        deleteDatabase("notifications_db");
        deleteDatabase("pinnedLocations_db");
        deleteDatabase("reminders_db");

        Toast.makeText(NotesActivity.this, "Signed out!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(NotesActivity.this, LoginActivity.class));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) menuItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (noteList.size() != 0) {
                    notesAdapter.searchNotes(newText);
                }

                return false;
            }
        });

        return true;
    }
}