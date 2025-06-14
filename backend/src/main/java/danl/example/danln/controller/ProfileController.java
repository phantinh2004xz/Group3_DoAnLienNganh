package danl.example.danln.controller;

import danl.example.danln.dto.ServiceResult;
import danl.example.danln.entity.Profile;
import danl.example.danln.service.ProfileService;
import danl.example.danln.service.UserService;
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
@RequestMapping(value = "/api/profiles")
@Slf4j
public class ProfileController {
    private final ProfileService profileService;
    private final UserService userService;

    @Autowired
    public ProfileController(ProfileService profileService, UserService userService) {
        this.profileService = profileService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllProfiles(
            @PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable) {
        try {
            Page<Profile> profiles = profileService.getAllProfiles(pageable);
            return ResponseEntity.ok(new ServiceResult(HttpStatus.OK.value(), 
                "Profiles retrieved successfully", profiles));
        } catch (Exception e) {
            log.error("Error retrieving profiles", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ServiceResult(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    "Error retrieving profiles", null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProfileById(@PathVariable Long id) {
        try {
            Optional<Profile> profile = profileService.getProfileById(id);
            if (profile.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ServiceResult(HttpStatus.NOT_FOUND.value(), 
                        "Profile not found", null));
            }
            return ResponseEntity.ok(new ServiceResult(HttpStatus.OK.value(), 
                "Profile retrieved successfully", profile.get()));
        } catch (Exception e) {
            log.error("Error retrieving profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ServiceResult(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    "Error retrieving profile", null));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createProfile(@Valid @RequestBody Profile profile) {
        try {
            Profile createdProfile = profileService.createProfile(profile);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ServiceResult(HttpStatus.CREATED.value(), 
                    "Profile created successfully", createdProfile));
        } catch (Exception e) {
            log.error("Error creating profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ServiceResult(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    "Error creating profile", null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProfile(@PathVariable Long id, 
                                         @Valid @RequestBody Profile profile) {
        try {
            Optional<Profile> existingProfile = profileService.getProfileById(id);
            if (existingProfile.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ServiceResult(HttpStatus.NOT_FOUND.value(), 
                        "Profile not found", null));
            }

            profile.setId(id);
            Profile updatedProfile = profileService.updateProfile(profile);
            return ResponseEntity.ok(new ServiceResult(HttpStatus.OK.value(), 
                "Profile updated successfully", updatedProfile));
        } catch (Exception e) {
            log.error("Error updating profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ServiceResult(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    "Error updating profile", null));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProfile(@PathVariable Long id) {
        try {
            Optional<Profile> profile = profileService.getProfileById(id);
            if (profile.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ServiceResult(HttpStatus.NOT_FOUND.value(), 
                        "Profile not found", null));
            }

            profileService.deleteProfile(id);
            return ResponseEntity.ok(new ServiceResult(HttpStatus.OK.value(), 
                "Profile deleted successfully", null));
        } catch (Exception e) {
            log.error("Error deleting profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ServiceResult(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    "Error deleting profile", null));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchProfiles(
            @RequestParam(value = "keyword") String keyword,
            @PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable) {
        try {
            Page<Profile> profiles = profileService.searchProfiles(keyword, pageable);
            return ResponseEntity.ok(new ServiceResult(HttpStatus.OK.value(), 
                "Profiles search completed successfully", profiles));
        } catch (Exception e) {
            log.error("Error searching profiles", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ServiceResult(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    "Error searching profiles", null));
        }
    }
}
