package com.wedplan.wedplan.vendorMgt.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vendors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Business name cannot be empty")
    @Size(min = 2, max = 100, message = "Business name must be between 2 and 100 characters")
    private String businessName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email format")
    private String email;

    @NotBlank(message = "Category is required")
    private String category; // e.g., Photography, Venues, Florals

    @NotBlank(message = "Location is required")
    private String location; // e.g., Colombo, Gampaha, Kandy

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private Double rating;
    private String imageUrl;
}
