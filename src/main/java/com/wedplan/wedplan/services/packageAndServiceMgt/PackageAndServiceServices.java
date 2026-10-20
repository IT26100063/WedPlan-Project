package com.wedplan.wedplan.services.packageAndServiceMgt;

import com.wedplan.wedplan.model.packageAndServiceMgt.PackageandServiceModel;
import com.wedplan.wedplan.repository.packageAndServiceMgt.PackageandServiceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PackageAndServiceServices {

    private final PackageandServiceRepository packageRepository;

    public PackageAndServiceServices(PackageandServiceRepository packageRepository) {
        this.packageRepository = packageRepository;
    }

    public PackageandServiceModel createPackage(PackageandServiceModel packageModel) {
        packageModel.setId(null);
        return packageRepository.save(packageModel);
    }

    public List<PackageandServiceModel> getAllPackages() {
        return packageRepository.findAll();
    }

    public List<PackageandServiceModel> getActivePackages() {
        return packageRepository.findByActiveTrue();
    }

    public PackageandServiceModel getPackageById(Long id) {
        return packageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Package not found with id: " + id));
    }

    public List<PackageandServiceModel> getPackagesByCategory(String category) {
        return packageRepository.findByCategoryIgnoreCase(category);
    }

    public List<PackageandServiceModel> getPackagesByVendor(Long vendorId) {
        return packageRepository.findByVendorId(vendorId);
    }

    public List<PackageandServiceModel> searchPackages(String keyword) {
        return packageRepository.findByPackageNameContainingIgnoreCase(keyword);
    }

    public List<PackageandServiceModel> getPackagesByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice.compareTo(maxPrice) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Minimum price cannot be greater than maximum price");
        }
        return packageRepository.findByPriceBetween(minPrice, maxPrice);
    }

    public PackageandServiceModel updatePackage(Long id, PackageandServiceModel updatedDetails) {
        PackageandServiceModel existingPackage = getPackageById(id);

        existingPackage.setPackageName(updatedDetails.getPackageName());
        existingPackage.setCategory(updatedDetails.getCategory());
        existingPackage.setDescription(updatedDetails.getDescription());
        existingPackage.setPrice(updatedDetails.getPrice());
        existingPackage.setIncludedServices(updatedDetails.getIncludedServices());
        existingPackage.setGuestCapacity(updatedDetails.getGuestCapacity());
        existingPackage.setDurationHours(updatedDetails.getDurationHours());
        existingPackage.setVendorId(updatedDetails.getVendorId());
        existingPackage.setImageUrl(updatedDetails.getImageUrl());
        if (updatedDetails.getActive() != null) {
            existingPackage.setActive(updatedDetails.getActive());
        }

        return packageRepository.save(existingPackage);
    }

    public PackageandServiceModel setPackageActive(Long id, boolean active) {
        PackageandServiceModel existingPackage = getPackageById(id);
        existingPackage.setActive(active);
        return packageRepository.save(existingPackage);
    }

    public void deletePackage(Long id) {
        PackageandServiceModel existingPackage = getPackageById(id);
        packageRepository.delete(existingPackage);
    }
}
