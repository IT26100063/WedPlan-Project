package com.wedplan.wedplan.services.reviewAndAlertMgt;

import com.wedplan.wedplan.repository.reviewAndAlertMgt.ReviewAndAlertRepository;
import com.wedplan.wedplan.service.reviewAndAlertMgt.ReviewAndAlertService;
import org.springframework.stereotype.Service;

/**
 * Backward compatibility alias for {@link ReviewAndAlertService}.
 */
@Service
public class ReviewAndAlertServices extends ReviewAndAlertService {

    public ReviewAndAlertServices(ReviewAndAlertRepository repository) {
        super(repository);
    }
}
