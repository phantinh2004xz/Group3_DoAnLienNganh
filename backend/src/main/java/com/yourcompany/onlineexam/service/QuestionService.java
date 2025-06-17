package com.yourcompany.onlineexam.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.yourcompany.onlineexam.model.Question; // Import model Question
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class QuestionService {

    private final Firestore firestore;
    private final CollectionReference questionsCollection;

    public QuestionService(Firestore firestore) {
        this.firestore = firestore;
        this.questionsCollection = firestore.collection("questions");
    }

    /**
     * Retrieves all questions from the Firestore 'questions' collection.
     * Lấy tất cả câu hỏi từ collection 'questions' trong Firestore.
     * @return A list of Question objects.
     * @throws ExecutionException If a problem occurs during asynchronous operation.
     * @throws InterruptedException If the current thread is interrupted while waiting.
     */
    public List<Question> getAllQuestions() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = questionsCollection.get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<Question> questions = new ArrayList<>();
        for (QueryDocumentSnapshot document : documents) {
            questions.add(document.toObject(Question.class));
        }
        return questions;
    }

    /**
     * Retrieves a single question by its ID from the Firestore 'questions' collection.
     * Lấy một câu hỏi duy nhất theo ID của nó từ collection 'questions'.
     * @param id The ID of the question to retrieve.
     * @return The Question object if found, otherwise null.
     * @throws ExecutionException If a problem occurs during asynchronous operation.
     * @throws InterruptedException If the current thread is interrupted while waiting.
     */
    public Question getQuestionById(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = questionsCollection.document(id);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            return document.toObject(Question.class);
        } else {
            return null;
        }
    }

    /**
     * Creates a new question in the Firestore 'questions' collection.
     * If the question object has an ID, it will be used as the document ID.
     * Otherwise, Firestore will generate a new ID.
     * Tạo một câu hỏi mới trong collection 'questions'.
     * @param question The Question object to create.
     * @return The created Question object with its assigned ID.
     * @throws ExecutionException If a problem occurs during asynchronous operation.
     * @throws InterruptedException If the current thread is interrupted while waiting.
     */
    public Question createQuestion(Question question) throws ExecutionException, InterruptedException {
        DocumentReference docRef;
        if (question.getId() != null && !question.getId().isEmpty()) {
            docRef = questionsCollection.document(question.getId()); // Use provided ID
        } else {
            docRef = questionsCollection.document(); // Firestore generates new ID
            question.setId(docRef.getId()); // Assign generated ID back to object
        }
        ApiFuture<com.google.cloud.firestore.WriteResult> future = docRef.set(question);
        future.get(); // Wait for the operation to complete
        return question;
    }

    /**
     * Updates an existing question in the Firestore 'questions' collection.
     * If the question does not exist, it will be created.
     * Cập nhật một câu hỏi hiện có. Nếu câu hỏi không tồn tại, nó sẽ được tạo mới.
     * @param id The ID of the question to update.
     * @param question The updated Question object.
     * @return The updated Question object.
     * @throws ExecutionException If a problem occurs during asynchronous operation.
     * @throws InterruptedException If the current thread is interrupted while waiting.
     */
    public Question updateQuestion(String id, Question question) throws ExecutionException, InterruptedException {
        DocumentReference docRef = questionsCollection.document(id);
        question.setId(id); // Ensure the ID in the object matches the document ID
        ApiFuture<com.google.cloud.firestore.WriteResult> future = docRef.set(question); // set() will overwrite the entire document
        future.get();
        return question;
    }

    /**
     * Deletes a question from the Firestore 'questions' collection by its ID.
     * Xóa một câu hỏi khỏi collection 'questions' theo ID của nó.
     * @param id The ID of the question to delete.
     * @throws ExecutionException If a problem occurs during asynchronous operation.
     * @throws InterruptedException If the current thread is interrupted while waiting.
     */
    public void deleteQuestion(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = questionsCollection.document(id);
        ApiFuture<com.google.cloud.firestore.WriteResult> future = docRef.delete();
        future.get();
    }
}