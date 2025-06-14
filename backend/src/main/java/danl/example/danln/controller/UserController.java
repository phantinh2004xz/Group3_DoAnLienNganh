package danl.example.danln.controller;

import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import danl.example.danln.dto.*;
import danl.example.danln.entity.Profile;
import danl.example.danln.entity.Role;
import danl.example.danln.entity.User;
import danl.example.danln.service.ExcelService;
import danl.example.danln.service.FilesStorageService;
import danl.example.danln.service.RoleService;
import danl.example.danln.service.UserService;
import danl.example.danln.ultilities.ERole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Optional;
import java.util.Set;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping(value = "/api/users")
@Slf4j
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final ExcelService excelService;
    private final FilesStorageService filesStorageService;

    @Autowired
    public UserController(UserService userService, RoleService roleService, 
                         PasswordEncoder passwordEncoder, ExcelService excelService, 
                         FilesStorageService filesStorageService) {
        this.userService = userService;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.excelService = excelService;
        this.filesStorageService = filesStorageService;
    }

    @GetMapping(value = "/profile")
    public ResponseEntity<?> getUser(@RequestParam(required = false) String username) {
        try {
            Optional<User> user = username == null || username.isEmpty() 
                ? userService.getUserByUsername(userService.getUserName())
                : userService.getUserByUsername(username);

            if (user.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ServiceResult(HttpStatus.NOT_FOUND.value(), 
                        "User not found: " + username, null));
            }

            return ResponseEntity.ok(new ServiceResult(HttpStatus.OK.value(), 
                "User profile retrieved successfully", user));
        } catch (Exception e) {
            log.error("Error retrieving user profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ServiceResult(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    "Error retrieving user profile", null));
        }
    }

    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUsername(@RequestParam("value") String value) {
        return ResponseEntity.ok(userService.existsByUsername(value));
    }

    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam("value") String value) {
        return ResponseEntity.ok(userService.existsByEmail(value));
    }

    @PatchMapping("/{id}/email/updating")
    public ResponseEntity<?> updateEmail(@Valid @RequestBody EmailUpdate data, @PathVariable Long id) {
        try {
            Optional<User> userOpt = userService.findUserById(id);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ServiceResult(HttpStatus.NOT_FOUND.value(), "User not found", null));
            }

            User user = userOpt.get();
            if (!passwordEncoder.matches(data.getPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ServiceResult(HttpStatus.UNAUTHORIZED.value(), "Invalid password", null));
            }

            user.setEmail(data.getEmail());
            userService.updateUser(user);
            return ResponseEntity.ok(new ServiceResult(HttpStatus.OK.value(), 
                "Email updated successfully", data.getEmail()));
        } catch (Exception e) {
            log.error("Error updating email", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ServiceResult(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    "Error updating email", null));
        }
    }

    @PatchMapping("/{id}/password/updating")
    public ResponseEntity<?> updatePassword(@Valid @RequestBody PasswordUpdate passwordUpdate, 
                                          @PathVariable Long id) {
        try {
            Optional<User> userOpt = userService.findUserById(id);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ServiceResult(HttpStatus.NOT_FOUND.value(), "User not found", null));
            }

            User user = userOpt.get();
            if (!passwordEncoder.matches(passwordUpdate.getCurrentPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ServiceResult(HttpStatus.UNAUTHORIZED.value(), 
                        "Current password is incorrect", null));
            }

            if (passwordUpdate.getCurrentPassword().equals(passwordUpdate.getNewPassword())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ServiceResult(HttpStatus.CONFLICT.value(), 
                        "New password must be different from current password", null));
            }

            user.setPassword(passwordEncoder.encode(passwordUpdate.getNewPassword()));
            userService.updateUser(user);
            return ResponseEntity.ok(new ServiceResult(HttpStatus.OK.value(), 
                "Password updated successfully", null));
        } catch (Exception e) {
            log.error("Error updating password", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ServiceResult(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    "Error updating password", null));
        }
    }

    @GetMapping("/{id}/check-email")
    public ResponseEntity<Boolean> checkExistsEmailUpdate(@RequestParam("value") String value, 
                                                        @PathVariable Long id) {
        Optional<User> userOpt = userService.findUserById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.ok(false);
        }
        
        User user = userOpt.get();
        if (user.getEmail().equals(value)) {
            return ResponseEntity.ok(false);
        }
        
        return ResponseEntity.ok(userService.existsByEmail(value));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/deleted/{deleted}")
    public ResponseEntity<?> updateUserDeletedStatus(@PathVariable Long id, 
                                                   @PathVariable boolean deleted) {
        try {
            Optional<User> userOpt = userService.findUserById(id);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ServiceResult(HttpStatus.NOT_FOUND.value(), "User not found", null));
            }

            User user = userOpt.get();
            user.setDeleted(deleted);
            userService.updateUser(user);
            return ResponseEntity.ok(new ServiceResult(HttpStatus.OK.value(), 
                "User status updated successfully", null));
        } catch (Exception e) {
            log.error("Error updating user status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ServiceResult(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    "Error updating user status", null));
        }
    }

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUsersByPage(@PageableDefault(page = 0, size = 10, sort = "id") 
                                          Pageable pageable) {
        try {
            Page<User> userPage = userService.findUsersByPage(pageable);
            return ResponseEntity.ok(new PageResult(userPage));
        } catch (Exception e) {
            log.error("Error retrieving users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ServiceResult(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    "Error retrieving users", null));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchUsersByUsernameOrEmail(
            @RequestParam(value = "search-keyword") String info,
            @PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable) {
        try {
            Page<User> userPage = userService.findAllByUsernameContainsOrEmailContains(info, info, pageable);
            return ResponseEntity.ok(new PageResult(userPage));
        } catch (Exception e) {
            log.error("Error searching users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ServiceResult(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    "Error searching users", null));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserUpdate userReq, 
                                      @PathVariable Long id) {
        try {
            Optional<User> userOpt = userService.findUserById(id);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ServiceResult(HttpStatus.NOT_FOUND.value(), "User not found", null));
            }

            User userUpdate = userOpt.get();
            if (userReq.getPassword() != null) {
                userUpdate.setPassword(passwordEncoder.encode(userReq.getPassword()));
            }
            
            userUpdate.setEmail(userReq.getEmail());
            Profile profile = userReq.getProfile();
            profile.setId(userUpdate.getProfile().getId());
            profile.setFirstName(userReq.getProfile().getFirstName());
            profile.setLastName(userReq.getProfile().getLastName());
            userUpdate.setProfile(profile);
            
            userService.updateUser(userUpdate);
            return ResponseEntity.ok(new ServiceResult(HttpStatus.OK.value(), 
                "User updated successfully", userUpdate));
        } catch (Exception e) {
            log.error("Error updating user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ServiceResult(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    "Error updating user", null));
        }
    }

    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@Valid @RequestBody User user) {
        try {
            if (userService.existsByUsername(user.getUsername())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ServiceResult(HttpStatus.CONFLICT.value(), 
                        "Username already exists", null));
            }

            if (userService.existsByEmail(user.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ServiceResult(HttpStatus.CONFLICT.value(), 
                        "Email already exists", null));
            }

            userService.createUser(user);
            return ResponseEntity.ok(new ServiceResult(HttpStatus.OK.value(), 
                "User created successfully", user));
        } catch (Exception e) {
            log.error("Error creating user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ServiceResult(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    "Error creating user", null));
        }
    }

    @GetMapping("deleted/{status}/export/users.csv")
    public void exportUsersToCSV(HttpServletResponse response) throws Exception {
        String fileName = "users.csv";
        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, 
            "attachment; filename=\"" + fileName + "\"");

        StatefulBeanToCsv<UserExport> writer = new StatefulBeanToCsvBuilder<UserExport>(response.getWriter())
                .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                .withOrderedResults(false)
                .build();

        writer.write(userService.findAllByDeletedToExport(false));
    }
}   
