package com.RentEaseLanka.RentEaseLankaTeam.User_Management.Service;

import com.RentEaseLanka.RentEaseLankaTeam.User_Management.DTOs.CreateUserRequest;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Model.Role;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Model.User;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Model.VerificationStatus;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    public User createManager(CreateUserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        User manager = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.MANAGER)
                .verificationStatus(VerificationStatus.VERIFIED)
                .build();
        return userRepository.save(manager);
    }

    public User createStaff(CreateUserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        User staff = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.STAFF)
                .verificationStatus(VerificationStatus.VERIFIED)
                .build();
        return userRepository.save(staff);
    }

    public User uploadNic(String email, MultipartFile file) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Guard: do not allow re-upload if already verified
        if (user.getVerificationStatus() == VerificationStatus.VERIFIED) {
            throw new RuntimeException("NIC already verified. Re-upload is not permitted.");
        }

        String fileName = fileStorageService.storeFile(file);
        user.setNicImageUrl(fileName);
        user.setVerificationStatus(VerificationStatus.PENDING);

        return userRepository.save(user);
    }

    public List<User> getPendingCustomers() {
        return userRepository.findByVerificationStatus(VerificationStatus.PENDING);
    }

    public User verifyCustomer(Long customerId) {
        User user = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setVerificationStatus(VerificationStatus.VERIFIED);
        return userRepository.save(user);
    }

    public User rejectCustomer(Long customerId) {
        User user = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setVerificationStatus(VerificationStatus.REJECTED);
        return userRepository.save(user);
    }

    public String getFileUrl(String fileName) {
        return fileStorageService.getFileUrl(fileName);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
