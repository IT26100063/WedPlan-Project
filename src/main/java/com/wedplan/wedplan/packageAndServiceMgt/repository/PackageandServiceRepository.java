package com.wedplan.wedplan.packageAndServiceMgt.repository;

import com.wedplan.wedplan.packageAndServiceMgt.model.PackageandServiceModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageandServiceRepository extends JpaRepository<PackageandServiceModel, Long> {

    List<PackageandServiceModel> findByVendorId(Long vendorId);

    List<PackageandServiceModel> findByCategoryIgnoreCase(String category);

    List<PackageandServiceModel> findByActiveTrue();
}
