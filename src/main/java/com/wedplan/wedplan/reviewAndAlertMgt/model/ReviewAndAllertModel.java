package com.wedplan.wedplan.reviewAndAlertMgt.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews_and_alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewAndAllertModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Couple or reviewer name is required")
    @Size(min = 2, max = 120, message = "Name must be between 2 and 120 characters")
    @Column(name = "couple_name", nullable = false, length = 120)
    private String coupleName;

    @Column(name = "couple_email", length = 180)
    private String coupleEmail;

    private Long vendorId;

    @Column(name = "vendor_name", length = 150)
    private String vendorName;

    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @NotBlank(message = "Story or review feedback is required")
    @Size(max = 2500, message = "Review cannot exceed 2500 characters")
    @Column(length = 2500, nullable = false)
    private String story;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1 star")
    @Max(value = 5, message = "Rating cannot exceed 5 stars")
    @Column(nullable = false)
    private Integer rating = 5;

    @Column(length = 30)
    private String status = "APPROVED"; // PENDING, APPROVED, REJECTED

    @Column(name = "alert_type", length = 50)
    private String alertType = "REVIEW"; // REVIEW, ALERT, SYSTEM_ANNOUNCEMENT

    @Column(name = "alert_message", length = 500)
    private String alertMessage;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) status = "APPROVED";
        if (alertType == null) alertType = "REVIEW";
        if (isRead == null) isRead = false;
        if (rating == null) rating = 5;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
