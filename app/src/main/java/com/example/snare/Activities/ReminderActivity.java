package com.example.snare.Activities;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.snare.Entities.Reminder;
import com.example.snare.R;
import com.example.snare.adapters.ReminderAdapter;
import com.example.snare.dao.ReminderDataBase;
import com.example.snare.listeners.RemindersListeners;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ReminderActivity extends AppCompatActivity implements RemindersListeners {

    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    public NavigationView navigationView;
    private RecyclerView reminderRecycleView;
    private ImageView imageAddReminderMain;
    private List<Reminder> remindersList;
    private ReminderAdapter reminderAdapter;
    public static final int REQUEST_CODE_ADD_REMINDER = 6;
    public static final int REQUEST_CODE_UPDATE_REMINDER = 7;
    private static final int REQUEST_CODE_SHOW_REMINDER = 8;
    private int onClickPosition = -1;
    public static final int REQUEST_CODE_ADD_NOTE = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 4;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        setActivity();

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        fillNavDrawer();
        navOnClickAction();
    }

    private void setActivity() {
        initializeActivity();
        setListeners();
        setRecycleView();
        getAllReminders(REQUEST_CODE_SHOW_REMINDER, false);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel("channel_id", "channel_name", NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("channel_description");
        channel.enableLights(true);
        channel.enableVibration(true);
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }

    private void initializeActivity() {
        drawerLayout = findViewById(R.id.reminder_drawer_layout);
        navigationView = findViewById(R.id.reminder_nav_menu);
        reminderRecycleView = findViewById(R.id.reminderRecycleView);
        imageAddReminderMain = findViewById(R.id.imageAddReminderMain);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
    }

    private void setListeners() {
        setImageAddReminderMainListener();
    }

    private void setImageAddReminderMainListener() {
        imageAddReminderMain.setOnClickListener(view -> startActivityForResult(new Intent(getApplicationContext(), CreateNoteActivity.class),
                REQUEST_CODE_ADD_REMINDER));
    }

    @SuppressLint("IntentReset")
    private void selectImage() {
        // Create an Intent to open the image picker
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }

    @SuppressLint("Range")
    private String getPathFromUri(Uri imageUri) {
        String filePath = null;
        // Get the file path from the Uri
        Cursor cursor = getContentResolver().query(imageUri, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            cursor.close();
        }

        return filePath;
    }

    private void getAllReminders(int requestCode, boolean isReminderDeleted) {

        class GetRemindersTask extends AsyncTask<Void, Void, List<Reminder>> {

            @Override
            protected List<Reminder> doInBackground(Void... voids) {
                if (isNetworkAvailable(getApplicationContext())) {

                    FirebaseReminders firebaseReminders = new FirebaseReminders();
                    firebaseReminders.getAllReminders(new FirebaseReminders.RemindersCallback() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onRemindersRetrieved(List<Reminder> reminders) {
                            if(reminders != null) {
                                if (requestCode == REQUEST_CODE_SHOW_REMINDER) {
                                    remindersList.addAll(reminders);
                                    reminderAdapter.notifyDataSetChanged();
                                }
                                else if (requestCode == REQUEST_CODE_ADD_REMINDER) {
                                    remindersList.add(0, reminders.get(0));
                                    reminderAdapter.notifyItemInserted(0);
                                    reminderRecycleView.smoothScrollToPosition(0);
                                } else if (requestCode == REQUEST_CODE_UPDATE_REMINDER) {
                                    remindersList.remove(onClickPosition);
                                    if (isReminderDeleted) {
                                        reminderAdapter.notifyItemRemoved(onClickPosition);
                                    } else {
                                        remindersList.add(onClickPosition, reminders.get(onClickPosition));
                                        reminderAdapter.notifyItemChanged(onClickPosition);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onRemindersRetrieveError(String error) {
                            System.out.println(error);
                        }
                    });

                    return null;

                }else{
                    return ReminderDataBase.getDatabase(getApplicationContext()).reminderDao().getAllReminders();
                }
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void onPostExecute(List<Reminder> reminders) {
                super.onPostExecute(reminders);
                if (reminders != null) {
                    if (requestCode == REQUEST_CODE_SHOW_REMINDER) {
                        remindersList.addAll(reminders);
                        reminderAdapter.notifyDataSetChanged();
                    }
                    else if (requestCode == REQUEST_CODE_ADD_REMINDER) {
                        remindersList.add(0, reminders.get(0));
                        reminderAdapter.notifyItemInserted(0);
                        reminderRecycleView.smoothScrollToPosition(0);
                    } else if (requestCode == REQUEST_CODE_UPDATE_REMINDER) {
                        remindersList.remove(onClickPosition);
                        if (isReminderDeleted) {
                            reminderAdapter.notifyItemRemoved(onClickPosition);
                        } else {
                            remindersList.add(onClickPosition, reminders.get(onClickPosition));
                            reminderAdapter.notifyItemChanged(onClickPosition);
                        }
                    }
                }

            }
        }

        new GetRemindersTask().execute();
    }

    public  boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void setRecycleView() {
        reminderRecycleView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        remindersList = new ArrayList<>();
        reminderAdapter = new ReminderAdapter(remindersList, this);
        reminderRecycleView.setAdapter(reminderAdapter);
    }

    /////////////////////////////////////////////////////////////////////
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE_ADD_REMINDER & resultCode == RESULT_OK) {
            getAllReminders(REQUEST_CODE_ADD_REMINDER, false);
        } else if(requestCode == REQUEST_CODE_UPDATE_REMINDER && resultCode == RESULT_OK) {
            if(data != null) {
                getAllReminders(REQUEST_CODE_UPDATE_REMINDER, data.getBooleanExtra("isReminderDeleted", false));
            }
        } else if(requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            // Get the selected image's URI
            Uri selectedImageUri = null;
            if(data != null) {
                selectedImageUri = data.getData();
            }
            if(selectedImageUri != null) {
                try {
                    String selectedImagePath = getPathFromUri(selectedImageUri);
                    Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                    intent.putExtra("isFromQuickActionsBar", true);
                    intent.putExtra("quickActionBarType", "image");
                    intent.putExtra("imagePath", selectedImagePath);
                    startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                Toast.makeText(this, "no image selected", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void onReminderClick(Reminder reminder, int position) {
        onClickPosition = position;
        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("isReminder", true);
        intent.putExtra("reminder", reminder);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_REMINDER);
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

    //////////////////////////////////////////////////////////////////////////////////////////////
    private void fillNavDrawer() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        databaseReference.get().addOnSuccessListener(snapshot -> {
            ImageView imageView = findViewById(R.id.profileImg);
            TextView name = findViewById(R.id.nameTxt);
            TextView email = findViewById(R.id.emailTxtNav);

            Picasso.get().load(snapshot.child("profilePic").getValue(String.class)).into(imageView);
            name.setText(snapshot.child("name").getValue(String.class));
            email.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        fillNavDrawer();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(getApplicationContext(), "Signed out!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class));
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
                if (remindersList.size() != 0) {
                    reminderAdapter.searchReminders(newText);
                }
                return false;
            }
        });

        return true;
    }

    private void navOnClickAction() {
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getTitle().toString().equals("Logout")) {
                logout();
            } else if (item.getTitle().toString().equals("Profile")) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
            } else if (item.getTitle().toString().equals("Friends")) {
                startActivity(new Intent(getApplicationContext(), FriendsActivity.class));
            } else if(item.getTitle().toString().equals("Notes")){
                startActivity(new Intent(getApplicationContext(), NotesActivity.class));
                finish();
            }else if (item.getTitle().toString().equals("Shouts")) {
                startActivity(new Intent(getApplicationContext(), ShoutsActivity.class));
                finish();
            } else if(item.getTitle().toString().equals("Notifications")) {
                startActivity(new Intent(ReminderActivity.this, NotificationsActivity.class));
            } else if(item.getTitle().toString().equals("Pinned Locations")) {
                startActivity(new Intent(ReminderActivity.this, PinnedLocationsActivity.class));
            }
            return true;
        });
    }

}