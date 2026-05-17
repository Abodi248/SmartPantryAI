package com.example.smartpantry.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.smartpantry.database.AppDatabase;
import com.example.smartpantry.database.UserDao;
import com.example.smartpantry.database.UserEntity;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserRepository {

    public interface Callback<T> {
        void onResult(T result);
    }

    private final UserDao userDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public UserRepository(Application app) {
        userDao = AppDatabase.getInstance(app).userDao();
    }

    public void insert(UserEntity user, Callback<Long> callback) {
        executor.execute(() -> {
            long id;
            try {
                id = userDao.insert(user);
            } catch (Exception e) {
                id = -1L;
            }
            long finalId = id;
            new android.os.Handler(android.os.Looper.getMainLooper())
                    .post(() -> callback.onResult(finalId));
        });
    }

    public void update(UserEntity user) {
        executor.execute(() -> userDao.update(user));
    }

    public void findByUsername(String username, Callback<UserEntity> callback) {
        executor.execute(() -> {
            UserEntity result = userDao.findByUsernameSync(username);
            new android.os.Handler(android.os.Looper.getMainLooper())
                    .post(() -> callback.onResult(result));
        });
    }

    public LiveData<UserEntity> findById(long id) {
        return userDao.findById(id);
    }
}
