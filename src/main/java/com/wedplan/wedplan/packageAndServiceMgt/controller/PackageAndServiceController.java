package com.wedplan.wedplan.packageAndServiceMgt.controller;

import com.wedplan.wedplan.packageAndServiceMgt.model.PackageandServiceModel;
import com.wedplan.wedplan.packageAndServiceMgt.service.PackageAndServiceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/packages")
@CrossOrigin(origins = "*")
public class PackageAndServiceController {

    private final PackageAndServiceService service;

    public PackageAndServiceController(PackageAndServiceService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PackageandServiceModel> createPackage(
            @Valid @RequestBody PackageandServiceModel servicePackage) {
        PackageandServiceModel saved = service.createPackage(servicePackage);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PackageandServiceModel>> getPackages(
            @RequestParam(required = false) Long vendorId,
            @RequestParam(required = false) String category) {
        if (vendorId != null) {
            return ResponseEntity.ok(service.getPackagesByVendorId(vendorId));
        }
        if (category != null && !category.isBlank()) {
            return ResponseEntity.ok(service.getPackagesByCategory(category));
        }
        return ResponseEntity.ok(service.getAllPackages());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PackageandServiceModel> getPackageById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getPackageById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PackageandServiceModel> updatePackage(
            @PathVariable Long id,
            @Valid @RequestBody PackageandServiceModel details) {
        return ResponseEntity.ok(service.updatePackage(id, details));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePackage(@PathVariable Long id) {
        service.deletePackage(id);
        return ResponseEntity.ok("Package or Service deleted successfully");
    }
}
