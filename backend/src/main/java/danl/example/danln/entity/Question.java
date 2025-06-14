package danl.example.danln.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import danl.example.danln.ultilities.DifficultyLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "question")
public class Question implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "Question text cannot be empty")
    @Column(name = "question_text", columnDefinition = "text", nullable = false)
    private String questionText;

    @NotNull(message = "Difficulty level cannot be null")
    @Column(name = "difficulty_level", nullable = false)
    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    @Min(value = 0, message = "Point cannot be negative")
    @Column(name = "point", nullable = false)
    private int point;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @NotNull(message = "Question type cannot be null")
    @OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "question_type_id", nullable = false)
    private QuestionType questionType;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "question_id")
    private List<Choice> choices = new ArrayList<>();

    @NotNull(message = "Part cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id", nullable = false)
    private Part part;
}
