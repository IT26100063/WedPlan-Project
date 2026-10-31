package com.wedplan.wedplan.bookingMgt.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings", uniqueConstraints = {
        @UniqueConstraint(name = "uk_booking_reference", columnNames = "booking_reference")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Booking reference is required")
    @Column(name = "booking_reference", nullable = false, length = 60, unique = true)
    private String bookingReference;

    @NotBlank(message = "Customer name is required")
    @Size(min = 2, max = 120, message = "Customer name must be between 2 and 120 characters")
    @Column(name = "customer_name", nullable = false, length = 120)
    private String customerName;

    @NotBlank(message = "Customer email is required")
    @Email(message = "Enter a valid email format")
    @Column(name = "customer_email", nullable = false, length = 180)
    private String customerEmail;

    @Column(name = "customer_phone", length = 30)
    private String customerPhone;

    @Column(name = "partner_name", length = 120)
    private String partnerName;

    @NotNull(message = "Wedding date is required")
    @Column(name = "wedding_date", nullable = false)
    private LocalDate weddingDate;

    @Column(length = 150)
    private String location;

    private Long vendorId;

    @Column(name = "vendor_name", length = 150)
    private String vendorName;

    private Long packageId;

    @Column(name = "package_name", length = 150)
    private String packageName;

    @Column(name = "guest_count")
    private Integer guestCount;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be greater than zero")
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "deposit_amount", precision = 12, scale = 2)
    private BigDecimal depositAmount;

    @Column(name = "is_plus_member")
    private Boolean isPlusMember = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BookingStatus status = BookingStatus.PENDING;

    @Size(max = 2000, message = "Special notes cannot exceed 2000 characters")
    @Column(name = "special_notes", length = 2000)
    private String specialNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) status = BookingStatus.PENDING;
        if (isPlusMember == null) isPlusMember = false;
        if (bookingReference == null || bookingReference.isBlank()) {
            bookingReference = "WP-" + (System.currentTimeMillis() % 10000000);
        }
        if (depositAmount == null && totalAmount != null) {
            depositAmount = totalAmount.multiply(new BigDecimal("0.30")).setScale(2, java.math.RoundingMode.HALF_UP);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
