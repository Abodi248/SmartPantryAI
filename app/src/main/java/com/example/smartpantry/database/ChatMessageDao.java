package com.example.smartpantry.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ChatMessageDao {

    @Insert
    void insert(ChatMessageEntity entity);

    @Query("DELETE FROM chat_messages")
    void deleteAll();

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    LiveData<List<ChatMessageEntity>> getAllMessages();

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    List<ChatMessageEntity> getAllMessagesSync();
}
