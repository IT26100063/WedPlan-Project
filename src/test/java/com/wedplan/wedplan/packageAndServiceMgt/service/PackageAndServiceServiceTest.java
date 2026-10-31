package com.wedplan.wedplan.packageAndServiceMgt.service;

import com.wedplan.wedplan.packageAndServiceMgt.model.PackageandServiceModel;
import com.wedplan.wedplan.packageAndServiceMgt.repository.PackageandServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PackageAndServiceServiceTest {

    private PackageandServiceRepository repository;
    private PackageAndServiceService service;

    @BeforeEach
    void setUp() {
        repository = (PackageandServiceRepository) Proxy.newProxyInstance(
                PackageandServiceRepository.class.getClassLoader(),
                new Class<?>[]{PackageandServiceRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("save".equals(name)) {
                        return args[0];
                    }
                    if ("findByCategoryIgnoreCase".equals(name)) {
                        PackageandServiceModel pkg = new PackageandServiceModel();
                        pkg.setId(1L);
                        pkg.setTitle("Diamond Photo Shoot");
                        pkg.setCategory("Photography");
                        pkg.setPrice(new BigDecimal("75000.00"));
                        return List.of(pkg);
                    }
                    return null;
                }
        );
        service = new PackageAndServiceService(repository);
    }

    @Test
    void testGetPackagesByCategory() {
        List<PackageandServiceModel> pkgs = service.getPackagesByCategory("Photography");
        assertEquals(1, pkgs.size());
        assertEquals("Diamond Photo Shoot", pkgs.get(0).getTitle());
    }
}
