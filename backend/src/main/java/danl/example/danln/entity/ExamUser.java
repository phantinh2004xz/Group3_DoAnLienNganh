package danl.example.danln.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "exam_user", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"exam_id", "user_id"})
})
public class ExamUser implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Exam cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @JsonIgnore
    @NotNull(message = "User cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "is_started", columnDefinition = "TINYINT", nullable = false)
    @Type(type = "org.hibernate.type.NumericBooleanType")
    private Boolean isStarted = false;

    @Column(name = "time_start")
    @Temporal(TemporalType.TIMESTAMP)
    private Date timeStart;

    @Column(name = "time_finish")
    @Temporal(TemporalType.TIMESTAMP)
    private Date timeFinish;

    @JsonIgnore
    @Column(name = "answer_sheet", columnDefinition = "TEXT")
    private String answerSheet;

    @Column(name = "is_finished", columnDefinition = "TINYINT", nullable = false)
    @Type(type = "org.hibernate.type.NumericBooleanType")
    private Boolean isFinished = false;

    @Min(value = 0, message = "Remaining time cannot be negative")
    @Column(name = "remaining_time", nullable = false)
    private int remainingTime;

    @Min(value = 0, message = "Total point cannot be negative")
    @Column(name = "total_point")
    private Double totalPoint;

    @Column(name = "attempt_number", nullable = false)
    @Min(value = 1, message = "Attempt number must be at least 1")
    private int attemptNumber = 1;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ExamUserStatus status = ExamUserStatus.NOT_STARTED;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date", updatable = false, nullable = false)
    private Date createdDate;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    public enum ExamUserStatus {
        NOT_STARTED, IN_PROGRESS, SUBMITTED, GRADED, CANCELLED
    }
}

