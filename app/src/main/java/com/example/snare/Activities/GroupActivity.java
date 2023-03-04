package com.example.snare.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.snare.Entities.Group;
import com.example.snare.R;
import com.example.snare.adapters.GroupAdapter;
import com.example.snare.firebaseRef.GroupFirebase;
import com.example.snare.listeners.GroupListener;

import java.util.ArrayList;
import java.util.List;

public class GroupActivity extends AppCompatActivity implements GroupListener {

    private AlertDialog dialog;
    private EditText groupName;
    private static final int REQUEST_CODE_SELECT_IMAGE = 4;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 5;
    private static final int REQUEST_CODE_SHOW_GROUPS = 3;
    private RecyclerView groupRecycleView;
    private ImageView imageAddGroupMain;
    private String imagePath ;
    private List<Group> groups = new ArrayList<>();
    private GroupAdapter groupAdapter;
    private int onClickPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        setActivity();
    }

    private void setActivity() {
        initializeActivity();
        setListeners();
        getAllGroups(REQUEST_CODE_SHOW_GROUPS, false);
        setRecycleView();

    }

    private void initializeActivity() {
        imageAddGroupMain = findViewById(R.id.imageAddGroupMain);
        groupRecycleView = findViewById(R.id.groupRecycleView);
    }

    private void setListeners() {
        setImageAddGroupMainListener();
    }

    private void setImageAddGroupMainListener() {
       imageAddGroupMain.setOnClickListener(v -> createNewGroupDialog());
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

    private void createNewGroupDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View groupPopUp = getLayoutInflater().inflate(R.layout.group, null);

        groupName = groupPopUp.findViewById(R.id.groupName);
        Button create = groupPopUp.findViewById(R.id.create);
        Button cancel = groupPopUp.findViewById(R.id.cancel);
        Button addImage = groupPopUp.findViewById(R.id.addImage);

        dialogBuilder.setView(groupPopUp);
        dialog = dialogBuilder.create();
        dialog.show();

        create.setOnClickListener(v -> {
            String groupSelectedName = groupName.getText().toString().trim();

            if(groupSelectedName.isEmpty()) {
                Toast.makeText(GroupActivity.this, "select name", Toast.LENGTH_SHORT).show();
                return;
            }

            Group newGroup = new Group();
            newGroup.setName(groupSelectedName);

            if(imagePath != null){
                newGroup.setImagePath(imagePath);
            }

            GroupFirebase groupFirebase = new GroupFirebase();
            groupFirebase.save(newGroup);
            groups.add(0,newGroup);
            groupAdapter.notifyItemInserted(0);

            dialog.dismiss();

        });

        addImage.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(GroupActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_STORAGE_PERMISSION);
            } else {
                selectImage();
            }
        });

        cancel.setOnClickListener(view -> dialog.dismiss());
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
                if (groups.size() != 0) {
                    groupAdapter.searchGroups(newText);
                }

                return false;
            }
        });

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            // Get the selected image's URI
            assert data != null;
            Uri selectedImageUri = data.getData();
            if(selectedImageUri != null) {
                try {
                    imagePath = getPathFromUri(selectedImageUri);
                    Toast.makeText(this, "image selected", Toast.LENGTH_SHORT).show();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "no image selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getAllGroups(int requestCode, boolean isNoteDeleted) {

        if(isNetworkAvailable(getApplicationContext())) {
            GroupFirebase groupFirebase = new GroupFirebase();
            groupFirebase.getAllGroups(new GroupFirebase.GroupCallback() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onGroupRetrieved(List<Group> groups) {
                    GroupActivity.this.groups.addAll(groups);
                    groupAdapter.notifyDataSetChanged();
                }

                @Override
                public void onGroupRetrieveError(String error) {

                }
            });

        }else{
            Toast.makeText(this, "No Internet", Toast.LENGTH_SHORT).show();
        }
    }

    public  boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void setRecycleView() {
        groupRecycleView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        groupAdapter = new GroupAdapter(groups,this);
        groupRecycleView.setAdapter(groupAdapter);
    }

    @Override
    public void onGroupClick(Group group, int position) {
        onClickPosition = position;
        Intent intent = new Intent(getApplicationContext(), MembersActivity.class);
        intent.putExtra("group", group);
        startActivity(intent);
        finish();
    }

}