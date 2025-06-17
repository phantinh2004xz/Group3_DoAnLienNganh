package com.yourcompany.onlineexam.controller;

import com.yourcompany.onlineexam.model.Subject;
import com.yourcompany.onlineexam.service.SubjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/subjects")
@CrossOrigin(origins = "http://localhost:4200") // Cho phép frontend (Angular) gọi API
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping
    public ResponseEntity<List<Subject>> getAllSubjects() throws ExecutionException, InterruptedException {
        List<Subject> subjects = subjectService.getAllSubjects();
        return new ResponseEntity<>(subjects, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Subject> getSubjectById(@PathVariable String id) throws ExecutionException, InterruptedException {
        Subject subject = subjectService.getSubjectById(id);
        if (subject != null) {
            return new ResponseEntity<>(subject, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<Subject> createSubject(@RequestBody Subject subject) throws ExecutionException, InterruptedException {
        Subject createdSubject = subjectService.createSubject(subject);
        return new ResponseEntity<>(createdSubject, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Subject> updateSubject(@PathVariable String id, @RequestBody Subject subject) throws ExecutionException, InterruptedException {
        Subject updatedSubject = subjectService.updateSubject(id, subject);
        if (updatedSubject != null) {
            return new ResponseEntity<>(updatedSubject, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubject(@PathVariable String id) throws ExecutionException, InterruptedException {
        subjectService.deleteSubject(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}