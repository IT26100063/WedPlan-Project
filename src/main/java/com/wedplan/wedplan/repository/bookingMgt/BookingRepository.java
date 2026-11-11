package com.wedplan.wedplan.repository.bookingMgt;

import com.wedplan.wedplan.model.bookingMgt.BookingModel;
import com.wedplan.wedplan.model.bookingMgt.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<BookingModel, Long> {

    Optional<BookingModel> findByBookingReferenceIgnoreCase(String bookingReference);

    List<BookingModel> findByCustomerEmailIgnoreCase(String customerEmail);

    List<BookingModel> findByVendorId(Long vendorId);

    List<BookingModel> findByStatus(BookingStatus status);

    boolean existsByBookingReferenceIgnoreCase(String bookingReference);
}
