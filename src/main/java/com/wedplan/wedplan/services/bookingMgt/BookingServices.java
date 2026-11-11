package com.wedplan.wedplan.services.bookingMgt;

import com.wedplan.wedplan.repository.bookingMgt.BookingRepository;
import com.wedplan.wedplan.service.bookingMgt.BookingService;
import org.springframework.stereotype.Service;

/**
 * Backward compatibility alias for {@link BookingService}.
 */
@Service
public class BookingServices extends BookingService {

    public BookingServices(BookingRepository repository) {
        super(repository);
    }
}
