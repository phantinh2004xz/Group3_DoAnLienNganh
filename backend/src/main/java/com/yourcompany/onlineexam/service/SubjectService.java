package com.yourcompany.onlineexam.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.yourcompany.onlineexam.model.Subject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class SubjectService {

    private final Firestore firestore;
    private final CollectionReference subjectsCollection;

    public SubjectService(Firestore firestore) {
        this.firestore = firestore;
        this.subjectsCollection = firestore.collection("subjects");
    }

    // Lấy tất cả môn học
    public List<Subject> getAllSubjects() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = subjectsCollection.get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<Subject> subjects = new ArrayList<>();
        for (QueryDocumentSnapshot document : documents) {
            subjects.add(document.toObject(Subject.class));
        }
        return subjects;
    }

    // Lấy môn học theo ID
    public Subject getSubjectById(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = subjectsCollection.document(id);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            return document.toObject(Subject.class);
        } else {
            return null;
        }
    }

    // Tạo môn học mới (ID sẽ được tự động tạo bởi Firestore nếu id trong Subject là null)
    public Subject createSubject(Subject subject) throws ExecutionException, InterruptedException {
        DocumentReference docRef = subjectsCollection.document(); // Firestore tự động tạo ID
        subject.setId(docRef.getId()); // Gán lại ID được tạo vào object
        ApiFuture<com.google.cloud.firestore.WriteResult> future = docRef.set(subject);
        future.get();
        return subject;
    }

    // Cập nhật môn học
    public Subject updateSubject(String id, Subject subject) throws ExecutionException, InterruptedException {
        DocumentReference docRef = subjectsCollection.document(id);
        subject.setId(id); // Đảm bảo ID trong object khớp với ID document
        ApiFuture<com.google.cloud.firestore.WriteResult> future = docRef.set(subject);
        future.get();
        return subject;
    }

    // Xóa môn học
    public void deleteSubject(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = subjectsCollection.document(id);
        ApiFuture<com.google.cloud.firestore.WriteResult> future = docRef.delete();
        future.get();
    }
}