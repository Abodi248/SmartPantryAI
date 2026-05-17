package com.example.smartpantry.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.smartpantry.database.UserEntity;
import com.example.smartpantry.repository.UserRepository;
import com.example.smartpantry.utils.PasswordHasher;
import com.example.smartpantry.utils.SessionManager;

public class UserViewModel extends AndroidViewModel {

    public enum AuthResult { SUCCESS, INVALID_CREDENTIALS, USERNAME_TAKEN, ERROR }

    private final UserRepository repository;
    private final SessionManager session;

    private final MutableLiveData<AuthResult> authResult = new MutableLiveData<>();
    private LiveData<UserEntity> loggedInUser;

    public UserViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
        session = new SessionManager(application);
        if (session.isLoggedIn()) {
            loggedInUser = repository.findById(session.getUserId());
        }
    }

    public LiveData<AuthResult> getAuthResult() { return authResult; }

    public void resetAuthResult() { authResult.setValue(null); }

    public LiveData<UserEntity> getLoggedInUser() { return loggedInUser; }

    public boolean isLoggedIn() { return session.isLoggedIn(); }

    public void login(String username, String password) {
        if (username.trim().isEmpty() || password.isEmpty()) {
            authResult.setValue(AuthResult.INVALID_CREDENTIALS);
            return;
        }
        String hash = PasswordHasher.hash(password);
        repository.findByUsername(username.trim(), user -> {
            if (user == null || !user.passwordHash.equals(hash)) {
                authResult.setValue(AuthResult.INVALID_CREDENTIALS);
            } else {
                session.login(user.id, user.displayName);
                loggedInUser = repository.findById(user.id);
                authResult.setValue(AuthResult.SUCCESS);
            }
        });
    }

    public void createAccount(String displayName, String username, String password) {
        if (displayName.trim().isEmpty() || username.trim().isEmpty() || password.isEmpty()) {
            authResult.setValue(AuthResult.ERROR);
            return;
        }
        repository.findByUsername(username.trim(), existing -> {
            if (existing != null) {
                authResult.setValue(AuthResult.USERNAME_TAKEN);
                return;
            }
            UserEntity newUser = new UserEntity();
            newUser.displayName = displayName.trim();
            newUser.username = username.trim();
            newUser.passwordHash = PasswordHasher.hash(password);
            repository.insert(newUser, id -> {
                if (id <= 0) {
                    authResult.setValue(AuthResult.USERNAME_TAKEN);
                } else {
                    session.login(id, newUser.displayName);
                    loggedInUser = repository.findById(id);
                    authResult.setValue(AuthResult.SUCCESS);
                }
            });
        });
    }

    public void updateDisplayName(String newName) {
        if (loggedInUser == null || loggedInUser.getValue() == null) return;
        UserEntity user = loggedInUser.getValue();
        user.displayName = newName.trim();
        repository.update(user);
        session.updateDisplayName(newName.trim());
    }

    public void logout() {
        session.logout();
        loggedInUser = null;
    }
}
