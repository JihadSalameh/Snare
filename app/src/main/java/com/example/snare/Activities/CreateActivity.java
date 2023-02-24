package com.example.snare.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import com.example.snare.Entities.Note;
import com.example.snare.Entities.PinnedLocations;
import com.example.snare.Entities.Reminder;
import com.example.snare.Entities.WrappingFriends;
import com.example.snare.R;
import com.example.snare.dao.NotesDataBase;
import com.example.snare.dao.ReminderDataBase;
import com.example.snare.listeners.GroupListeners;
import com.example.snare.listeners.PinnedLocationListener;
import com.example.snare.reminders.AlarmReceiver;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CreateActivity extends AppCompatActivity implements GroupListeners , PinnedLocationListener {

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
    private Reminder alreadyAvailableReminder;
    private ImageView imageRemoveImage;
    private AlertDialog dialogDeleteNote;
    private AlertDialog dialogReminder;
    private int year = -1, month = -1, day =-1;
    private int hour, minute;
    private boolean isReminder = false;
    private AlarmManager alarmManager;
    private List<String> group ;
    private List<PinnedLocations> pinnedLocations;
    private GroupLayout popupGroup;
    private PinnedLocationDialog pinnedLocationDialog ;


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
        layoutMiscellaneous = findViewById(R.id.layoutMiscellaneous);
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
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
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
        setAddReminderListener();
        setCollaborateListener();
        setAddPinnedListener();
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

    private void setAddReminderListener() {
        LinearLayout layoutAddDateTime = findViewById(R.id.layoutAddDateTime);
        layoutAddDateTime.setOnClickListener(view -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            showReminderDialog();
        });
    }

    private void showReminderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Inflate the custom layout
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_time_date_picker, findViewById(R.id.layoutReminderContainer));

        // Set the custom layout as the view for the delete dialog
        builder.setView(dialogView);

        dialogReminder = builder.create();

        if (dialogReminder.getWindow() != null) {
            dialogReminder.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        dialogView.findViewById(R.id.addDate).setOnClickListener(view -> setAddDateListener());

        dialogView.findViewById(R.id.addTime).setOnClickListener(view -> setAddTimeListener());

        dialogView.findViewById(R.id.textSave).setOnClickListener(view -> setTextSaveListener());

        dialogView.findViewById(R.id.textCancel).setOnClickListener(view -> dialogReminder.dismiss());

        dialogReminder.show();
    }

    private void setAddTimeListener() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                CreateActivity.this,
                (timePicker, hour1, minute1) -> getTime(hour1, minute1),
                hour,
                minute,
                true);
        timePickerDialog.show();
    }

    private void getTime(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    private void setTextSaveListener() {
        if (year != -1 && month != -1 && day != -1 && hour != 0 && minute != 0){
            isReminder = true;
        }else {
            Toast.makeText(getApplicationContext(),"Choose Date and Time",Toast.LENGTH_SHORT).show();
        }

        dialogReminder.dismiss();
    }

    private void setAddDateListener() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                CreateActivity.this,
                (datePicker, year1, month1, day1) -> getDate(year1, month1, day1),
                year,
                month,
                day);
        datePickerDialog.show();
    }

    private void getDate(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    private void saveReminder(Reminder reminder) {
        if (isTitleEmpty()) {
            Toast.makeText(this, "Reminder Title is Empty", Toast.LENGTH_LONG).show();
            return;
        }

        class SaveReminderTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                ReminderDataBase.getDatabase(getApplicationContext()).reminderDao().insertReminder(reminder);
                setAlarm(reminder);
                FirebaseReminders firebaseReminders = new FirebaseReminders();
                firebaseReminders.save(reminder);
                return null;
            }

            @Override
            protected void onPostExecute(Void avoid) {
                super.onPostExecute(avoid);
                Intent intent = new Intent();
                intent.putExtra("type","reminder");
                setResult(RESULT_OK, intent);
                finish();
            }
        }

        new SaveReminderTask().execute();
    }

    private void setAlarm(Reminder reminder) {
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        intent.putExtra("title",reminder.getTitle());
        intent.putExtra("description",reminder.getReminderText());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),reminder.hashCode(),intent,PendingIntent.FLAG_IMMUTABLE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, reminder.getHour());
        calendar.set(Calendar.MINUTE, reminder.getMinute());
        calendar.set(Calendar.YEAR, reminder.getYear());
        calendar.set(Calendar.MONTH, reminder.getMonth());
        calendar.set(Calendar.DAY_OF_MONTH, reminder.getDay());
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    //////////////////////////////////////////////////////////
    //1
    private void setImageSaveListener() {
        imageSave.setOnClickListener(view -> {
            setTimeDate();

            if(isReminder) {
                isReminder = false;
                Reminder reminder = new Reminder();
                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("reminders");
                reminder.setIdFirebase(mDatabase.getRef().push().getKey());
                reminder.setTitle(inputNoteTitle.getText().toString());
                reminder.setReminderText(inputNote.getText().toString());
                reminder.setDateTime(textDateTime.getText().toString());
                reminder.setColor(selectedNoteColor);
                reminder.setImagePath(selectedImagePath);
                reminder.setYear(year);
                reminder.setMonth(month);
                reminder.setDay(day);
                reminder.setHour(hour);
                reminder.setMinute(minute);
                if(group == null){
                    group = new ArrayList<>();
                }
                reminder.setGroup(group);
                if(alreadyAvailableReminder != null) {
                    reminder.setIdFirebase(alreadyAvailableReminder.getIdFirebase());
                    reminder.setCount(alreadyAvailableReminder.getCount());
                    reminder.setGroup(alreadyAvailableReminder.getGroup());
                }
                saveReminder(reminder);
            } else {
                Note note = new Note();
                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("notes");
                note.setIdFirebase(mDatabase.getRef().push().getKey());
                note.setTitle(inputNoteTitle.getText().toString());
                note.setNoteText(inputNote.getText().toString());
                note.setDateTime(textDateTime.getText().toString());
                note.setColor(selectedNoteColor);
                note.setImagePath(selectedImagePath);
                if(group == null){
                    group = new ArrayList<>();
                }
                note.setGroup(group);
                if(alreadyAvailableNote != null) {
                    note.setIdFirebase(alreadyAvailableNote.getIdFirebase());
                    note.setCount(alreadyAvailableNote.getCount());
                    note.setGroup(alreadyAvailableNote.getGroup());
                }
                saveNote(note);
            }
        });
    }

    private void setAddImageListener() {
        layoutMiscellaneous.findViewById(R.id.layoutAddImage).setOnClickListener(view -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CreateActivity.this,
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

    //////////////////////////////////////////////////////////
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
                //NotesDataBase.getDatabase(getApplicationContext()).noteDao().insertNote(note);
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

    //////////////////////////////////////////////////////////
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

    ////////////////////////////////////////////////////////
    private void checkIfUpdateOrCreate() {
        if(getIntent().getBooleanExtra("isViewOrUpdate", false)) {
            if(getIntent().getBooleanExtra("isReminder", false)) {
                alreadyAvailableReminder = (Reminder) getIntent().getSerializableExtra("reminder");
                isReminder = true;
                setViewReminder();
                setViewColor();
            } else {
                alreadyAvailableNote = (Note) getIntent().getSerializableExtra("note");
                setViewNote();
                setViewColor();
            }
        }
    }

    private void setViewReminder() {
        inputNoteTitle.setText(alreadyAvailableReminder.getTitle());
        inputNote.setText(alreadyAvailableReminder.getReminderText());
        textDateTime.setText(alreadyAvailableReminder.getDateTime());
        getDate(alreadyAvailableReminder.getYear(),alreadyAvailableReminder.getMonth(),alreadyAvailableReminder.getDay());
        getTime(alreadyAvailableReminder.getHour(),alreadyAvailableReminder.getMinute());
        selectedNoteColor = alreadyAvailableReminder.getColor();

        if(alreadyAvailableReminder.getImagePath() != null && !alreadyAvailableReminder.getImagePath().trim().isEmpty()) {
            imageNote.setImageBitmap(BitmapFactory.decodeFile(alreadyAvailableReminder.getImagePath()));
            imageNote.setVisibility(View.VISIBLE);
            imageRemoveImage.setVisibility(View.VISIBLE);
            selectedImagePath = alreadyAvailableReminder.getImagePath();
        }

        setDeleteListener();
    }

    private void checkIfAddNoteFromQuickAction() {
        Log.d("add","6");
        if(getIntent().getBooleanExtra("isFromQuickActionsBar", false)) {
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

        if(alreadyAvailableNote.getImagePath() != null && !alreadyAvailableNote.getImagePath().trim().isEmpty()) {
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

    //2
    private void showDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Inflate the custom layout
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_delete_note, (ViewGroup) findViewById(R.id.layoutDeleteNoteContainer));

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
                        if(isReminder){
                            ReminderDataBase.getDatabase(getApplicationContext()).reminderDao().deleteReminder(alreadyAvailableReminder);
                            FirebaseReminders firebaseReminders = new FirebaseReminders();
                            firebaseReminders.delete(alreadyAvailableReminder);

                        }else{
                            NotesDataBase.getDatabase(getApplicationContext()).noteDao().deleteNote(alreadyAvailableNote);
                            FirebaseNotes firebaseNotes = new FirebaseNotes();
                            firebaseNotes.delete(alreadyAvailableNote);

                        }
                        return null;
                    }


                    @Override
                    protected void onPostExecute(Void avoid) {
                        super.onPostExecute(avoid);
                        Intent intent = new Intent();
                        if(isReminder){
                            intent.putExtra("isReminderDeleted", true);
                        }else{
                            intent.putExtra("isNoteDeleted", true);
                        }
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
            if(!isNetworkAvailable(getApplicationContext())){
                Toast.makeText(getApplicationContext(),"No internet",Toast.LENGTH_SHORT).show();
                return;
            }
            popupGroup = new GroupLayout(CreateActivity.this);
            popupGroup.setDialog(CreateActivity.this);
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
    public void onFriendClick(WrappingFriends friend, int position) {
        if(group == null){
            group = new ArrayList<>();
        }

        group.add(friend.getId());
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

    public  boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void setAddPinnedListener() {
        layoutMiscellaneous.findViewById(R.id.layoutAddPlace).setOnClickListener(view -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            if(!isNetworkAvailable(getApplicationContext())){
                Toast.makeText(getApplicationContext(),"No internet",Toast.LENGTH_SHORT).show();
                return;
            }
            pinnedLocationDialog = new PinnedLocationDialog(CreateActivity.this);
            pinnedLocationDialog.setDialog(CreateActivity.this);
            Window window = pinnedLocationDialog.getWindow();
            if (window != null) {
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.copyFrom(window.getAttributes());
                layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                window.setAttributes(layoutParams);
            }
            pinnedLocationDialog.show();
        });
    }

    @Override
    public void onPinnedClick(PinnedLocations pinnedLocation, int position) {

        if(pinnedLocations == null){
            pinnedLocations = new ArrayList<>();
        }

        pinnedLocations.add(pinnedLocation);
        pinnedLocationDialog.dismiss();

    }
}