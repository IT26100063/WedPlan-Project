package com.wedplan.wedplan.reviewAndAlertMgt.service;

import com.wedplan.wedplan.reviewAndAlertMgt.repository.ReviewAndAlertRepository;
import org.springframework.stereotype.Service;

@Service
public class ReviewAndAlertServices extends ReviewAndAlertService {
    public ReviewAndAlertServices(ReviewAndAlertRepository repository) {
        super(repository);
    }
}
