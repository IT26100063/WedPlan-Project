package com.wedplan.wedplan.controller.vendorMgt;

import com.wedplan.wedplan.model.vendorMgt.VendorModel;
import com.wedplan.wedplan.services.vendorMgt.VendorServices;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendors")
@CrossOrigin(origins = "*")
public class VendorController {

    private final VendorServices vendorService;


    public VendorController(VendorServices vendorServices) {
        this.vendorService = vendorServices;
    }

    @PostMapping
    public ResponseEntity<VendorModel> createVendor(@Valid @RequestBody VendorModel vendor) {
        VendorModel savedVendor = vendorService.createVendor(vendor);
        return new ResponseEntity<>(savedVendor, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<VendorModel>> getAllVendors() {
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