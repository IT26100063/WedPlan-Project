package com.wedplan.wedplan.services.paymentMgt;

import org.springframework.core.io.Resource;

public record ReceiptFile(Resource resource, String contentType, String originalName) {
}
