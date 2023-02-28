package com.example.snare.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.snare.Entities.Group;
import com.example.snare.Entities.Note;
import com.example.snare.firebaseRef.FirebaseNotes;
import com.example.snare.R;
import com.example.snare.dao.NotesDataBase;
import com.example.snare.listeners.GroupListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class CreateNoteActivity extends AppCompatActivity implements GroupListener {

    private ImageView imageSave;
    private EditText inputNoteTitle;
    private EditText inputNote;
    private TextView textDateTime;
    private LinearLayout layoutMiscellaneous;
    private String selectedNoteColor = "#333333";
    private View viewTitleIndicator;
    private ImageView imageColor1;
    private ImageView imageColor2;
    private ImageView imageColor3;
    private ImageView imageColor4;
    private ImageView imageColor5;
    private BottomSheetBehavior bottomSheetBehavior;
    private ImageView imageNote;
    private String selectedImagePath;
    private Note alreadyAvailableNote;
    private ImageView imageRemoveImage;
    private AlertDialog dialogDeleteNote;
    private List<String> groupIDs;
    private GroupLayout popupGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);
        setActivity();
    }

    private void setActivity() {
        initializeActivity();
        setListeners();
    }

    private void initializeActivity() {
        imageSave = findViewById(R.id.imageSave);
        inputNoteTitle = findViewById(R.id.inputNoteTitle);
        inputNote = findViewById(R.id.inputNote);
        textDateTime = findViewById(R.id.textDateTime);
        layoutMiscellaneous = findViewById(R.id.layoutMiscellaneousNote);
        viewTitleIndicator = findViewById(R.id.viewTitleIndicator);
        setViewTitleIndicatorColor();
        bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous);
        imageColor1 = findViewById(R.id.imageColor1);
        imageColor2 = findViewById(R.id.imageColor2);
        imageColor3 = findViewById(R.id.imageColor3);
        imageColor4 = findViewById(R.id.imageColor4);
        imageColor5 = findViewById(R.id.imageColor5);
        imageNote = findViewById(R.id.imageNote);
        imageRemoveImage = findViewById(R.id.imageRemoveImage);
        checkIfUpdateOrCreate();
        checkIfAddNoteFromQuickAction();
    }

    private void setListeners() {
        setImageBackListener();
        setImageSaveListener();
        setBottomSheetBehaviorListener();
        setImageColor1Listener();
        setImageColor2Listener();
        setImageColor3Listener();
        setImageColor4Listener();
        setImageColor5Listener();
        setAddImageListener();
        setRemoveImageListener();
        setCollaborateListener();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                Toast.makeText(this, "permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.SELECT_IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            // Get the selected image's URI
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                InputStream inputStream;
                try {
                    inputStream = getContentResolver().openInputStream(selectedImageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    imageNote.setImageBitmap(bitmap);
                    imageNote.setVisibility(View.VISIBLE);
                    imageRemoveImage.setVisibility(View.VISIBLE);
                    selectedImagePath = getPathFromUri(selectedImageUri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "no image selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setImageBackListener() {
        findViewById(R.id.imageBack).setOnClickListener(view -> onBackPressed());
    }

    private void setBottomSheetBehaviorListener() {
        layoutMiscellaneous.findViewById(R.id.textMiscellaneous).setOnClickListener(view -> {
            if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
    }

    private void setViewTitleIndicatorColor() {
        GradientDrawable gradientDrawable = (GradientDrawable) viewTitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedNoteColor));
    }

    private void setImageSaveListener() {
        imageSave.setOnClickListener(view -> {
            setTimeDate();
            Note note = new Note();
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("notes");
            note.setIdFirebase(Objects.requireNonNull(mDatabase.getRef().push().getKey()));
            note.setTitle(inputNoteTitle.getText().toString());
            note.setNoteText(inputNote.getText().toString());
            note.setDateTime(textDateTime.getText().toString());
            note.setColor(selectedNoteColor);
            note.setImagePath(selectedImagePath);
            if (groupIDs == null) {
                groupIDs = new ArrayList<>();
            }
            note.setGroup(groupIDs);
            if (alreadyAvailableNote != null) {
                note.setIdFirebase(alreadyAvailableNote.getIdFirebase());
                note.setGroup(alreadyAvailableNote.getGroup());
            }
            saveNote(note);
        });
    }

    private void setAddImageListener() {
        layoutMiscellaneous.findViewById(R.id.layoutAddImage).setOnClickListener(view -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CreateNoteActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        Constants.REQUEST_CODE_STORAGE_PERMISSION);
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
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, Constants.SELECT_IMAGE_REQUEST_CODE);
        }
    }

    private void makeNoteWithImage() {
        String imagePath = getIntent().getStringExtra("imagePath");
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            imageNote.setImageBitmap(BitmapFactory.decodeFile(imagePath));
            imageNote.setVisibility(View.VISIBLE);
            imageRemoveImage.setVisibility(View.VISIBLE);
            selectedImagePath = imagePath;
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

    private void setRemoveImageListener() {
        imageRemoveImage.setOnClickListener(view -> {
            imageNote.setImageBitmap(null);
            imageNote.setVisibility(View.GONE);
            imageRemoveImage.setVisibility(View.GONE);
            selectedImagePath = "";
        });
    }

    private void setTimeDate() {
        textDateTime.setText(new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a",
                Locale.getDefault()).format(new Date()));
    }

    private void saveNote(Note note) {

        if (isTitleEmpty()) {
            Toast.makeText(this, "Note Title is Empty", Toast.LENGTH_LONG).show();
            return;
        }

        class SaveNoteTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                NotesDataBase.getDatabase(getApplicationContext()).noteDao().insertNote(note);
                FirebaseNotes firebaseNotes = new FirebaseNotes();
                firebaseNotes.save(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void avoid) {
                super.onPostExecute(avoid);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }

        }

        new SaveNoteTask().execute();
    }

    private boolean isTitleEmpty() {
        return inputNoteTitle.getText().toString().isEmpty();
    }

    private void setImageColor1Listener() {
        layoutMiscellaneous.findViewById(R.id.imageColor1).setOnClickListener(view -> {
            selectedNoteColor = "#333333";
            imageColor1.setImageResource(R.drawable.ic_done);
            imageColor2.setImageResource(0);
            imageColor3.setImageResource(0);
            imageColor4.setImageResource(0);
            imageColor5.setImageResource(0);
            setViewTitleIndicatorColor();
        });
    }

    private void setImageColor2Listener() {
        layoutMiscellaneous.findViewById(R.id.imageColor2).setOnClickListener(view -> {
            selectedNoteColor = "#FDBE3B";
            imageColor1.setImageResource(0);
            imageColor2.setImageResource(R.drawable.ic_done);
            imageColor3.setImageResource(0);
            imageColor4.setImageResource(0);
            imageColor5.setImageResource(0);
            setViewTitleIndicatorColor();
        });
    }

    private void setImageColor3Listener() {
        layoutMiscellaneous.findViewById(R.id.imageColor3).setOnClickListener(view -> {
            selectedNoteColor = "#FF4842";
            imageColor1.setImageResource(0);
            imageColor2.setImageResource(0);
            imageColor3.setImageResource(R.drawable.ic_done);
            imageColor4.setImageResource(0);
            imageColor5.setImageResource(0);
            setViewTitleIndicatorColor();
        });
    }

    private void setImageColor4Listener() {
        layoutMiscellaneous.findViewById(R.id.imageColor4).setOnClickListener(view -> {
            selectedNoteColor = "#3A52FC";
            imageColor1.setImageResource(0);
            imageColor2.setImageResource(0);
            imageColor3.setImageResource(0);
            imageColor4.setImageResource(R.drawable.ic_done);
            imageColor5.setImageResource(0);
            setViewTitleIndicatorColor();
        });
    }

    private void setImageColor5Listener() {
        layoutMiscellaneous.findViewById(R.id.imageColor5).setOnClickListener(view -> {
            selectedNoteColor = "#000000";
            imageColor1.setImageResource(0);
            imageColor2.setImageResource(0);
            imageColor3.setImageResource(0);
            imageColor4.setImageResource(0);
            imageColor5.setImageResource(R.drawable.ic_done);
            setViewTitleIndicatorColor();
        });
    }

    private void setViewColor() {
        if (alreadyAvailableNote != null && alreadyAvailableNote.getColor() != null && !alreadyAvailableNote.getColor().trim().isEmpty()) {

            switch (alreadyAvailableNote.getColor()) {
                case "#FDBE3B":
                    layoutMiscellaneous.findViewById(R.id.viewColor2).performClick();
                    break;
                case "#FF4842":
                    layoutMiscellaneous.findViewById(R.id.viewColor3).performClick();
                    break;
                case "#3A52FC":
                    layoutMiscellaneous.findViewById(R.id.viewColor4).performClick();
                    break;
                case "#000000":
                    layoutMiscellaneous.findViewById(R.id.viewColor5).performClick();
                    break;
            }

        }
    }

    private void checkIfUpdateOrCreate() {
        if (getIntent().getBooleanExtra("isViewOrUpdate", false)) {
            alreadyAvailableNote = (Note) getIntent().getSerializableExtra("note");
            setViewNote();
            setViewColor();

        }
    }

    private void checkIfAddNoteFromQuickAction() {
        if (getIntent().getBooleanExtra("isFromQuickActionsBar", false)) {
            if (getIntent().getStringExtra("quickActionBarType").equals("image")) {
                makeNoteWithImage();
            }
        }
    }

    private void setViewNote() {
        inputNoteTitle.setText(alreadyAvailableNote.getTitle());
        inputNote.setText(alreadyAvailableNote.getNoteText());
        textDateTime.setText(alreadyAvailableNote.getDateTime());
        selectedNoteColor = alreadyAvailableNote.getColor();

        if (alreadyAvailableNote.getImagePath() != null && !alreadyAvailableNote.getImagePath().trim().isEmpty()) {
            imageNote.setImageBitmap(BitmapFactory.decodeFile(alreadyAvailableNote.getImagePath()));
            imageNote.setVisibility(View.VISIBLE);
            imageRemoveImage.setVisibility(View.VISIBLE);
            selectedImagePath = alreadyAvailableNote.getImagePath();
        }

        setDeleteListener();
    }

    private void setDeleteListener() {
        LinearLayout layoutDeleteNote = findViewById(R.id.layoutDeleteNote);
        layoutDeleteNote.setVisibility(View.VISIBLE);
        layoutDeleteNote.setOnClickListener(view -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            showDeleteDialog();
        });
    }

    private void showDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Inflate the custom layout
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_delete_note, findViewById(R.id.layoutDeleteNoteContainer));

        // Set the custom layout as the view for the delete dialog
        builder.setView(dialogView);

        dialogDeleteNote = builder.create();

        if (dialogDeleteNote.getWindow() != null) {
            dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        dialogView.findViewById(R.id.textDeleteNote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                class DeleteNoteTask extends AsyncTask<Void, Void, Void> {

                    @Override
                    protected Void doInBackground(Void... voids) {
                        NotesDataBase.getDatabase(getApplicationContext()).noteDao().deleteNote(alreadyAvailableNote);
                        FirebaseNotes firebaseNotes = new FirebaseNotes();
                        firebaseNotes.delete(alreadyAvailableNote);


                        return null;
                    }


                    @Override
                    protected void onPostExecute(Void avoid) {
                        super.onPostExecute(avoid);
                        Intent intent = new Intent();
                        intent.putExtra("isNoteDeleted", true);
                        setResult(RESULT_OK, intent);
                        finish();
                    }

                }

                new DeleteNoteTask().execute();
            }
        });

        dialogView.findViewById(R.id.textCancel).setOnClickListener(view -> dialogDeleteNote.dismiss());

        dialogDeleteNote.show();
    }

    private void setCollaborateListener() {
        layoutMiscellaneous.findViewById(R.id.layoutAddCollaborator).setOnClickListener(view -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            if (!isNetworkAvailable(getApplicationContext())) {
                Toast.makeText(getApplicationContext(), "No internet", Toast.LENGTH_SHORT).show();
                return;
            }
            popupGroup = new GroupLayout(CreateNoteActivity.this);
            popupGroup.setDialog(CreateNoteActivity.this);
            Window window = popupGroup.getWindow();
            if (window != null) {
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.copyFrom(window.getAttributes());
                layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                window.setAttributes(layoutParams);
            }
            popupGroup.show();
        });
    }

    @Override
    public void onGroupClick(Group group, int position) {
        if (groupIDs == null) {
            groupIDs = new ArrayList<>();
        }

        groupIDs.addAll(group.getGroupMembers());
        popupGroup.dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Dismiss the dialog if it's still showing
        if (dialogDeleteNote != null && dialogDeleteNote.isShowing()) {
            dialogDeleteNote.dismiss();
        }
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}