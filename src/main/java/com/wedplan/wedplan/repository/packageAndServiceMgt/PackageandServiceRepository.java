package com.wedplan.wedplan.repository.packageAndServiceMgt;

import com.wedplan.wedplan.model.packageAndServiceMgt.PackageandServiceModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PackageandServiceRepository extends JpaRepository<PackageandServiceModel, Long> {

    List<PackageandServiceModel> findByCategoryIgnoreCase(String category);

    List<PackageandServiceModel> findByVendorId(Long vendorId);

    List<PackageandServiceModel> findByActiveTrue();

    List<PackageandServiceModel> findByPackageNameContainingIgnoreCase(String keyword);

    List<PackageandServiceModel> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
}