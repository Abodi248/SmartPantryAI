package com.example.smartpantry.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.example.smartpantry.model.ChatMessage;
import com.example.smartpantry.model.Ingredient;
import com.example.smartpantry.repository.ChatRepository;
import com.example.smartpantry.repository.IngredientRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatViewModel extends AndroidViewModel {

    private final ChatRepository chatRepository;
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

        pantryLiveData = new IngredientRepository(application).getAll();
        pantryObserver = list -> currentPantry = list != null ? list : Collections.emptyList();
        pantryLiveData.observeForever(pantryObserver);
    }

    public LiveData<List<ChatMessage>> getMessages() { return messages; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getError() { return error; }

    /**
     * Emits "On-device (GPU)", "On-device (CPU)", or "Cloud".
     * Updates asynchronously once the local model init resolves.
     */
    public LiveData<String> getBackendLabel() { return chatRepository.getBackendLabel(); }

    public void sendMessage(String userText) {
        if (userText == null || userText.trim().isEmpty()) return;

        List<ChatMessage> current = messages.getValue();
        List<ChatMessage> updated = current != null ? new ArrayList<>(current) : new ArrayList<>();
        updated.add(new ChatMessage(userText.trim(), ChatMessage.Role.USER));
        messages.setValue(updated);

        isLoading.setValue(true);
        error.setValue(null);

        chatRepository.sendMessage(
                userText.trim(),
                currentPantry,
                reply -> {
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

    @Override
    protected void onCleared() {
        super.onCleared();
        chatRepository.shutdown();
        pantryLiveData.removeObserver(pantryObserver);
    }
}
