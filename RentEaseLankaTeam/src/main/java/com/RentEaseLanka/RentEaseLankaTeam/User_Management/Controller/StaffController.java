package com.RentEaseLanka.RentEaseLankaTeam.User_Management.Controller;

import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Model.User;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    private final UserService userService;

    // ── NIC directory resolved at server root ──────────────────────────────
    private Path nicStorageLocation =
            Paths.get("uploads/nic").toAbsolutePath().normalize();

    @Autowired
    public StaffController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/pending-customers")
    public ResponseEntity<List<User>> getPendingCustomers() {
        return ResponseEntity.ok(userService.getPendingCustomers());
    }

    @PostMapping("/verify-customer/{id}")
    public ResponseEntity<User> verifyCustomer(@PathVariable Long id) {
        return ResponseEntity.ok(userService.verifyCustomer(id));
    }

    @PostMapping("/reject-customer/{id}")
    public ResponseEntity<User> rejectCustomer(@PathVariable Long id) {
        return ResponseEntity.ok(userService.rejectCustomer(id));
    }

    /**
     * Securely streams a NIC file to authenticated STAFF / MANAGER / ADMIN users.
     *
     * Security controls:
     *  - Path traversal prevention: rejects any fileName containing ".."
     *  - The resolved path is validated to stay inside nicStorageLocation
     *  - Spring Security ensures only authenticated users with the correct role reach this endpoint
     *
     * @param fileName the stored file name (UUID-prefixed, e.g. "abc123_nic.jpg")
     * @return the raw image binary with the correct Content-Type header
     */
    @GetMapping("/file/{fileName:.+}")
    public ResponseEntity<Resource> serveNicFile(@PathVariable String fileName) {
        // 1. Reject path traversal attempts immediately
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            return ResponseEntity.badRequest().build();
        }

        try {
            // 2. Resolve path and ensure it stays within the NIC directory
            Path filePath = nicStorageLocation.resolve(fileName).normalize();
            if (!filePath.startsWith(nicStorageLocation)) {
                return ResponseEntity.badRequest().build();
            }

            // 3. Load as a Spring Resource
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // 4. Detect content type (image/jpeg, image/png, etc.)
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (MalformedURLException ex) {
            return ResponseEntity.badRequest().build();
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
