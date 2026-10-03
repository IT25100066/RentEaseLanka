package com.RentEaseLanka.RentEaseLankaTeam.Customer_Review.Service;

import com.RentEaseLanka.RentEaseLankaTeam.Customer_Review.DTOs.ReviewRequestDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Customer_Review.DTOs.ReviewResponseDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Customer_Review.Model.CustomerReview;
import com.RentEaseLanka.RentEaseLankaTeam.Customer_Review.Repository.CustomerReviewRepository;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Model.User;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerReviewService {

    private final CustomerReviewRepository reviewRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReviewResponseDTO createReview(String email, ReviewRequestDTO requestDTO) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        CustomerReview review = CustomerReview.builder()
                .user(user)
                .rating(requestDTO.getRating())
                .comment(requestDTO.getComment())
                .location(requestDTO.getLocation() != null && !requestDTO.getLocation().isBlank() 
                        ? requestDTO.getLocation().trim() 
                        : "Sri Lanka")
                .isApproved(true)
                .build();

        CustomerReview saved = reviewRepository.save(review);
        return mapToDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getPublicReviews() {
        return reviewRepository.findByIsApprovedTrueOrderByCreatedAtDesc().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getMyReviews(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        return reviewRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteReview(Long reviewId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        CustomerReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));

        // Allow deletion if the review belongs to the user or user is staff/manager/admin
        boolean isOwner = review.getUser().getId().equals(user.getId());
        boolean isStaffOrAdmin = user.getRole() != null && 
                (user.getRole().name().equals("ADMIN") || 
                 user.getRole().name().equals("MANAGER") || 
                 user.getRole().name().equals("STAFF"));

        if (!isOwner && !isStaffOrAdmin) {
            throw new RuntimeException("You are not authorized to delete this review");
        }

        reviewRepository.delete(review);
    }

    private ReviewResponseDTO mapToDTO(CustomerReview review) {
        User u = review.getUser();
        String firstName = u.getFirstName() != null ? u.getFirstName().trim() : "";
        String lastName = u.getLastName() != null ? u.getLastName().trim() : "";
        String fullName = (firstName + " " + lastName).trim();
        if (fullName.isEmpty()) {
            fullName = u.getEmail() != null ? u.getEmail().split("@")[0] : "Customer";
        }

        String firstInitial = !firstName.isEmpty() ? String.valueOf(firstName.charAt(0)).toUpperCase() : "C";
        String lastInitial = !lastName.isEmpty() ? String.valueOf(lastName.charAt(0)).toUpperCase() : "";
        String initials = (firstInitial + lastInitial).trim();
        if (initials.isEmpty()) initials = "C";

        return ReviewResponseDTO.builder()
                .id(review.getId())
                .customerName(fullName)
                .initials(initials)
                .location(review.getLocation())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
