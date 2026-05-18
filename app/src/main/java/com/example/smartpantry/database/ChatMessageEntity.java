package com.example.smartpantry.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_messages")
public class ChatMessageEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String role = "";

    @NonNull
    public String text = "";

    public long timestamp;
}
