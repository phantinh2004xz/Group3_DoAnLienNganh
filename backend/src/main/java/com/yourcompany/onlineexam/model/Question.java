package com.yourcompany.onlineexam.model;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    @DocumentId
    private String id;
    private String text; // question_text
    private List<String> options; // Chứa các text của lựa chọn
    private Integer correctAnswerIndex; // Chỉ số của đáp án đúng trong list options
    private String subjectId; // Liên kết với môn học
    private String level; // "easy", "medium", "hard" (từ difficulty_level)
    private Integer point;
    private Date createdAt;
    private String createdBy;
    private String partId; // Tùy chọn, nếu bạn muốn liên kết với Part
}