package com.example.natour.utils;


import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import com.example.natour.R;
import com.example.natour.entity.Message;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SentMessageHolder extends RecyclerView.ViewHolder {
    TextView messageText;
    TextView timeText;
    TextView dateText;

    SentMessageHolder(View itemView) {
        super(itemView);

        messageText = (TextView) itemView.findViewById(R.id.text_message_me);
        timeText = (TextView) itemView.findViewById(R.id.text_timestamp_me);
        dateText = (TextView) itemView.findViewById(R.id.text_date_me);
    }

    void bind(Message message) {
        messageText.setText(message.getBody());

        // Format the stored timestamp into a readable String using method.e
        Timestamp time = message.getDate();

        SimpleDateFormat formatterDate = new SimpleDateFormat("dd-MM-yyyy");
        String dateString = formatterDate.format(time);

        SimpleDateFormat formatterTime = new SimpleDateFormat("HH:mm");
        String timeString = formatterTime.format(time);

        timeText.setText(timeString);
        dateText.setText(dateString);

    }
}
