package com.tngoc.familytaskapp.utils;

public class Constants {
    // Firestore collections
    public static final String COLLECTION_USERS          = "users";
    public static final String COLLECTION_WORKSPACES     = "workspaces";
    public static final String COLLECTION_TASKS          = "tasks";
    public static final String COLLECTION_INVITATIONS    = "invitations";
    public static final String COLLECTION_NOTIFICATIONS  = "notifications";
    public static final String COLLECTION_REWARDS        = "rewards";
    public static final String COLLECTION_CHAT_HISTORY   = "chat_history"; // Sửa theo sơ đồ
    public static final String COLLECTION_MESSAGES       = "message";      // Sửa theo sơ đồ

    // Task status
    public static final String TASK_STATUS_TODO        = "todo";
    public static final String TASK_STATUS_IN_PROGRESS = "in_progress";
    public static final String TASK_STATUS_PENDING     = "pending"; // Chờ duyệt
    public static final String TASK_STATUS_DONE        = "done";
    public static final String TASK_STATUS_OVERDUE     = "overdue"; // Quá hạn

    // Invitation status
    public static final String INVITATION_PENDING  = "pending";
    public static final String INVITATION_ACCEPTED = "accepted";
    public static final String INVITATION_DECLINED = "declined";

    // Notification types
    public static final String NOTIF_TASK_ASSIGNED = "task_assigned";
    public static final String NOTIF_TASK_DONE     = "task_done";
    public static final String NOTIF_INVITATION    = "invitation";
    public static final String NOTIF_REWARD        = "reward";
    public static final String NOTIF_REWARD_ADD    = "reward_add";
    public static final String NOTIF_REWARD_PENALTY = "reward_penalty";
    public static final String NOTIF_TASK_ALMOST_OVERDUE = "task_almost_overdue";

    // SharedPreferences
    public static final String PREF_NAME     = "FamilyTaskPrefs";
    public static final String PREF_LANGUAGE = "language";
    public static final String PREF_USER_ID  = "userId";

    // Chat roles
    public static final String CHAT_ROLE_USER = "user";
    public static final String CHAT_ROLE_BOT  = "bot";

    // Gemini API Key
    public static final String GEMINI_API_KEY = "AIzaSyDDlzLycTZbO3cgMi0q6S4YA0hoN6wORJo";
}
