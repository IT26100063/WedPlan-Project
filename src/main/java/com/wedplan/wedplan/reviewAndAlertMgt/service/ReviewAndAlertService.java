package com.wedplan.wedplan.reviewAndAlertMgt.service;

import com.wedplan.wedplan.reviewAndAlertMgt.model.ReviewAndAllertModel;
import com.wedplan.wedplan.reviewAndAlertMgt.repository.ReviewAndAlertRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Primary
public class ReviewAndAlertService {

    private final ReviewAndAlertRepository repository;

    public ReviewAndAlertService(ReviewAndAlertRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ReviewAndAllertModel createReview(ReviewAndAllertModel review) {
        return repository.save(review);
    }

    public List<ReviewAndAllertModel> getAllReviews() {
        return repository.findAll();
    }

    public List<ReviewAndAllertModel> getApprovedReviews() {
        return repository.findByStatusIgnoreCase("APPROVED");
    }

    public List<ReviewAndAllertModel> getReviewsByVendor(Long vendorId) {
        return repository.findByVendorId(vendorId);
    }

    public List<ReviewAndAllertModel> getReviewsByStatus(String status) {
        return repository.findByStatusIgnoreCase(status);
    }

    public List<ReviewAndAllertModel> getUnreadAlerts() {
        return repository.findByIsReadFalse();
    }

    public ReviewAndAllertModel getReviewById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review or Alert not found with id: " + id));
    }

    @Transactional
    public ReviewAndAllertModel updateReview(Long id, ReviewAndAllertModel details) {
        ReviewAndAllertModel existing = getReviewById(id);

        existing.setCoupleName(details.getCoupleName());
        existing.setCoupleEmail(details.getCoupleEmail());
        existing.setVendorId(details.getVendorId());
        existing.setVendorName(details.getVendorName());
        existing.setTitle(details.getTitle());
        existing.setStory(details.getStory());
        existing.setRating(details.getRating());
        if (details.getStatus() != null) {
            existing.setStatus(details.getStatus());
        }
        if (details.getAlertType() != null) {
            existing.setAlertType(details.getAlertType());
        }
        if (details.getAlertMessage() != null) {
            existing.setAlertMessage(details.getAlertMessage());
        }

        return repository.save(existing);
    }

    @Transactional
    public ReviewAndAllertModel moderateReview(Long id, String status) {
        ReviewAndAllertModel existing = getReviewById(id);
        existing.setStatus(status.toUpperCase());
        return repository.save(existing);
    }

    @Transactional
    public ReviewAndAllertModel markAlertRead(Long id) {
        ReviewAndAllertModel existing = getReviewById(id);
        existing.setIsRead(true);
        return repository.save(existing);
    }

    @Transactional
    public void deleteReview(Long id) {
        ReviewAndAllertModel existing = getReviewById(id);
        repository.delete(existing);
    }
}
