package com.wedplan.wedplan.model.packageAndServiceMgt;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "packages_services")
public class PackageAndServiceModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String packageName;

    private String description;

    private double price;

    private String category;

    private String imageUrl;

    public PackageAndServiceModel() {
    }

    public PackageAndServiceModel(Long id, String packageName, String description,
                                  double price, String category, String imageUrl) {
        this.id = id;
        this.packageName = packageName;
        this.description = description;
        this.price = price;
        this.category = category;
        this.imageUrl = imageUrl;
    }


}