package com.example.natour.utils;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import com.example.natour.R;
import com.example.natour.entity.Message;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private Context mContext;
    private List<Message> mMessageList;
    private String mSender;

    public MessageListAdapter(Context context, List<Message> messageList, String sender) {
        mContext = context;
        mMessageList = messageList;
        mSender = sender;
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {
        Message message = (Message) mMessageList.get(position);

        if (message.getSender().equals(mSender)) {
            // If the current user is the sender of the message
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            // If some other user sent the message
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }


    // Inflates the appropriate layout according to the ViewType.
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_sender_view, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_receiver_view, parent, false);
            return new ReceivedMessageHolder(view);
        }

        return null;
    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message message = (Message) mMessageList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
        }
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText,dateText, timeText;

        SentMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_message_me);
            timeText = (TextView) itemView.findViewById(R.id.text_timestamp_me);
            dateText = (TextView) itemView.findViewById(R.id.text_date_me);
        }

        void bind(Message message) {
            messageText.setText(message.getBody());

            // Format the stored timestamp into a readable String using method.
            Date time = new Date();
            time = message.getDate();

            SimpleDateFormat formatterDate = new SimpleDateFormat("dd-MM-yyyy");
            String dateString = formatterDate.format(time);

            SimpleDateFormat formatterTime = new SimpleDateFormat("HH:mm");
            String timeString = formatterTime.format(time);
            dateText.setText(dateString);
            timeText.setText(timeString);
        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, dateText, timeText, nameText;
        ImageView profileImage;

        ReceivedMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_message_other);
            dateText = (TextView) itemView.findViewById(R.id.text_date_other);
            timeText = (TextView) itemView.findViewById(R.id.text_timestamp_other);
            nameText = (TextView) itemView.findViewById(R.id.text_user_other);
            profileImage = (ImageView) itemView.findViewById(R.id.image_profile_other);
        }

        void bind(Message message) {
            messageText.setText(message.getBody());

            // Format the stored timestamp into a readable String using method.
            Date time = new Date();
            time = message.getDate();

            SimpleDateFormat formatterDate = new SimpleDateFormat("dd-MM-yyyy");
            String dateString = formatterDate.format(time);

            SimpleDateFormat formatterTime = new SimpleDateFormat("HH:mm");
            String timeString = formatterTime.format(time);
            dateText.setText(dateString);
            timeText.setText(timeString);


            nameText.setText(message.getSender());

        }
    }
}