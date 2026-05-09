package com.example.smartpantry.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.smartpantry.model.ChatMessage;
import java.util.ArrayList;
import java.util.List;

public class ChatViewModel extends ViewModel {

    private final MutableLiveData<List<ChatMessage>> messages = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public LiveData<List<ChatMessage>> getMessages() { return messages; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    // do later: sendMessage(String userText) → SafetyFilter → Gemini API → append reply
}
