package com.example.natour.utils;


import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import com.example.natour.R;
import com.example.natour.entity.Message;


import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReceivedMessageHolder extends RecyclerView.ViewHolder {
    TextView messageText;
    TextView timeText;
    TextView dateText;
    TextView nameText;
    ImageView profileImage;

    ReceivedMessageHolder(View itemView) {
        super(itemView);
        messageText = (TextView) itemView.findViewById(R.id.text_message_other);
        timeText = (TextView) itemView.findViewById(R.id.text_timestamp_other);
        dateText = (TextView) itemView.findViewById(R.id.text_date_other);
        nameText = (TextView) itemView.findViewById(R.id.text_user_other);
        profileImage = (ImageView) itemView.findViewById(R.id.image_profile_other);
    }

    void bind(Message message) {
        messageText.setText(message.getBody());


        Timestamp time = message.getDate();

        SimpleDateFormat formatterDate = new SimpleDateFormat("dd-MM-yyyy");
        String dateString = formatterDate.format(time);

        SimpleDateFormat formatterTime = new SimpleDateFormat("HH:mm");
        String timeString = formatterTime.format(time);

        // Format the stored timestamp into a readable String using method.
        dateText.setText(dateString);
        timeText.setText(timeString);
        nameText.setText(message.getSender());


    }
}
