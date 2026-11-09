package com.wedplan.wedplan.repository.reviewAndAlertMgt;

import com.wedplan.wedplan.model.reviewAndAlertMgt.ReviewAndAllertModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewAndAlertRepository extends JpaRepository<ReviewAndAllertModel, Long> {

    List<ReviewAndAllertModel> findByVendorId(Long vendorId);

    List<ReviewAndAllertModel> findByStatusIgnoreCase(String status);

    List<ReviewAndAllertModel> findByAlertTypeIgnoreCase(String alertType);

    List<ReviewAndAllertModel> findByCoupleEmailIgnoreCase(String coupleEmail);

    List<ReviewAndAllertModel> findByIsReadFalse();
}
