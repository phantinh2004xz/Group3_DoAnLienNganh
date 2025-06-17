package com.yourcompany.onlineexam.controller;

import com.yourcompany.onlineexam.model.Question; // Import model Question
import com.yourcompany.onlineexam.service.QuestionService; // Import service QuestionService
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/questions") // Base path for all endpoints in this controller
@CrossOrigin(origins = "http://localhost:4200") // Allow requests from your Angular frontend
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    /**
     * GET /api/questions
     * Retrieves all questions.
     * Lấy tất cả câu hỏi.
     * @return A list of Question objects.
     */
    @GetMapping
    public ResponseEntity<List<Question>> getAllQuestions() throws ExecutionException, InterruptedException {
        List<Question> questions = questionService.getAllQuestions();
        return new ResponseEntity<>(questions, HttpStatus.OK);
    }

    /**
     * GET /api/questions/{id}
     * Retrieves a question by its ID.
     * Lấy một câu hỏi theo ID.
     * @param id The ID of the question.
     * @return The Question object if found, otherwise HTTP 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Question> getQuestionById(@PathVariable String id) throws ExecutionException, InterruptedException {
        Question question = questionService.getQuestionById(id);
        if (question != null) {
            return new ResponseEntity<>(question, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * POST /api/questions
     * Creates a new question.
     * Tạo một câu hỏi mới.
     * @param question The Question object to create (from request body).
     * @return The created Question object with HTTP 201 Created.
     */
    @PostMapping
    public ResponseEntity<Question> createQuestion(@RequestBody Question question) throws ExecutionException, InterruptedException {
        Question createdQuestion = questionService.createQuestion(question);
        return new ResponseEntity<>(createdQuestion, HttpStatus.CREATED);
    }

    /**
     * PUT /api/questions/{id}
     * Updates an existing question.
     * Cập nhật một câu hỏi hiện có.
     * @param id The ID of the question to update.
     * @param question The updated Question object (from request body).
     * @return The updated Question object, or HTTP 404 Not Found if original not found.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Question> updateQuestion(@PathVariable String id, @RequestBody Question question) throws ExecutionException, InterruptedException {
        Question updatedQuestion = questionService.updateQuestion(id, question);
        if (updatedQuestion != null) {
            return new ResponseEntity<>(updatedQuestion, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Should ideally check if it exists before trying to update
        }
    }

    /**
     * DELETE /api/questions/{id}
     * Deletes a question by its ID.
     * Xóa một câu hỏi theo ID.
     * @param id The ID of the question to delete.
     * @return HTTP 204 No Content upon successful deletion.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable String id) throws ExecutionException, InterruptedException {
        questionService.deleteQuestion(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}