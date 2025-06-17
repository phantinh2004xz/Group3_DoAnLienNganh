package com.yourcompany.onlineexam.model;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Part {
    @DocumentId
    private String id;
    private String name;
    private String subjectId; // course_id trong MySQL
}