package com.example.snare.ViewHolders;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snare.Entities.Reminder;
import com.example.snare.R;
import com.makeramen.roundedimageview.RoundedImageView;

public class ReminderViewHolder extends RecyclerView.ViewHolder {

    public LinearLayout layoutReminder;
    private final TextView textTitleReminder;
    private final TextView textReminder;
    private final TextView textDateTimeReminder;
    private final RoundedImageView imageReminder;
    private final TextView dateTimeToNotify;

    public ReminderViewHolder(@NonNull View itemView) {
        super(itemView);
        textTitleReminder = itemView.findViewById(R.id.textTitleReminder);
        textReminder = itemView.findViewById(R.id.textReminder);
        textDateTimeReminder = itemView.findViewById(R.id.textDateTimeReminder);
        layoutReminder = itemView.findViewById(R.id.layoutReminder);
        imageReminder = itemView.findViewById(R.id.imageReminder);
        dateTimeToNotify = itemView.findViewById(R.id.dateTimeToNotify);
    }

    public void setReminder(Reminder reminder) {
        textTitleReminder.setText(reminder.getTitle());
        textReminder.setText(reminder.getReminderText());
        textDateTimeReminder.setText(reminder.getDateTime());
        String notifyAt = setDateTime(reminder);
        dateTimeToNotify.setText(notifyAt);
        GradientDrawable gradientDrawable = (GradientDrawable) layoutReminder.getBackground();
        if (reminder.getColor() != null) {
            gradientDrawable.setColor(Color.parseColor(reminder.getColor()));
        } else {
            gradientDrawable.setColor(Color.parseColor("#333333"));
        }

        if (reminder.getImagePath() != null) {
            imageReminder.setImageBitmap(BitmapFactory.decodeFile(reminder.getImagePath()));
            imageReminder.setVisibility(View.VISIBLE);
        } else {
            imageReminder.setVisibility(View.GONE);
        }
    }

    private String setDateTime(Reminder reminder) {
        String output = reminder.getDay() + " ";
        String month = "";
        switch (reminder.getMonth()) {
            case 0:
                month = "Jan";
                break;
            case 1:
                month = "Feb";
                break;
            case 2:
                month = "Mar";
                break;
            case 3:
                month = "Apr";
                break;
            case 4:
                month = "May";
                break;
            case 5:
                month = "Jun";
                break;
            case 6:
                month = "Jul";
                break;
            case 7:
                month = "Aug";
                break;
            case 8:
                month = "Sep";
                break;
            case 9:
                month = "Oct";
                break;
            case 10:
                month = "Nov";
                break;
            case 11:
                month = "Dec";
                break;
        }
        if(reminder.getHour() == 0){
            output = "";
            return output;
        }

        output+=month + " ,"+ reminder.getHour()+":"+reminder.getMinute();
        return output;
    }
}
