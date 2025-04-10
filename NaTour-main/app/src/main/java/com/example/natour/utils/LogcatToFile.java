package com.example.natour.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class LogcatToFile {
    private Context context;
    private String baseCommand = "logcat -D -f ";
    private String endCommand = "*:S";
    private String completeCommand;
    private String fileName;
    private List<String> tagList;

    public LogcatToFile(String fileName, List<String> tagList, @NonNull Context context) {
        this.fileName = fileName + ".txt ";
        this.tagList = tagList;
        this.context = context;
        completeCommand = baseCommand + context.getFilesDir() + "/" + this.fileName + " ";
        for(int i = 0; i < tagList.size(); i++)
            completeCommand = completeCommand + tagList.get(i) + ":I ";
        completeCommand = completeCommand + endCommand;
    }

    public void execute() throws IOException {
        File filename = new File(context.getFilesDir() + "/" + this.fileName);
        filename.createNewFile();
        Runtime.getRuntime().exec(completeCommand);
        Log.d("LOGCAT", "Command -> " + completeCommand);
    }
}
