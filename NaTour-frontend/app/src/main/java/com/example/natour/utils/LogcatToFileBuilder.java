package com.example.natour.utils;

import android.content.Context;

import java.util.LinkedList;

public class LogcatToFileBuilder {
    private Context context;
    private String fileName;
    private LinkedList<String> tagList = new LinkedList<>();

    private LogcatToFileBuilder(Context context){
        this.context = context;
    }

    public static LogcatToFileBuilder newBuilder(Context context){
        return new LogcatToFileBuilder(context);
    }

    public LogcatToFileBuilder addTag(String tag){
        tagList.add(tag);
        return this;
    }

    public LogcatToFileBuilder fileName(String fileName){
        this.fileName = fileName;
        return this;
    }

    public LogcatToFile build(){
        return new LogcatToFile(fileName, tagList, context);
    }
}
