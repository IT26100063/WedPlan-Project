package com.wedplan.wedplan.bookingMgt.service;

import com.wedplan.wedplan.bookingMgt.repository.BookingRepository;
import org.springframework.stereotype.Service;

@Service
public class BookingServices extends BookingService {
    public BookingServices(BookingRepository repository) {
        super(repository);
    }
}
