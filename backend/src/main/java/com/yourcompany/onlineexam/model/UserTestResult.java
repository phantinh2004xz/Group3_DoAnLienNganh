package com.yourcompany.onlineexam.model;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.util.List;
import java.util.Map; // Hoặc một POJO riêng cho answerSheet

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTestResult {
    @DocumentId
    private String id;
    private String userId;
    private String testId;
    private Date startTime;
    private Date finishTime;
    private Double totalScore;
    private Boolean isFinished;
    private Boolean isStarted;
    private Integer remainingTime;
    // answerSheet có thể là List<Map<String, Object>> hoặc một POJO riêng
    private List<Map<String, Object>> answerSheet; // ví dụ: [{"questionId": "id1", "selectedOptionIndex": 0}]
    private Date createdAt;
}