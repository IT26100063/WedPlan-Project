package com.wedplan.wedplan.controller.packageAndServiceMgt;

import com.wedplan.wedplan.model.packageAndServiceMgt.PackageandServiceModel;
import com.wedplan.wedplan.services.packageAndServiceMgt.PackageAndServiceServices;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/packages")
@CrossOrigin(origins = "*")
public class PackageAndServiceController {

    private final PackageAndServiceServices packageService;

    public PackageAndServiceController(PackageAndServiceServices packageService) {
        this.packageService = packageService;
    }

    @PostMapping
    public ResponseEntity<PackageandServiceModel> createPackage(@Valid @RequestBody PackageandServiceModel packageModel) {
        PackageandServiceModel savedPackage = packageService.createPackage(packageModel);
        return new ResponseEntity<>(savedPackage, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PackageandServiceModel>> getAllPackages() {
        return ResponseEntity.ok(packageService.getAllPackages());
    }

    @GetMapping("/active")
    public ResponseEntity<List<PackageandServiceModel>> getActivePackages() {
        return ResponseEntity.ok(packageService.getActivePackages());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PackageandServiceModel> getPackageById(@PathVariable Long id) {
        return ResponseEntity.ok(packageService.getPackageById(id));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<PackageandServiceModel>> getPackagesByCategory(@PathVariable String category) {
        return ResponseEntity.ok(packageService.getPackagesByCategory(category));
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<PackageandServiceModel>> getPackagesByVendor(@PathVariable Long vendorId) {
        return ResponseEntity.ok(packageService.getPackagesByVendor(vendorId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<PackageandServiceModel>> searchPackages(@RequestParam String keyword) {
        return ResponseEntity.ok(packageService.searchPackages(keyword));
    }

    @GetMapping("/price")
    public ResponseEntity<List<PackageandServiceModel>> getPackagesByPriceRange(@RequestParam BigDecimal min,
                                                                                @RequestParam BigDecimal max) {
        return ResponseEntity.ok(packageService.getPackagesByPriceRange(min, max));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PackageandServiceModel> updatePackage(@PathVariable Long id,
                                                                @Valid @RequestBody PackageandServiceModel packageModel) {
        return ResponseEntity.ok(packageService.updatePackage(id, packageModel));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PackageandServiceModel> setPackageActive(@PathVariable Long id, @RequestParam boolean active) {
        return ResponseEntity.ok(packageService.setPackageActive(id, active));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePackage(@PathVariable Long id) {
        packageService.deletePackage(id);
        return ResponseEntity.ok("Package deleted successfully");
    }
}