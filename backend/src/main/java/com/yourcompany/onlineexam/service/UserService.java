package com.yourcompany.onlineexam.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.yourcompany.onlineexam.model.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {

    private final Firestore firestore;
    private final CollectionReference usersCollection;

    public UserService(Firestore firestore) {
        this.firestore = firestore;
        this.usersCollection = firestore.collection("users");
    }

    // Lấy tất cả người dùng
    public List<User> getAllUsers() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = usersCollection.get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<User> users = new ArrayList<>();
        for (QueryDocumentSnapshot document : documents) {
            users.add(document.toObject(User.class));
        }
        return users;
    }

    // Lấy người dùng theo UID
    public User getUserById(String uid) throws ExecutionException, InterruptedException {
        DocumentReference docRef = usersCollection.document(uid);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            return document.toObject(User.class);
        } else {
            return null; // Hoặc ném ngoại lệ
        }
    }

    // Tạo người dùng mới (chú ý: uid thường đến từ Firebase Auth)
    public User createUser(User user) throws ExecutionException, InterruptedException {
        DocumentReference docRef = usersCollection.document(user.getUid()); // Sử dụng UID làm ID document
        ApiFuture<com.google.cloud.firestore.WriteResult> future = docRef.set(user);
        future.get(); // Chờ thao tác hoàn tất
        return user;
    }

    // Cập nhật người dùng
    public User updateUser(String uid, User user) throws ExecutionException, InterruptedException {
        DocumentReference docRef = usersCollection.document(uid);
        ApiFuture<com.google.cloud.firestore.WriteResult> future = docRef.set(user); // set() sẽ ghi đè toàn bộ document
        // Để cập nhật từng phần, dùng update(Map<String, Object> updates)
        future.get();
        return user;
    }

    // Cập nhật vai trò người dùng (chỉ trường 'role')
    public User updateUserRole(String uid, String newRole) throws ExecutionException, InterruptedException {
        DocumentReference docRef = usersCollection.document(uid);
        ApiFuture<com.google.cloud.firestore.WriteResult> future = docRef.update("role", newRole);
        future.get();
        // Sau khi cập nhật, bạn có thể lấy lại user để trả về object hoàn chỉnh
        return getUserById(uid);
    }

    // Xóa người dùng
    public void deleteUser(String uid) throws ExecutionException, InterruptedException {
        DocumentReference docRef = usersCollection.document(uid);
        ApiFuture<com.google.cloud.firestore.WriteResult> future = docRef.delete();
        future.get();
    }
}