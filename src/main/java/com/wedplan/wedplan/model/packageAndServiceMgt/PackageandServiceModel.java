package com.wedplan.wedplan.model.packageAndServiceMgt;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "packages_and_services")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackageandServiceModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Package name cannot be empty")
    @Size(min = 2, max = 100, message = "Package name must be between 2 and 100 characters")
    private String packageName;

    @NotBlank(message = "Category is required")
    private String category; // e.g., Photography, Catering, Florals, Full Wedding

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Column(length = 1000)
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", message = "Price cannot be negative")
    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    @Size(max = 2000, message = "Included services cannot exceed 2000 characters")
    @Column(length = 2000)
    private String includedServices; // comma-separated list, e.g., "Album, Drone shots, 2 photographers"

    @Min(value = 0, message = "Guest capacity cannot be negative")
    private Integer guestCapacity;

    @Min(value = 0, message = "Duration cannot be negative")
    private Integer durationHours;

    private Long vendorId; // ID of the vendor offering this package (optional)

    private String imageUrl;

    private Boolean active = true;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (active == null) {
            active = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}