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
import com.example.snare.Entities.PinnedLocations;
import com.example.snare.Entities.Reminder;
import com.example.snare.firebaseRef.FirebaseReminders;
import com.example.snare.R;
import com.example.snare.dao.ReminderDataBase;
import com.example.snare.listeners.GroupListener;
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
import java.util.Objects;

public class CreateReminderActivity extends AppCompatActivity implements GroupListener, PinnedLocationListener {

    private ImageView imageSaveReminder;
    private EditText inputReminderTitle;
    private EditText inputReminder;
    private TextView textDateTimeReminder;
    private LinearLayout layoutMiscellaneous;
    private String selectedReminderColor = "#333333";
    private View viewTitleIndicatorReminder;
    private ImageView imageColor1Reminder;
    private ImageView imageColor2Reminder;
    private ImageView imageColor3Reminder;
    private ImageView imageColor4Reminder;
    private ImageView imageColor5Reminder;
    private BottomSheetBehavior bottomSheetBehavior;
    private ImageView imageReminder;
    private String selectedImagePath;
    private Reminder alreadyAvailableReminder;
    private ImageView imageRemoveImageReminder;
    private AlertDialog dialogDeleteReminder;
    private AlertDialog dialogReminder;
    private int year = -1, month = -1, day = -1;
    private int hour, minute;
    private AlarmManager alarmManager;
    private List<String> groupIDs;
    private List<PinnedLocations> pinnedLocations;
    private GroupLayout popupGroup;
    private PinnedLocationDialog pinnedLocationDialog;
    boolean isTimeDateReminder = false , isLocationReminder = false ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_reminder);
        setActivity();
    }

    private void setActivity() {
        initializeActivity();
        setListeners();
    }

    private void initializeActivity() {
        imageSaveReminder = findViewById(R.id.imageSaveReminder);
        inputReminderTitle = findViewById(R.id.inputReminderTitle);
        inputReminder = findViewById(R.id.inputReminder);
        textDateTimeReminder = findViewById(R.id.textDateTimeReminder);
        layoutMiscellaneous = findViewById(R.id.layoutMiscellaneousReminder);
        viewTitleIndicatorReminder = findViewById(R.id.viewTitleIndicatorReminder);
        setViewTitleIndicatorColor();
        bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous);
        imageColor1Reminder = findViewById(R.id.imageColor1Reminder);
        imageColor2Reminder = findViewById(R.id.imageColor2Reminder);
        imageColor3Reminder = findViewById(R.id.imageColor3Reminder);
        imageColor4Reminder = findViewById(R.id.imageColor4Reminder);
        imageColor5Reminder = findViewById(R.id.imageColor5Reminder);
        imageReminder = findViewById(R.id.imageReminder);
        imageRemoveImageReminder = findViewById(R.id.imageRemoveImageReminder);
        checkIfUpdateOrCreate();
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
        checkIsConvert();
    }

    private void checkIsConvert() {
        Intent intent = getIntent();
        boolean isConverted = intent.getBooleanExtra("isConverted",false);
        if(!isConverted){
           return;
        }

        String title = intent.getStringExtra("title");
        String color = intent.getStringExtra("color");
        String group = intent.getStringExtra("group");
        String image = intent.getStringExtra("image");
        String content = intent.getStringExtra("content");

        alreadyAvailableReminder = new Reminder();
        alreadyAvailableReminder.setTitle(title);
        alreadyAvailableReminder.setReminderText(content);
        alreadyAvailableReminder.setColor(color);
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("reminders");
        alreadyAvailableReminder.setIdFirebase(Objects.requireNonNull(mDatabase.getRef().push().getKey()));
        if (image != null) {
            imageReminder.setImageBitmap(BitmapFactory.decodeFile(image));
            imageReminder.setVisibility(View.VISIBLE);
            imageRemoveImageReminder.setVisibility(View.VISIBLE);
            selectedImagePath = alreadyAvailableReminder.getImagePath();
        }
        if(group != null){
            ArrayList<String> groups = new ArrayList<>();
            groups.add(group);
            alreadyAvailableReminder.setGroup(groups);
        }

        alreadyAvailableReminder.setLocation("");

        fillViewReminder();
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
                    imageReminder.setImageBitmap(bitmap);
                    imageReminder.setVisibility(View.VISIBLE);
                    imageRemoveImageReminder.setVisibility(View.VISIBLE);
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
        findViewById(R.id.imageBackReminder).setOnClickListener(view -> onBackPressed());
    }

    private void setBottomSheetBehaviorListener() {
        layoutMiscellaneous.findViewById(R.id.textMiscellaneousReminder).setOnClickListener(view -> {
            if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
    }

    private void setViewTitleIndicatorColor() {
        GradientDrawable gradientDrawable = (GradientDrawable) viewTitleIndicatorReminder.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedReminderColor));
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
                CreateReminderActivity.this,
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
        if (year != -1 && month != -1 && day != -1 && hour != 0 && minute != 0) {
            layoutMiscellaneous.findViewById(R.id.layoutAddPlace).setAlpha(0.5f);
            layoutMiscellaneous.findViewById(R.id.layoutAddPlace).setOnClickListener(v -> {

            });
            isTimeDateReminder = true;
            Toast.makeText(getApplicationContext(), "Date and Time Selected", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Choose Date and Time", Toast.LENGTH_SHORT).show();
        }

        dialogReminder.dismiss();
    }

    private void setAddDateListener() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                CreateReminderActivity.this,
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
                if(isTimeDateReminder){
                    setAlarm(reminder);
                }
                FirebaseReminders firebaseReminders = new FirebaseReminders();
                firebaseReminders.save(reminder);
                return null;
            }

            @Override
            protected void onPostExecute(Void avoid) {
                super.onPostExecute(avoid);
                Intent intent = new Intent();
                intent.putExtra("type", "reminder");
                setResult(RESULT_OK, intent);
                finish();
            }
        }

        new SaveReminderTask().execute();
    }

    private void setAlarm(Reminder reminder) {
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        intent.putExtra("title", reminder.getTitle());
        intent.putExtra("description", reminder.getReminderText());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), reminder.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, reminder.getHour());
        calendar.set(Calendar.MINUTE, reminder.getMinute());
        calendar.set(Calendar.YEAR, reminder.getYear());
        calendar.set(Calendar.MONTH, reminder.getMonth());
        calendar.set(Calendar.DAY_OF_MONTH, reminder.getDay());
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    private void setImageSaveListener() {
        imageSaveReminder.setOnClickListener(view -> {
            setTimeDate();
            Reminder reminder = new Reminder();
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("reminders");
            reminder.setIdFirebase(Objects.requireNonNull(mDatabase.getRef().push().getKey()));
            reminder.setTitle(inputReminderTitle.getText().toString());
            reminder.setReminderText(inputReminder.getText().toString());
            reminder.setDateTime(textDateTimeReminder.getText().toString());
            reminder.setColor(selectedReminderColor);
            reminder.setImagePath(selectedImagePath);

            if(isTimeDateReminder){
                reminder.setYear(year);
                reminder.setMonth(month);
                reminder.setDay(day);
                reminder.setHour(hour);
                reminder.setMinute(minute);
            }

            if (groupIDs == null) {
                groupIDs = new ArrayList<>();
            }
            reminder.setGroup(groupIDs);

            if(isLocationReminder){
                reminder.setLocation(pinnedLocations.get(0).getName());
              //  pinnedLocations.clear();
            }else{
                reminder.setLocation("");
            }


            if (alreadyAvailableReminder != null) {
                reminder.setIdFirebase(alreadyAvailableReminder.getIdFirebase());

                if(isLocationReminder){
                    reminder.setLocation(pinnedLocations.get(0).getName());
                    reminder.setYear(year);
                    reminder.setMonth(month);
                    reminder.setDay(day);
                    reminder.setHour(hour);
                    reminder.setMinute(minute);
                }else{
                    reminder.setLocation(alreadyAvailableReminder.getLocation());
                }

                if(isTimeDateReminder){
                    reminder.setYear(year);
                    reminder.setMonth(month);
                    reminder.setDay(day);
                    reminder.setHour(hour);
                    reminder.setMinute(minute);
                    reminder.setLocation("");
                }else{
                    reminder.setYear(alreadyAvailableReminder.getYear());
                    reminder.setMonth(alreadyAvailableReminder.getMonth());
                    reminder.setDay(alreadyAvailableReminder.getDay());
                    reminder.setHour(alreadyAvailableReminder.getHour());
                    reminder.setMinute(alreadyAvailableReminder.getMinute());
                }


                reminder.setGroup(alreadyAvailableReminder.getGroup());
            }

            if(isLocationReminder || isTimeDateReminder ||
                    (alreadyAvailableReminder != null && alreadyAvailableReminder.getHour() != 0)
                    || (alreadyAvailableReminder != null && !alreadyAvailableReminder.getLocation().isEmpty())
            ){
                saveReminder(reminder);
            }else {
                Toast.makeText(getApplicationContext(),"Select time or location" , Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setAddImageListener() {
        layoutMiscellaneous.findViewById(R.id.layoutAddImageReminder).setOnClickListener(view -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CreateReminderActivity.this,
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
        imageRemoveImageReminder.setOnClickListener(view -> {
            imageReminder.setImageBitmap(null);
            imageReminder.setVisibility(View.GONE);
            imageRemoveImageReminder.setVisibility(View.GONE);
            selectedImagePath = "";
        });
    }

    private void setTimeDate() {
        textDateTimeReminder.setText(new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a",
                Locale.getDefault()).format(new Date()));
    }

    private boolean isTitleEmpty() {
        return inputReminderTitle.getText().toString().isEmpty();
    }

    private void setImageColor1Listener() {
        layoutMiscellaneous.findViewById(R.id.imageColor1Reminder).setOnClickListener(view -> {
            selectedReminderColor = "#333333";
            imageColor1Reminder.setImageResource(R.drawable.ic_done);
            imageColor2Reminder.setImageResource(0);
            imageColor3Reminder.setImageResource(0);
            imageColor4Reminder.setImageResource(0);
            imageColor5Reminder.setImageResource(0);
            setViewTitleIndicatorColor();
        });
    }

    private void setImageColor2Listener() {
        layoutMiscellaneous.findViewById(R.id.imageColor2Reminder).setOnClickListener(view -> {
            selectedReminderColor = "#FDBE3B";
            imageColor1Reminder.setImageResource(0);
            imageColor2Reminder.setImageResource(R.drawable.ic_done);
            imageColor3Reminder.setImageResource(0);
            imageColor4Reminder.setImageResource(0);
            imageColor5Reminder.setImageResource(0);
            setViewTitleIndicatorColor();
        });
    }

    private void setImageColor3Listener() {
        layoutMiscellaneous.findViewById(R.id.imageColor3Reminder).setOnClickListener(view -> {
            selectedReminderColor = "#FF4842";
            imageColor1Reminder.setImageResource(0);
            imageColor2Reminder.setImageResource(0);
            imageColor3Reminder.setImageResource(R.drawable.ic_done);
            imageColor4Reminder.setImageResource(0);
            imageColor5Reminder.setImageResource(0);
            setViewTitleIndicatorColor();
        });
    }

    private void setImageColor4Listener() {
        layoutMiscellaneous.findViewById(R.id.imageColor4Reminder).setOnClickListener(view -> {
            selectedReminderColor = "#3A52FC";
            imageColor1Reminder.setImageResource(0);
            imageColor2Reminder.setImageResource(0);
            imageColor3Reminder.setImageResource(0);
            imageColor4Reminder.setImageResource(R.drawable.ic_done);
            imageColor5Reminder.setImageResource(0);
            setViewTitleIndicatorColor();
        });
    }

    private void setImageColor5Listener() {
        layoutMiscellaneous.findViewById(R.id.imageColor5Reminder).setOnClickListener(view -> {
            selectedReminderColor = "#000000";
            imageColor1Reminder.setImageResource(0);
            imageColor2Reminder.setImageResource(0);
            imageColor3Reminder.setImageResource(0);
            imageColor4Reminder.setImageResource(0);
            imageColor5Reminder.setImageResource(R.drawable.ic_done);
            setViewTitleIndicatorColor();
        });
    }

    private void setViewColor() {
        if (alreadyAvailableReminder != null && alreadyAvailableReminder.getColor() != null && !alreadyAvailableReminder.getColor().trim().isEmpty()) {

            switch (alreadyAvailableReminder.getColor()) {
                case "#FDBE3B":
                    layoutMiscellaneous.findViewById(R.id.viewColor2Reminder).performClick();
                    break;
                case "#FF4842":
                    layoutMiscellaneous.findViewById(R.id.viewColor3Reminder).performClick();
                    break;
                case "#3A52FC":
                    layoutMiscellaneous.findViewById(R.id.viewColor4Reminder).performClick();
                    break;
                case "#000000":
                    layoutMiscellaneous.findViewById(R.id.viewColor5Reminder).performClick();
                    break;
            }

        }
    }

    private void checkIfUpdateOrCreate() {
        if (getIntent().getBooleanExtra("isViewOrUpdate", false)) {
            alreadyAvailableReminder = (Reminder) getIntent().getSerializableExtra("reminder");
            setViewReminder();
            setViewColor();

        }
    }


    private void setViewReminder() {
        fillViewReminder();
        setDeleteListener();
    }

    private void fillViewReminder() {
        inputReminderTitle.setText(alreadyAvailableReminder.getTitle());
        inputReminder.setText(alreadyAvailableReminder.getReminderText());
        textDateTimeReminder.setText(alreadyAvailableReminder.getDateTime());
        getDate(alreadyAvailableReminder.getYear(), alreadyAvailableReminder.getMonth(), alreadyAvailableReminder.getDay());
        getTime(alreadyAvailableReminder.getHour(), alreadyAvailableReminder.getMinute());
        selectedReminderColor = alreadyAvailableReminder.getColor();

        if (alreadyAvailableReminder.getImagePath() != null && !alreadyAvailableReminder.getImagePath().trim().isEmpty()) {
            imageReminder.setImageBitmap(BitmapFactory.decodeFile(alreadyAvailableReminder.getImagePath()));
            imageReminder.setVisibility(View.VISIBLE);
            imageRemoveImageReminder.setVisibility(View.VISIBLE);
            selectedImagePath = alreadyAvailableReminder.getImagePath();
        }
    }

    private void setDeleteListener() {
        LinearLayout layoutDeleteNote = findViewById(R.id.layoutDeleteReminder);
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

        dialogDeleteReminder = builder.create();

        if (dialogDeleteReminder.getWindow() != null) {
            dialogDeleteReminder.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        dialogView.findViewById(R.id.textDeleteNote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                class DeleteNoteTask extends AsyncTask<Void, Void, Void> {

                    @Override
                    protected Void doInBackground(Void... voids) {

                        ReminderDataBase.getDatabase(getApplicationContext()).reminderDao().deleteReminder(alreadyAvailableReminder);
                        FirebaseReminders firebaseReminders = new FirebaseReminders();
                        firebaseReminders.delete(alreadyAvailableReminder);

                        return null;
                    }


                    @Override
                    protected void onPostExecute(Void avoid) {
                        super.onPostExecute(avoid);
                        Intent intent = new Intent();
                        intent.putExtra("isReminderDeleted", true);
                        setResult(RESULT_OK, intent);
                        finish();
                    }

                }

                new DeleteNoteTask().execute();
            }
        });

        dialogView.findViewById(R.id.textCancel).setOnClickListener(view -> dialogDeleteReminder.dismiss());

        dialogDeleteReminder.show();
    }

    private void setCollaborateListener() {
        layoutMiscellaneous.findViewById(R.id.layoutAddCollaboratorReminder).setOnClickListener(view -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            if (!isNetworkAvailable(getApplicationContext())) {
                Toast.makeText(getApplicationContext(), "No internet", Toast.LENGTH_SHORT).show();
                return;
            }
            popupGroup = new GroupLayout(CreateReminderActivity.this);
            popupGroup.setDialog(CreateReminderActivity.this);
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
        if (dialogDeleteReminder != null && dialogDeleteReminder.isShowing()) {
            dialogDeleteReminder.dismiss();
        }
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void setAddPinnedListener() {
        layoutMiscellaneous.findViewById(R.id.layoutAddPlace).setOnClickListener(view -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            if (!isNetworkAvailable(getApplicationContext())) {
                Toast.makeText(getApplicationContext(), "No internet", Toast.LENGTH_SHORT).show();
                return;
            }
            pinnedLocationDialog = new PinnedLocationDialog(CreateReminderActivity.this);
            pinnedLocationDialog.setDialog(CreateReminderActivity.this);
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

        if (pinnedLocations == null) {
            pinnedLocations = new ArrayList<>();
        }

        pinnedLocations.add(pinnedLocation);
        layoutMiscellaneous.findViewById(R.id.layoutAddDateTime).setAlpha(0.5f);
        layoutMiscellaneous.findViewById(R.id.layoutAddDateTime).setOnClickListener(v -> {

        });
        isLocationReminder = true;
        pinnedLocationDialog.dismiss();

    }
}