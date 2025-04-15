package com.example.natour.daointerfaces;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.natour.callbackinterfaces.GetMessagesResultCallback;
import com.example.natour.callbackinterfaces.GetNotificationNumberResultCallback;
import com.example.natour.callbackinterfaces.GetUserChatsResultCallback;
import com.example.natour.callbackinterfaces.SaveMessageResultCallback;
import com.example.natour.entity.Message;

import java.util.ArrayList;

public interface MessageDAO {
    void saveMessage(Message message, Context context,@NonNull SaveMessageResultCallback callback);
    ArrayList<Message> getMessages(String sender, String receiver, Context context,@NonNull GetMessagesResultCallback callback);
    void getUserChats(String sender, Context context, @NonNull GetUserChatsResultCallback callback);
    int getNotificationNumber(String email, Context context,@NonNull GetNotificationNumberResultCallback callback);
}
