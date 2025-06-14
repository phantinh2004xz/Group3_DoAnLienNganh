package danl.example.danln.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import danl.example.danln.dto.*;
import danl.example.danln.entity.*;
import danl.example.danln.service.*;
import danl.example.danln.ultilities.ERole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping(value = "/api/exams")
@Slf4j
public class ExamController {
    private final ExamService examService;
    private final QuestionService questionService;
    private final UserService userService;
    private final IntakeService intakeService;
    private final PartService partService;
    private final ExamUserService examUserService;
    private final ObjectMapper mapper;

    @Autowired
    public ExamController(ExamService examService, QuestionService questionService,
                         UserService userService, IntakeService intakeService,
                         PartService partService, ExamUserService examUserService,
                         ObjectMapper mapper) {
        this.examService = examService;
        this.questionService = questionService;
        this.userService = userService;
        this.intakeService = intakeService;
        this.partService = partService;
        this.examUserService = examUserService;
        this.mapper = mapper;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LECTURER')")
    public ResponseEntity<?> getExamsByPage(
            @PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable) {
        try {
            String username = userService.getUserName();
            User user = userService.getUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            Page<Exam> examPage = user.getRoles().contains(ERole.ROLE_ADMIN)
                ? examService.findAll(pageable)
                : examService.findAllByCreatedBy_Username(pageable, username);
            
            return ResponseEntity.ok(new PageResult(examPage));
        } catch (Exception e) {
            log.error("Error retrieving exams", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ServiceResult(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Error retrieving exams", null));
        }
    }

    @GetMapping("/list-all-by-user")
    public ResponseEntity<?> getAllByUser() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            List<ExamUser> examUserList = examUserService.getExamListByUsername(username);
            
            Date currentDate = new Date();
            examUserList.forEach(examUser -> 
                examUser.getExam().setLocked(currentDate.compareTo(examUser.getExam().getBeginExam()) >= 0));
            
            return ResponseEntity.ok(new ServiceResult(HttpStatus.OK.value(),
                "Exams retrieved successfully", examUserList));
        } catch (Exception e) {
            log.error("Error retrieving user exams", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ServiceResult(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Error retrieving user exams", null));
        }
    }

    @GetMapping("/exam-user/{examId}")
    public ResponseEntity<?> getExamUserById(@PathVariable Long examId) {
        try {
            String username = userService.getUserName();
            ExamUser examUser = examUserService.findByExamAndUser(examId, username);
            
            if (examUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ServiceResult(HttpStatus.NOT_FOUND.value(),
                        "Exam user not found", null));
            }
            
            return ResponseEntity.ok(new ServiceResult(HttpStatus.OK.value(),
                "Exam user retrieved successfully", examUser));
        } catch (Exception e) {
            log.error("Error retrieving exam user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ServiceResult(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Error retrieving exam user", null));
        }
    }

    @GetMapping("/{examId}/questions")
    public ResponseEntity<?> getAllQuestions(@PathVariable Long examId) {
        try {
            String username = userService.getUserName();
            Optional<Exam> examOpt = examService.getExamById(examId);
            
            if (examOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ServiceResult(HttpStatus.NOT_FOUND.value(),
                        "Exam not found", null));
            }

            Exam exam = examOpt.get();
            Date currentTime = new Date();
            
            if (exam.isLocked() || exam.getBeginExam().compareTo(currentTime) > 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ServiceResult(HttpStatus.BAD_REQUEST.value(),
                        "Exam is locked or not started yet", null));
            }

            ExamUser examUser = examUserService.findByExamAndUser(examId, username);
            ExamQuestionList examQuestionList = new ExamQuestionList();
            examQuestionList.setRemainingTime(examUser.getRemainingTime());

            if (examUser.getIsStarted()) {
                return handleStartedExam(examUser, examQuestionList);
            } else if (exam.isShuffle()) {
                return handleShuffledExam(exam, examUser, examQuestionList);
            } else {
                return handleNormalExam(exam, examUser, examQuestionList);
            }
        } catch (Exception e) {
            log.error("Error retrieving exam questions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ServiceResult(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Error retrieving exam questions", null));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LECTURER')")
    public ResponseEntity<?> createExam(@Valid @RequestBody Exam exam,
                                      @RequestParam Long intakeId,
                                      @RequestParam Long partId,
                                      @RequestParam boolean isShuffle) {
        try {
            String username = userService.getUserName();
            User user = userService.getUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

            Optional<Intake> intake = intakeService.findById(intakeId);
            Optional<Part> part = partService.findPartById(partId);

            if (intake.isEmpty() || part.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ServiceResult(HttpStatus.BAD_REQUEST.value(),
                        "Invalid intake or part ID", null));
            }

            exam.setIntake(intake.get());
            exam.setPart(part.get());
            exam.setCreatedBy(user);
            exam.setShuffle(isShuffle);
            exam.setCanceled(false);

            Exam savedExam = examService.saveExam(exam);
            List<User> users = userService.findAllByIntakeId(intakeId);
            examUserService.create(savedExam, users);

            return ResponseEntity.ok(new ServiceResult(HttpStatus.CREATED.value(),
                "Exam created successfully", savedExam));
        } catch (Exception e) {
            log.error("Error creating exam", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ServiceResult(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Error creating exam", null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getExamById(@PathVariable Long id) {
        try {
            Optional<Exam> exam = examService.getExamById(id);
            if (exam.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ServiceResult(HttpStatus.NOT_FOUND.value(),
                        "Exam not found", null));
            }
            return ResponseEntity.ok(new ServiceResult(HttpStatus.OK.value(),
                "Exam retrieved successfully", exam.get()));
        } catch (Exception e) {
            log.error("Error retrieving exam", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ServiceResult(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Error retrieving exam", null));
        }
    }

    @PutMapping("/{examId}/questions-by-user")
    public ResponseEntity<?> saveUserExamAnswer(@RequestBody List<AnswerSheet> answerSheets,
                                              @PathVariable Long examId,
                                              @RequestParam boolean isFinish,
                                              @RequestParam int remainingTime) {
        try {
            String username = userService.getUserName();
            ExamUser examUser = examUserService.findByExamAndUser(examId, username);
            
            if (examUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ServiceResult(HttpStatus.NOT_FOUND.value(),
                        "Exam user not found", null));
            }

            String answerSheetJson = mapper.writeValueAsString(answerSheets);
            examUser.setAnswerSheet(answerSheetJson);
            examUser.setIsFinished(isFinish);
            examUser.setRemainingTime(remainingTime);
            
            if (isFinish) {
                examUser.setTimeFinish(new Date());
            }
            
            examUserService.update(examUser);
            return ResponseEntity.ok(new ServiceResult(HttpStatus.OK.value(),
                "Exam answers saved successfully", null));
        } catch (Exception e) {
            log.error("Error saving exam answers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ServiceResult(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Error saving exam answers", null));
        }
    }

    @GetMapping("/{examId}/result")
    public ResponseEntity<?> getResultExam(@PathVariable Long examId) {
        try {
            String username = userService.getUserName();
            ExamUser examUser = examUserService.findByExamAndUser(examId, username);
            
            if (examUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ServiceResult(HttpStatus.NOT_FOUND.value(),
                        "Exam user not found", null));
            }

            if (!examUser.getIsFinished()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ServiceResult(HttpStatus.BAD_REQUEST.value(),
                        "Exam is not finished yet", null));
            }

            List<AnswerSheet> answerSheets = convertAnswerJsonToObject(examUser);
            double totalPoint = calculateTotalPoint(answerSheets);

            return ResponseEntity.ok(new ServiceResult(HttpStatus.OK.value(),
                "Exam result retrieved successfully", new ExamResult(totalPoint, answerSheets)));
        } catch (Exception e) {
            log.error("Error retrieving exam result", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ServiceResult(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Error retrieving exam result", null));
        }
    }

    @GetMapping("/schedule")
    public ResponseEntity<?> getExamCalendar() {
        try {
            String username = userService.getUserName();
            List<ExamCalendar> examCalendar = examService.getExamCalendar(username);
            return ResponseEntity.ok(new ServiceResult(HttpStatus.OK.value(),
                "Exam calendar retrieved successfully", examCalendar));
        } catch (Exception e) {
            log.error("Error retrieving exam calendar", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ServiceResult(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Error retrieving exam calendar", null));
        }
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LECTURER')")
    public ResponseEntity<?> cancelExam(@PathVariable Long id) {
        try {
            Optional<Exam> examOpt = examService.getExamById(id);
            if (examOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ServiceResult(HttpStatus.NOT_FOUND.value(),
                        "Exam not found", null));
            }

            Exam exam = examOpt.get();
            exam.setCanceled(true);
            examService.saveExam(exam);

            return ResponseEntity.ok(new ServiceResult(HttpStatus.OK.value(),
                "Exam cancelled successfully", null));
        } catch (Exception e) {
            log.error("Error cancelling exam", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ServiceResult(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Error cancelling exam", null));
        }
    }

    private ResponseEntity<?> handleStartedExam(ExamUser examUser, ExamQuestionList examQuestionList) 
            throws JsonProcessingException {
        List<AnswerSheet> choiceUsers = convertAnswerJsonToObject(examUser);
        List<Question> questions = choiceUsers.stream()
            .map(answerSheet -> {
                Question question = questionService.getQuestionById(answerSheet.getQuestionId())
                    .orElseThrow(() -> new RuntimeException("Question not found"));
                question.setChoices(answerSheet.getChoices());
                question.setPoint(answerSheet.getPoint());
                return question;
            })
            .collect(Collectors.toList());

        examQuestionList.setQuestions(questions);
        examQuestionList.setExam(examUser.getExam());
        return ResponseEntity.ok(new ServiceResult(HttpStatus.OK.value(),
            "Started exam questions retrieved successfully", examQuestionList));
    }

    private ResponseEntity<?> handleShuffledExam(Exam exam, ExamUser examUser, 
            ExamQuestionList examQuestionList) throws JsonProcessingException {
        List<ExamQuestionPoint> examQuestionPoints = mapper.readValue(
            exam.getQuestionData(), 
            new TypeReference<List<ExamQuestionPoint>>() {}
        );
        Collections.shuffle(examQuestionPoints);

        List<Question> questions = questionService.getQuestionPointList(examQuestionPoints);
        List<AnswerSheet> answerSheets = questionService.convertFromQuestionList(questions);
        String answerSheetJson = mapper.writeValueAsString(answerSheets);

        examUser.setAnswerSheet(answerSheetJson);
        examUser.setIsStarted(true);
        examUser.setTimeStart(new Date());
        examUserService.update(examUser);

        List<Question> questionsWithChoices = answerSheets.stream()
            .map(answerSheet -> {
                Question question = questionService.getQuestionById(answerSheet.getQuestionId())
                    .orElseThrow(() -> new RuntimeException("Question not found"));
                question.setChoices(answerSheet.getChoices());
                question.setPoint(answerSheet.getPoint());
                return question;
            })
            .collect(Collectors.toList());

        examQuestionList.setQuestions(questionsWithChoices);
        examQuestionList.setExam(exam);
        return ResponseEntity.ok(new ServiceResult(HttpStatus.OK.value(),
            "Shuffled exam questions retrieved successfully", examQuestionList));
    }

    private ResponseEntity<?> handleNormalExam(Exam exam, ExamUser examUser, 
            ExamQuestionList examQuestionList) throws JsonProcessingException {
        List<ExamQuestionPoint> examQuestionPoints = mapper.readValue(
            exam.getQuestionData(), 
            new TypeReference<List<ExamQuestionPoint>>() {}
        );

        List<Question> questions = questionService.getQuestionPointList(examQuestionPoints);
        List<AnswerSheet> answerSheets = questionService.convertFromQuestionList(questions);
        String answerSheetJson = mapper.writeValueAsString(answerSheets);

        examUser.setAnswerSheet(answerSheetJson);
        examUser.setIsStarted(true);
        examUser.setTimeStart(new Date());
        examUserService.update(examUser);

        List<Question> questionsWithChoices = answerSheets.stream()
            .map(answerSheet -> {
                Question question = questionService.getQuestionById(answerSheet.getQuestionId())
                    .orElseThrow(() -> new RuntimeException("Question not found"));
                question.setChoices(answerSheet.getChoices());
                question.setPoint(answerSheet.getPoint());
                return question;
            })
            .collect(Collectors.toList());

        examQuestionList.setQuestions(questionsWithChoices);
        examQuestionList.setExam(exam);
        return ResponseEntity.ok(new ServiceResult(HttpStatus.OK.value(),
            "Normal exam questions retrieved successfully", examQuestionList));
    }

    private List<AnswerSheet> convertAnswerJsonToObject(ExamUser examUser) throws JsonProcessingException {
        return mapper.readValue(examUser.getAnswerSheet(), 
            new TypeReference<List<AnswerSheet>>() {});
    }

    private double calculateTotalPoint(List<AnswerSheet> answerSheets) {
        return answerSheets.stream()
            .mapToDouble(AnswerSheet::getPoint)
            .sum();
    }
}


