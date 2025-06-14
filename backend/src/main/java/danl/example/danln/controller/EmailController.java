package danl.example.danln.controller;

import danl.example.danln.dto.EmailRequest;
import danl.example.danln.dto.ServiceResult;
import danl.example.danln.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/email")
@Slf4j
public class EmailController {
    private final EmailService emailService;

    @Autowired
    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendEmail(@Valid @RequestBody EmailRequest emailRequest) {
        try {
            emailService.sendEmail(emailRequest.getTo(), emailRequest.getSubject(), emailRequest.getBody());
            return ResponseEntity.ok(new ServiceResult(HttpStatus.OK.value(),
                "Email sent successfully", null));
        } catch (Exception e) {
            log.error("Error sending email", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ServiceResult(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Error sending email: " + e.getMessage(), null));
        }
    }

    @PostMapping("/send-bulk")
    public ResponseEntity<?> sendBulkEmails(@Valid @RequestBody EmailRequest emailRequest) {
        try {
            emailService.sendBulkEmails(emailRequest.getTo(), emailRequest.getSubject(), emailRequest.getBody());
            return ResponseEntity.ok(new ServiceResult(HttpStatus.OK.value(),
                "Bulk emails sent successfully", null));
        } catch (Exception e) {
            log.error("Error sending bulk emails", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ServiceResult(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Error sending bulk emails: " + e.getMessage(), null));
        }
    }

    @PostMapping("/send-with-attachment")
    public ResponseEntity<?> sendEmailWithAttachment(@Valid @RequestBody EmailRequest emailRequest) {
        try {
            emailService.sendEmailWithAttachment(emailRequest.getTo(), emailRequest.getSubject(), 
                emailRequest.getBody(), emailRequest.getAttachmentPath());
            return ResponseEntity.ok(new ServiceResult(HttpStatus.OK.value(),
                "Email with attachment sent successfully", null));
        } catch (Exception e) {
            log.error("Error sending email with attachment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ServiceResult(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Error sending email with attachment: " + e.getMessage(), null));
        }
    }
}
