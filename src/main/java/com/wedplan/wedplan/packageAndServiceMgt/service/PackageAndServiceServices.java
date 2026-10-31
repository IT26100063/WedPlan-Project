package com.wedplan.wedplan.packageAndServiceMgt.service;

import com.wedplan.wedplan.packageAndServiceMgt.repository.PackageandServiceRepository;
import org.springframework.stereotype.Service;

@Service
public class PackageAndServiceServices extends PackageAndServiceService {
    public PackageAndServiceServices(PackageandServiceRepository repository) {
        super(repository);
    }
}
