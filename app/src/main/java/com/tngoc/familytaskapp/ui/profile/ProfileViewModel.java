package com.tngoc.familytaskapp.ui.profile;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.tngoc.familytaskapp.data.model.User;
import com.tngoc.familytaskapp.data.repository.UserRepository;

public class ProfileViewModel extends ViewModel {

    private final UserRepository userRepository;

    public final MutableLiveData<User> userLiveData = new MutableLiveData<>();
    public final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    public final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    public final MutableLiveData<Boolean> successLiveData = new MutableLiveData<>();

    public ProfileViewModel() {
        this.userRepository = new UserRepository();
    }

    public void loadUser(String userId) {
        userRepository.getUser(userId, userLiveData, errorLiveData);
    }

    public void updateUser(User user) {
        userRepository.updateUser(user, successLiveData, errorLiveData);
    }

}

