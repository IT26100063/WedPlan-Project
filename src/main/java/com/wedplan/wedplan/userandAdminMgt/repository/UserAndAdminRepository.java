package com.wedplan.wedplan.userandAdminMgt.repository;

import com.wedplan.wedplan.userandAdminMgt.model.UserAndAdminModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAndAdminRepository extends JpaRepository<UserAndAdminModel, Long> {

    UserAndAdminModel findByEmail(String email);
}
