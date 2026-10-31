package com.wedplan.wedplan.vendorMgt.controller;

import com.wedplan.wedplan.vendorMgt.model.VendorModel;
import com.wedplan.wedplan.vendorMgt.service.VendorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendors")
@CrossOrigin(origins = "*")
public class VendorController {

    private final VendorService vendorService;

    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    @PostMapping
    public ResponseEntity<VendorModel> createVendor(@Valid @RequestBody VendorModel vendor) {
        VendorModel savedVendor = vendorService.createVendor(vendor);
        return new ResponseEntity<>(savedVendor, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<VendorModel>> getAllVendors(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {
        if (category != null && !category.isBlank()) {
            return ResponseEntity.ok(vendorService.getVendorsByCategory(category));
        }
        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(vendorService.searchVendors(search));
        }
        return ResponseEntity.ok(vendorService.getAllVendors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendorModel> getVendorById(@PathVariable Long id) {
        return ResponseEntity.ok(vendorService.getVendorById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VendorModel> updateVendor(@PathVariable Long id, @Valid @RequestBody VendorModel vendor) {
        return ResponseEntity.ok(vendorService.updateVendor(id, vendor));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteVendor(@PathVariable Long id) {
        vendorService.deleteVendor(id);
        return ResponseEntity.ok("Vendor deleted successfully");
    }
}
