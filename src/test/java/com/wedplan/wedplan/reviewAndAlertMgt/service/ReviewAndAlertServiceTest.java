package com.wedplan.wedplan.reviewAndAlertMgt.service;

import com.wedplan.wedplan.reviewAndAlertMgt.model.ReviewAndAllertModel;
import com.wedplan.wedplan.reviewAndAlertMgt.repository.ReviewAndAlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ReviewAndAlertServiceTest {

    private ReviewAndAlertRepository repository;
    private ReviewAndAlertService service;

    @BeforeEach
    void setUp() {
        repository = (ReviewAndAlertRepository) Proxy.newProxyInstance(
                ReviewAndAlertRepository.class.getClassLoader(),
                new Class<?>[]{ReviewAndAlertRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("save".equals(name)) {
                        return args[0];
                    }
                    if ("findById".equals(name)) {
                        ReviewAndAllertModel review = new ReviewAndAllertModel();
                        review.setId(1L);
                        review.setCoupleName("Saman & Malini");
                        review.setStory("Wonderful vendor experience!");
                        review.setRating(5);
                        review.setStatus("PENDING");
                        return Optional.of(review);
                    }
                    return null;
                }
        );
        service = new ReviewAndAlertService(repository);
    }

    @Test
    void testModerateReview() {
        ReviewAndAllertModel approved = service.moderateReview(1L, "APPROVED");
        assertNotNull(approved);
        assertEquals("APPROVED", approved.getStatus());
    }

    @Test
    void testMarkAlertRead() {
        ReviewAndAllertModel read = service.markAlertRead(1L);
        assertNotNull(read);
        assertTrue(read.getIsRead());
    }
}
