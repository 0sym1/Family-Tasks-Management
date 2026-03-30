package com.tngoc.familytaskapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class AuthRepository {

    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore db;

    public AuthRepository() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    /** Đăng nhập bằng email + password qua Firebase Auth */
    public void login(String email, String password,
                      MutableLiveData<FirebaseUser> userLiveData,
                      MutableLiveData<Boolean> loadingLiveData,
                      MutableLiveData<String> errorLiveData) {
        loadingLiveData.setValue(true);
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    loadingLiveData.setValue(false);
                    userLiveData.setValue(authResult.getUser());
                })
                .addOnFailureListener(e -> {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue(mapError(e.getMessage()));
                });
    }

    /** Đăng ký: tạo Firebase Auth account, rồi lưu profile vào Firestore */
    public void register(String email, String password, String name, String username,
                         MutableLiveData<FirebaseUser> userLiveData,
                         MutableLiveData<Boolean> loadingLiveData,
                         MutableLiveData<String> errorLiveData) {
        loadingLiveData.setValue(true);
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user == null) {
                        loadingLiveData.setValue(false);
                        errorLiveData.setValue("Đăng ký thất bại");
                        return;
                    }
                    // Lưu profile vào Firestore collection "users"
                    Map<String, Object> profile = new HashMap<>();
                    profile.put("name", name);
                    profile.put("username", username);
                    profile.put("password", hashPassword(password));
                    profile.put("email", email);
                    profile.put("language", "vietnamese");
                    profile.put("point", 0);
                    profile.put("avt_url", "");

                    db.collection("users")
                            .document(user.getUid())
                            .set(profile)
                            .addOnSuccessListener(unused -> {
                                loadingLiveData.setValue(false);
                                userLiveData.setValue(user);
                            })
                            .addOnFailureListener(e -> {
                                loadingLiveData.setValue(false);
                                errorLiveData.setValue(e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue(mapError(e.getMessage()));
                });
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return password; // Fallback to plain text if hashing fails (not ideal)
        }
    }

    public void logout() {
        firebaseAuth.signOut();
    }

    /** Dịch lỗi Firebase sang tiếng Việt */
    private String mapError(String error) {
        if (error == null) return "Lỗi không xác định";
        // Đăng ký
        if (error.contains("email address is already in use"))
            return "Email đã được sử dụng";
        if (error.contains("badly formatted"))
            return "Email không đúng định dạng";
        // Đăng nhập
        if (error.contains("no user record") || error.contains("user-not-found"))
            return "Email không tồn tại trong hệ thống";
        if (error.contains("password is invalid") || error.contains("wrong-password")
                || error.contains("INVALID_LOGIN_CREDENTIALS"))
            return "Email hoặc mật khẩu không đúng";
        if (error.contains("user disabled"))
            return "Tài khoản đã bị khoá, vui lòng liên hệ hỗ trợ";
        // Mật khẩu
        if (error.contains("weak password") || error.contains("at least 6 characters"))
            return "Mật khẩu quá yếu, cần ít nhất 6 ký tự";
        // Mạng
        if (error.contains("network error") || error.contains("NETWORK_ERROR"))
            return "Lỗi kết nối mạng, vui lòng thử lại";
        if (error.contains("too many requests") || error.contains("TOO_MANY_ATTEMPTS"))
            return "Quá nhiều lần thử, vui lòng thử lại sau";
        return error;
    }
}
