package com.tngoc.familytaskapp.ui.auth;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.tngoc.familytaskapp.data.repository.AuthRepository;

public class AuthViewModel extends ViewModel {

    private final AuthRepository authRepository;

    public final MutableLiveData<FirebaseUser> userLiveData    = new MutableLiveData<>();
    public final MutableLiveData<String>       errorLiveData    = new MutableLiveData<>();
    public final MutableLiveData<Boolean>      loadingLiveData  = new MutableLiveData<>();

    public AuthViewModel() {
        this.authRepository = new AuthRepository();
    }

    public FirebaseUser getCurrentUser() {
        return authRepository.getCurrentUser();
    }

    public void login(String email, String password) {
        authRepository.login(email, password, userLiveData, loadingLiveData, errorLiveData);
    }

    public void register(String email, String password, String name) {
        authRepository.register(email, password, name, userLiveData, loadingLiveData, errorLiveData);
    }

    public void logout() {
        authRepository.logout();
    }
}
