package com.wedplan.wedplan.services.vendorMgt;

import com.wedplan.wedplan.model.vendorMgt.VendorModel;
import com.wedplan.wedplan.repository.vendorMgt.VendorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VendorServices {

    private final VendorRepository vendorRepository;

    public VendorServices(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    public VendorModel createVendor(VendorModel vendor) {
        return vendorRepository.save(vendor);
    }

    public List<VendorModel> getAllVendors() {
        return vendorRepository.findAll();
    }

    public VendorModel getVendorById(Long id) {
        return vendorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendor not found with id: " + id));
    }

    public VendorModel updateVendor(Long id, VendorModel updatedVendorDetails) {
        VendorModel existingVendor = getVendorById(id);

        existingVendor.setBusinessName(updatedVendorDetails.getBusinessName());
        existingVendor.setEmail(updatedVendorDetails.getEmail());
        existingVendor.setCategory(updatedVendorDetails.getCategory());
        existingVendor.setLocation(updatedVendorDetails.getLocation());
        existingVendor.setDescription(updatedVendorDetails.getDescription());

        return vendorRepository.save(existingVendor);
    }

    public void deleteVendor(Long id) {
        VendorModel existingVendor = getVendorById(id);
        vendorRepository.delete(existingVendor);
    }
}