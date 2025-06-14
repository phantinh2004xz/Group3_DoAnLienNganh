package danl.example.danln.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "course", uniqueConstraints = {
    @UniqueConstraint(columnNames = "course_code")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "Course code cannot be empty")
    @Size(min = 3, max = 20, message = "Course code must be between 3 and 20 characters")
    @Column(name = "course_code", nullable = false, unique = true)
    private String courseCode;

    @NotBlank(message = "Course name cannot be empty")
    @Size(min = 3, max = 100, message = "Course name must be between 3 and 100 characters")
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @Column(name = "img_url")
    private String imgUrl;

    @Column(name = "duration")
    private Integer duration; // Duration in hours

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinTable(
        name = "course_intake",
        joinColumns = {@JoinColumn(name = "course_id", referencedColumnName = "id")},
        inverseJoinColumns = {@JoinColumn(name = "intake_id", referencedColumnName = "id")}
    )
    private List<Intake> intakes = new ArrayList<>();
}
