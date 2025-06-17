package com.yourcompany.onlineexam.model;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subject {
    @DocumentId
    private String id;
    private String name;
    private String code; // Tương ứng với course_code
    private String imageUrl;
    private String description;
    private Date createdAt;
    private String createdBy; // UID của admin tạo
}