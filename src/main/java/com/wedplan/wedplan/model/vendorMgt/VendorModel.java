package com.wedplan.wedplan.model.vendorMgt;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity // Tells Spring this class represents a database table
@Table(name = "vendors") // Names the table in your database
@Data // Lombok: Automatically generates Getters, Setters, and toString() methods
@NoArgsConstructor // Lombok: Generates an empty constructor required by JPA
@AllArgsConstructor // Lombok: Generates a constructor with all properties
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

    // Optional fields you can expand on later
    private Double rating;
    private String imageUrl;
}