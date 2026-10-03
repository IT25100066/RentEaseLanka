package com.RentEaseLanka.RentEaseLankaTeam.Customer_Review.Repository;

import com.RentEaseLanka.RentEaseLankaTeam.Customer_Review.Model.CustomerReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerReviewRepository extends JpaRepository<CustomerReview, Long> {

    List<CustomerReview> findByIsApprovedTrueOrderByCreatedAtDesc();

    List<CustomerReview> findByUserIdOrderByCreatedAtDesc(Long userId);
}
