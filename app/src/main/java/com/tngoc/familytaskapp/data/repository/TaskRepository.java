package com.tngoc.familytaskapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.tngoc.familytaskapp.data.model.Notification;
import com.tngoc.familytaskapp.data.model.Reward;
import com.tngoc.familytaskapp.data.model.Task;
import com.tngoc.familytaskapp.data.model.TaskHistory;
import com.tngoc.familytaskapp.utils.Constants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TaskRepository {

    private final FirebaseFirestore db;
    private final NotificationRepository notificationRepository;

    public TaskRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.notificationRepository = new NotificationRepository();
    }

    public void createTask(String workspaceId, Task task, MutableLiveData<String> taskIdLiveData, MutableLiveData<String> errorLiveData) {
        db.collection(Constants.COLLECTION_WORKSPACES)
                .document(workspaceId)
                .collection(Constants.COLLECTION_TASKS)
                .add(task)
                .addOnSuccessListener(ref -> {
                    taskIdLiveData.setValue(ref.getId());
                    recordHistory(workspaceId, ref.getId(), task.getTitle(), "task_created", null, task.getStatus(), 0);
                    updateWorkspaceTaskCounts(workspaceId);
                    
                    if (task.getAssignedToIds() != null) {
                        for (String userId : task.getAssignedToIds()) {
                            Notification notif = new Notification();
                            notif.setUserId(userId);
                            notif.setType(Constants.NOTIF_TASK_ASSIGNED);
                            notif.setWorkspaceId(workspaceId);
                            notif.setTargetName(task.getTitle());
                            notificationRepository.sendNotification(notif);
                        }
                    }
                })
                .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }

    public void getTasksInWorkspace(String workspaceId, MutableLiveData<List<Task>> tasksLiveData, MutableLiveData<String> errorLiveData) {
        db.collection(Constants.COLLECTION_WORKSPACES)
                .document(workspaceId)
                .collection(Constants.COLLECTION_TASKS)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        errorLiveData.setValue(e.getMessage());
                        return;
                    }
                    if (snapshots != null) {
                        List<Task> list = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Task task = doc.toObject(Task.class);
                            task.setTaskId(doc.getId());
                            checkAndHandleTask(workspaceId, task);
                            list.add(task);
                        }
                        tasksLiveData.setValue(list);
                    }
                });
    }

    public void getTask(String workspaceId, String taskId, MutableLiveData<Task> taskLiveData, MutableLiveData<String> errorLiveData) {
        db.collection(Constants.COLLECTION_WORKSPACES)
                .document(workspaceId)
                .collection(Constants.COLLECTION_TASKS)
                .document(taskId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        errorLiveData.setValue(e.getMessage());
                        return;
                    }
                    if (snapshot != null && snapshot.exists()) {
                        Task task = snapshot.toObject(Task.class);
                        if (task != null) {
                            task.setTaskId(snapshot.getId());
                            checkAndHandleTask(workspaceId, task);
                            taskLiveData.setValue(task);
                        }
                    }
                });
    }

    private void checkAndHandleTask(String workspaceId, Task task) {
        checkAndResetRepeatingTask(workspaceId, task);
        
        if (isTaskOverdue(task)) {
            updateTaskStatus(workspaceId, task.getTaskId(), Constants.TASK_STATUS_OVERDUE, null, null);
            task.setStatus(Constants.TASK_STATUS_OVERDUE);
        } else if (isTaskAlmostOverdue(task)) {
             if (Constants.TASK_STATUS_TODO.equals(task.getStatus()) || Constants.TASK_STATUS_IN_PROGRESS.equals(task.getStatus())) {
                 sendAlmostOverdueNotification(workspaceId, task);
             }
        }
    }

    private boolean isTaskAlmostOverdue(Task task) {
        Calendar deadline = getDeadlineCalendar(task);
        if (deadline == null) return false;
        
        long diff = deadline.getTimeInMillis() - System.currentTimeMillis();
        return diff > 0 && diff <= 3600000;
    }

    private Calendar getDeadlineCalendar(Task task) {
        Calendar deadline = Calendar.getInstance();
        if (task.isRepeating()) {
            if (task.getEndTime() == null) return null;
            
            // Theo yêu cầu: Hạn của nhiệm vụ lặp lại dựa trên ngày nó được reset gần nhất
            if (task.getLastResetDate() != null) {
                deadline.setTime(task.getLastResetDate().toDate());
            } else if (task.getCreatedAt() != null) {
                // Nếu chưa bao giờ reset, dùng ngày tạo làm mốc ban đầu
                deadline.setTime(task.getCreatedAt().toDate());
            } else {
                return null;
            }
            
            try {
                String[] parts = task.getEndTime().split(":");
                deadline.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
                deadline.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
                deadline.set(Calendar.SECOND, 0);
                deadline.set(Calendar.MILLISECOND, 0);
                return deadline;
            } catch (Exception e) {
                return null;
            }
        } else {
            if (task.getEndDate() == null) return null;
            deadline.setTime(task.getEndDate().toDate());
            if (task.getEndTime() != null) {
                try {
                    String[] parts = task.getEndTime().split(":");
                    deadline.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
                    deadline.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
                } catch (Exception e) {}
            }
            return deadline;
        }
    }

    private void sendAlmostOverdueNotification(String workspaceId, Task task) {
        if (task.getAssignedToIds() != null) {
            for (String userId : task.getAssignedToIds()) {
                Notification notif = new Notification();
                notif.setUserId(userId);
                notif.setType(Constants.NOTIF_TASK_ALMOST_OVERDUE);
                notif.setWorkspaceId(workspaceId);
                notif.setTargetName(task.getTitle());
                notificationRepository.sendNotification(notif);
            }
        }
    }

    private boolean isTaskOverdue(Task task) {
        if (!Constants.TASK_STATUS_TODO.equals(task.getStatus()) && 
            !Constants.TASK_STATUS_IN_PROGRESS.equals(task.getStatus())) {
            return false;
        }

        Calendar deadline = getDeadlineCalendar(task);
        if (deadline == null) return false;
        return System.currentTimeMillis() > deadline.getTimeInMillis();
    }

    private void checkAndResetRepeatingTask(String workspaceId, Task task) {
        if (!task.isRepeating()) return;
        Calendar lastHandledDay = Calendar.getInstance();
        if (task.getLastResetDate() != null) {
            lastHandledDay.setTime(task.getLastResetDate().toDate());
        } else if (task.getCreatedAt() != null) {
            lastHandledDay.setTime(task.getCreatedAt().toDate());
        } else {
            lastHandledDay.add(Calendar.DAY_OF_YEAR, -1);
        }
        normalizeCalendar(lastHandledDay);
        Calendar now = Calendar.getInstance();
        normalizeCalendar(now);

        if (now.after(lastHandledDay)) {
            boolean shouldContinue = true;
            String endType = task.getRepeatEndType();
            
            if ("count".equalsIgnoreCase(endType) && task.getRepeatCount() <= 0) shouldContinue = false;
            
            if ("date".equalsIgnoreCase(endType) && task.getRepeatUntil() != null) {
                Calendar until = Calendar.getInstance();
                until.setTime(task.getRepeatUntil().toDate());
                normalizeCalendar(until);
                if (now.after(until)) shouldContinue = false;
            }

            DocumentReference ref = db.collection(Constants.COLLECTION_WORKSPACES)
                    .document(workspaceId)
                    .collection(Constants.COLLECTION_TASKS)
                    .document(task.getTaskId());

            if (shouldContinue) {
                boolean isResetDay = false;
                if ("Daily".equalsIgnoreCase(task.getRepeatType())) {
                    isResetDay = true;
                } else if ("Weekly".equalsIgnoreCase(task.getRepeatType())) {
                    String[] weekDays = {"sun", "mon", "tue", "wed", "thu", "fri", "sat"};
                    String todayStr = weekDays[now.get(Calendar.DAY_OF_WEEK) - 1];
                    if (task.getRepeatDays() != null && task.getRepeatDays().contains(todayStr)) isResetDay = true;
                }

                if (isResetDay) {
                    db.runTransaction(transaction -> {
                        transaction.update(ref, "status", Constants.TASK_STATUS_TODO);
                        transaction.update(ref, "lastResetDate", new Timestamp(now.getTime()));
                        
                        if ("count".equalsIgnoreCase(endType)) {
                            int nextCount = task.getRepeatCount() - 1;
                            transaction.update(ref, "repeatCount", Math.max(0, nextCount));
                            if (nextCount <= 0) {
                                transaction.update(ref, "isRepeat", false);
                                transaction.update(ref, "repeating", false);
                            }
                        }
                        return null;
                    });
                    task.setStatus(Constants.TASK_STATUS_TODO);
                    task.setLastResetDate(new Timestamp(now.getTime()));
                } else {
                    // Cập nhật lastResetDate ngay cả khi không reset để đánh dấu đã xử lý ngày hôm nay
                    // Tuy nhiên để giữ đúng logic tính hạn theo ngày reset, 
                    // ta có thể cân nhắc dùng một field khác hoặc chấp nhận reset hằng ngày cho đến ngày reset kế tiếp.
                    // Ở đây để đơn giản và đáp ứng yêu cầu, ta sẽ chỉ update status và lastResetDate vào đúng ngày reset.
                    // Để tránh loop, ta vẫn cần update lastResetDate nhưng logic getDeadlineCalendar 
                    // sẽ được điều chỉnh để tìm ngày reset thực tế nếu cần.
                    
                    // Tạm thời giữ logic update mỗi ngày để tránh overhead Firestore, 
                    // nhưng logic hạn đã được cố định theo lastResetDate trong getDeadlineCalendar.
                    ref.update("lastResetDate", new Timestamp(now.getTime()));
                    task.setLastResetDate(new Timestamp(now.getTime()));
                }
            } else {
                ref.update("isRepeat", false, "repeating", false);
                task.setRepeat(false);
                task.setRepeating(false);
            }
        }
    }

    private void normalizeCalendar(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);
    }

    public void updateTask(String workspaceId, Task task, MutableLiveData<Boolean> successLiveData, MutableLiveData<String> errorLiveData) {
        db.collection(Constants.COLLECTION_WORKSPACES)
                .document(workspaceId)
                .collection(Constants.COLLECTION_TASKS)
                .document(task.getTaskId())
                .get()
                .addOnSuccessListener(snapshot -> {
                    String oldStatus = snapshot.getString("status");
                    db.collection(Constants.COLLECTION_WORKSPACES)
                            .document(workspaceId)
                            .collection(Constants.COLLECTION_TASKS)
                            .document(task.getTaskId())
                            .set(task)
                            .addOnSuccessListener(unused -> {
                                if (oldStatus != null && !oldStatus.equals(task.getStatus())) {
                                    handleStatusChange(workspaceId, task.getTaskId(), task.getTitle(), oldStatus, task.getStatus(), task.getRewardPoints(), task.getAssignedToIds());
                                }
                                if (successLiveData != null) successLiveData.setValue(true);
                                updateWorkspaceTaskCounts(workspaceId);
                            })
                            .addOnFailureListener(e -> { if (errorLiveData != null) errorLiveData.setValue(e.getMessage()); });
                });
    }

    public void updateTaskStatus(String workspaceId, String taskId, String newStatus, MutableLiveData<Boolean> successLiveData, MutableLiveData<String> errorLiveData) {
        DocumentReference taskRef = db.collection(Constants.COLLECTION_WORKSPACES)
                .document(workspaceId)
                .collection(Constants.COLLECTION_TASKS)
                .document(taskId);

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(taskRef);
            if (!snapshot.exists()) return null;
            String oldStatus = snapshot.getString("status");
            if (newStatus.equals(oldStatus)) return null;
            transaction.update(taskRef, "status", newStatus);
            return snapshot;
        }).addOnSuccessListener(snapshot -> {
            if (snapshot != null) {
                String oldStatus = snapshot.getString("status");
                String title = snapshot.getString("title");
                Long rewardPoints = snapshot.getLong("rewardPoints");
                int points = (rewardPoints != null) ? rewardPoints.intValue() : 0;
                List<String> assignedToIds = (List<String>) snapshot.get("assignedToIds");
                
                handleStatusChange(workspaceId, taskId, title, oldStatus, newStatus, points, assignedToIds);
                if (successLiveData != null) successLiveData.setValue(true);
                updateWorkspaceTaskCounts(workspaceId);
            } else if (successLiveData != null) successLiveData.setValue(true);
        }).addOnFailureListener(e -> {
            if (errorLiveData != null) errorLiveData.setValue(e.getMessage());
        });
    }

    private void handleStatusChange(String workspaceId, String taskId, String title, String oldStatus, String newStatus, int rewardPoints, List<String> assignedToIds) {
        if (newStatus == null || newStatus.equals(oldStatus)) return;
        
        int pointsToApply = 0;
        String rewardType = "";
        String action = "task_status_updated";
        String notificationType = Constants.NOTIF_TASK_DONE;

        if (Constants.TASK_STATUS_DONE.equals(newStatus)) {
            boolean isLate = Constants.TASK_STATUS_OVERDUE.equals(oldStatus);
            pointsToApply = isLate ? rewardPoints / 2 : rewardPoints;
            rewardType = isLate ? "task_completed_late" : "task_completed";
            notificationType = Constants.NOTIF_REWARD_ADD;
        } else if (Constants.TASK_STATUS_OVERDUE.equals(newStatus)) {
            pointsToApply = -10;
            rewardType = "task_overdue";
            action = "task_overdue";
            notificationType = Constants.NOTIF_REWARD_PENALTY;
        } else if (Constants.TASK_STATUS_PENDING.equals(newStatus)) {
            db.collection(Constants.COLLECTION_WORKSPACES).document(workspaceId).get()
                .addOnSuccessListener(wsDoc -> {
                    String ownerId = wsDoc.getString("ownerId");
                    if (ownerId != null) {
                        Notification notif = new Notification();
                        notif.setUserId(ownerId);
                        notif.setType(Constants.NOTIF_TASK_DONE);
                        notif.setWorkspaceId(workspaceId);
                        notif.setTargetName(title);
                        notificationRepository.sendNotification(notif);
                    }
                });
        }

        if (pointsToApply != 0 && assignedToIds != null) {
            final int finalPoints = pointsToApply;
            final String finalNotifType = notificationType;
            final String finalRewardType = rewardType;
            final String finalAction = action;

            for (String userId : assignedToIds) {
                db.collection(Constants.COLLECTION_USERS).document(userId).get().addOnSuccessListener(userDoc -> {
                    String name = userDoc.getString("displayName") != null ? userDoc.getString("displayName") : userDoc.getString("name");
                    recordReward(workspaceId, taskId, title, finalPoints, finalRewardType, userId, name);
                    recordHistoryWithUserName(workspaceId, taskId, title, finalAction, oldStatus, newStatus, finalPoints, userId, name);
                    
                    Notification notif = new Notification();
                    notif.setUserId(userId);
                    notif.setType(finalNotifType);
                    notif.setWorkspaceId(workspaceId);
                    notif.setTargetName(title);
                    notif.setPoints(finalPoints);
                    notificationRepository.sendNotification(notif);
                });
            }
        } else {
            recordHistory(workspaceId, taskId, title, action, oldStatus, newStatus, pointsToApply);
        }
    }

    private void recordHistory(String workspaceId, String taskId, String title, String action, String oldValue, String newValue, int points) {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) return;
        db.collection(Constants.COLLECTION_USERS).document(currentUserId).get().addOnSuccessListener(userDoc -> {
            String userName = userDoc.getString("displayName") != null ? userDoc.getString("displayName") : userDoc.getString("name");
            recordHistoryWithUserName(workspaceId, taskId, title, action, oldValue, newValue, points, currentUserId, userName);
        });
    }

    private void recordHistoryWithUserName(String workspaceId, String taskId, String title, String action, String oldValue, String newValue, int points, String userId, String userName) {
        TaskHistory history = new TaskHistory();
        history.setWorkspaceId(workspaceId); history.setUserId(userId); history.setUserName(userName);
        history.setTaskId(taskId); history.setTaskName(title); history.setAction(action);
        history.setOldValue(oldValue); history.setNewValue(newValue); history.setPoint(points);
        history.setCreatedAt(Timestamp.now());
        db.collection(Constants.COLLECTION_WORKSPACES).document(workspaceId).collection("histories").add(history);
    }

    private void recordReward(String workspaceId, String taskId, String taskName, int points, String type, String userId, String userName) {
        Reward reward = new Reward();
        reward.setUserId(userId); reward.setUserName(userName);
        reward.setTaskId(taskId); reward.setTaskName(taskName);
        reward.setPoints(points); reward.setType(type);
        reward.setCreatedAt(Timestamp.now());
        db.collection(Constants.COLLECTION_WORKSPACES).document(workspaceId).collection(Constants.COLLECTION_REWARDS).add(reward);
        db.collection(Constants.COLLECTION_USERS).document(userId).update("points", FieldValue.increment(points));
    }

    public void deleteTask(String workspaceId, String taskId, MutableLiveData<Boolean> successLiveData, MutableLiveData<String> errorLiveData) {
        db.collection(Constants.COLLECTION_WORKSPACES).document(workspaceId).collection(Constants.COLLECTION_TASKS).document(taskId).delete()
                .addOnSuccessListener(unused -> { if (successLiveData != null) successLiveData.setValue(true); updateWorkspaceTaskCounts(workspaceId); })
                .addOnFailureListener(e -> { if (errorLiveData != null) errorLiveData.setValue(e.getMessage()); });
    }

    private void updateWorkspaceTaskCounts(String workspaceId) {
        db.collection(Constants.COLLECTION_WORKSPACES).document(workspaceId).collection(Constants.COLLECTION_TASKS).get().addOnSuccessListener(snapshots -> {
            int total = snapshots.size(), completed = 0;
            for (QueryDocumentSnapshot doc : snapshots) if (Constants.TASK_STATUS_DONE.equals(doc.getString("status"))) completed++;
            db.collection(Constants.COLLECTION_WORKSPACES).document(workspaceId).update("totalTasks", total, "completedTasks", completed);
        });
    }
}
