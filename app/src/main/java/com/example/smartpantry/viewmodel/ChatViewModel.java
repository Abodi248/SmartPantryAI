package com.example.smartpantry.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.example.smartpantry.database.ChatMessageEntity;
import com.example.smartpantry.model.ChatMessage;
import com.example.smartpantry.model.Ingredient;
import com.example.smartpantry.repository.ChatMessageRepository;
import com.example.smartpantry.repository.ChatRepository;
import com.example.smartpantry.repository.IngredientRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatViewModel extends AndroidViewModel {

    private final ChatRepository chatRepository;
    private final ChatMessageRepository messageRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final LiveData<List<Ingredient>> pantryLiveData;
    private final Observer<List<Ingredient>> pantryObserver;
    private List<Ingredient> currentPantry = Collections.emptyList();

    private final MutableLiveData<List<ChatMessage>> messages =
            new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public ChatViewModel(@NonNull Application application) {
        super(application);
        chatRepository = new ChatRepository(application);
        messageRepository = new ChatMessageRepository(application);

        pantryLiveData = new IngredientRepository(application).getAll();
        pantryObserver = list -> currentPantry = list != null ? list : Collections.emptyList();
        pantryLiveData.observeForever(pantryObserver);

        loadHistoryFromDb();
    }

    private void loadHistoryFromDb() {
        executor.execute(() -> {
            List<ChatMessageEntity> entities = messageRepository.getAllMessagesSync();
            if (entities == null || entities.isEmpty()) return;
            List<ChatMessage> loaded = new ArrayList<>();
            for (ChatMessageEntity e : entities) {
                ChatMessage.Role role = "user".equals(e.role)
                        ? ChatMessage.Role.USER : ChatMessage.Role.ASSISTANT;
                loaded.add(new ChatMessage(e.text, role, e.timestamp));
            }
            messages.postValue(loaded);
        });
    }

    public LiveData<List<ChatMessage>> getMessages() { return messages; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getError() { return error; }

    public LiveData<String> getBackendLabel() { return chatRepository.getBackendLabel(); }

    public boolean isAiAvailable() { return chatRepository.isAiAvailable(); }

    public LiveData<Boolean> getIsAiInitializing() { return chatRepository.getIsInitializing(); }

    public void sendMessage(String userText) {
        if (userText == null || userText.trim().isEmpty()) return;

        List<ChatMessage> current = messages.getValue();
        // Snapshot history before the new message — this is the context for the AI
        List<ChatMessage> history = current != null ? new ArrayList<>(current) : new ArrayList<>();

        List<ChatMessage> updated = new ArrayList<>(history);
        updated.add(new ChatMessage(userText.trim(), ChatMessage.Role.USER));
        messages.setValue(updated);

        messageRepository.insert("user", userText.trim(), System.currentTimeMillis());

        isLoading.setValue(true);
        error.setValue(null);

        chatRepository.sendMessage(
                userText.trim(),
                currentPantry,
                history,
                reply -> {
                    messageRepository.insert("assistant", reply, System.currentTimeMillis());
                    List<ChatMessage> list = messages.getValue();
                    List<ChatMessage> next = list != null ? new ArrayList<>(list) : new ArrayList<>();
                    next.add(new ChatMessage(reply, ChatMessage.Role.ASSISTANT));
                    messages.postValue(next);
                    isLoading.postValue(false);
                },
                err -> {
                    error.postValue(err);
                    isLoading.postValue(false);
                }
        );
    }

    public void clearHistory() {
        messageRepository.deleteAll();
        messages.setValue(new ArrayList<>());
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        pantryLiveData.removeObserver(pantryObserver);
        executor.shutdown();
    }
}
