package com.wedplan.wedplan.repository.vendorMgt;

import com.wedplan.wedplan.model.vendorMgt.VendorModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorRepository extends JpaRepository<VendorModel, Long> {

    List<VendorModel> findByCategoryIgnoreCase(String category);

    List<VendorModel> findByLocationContainingIgnoreCase(String location);

    List<VendorModel> findByBusinessNameContainingIgnoreCase(String query);
}
