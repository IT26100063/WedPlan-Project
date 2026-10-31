package com.wedplan.wedplan.vendorMgt.service;

import com.wedplan.wedplan.vendorMgt.model.VendorModel;
import com.wedplan.wedplan.vendorMgt.repository.VendorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class VendorServiceTest {

    private VendorRepository vendorRepository;
    private VendorService vendorServices;

    @BeforeEach
    void setUp() {
        vendorRepository = (VendorRepository) Proxy.newProxyInstance(
                VendorRepository.class.getClassLoader(),
                new Class<?>[]{VendorRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("save".equals(name)) {
                        return args[0];
                    }
                    if ("findById".equals(name)) {
                        Long id = (Long) args[0];
                        if (id.equals(1L)) {
                            VendorModel vendor = new VendorModel();
                            vendor.setId(1L);
                            vendor.setBusinessName("Grand Flora");
                            vendor.setCategory("Florals");
                            vendor.setLocation("Colombo");
                            vendor.setEmail("flora@example.com");
                            return Optional.of(vendor);
                        }
                        return Optional.empty();
                    }
                    if ("findByCategoryIgnoreCase".equals(name)) {
                        VendorModel vendor = new VendorModel();
                        vendor.setId(1L);
                        vendor.setBusinessName("Grand Flora");
                        vendor.setCategory("Florals");
                        return List.of(vendor);
                    }
                    return null;
                }
        );
        vendorServices = new VendorService(vendorRepository);
    }

    @Test
    void testGetVendorById() {
        VendorModel vendor = vendorServices.getVendorById(1L);
        assertNotNull(vendor);
        assertEquals("Grand Flora", vendor.getBusinessName());
    }

    @Test
    void testGetVendorsByCategory() {
        List<VendorModel> vendors = vendorServices.getVendorsByCategory("Florals");
        assertEquals(1, vendors.size());
        assertEquals("Grand Flora", vendors.get(0).getBusinessName());
    }
}
