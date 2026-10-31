package com.wedplan.wedplan.vendorMgt.service;

import com.wedplan.wedplan.vendorMgt.model.VendorModel;
import com.wedplan.wedplan.vendorMgt.repository.VendorRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Primary
public class VendorService {

    private final VendorRepository vendorRepository;

    public VendorService(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    @Transactional
    public VendorModel createVendor(VendorModel vendor) {
        return vendorRepository.save(vendor);
    }

    public List<VendorModel> getAllVendors() {
        return vendorRepository.findAll();
    }

    public List<VendorModel> getVendorsByCategory(String category) {
        return vendorRepository.findByCategoryIgnoreCase(category);
    }

    public List<VendorModel> searchVendors(String query) {
        return vendorRepository.findByBusinessNameContainingIgnoreCase(query);
    }

    public VendorModel getVendorById(Long id) {
        return vendorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendor not found with id: " + id));
    }

    @Transactional
    public VendorModel updateVendor(Long id, VendorModel updatedVendorDetails) {
        VendorModel existingVendor = getVendorById(id);

        existingVendor.setBusinessName(updatedVendorDetails.getBusinessName());
        existingVendor.setEmail(updatedVendorDetails.getEmail());
        existingVendor.setCategory(updatedVendorDetails.getCategory());
        existingVendor.setLocation(updatedVendorDetails.getLocation());
        existingVendor.setDescription(updatedVendorDetails.getDescription());

        return vendorRepository.save(existingVendor);
    }

    @Transactional
    public void deleteVendor(Long id) {
        VendorModel existingVendor = getVendorById(id);
        vendorRepository.delete(existingVendor);
    }
}
