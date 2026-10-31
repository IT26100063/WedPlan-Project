package com.wedplan.wedplan.vendorMgt.repository;

import com.wedplan.wedplan.vendorMgt.model.VendorModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorRepository extends JpaRepository<VendorModel, Long> {

    List<VendorModel> findByCategoryIgnoreCase(String category);

    List<VendorModel> findByLocationContainingIgnoreCase(String location);

    List<VendorModel> findByBusinessNameContainingIgnoreCase(String query);
}
