package com.wedplan.wedplan.packageAndServiceMgt.service;

import com.wedplan.wedplan.packageAndServiceMgt.model.PackageandServiceModel;
import com.wedplan.wedplan.packageAndServiceMgt.repository.PackageandServiceRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Primary
public class PackageAndServiceService {

    private final PackageandServiceRepository repository;

    public PackageAndServiceService(PackageandServiceRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PackageandServiceModel createPackage(PackageandServiceModel servicePackage) {
        return repository.save(servicePackage);
    }

    public List<PackageandServiceModel> getAllPackages() {
        return repository.findAll();
    }

    public List<PackageandServiceModel> getActivePackages() {
        return repository.findByActiveTrue();
    }

    public List<PackageandServiceModel> getPackagesByVendorId(Long vendorId) {
        return repository.findByVendorId(vendorId);
    }

    public List<PackageandServiceModel> getPackagesByCategory(String category) {
        return repository.findByCategoryIgnoreCase(category);
    }

    public PackageandServiceModel getPackageById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Package or Service not found with id: " + id));
    }

    @Transactional
    public PackageandServiceModel updatePackage(Long id, PackageandServiceModel details) {
        PackageandServiceModel existing = getPackageById(id);

        existing.setTitle(details.getTitle());
        existing.setCategory(details.getCategory());
        existing.setDescription(details.getDescription());
        existing.setPrice(details.getPrice());
        existing.setCurrency(details.getCurrency());
        existing.setGuestCapacity(details.getGuestCapacity());
        existing.setDurationHours(details.getDurationHours());
        existing.setFeatures(details.getFeatures());
        existing.setVendorId(details.getVendorId());
        existing.setVendorName(details.getVendorName());
        if (details.getActive() != null) {
            existing.setActive(details.getActive());
        }

        return repository.save(existing);
    }

    @Transactional
    public void deletePackage(Long id) {
        PackageandServiceModel existing = getPackageById(id);
        repository.delete(existing);
    }
}
