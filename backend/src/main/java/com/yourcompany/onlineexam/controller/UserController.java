package com.yourcompany.onlineexam.controller;

import com.yourcompany.onlineexam.model.User;
import com.yourcompany.onlineexam.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:4200") // Cho phép frontend (Angular) gọi API
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() throws ExecutionException, InterruptedException {
        List<User> users = userService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/{uid}")
    public ResponseEntity<User> getUserById(@PathVariable String uid) throws ExecutionException, InterruptedException {
        User user = userService.getUserById(uid);
        if (user != null) {
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping // Dùng cho tạo user mới (có thể từ admin dashboard)
    public ResponseEntity<User> createUser(@RequestBody User user) throws ExecutionException, InterruptedException {
        // Trong trường hợp này, UID sẽ được cung cấp từ request body hoặc tự tạo nếu cần
        // Nếu user đến từ Firebase Auth, UID sẽ là user.getUid()
        User createdUser = userService.createUser(user);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PutMapping("/{uid}")
    public ResponseEntity<User> updateUser(@PathVariable String uid, @RequestBody User user) throws ExecutionException, InterruptedException {
        User updatedUser = userService.updateUser(uid, user);
        if (updatedUser != null) {
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/{uid}/role") // Endpoint cụ thể để cập nhật vai trò
    public ResponseEntity<User> updateUserRole(@PathVariable String uid, @RequestParam String newRole) throws ExecutionException, InterruptedException {
        User updatedUser = userService.updateUserRole(uid, newRole);
        if (updatedUser != null) {
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{uid}")
    public ResponseEntity<Void> deleteUser(@PathVariable String uid) throws ExecutionException, InterruptedException {
        userService.deleteUser(uid);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}