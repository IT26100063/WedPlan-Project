package com.wedplan.wedplan.services.vendorMgt;

import com.wedplan.wedplan.repository.vendorMgt.VendorRepository;
import com.wedplan.wedplan.service.vendorMgt.VendorService;
import org.springframework.stereotype.Service;


@Service
public class VendorServices extends VendorService {

    public VendorServices(VendorRepository vendorRepository) {
        super(vendorRepository);
    }
}
