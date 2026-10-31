package com.wedplan.wedplan.vendorMgt.service;

import com.wedplan.wedplan.vendorMgt.repository.VendorRepository;
import org.springframework.stereotype.Service;

@Service
public class VendorServices extends VendorService {
    public VendorServices(VendorRepository vendorRepository) {
        super(vendorRepository);
    }
}
