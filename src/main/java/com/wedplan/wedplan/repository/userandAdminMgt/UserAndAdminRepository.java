package com.wedplan.wedplan.repository.userandAdminMgt;

import com.wedplan.wedplan.model.userandadminMgt.UserAndAdminModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAndAdminRepository extends JpaRepository<UserAndAdminModel, Long> {
    // Custom method to find a user by their email address
    UserAndAdminModel findByEmail(String email);
}