package com.wedplan.wedplan.reviewAndAlertMgt.controller;

import com.wedplan.wedplan.reviewAndAlertMgt.model.ReviewAndAllertModel;
import com.wedplan.wedplan.reviewAndAlertMgt.service.ReviewAndAlertService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewAndAlertController {

    private final ReviewAndAlertService service;

    public ReviewAndAlertController(ReviewAndAlertService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ReviewAndAllertModel> createReview(
            @Valid @RequestBody ReviewAndAllertModel review) {
        ReviewAndAllertModel saved = service.createReview(review);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ReviewAndAllertModel>> getReviews(
            @RequestParam(required = false) Long vendorId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean unreadAlerts) {

        if (Boolean.TRUE.equals(unreadAlerts)) {
            return ResponseEntity.ok(service.getUnreadAlerts());
        }
        if (vendorId != null) {
            return ResponseEntity.ok(service.getReviewsByVendor(vendorId));
        }
        if (status != null && !status.isBlank()) {
            return ResponseEntity.ok(service.getReviewsByStatus(status));
        }
        return ResponseEntity.ok(service.getAllReviews());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewAndAllertModel> getReviewById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getReviewById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewAndAllertModel> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewAndAllertModel details) {
        return ResponseEntity.ok(service.updateReview(id, details));
    }

    @PatchMapping("/{id}/moderate")
    public ResponseEntity<ReviewAndAllertModel> moderateReview(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(service.moderateReview(id, status));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ReviewAndAllertModel> markAlertRead(@PathVariable Long id) {
        return ResponseEntity.ok(service.markAlertRead(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteReview(@PathVariable Long id) {
        service.deleteReview(id);
        return ResponseEntity.ok("Review or Alert deleted successfully");
    }
}
