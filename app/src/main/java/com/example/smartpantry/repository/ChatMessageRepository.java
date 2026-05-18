package com.example.smartpantry.repository;

import android.app.Application;
import com.example.smartpantry.database.AppDatabase;
import com.example.smartpantry.database.ChatMessageDao;
import com.example.smartpantry.database.ChatMessageEntity;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatMessageRepository {

    private final ChatMessageDao dao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ChatMessageRepository(Application application) {
        dao = AppDatabase.getInstance(application).chatMessageDao();
    }

    public List<ChatMessageEntity> getAllMessagesSync() {
        return dao.getAllMessagesSync();
    }

    public void insert(String role, String text, long timestamp) {
        executor.execute(() -> {
            ChatMessageEntity e = new ChatMessageEntity();
            e.role = role;
            e.text = text;
            e.timestamp = timestamp;
            dao.insert(e);
        });
    }

    public void deleteAll() {
        executor.execute(dao::deleteAll);
    }
}
