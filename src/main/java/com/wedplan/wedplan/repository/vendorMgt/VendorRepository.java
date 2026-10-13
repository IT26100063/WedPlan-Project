package com.wedplan.wedplan.repository.vendorMgt;

import com.wedplan.wedplan.model.vendorMgt.VendorModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // Tells Spring Boot that this interface is responsible for database operations
public interface VendorRepository extends JpaRepository<VendorModel, Long> {

    /*
     * By extending JpaRepository<Vendor, Long>:
     * - 'Vendor' is the Entity class this repository manages.
     * - 'Long' is the data type of the Primary Key (@Id) in the Vendor class.
     *
     * You instantly get access to built-in methods like:
     * - save(Vendor vendor)     -> INSERT or UPDATE
     * - findAll()               -> SELECT * FROM vendors
     * - findById(Long id)       -> SELECT * FROM vendors WHERE id = ?
     * - delete(Vendor vendor)   -> DELETE FROM vendors WHERE id = ?
     */

    // You can also define custom search methods here just by naming them properly!
    // Example: Spring automatically writes the SQL to find vendors by their category.
    // List<Vendor> findByCategory(String category);
}