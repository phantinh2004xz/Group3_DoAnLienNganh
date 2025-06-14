package danl.example.danln.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.Type;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "exam")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Exam implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "canceled", nullable = false)
    private boolean canceled = false;

    @NotNull(message = "Intake cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intake_id", nullable = false)
    private Intake intake;

    @NotNull(message = "Part cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id", nullable = false)
    private Part part;

    @NotBlank(message = "Title cannot be empty")
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "shuffle", columnDefinition = "TINYINT", nullable = false)
    @Type(type = "org.hibernate.type.NumericBooleanType")
    private boolean isShuffle = false;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Column(name = "duration_exam", nullable = false)
    private int durationExam;

    @NotNull(message = "Begin exam time cannot be null")
    @Column(name = "begin_exam", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private Date beginExam;

    @NotNull(message = "Finish exam time cannot be null")
    @Column(name = "finish_exam", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private Date finishExam;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ExamStatus status = ExamStatus.DRAFT;

    @Column(name = "passing_score", nullable = false)
    @Min(value = 0, message = "Passing score cannot be negative")
    private int passingScore;

    @Column(name = "max_attempts", nullable = false)
    @Min(value = 1, message = "Max attempts must be at least 1")
    private int maxAttempts = 1;

    @Column(name = "question_data", columnDefinition = "TEXT")
    private String questionData;

    @Column(name = "instructions", columnDefinition = "TEXT")
    private String instructions;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    public enum ExamStatus {
        DRAFT, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
    }
}
