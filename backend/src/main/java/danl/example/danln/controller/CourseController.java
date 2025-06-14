package danl.example.danln.controller;

import danl.example.danln.dto.CourseDTO;
import danl.example.danln.dto.PageResult;
import danl.example.danln.dto.ServiceResult;
import danl.example.danln.entity.Course;
import danl.example.danln.service.CourseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping(value = "/api/courses")
@Slf4j
public class CourseController {
    private final CourseService courseService;

    @Autowired
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ResponseEntity<?> getAllCourses(
            @PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable) {
        try {
            Page<Course> courses = courseService.findAll(pageable);
            return ResponseEntity.ok(new PageResult(courses));
        } catch (Exception e) {
            log.error("Error retrieving courses", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ServiceResult(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Error retrieving courses", null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCourseById(@PathVariable Long id) {
        try {
            Optional<Course> course = courseService.findById(id);
            if (course.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ServiceResult(HttpStatus.NOT_FOUND.value(),
                        "Course not found", null));
            }
            return ResponseEntity.ok(new ServiceResult(HttpStatus.OK.value(),
                "Course retrieved successfully", course.get()));
        } catch (Exception e) {
            log.error("Error retrieving course", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ServiceResult(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Error retrieving course", null));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LECTURER')")
    public ResponseEntity<?> createCourse(@Valid @RequestBody CourseDTO courseDTO) {
        try {
            Course course = courseService.createCourse(courseDTO);
            return ResponseEntity.ok(new ServiceResult(HttpStatus.CREATED.value(),
                "Course created successfully", course));
        } catch (Exception e) {
            log.error("Error creating course", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ServiceResult(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Error creating course", null));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LECTURER')")
    public ResponseEntity<?> updateCourse(@PathVariable Long id, 
                                        @Valid @RequestBody CourseDTO courseDTO) {
        try {
            Optional<Course> existingCourse = courseService.findById(id);
            if (existingCourse.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ServiceResult(HttpStatus.NOT_FOUND.value(),
                        "Course not found", null));
            }

            Course updatedCourse = courseService.updateCourse(id, courseDTO);
            return ResponseEntity.ok(new ServiceResult(HttpStatus.OK.value(),
                "Course updated successfully", updatedCourse));
        } catch (Exception e) {
            log.error("Error updating course", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ServiceResult(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Error updating course", null));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LECTURER')")
    public ResponseEntity<?> deleteCourse(@PathVariable Long id) {
        try {
            Optional<Course> course = courseService.findById(id);
            if (course.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ServiceResult(HttpStatus.NOT_FOUND.value(),
                        "Course not found", null));
            }

            courseService.deleteById(id);
            return ResponseEntity.ok(new ServiceResult(HttpStatus.OK.value(),
                "Course deleted successfully", null));
        } catch (Exception e) {
            log.error("Error deleting course", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ServiceResult(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Error deleting course", null));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchCourses(
            @RequestParam(required = false) String keyword,
            @PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable) {
        try {
            Page<Course> courses = courseService.searchCourses(keyword, pageable);
            return ResponseEntity.ok(new PageResult(courses));
        } catch (Exception e) {
            log.error("Error searching courses", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ServiceResult(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Error searching courses", null));
        }
    }
}
