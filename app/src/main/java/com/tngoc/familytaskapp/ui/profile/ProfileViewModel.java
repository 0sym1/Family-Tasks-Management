package com.tngoc.familytaskapp.ui.profile;

import android.net.Uri;

import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.tngoc.familytaskapp.data.model.User;
import com.tngoc.familytaskapp.data.repository.UserRepository;

public class ProfileViewModel extends ViewModel {

    private final UserRepository userRepository;

    public final MutableLiveData<User> userLiveData = new MutableLiveData<>();
    public final MediatorLiveData<Boolean> loadingLiveData = new MediatorLiveData<>();
    public final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    public final MutableLiveData<Boolean> successLiveData = new MutableLiveData<>();
    public final MutableLiveData<String> avatarUrlLiveData = new MutableLiveData<>();

    public ProfileViewModel() {
        this.userRepository = new UserRepository();
        loadingLiveData.setValue(false);

        // Reset loading state when any of the results come back
        loadingLiveData.addSource(userLiveData, user -> loadingLiveData.setValue(false));
        loadingLiveData.addSource(errorLiveData, error -> loadingLiveData.setValue(false));
        loadingLiveData.addSource(successLiveData, success -> loadingLiveData.setValue(false));
        loadingLiveData.addSource(avatarUrlLiveData, url -> loadingLiveData.setValue(false));
    }

    public void loadUser(String userId) {
        loadingLiveData.setValue(true);
        userRepository.getUser(userId, userLiveData, errorLiveData);
    }

    public void uploadAvatar(String userId, Uri imageUri) {
        loadingLiveData.setValue(true);
        userRepository.uploadAvatar(userId, imageUri, avatarUrlLiveData, errorLiveData);
    }

    public void updateUser(User user) {
        loadingLiveData.setValue(true);
        userRepository.updateUser(user, successLiveData, errorLiveData);
    }
}
