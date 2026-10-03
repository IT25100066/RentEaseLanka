package com.RentEaseLanka.RentEaseLankaTeam.Customer_Review.Controller;

import com.RentEaseLanka.RentEaseLankaTeam.Customer_Review.DTOs.ReviewRequestDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Customer_Review.DTOs.ReviewResponseDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Customer_Review.Service.CustomerReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class CustomerReviewController {

    private final CustomerReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ReviewResponseDTO> createReview(
            @Valid @RequestBody ReviewRequestDTO requestDTO,
            Principal principal) {
        ReviewResponseDTO response = reviewService.createReview(principal.getName(), requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/public")
    public ResponseEntity<List<ReviewResponseDTO>> getPublicReviews() {
        return ResponseEntity.ok(reviewService.getPublicReviews());
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<ReviewResponseDTO>> getMyReviews(Principal principal) {
        return ResponseEntity.ok(reviewService.getMyReviews(principal.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Void> deleteReview(
            @PathVariable("id") Long id,
            Principal principal) {
        reviewService.deleteReview(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
