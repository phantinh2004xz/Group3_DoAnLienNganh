package com.yourcompany.onlineexam.model;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Test {
    @DocumentId
    private String id;
    private String title;
    private String description;
    private String subjectId;
    private List<String> questionIds; // Mảng các ID câu hỏi
    private Integer durationMinutes;
    private Integer passScore;
    private Date createdAt;
    private String createdBy;
    private String intakeId;
    private String partId;
    private Boolean isCanceled;
    private Date startTime; // Thời gian bắt đầu bài thi
    private Date endTime;   // Thời gian kết thúc bài thi
}