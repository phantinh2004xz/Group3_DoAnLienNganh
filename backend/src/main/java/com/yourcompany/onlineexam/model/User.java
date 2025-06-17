package com.yourcompany.onlineexam.model;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data // Tự động tạo getters, setters, toString, equals, hashCode
@NoArgsConstructor // Tự động tạo constructor không đối số
@AllArgsConstructor // Tự động tạo constructor với tất cả các đối số
public class User {
    @DocumentId // Đánh dấu trường này là ID của document trong Firestore
    private String uid; // UID từ Firebase Authentication
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String imageUrl;
    private String role; // "user", "admin", "lecturer", "student"
    private String intakeId;
    private Date createdAt;
    private Date lastLoginAt;
    private Boolean isDeleted;
}