package com.wedplan.wedplan.services.userAndAdminMgt;

import com.wedplan.wedplan.model.userandadminMgt.UserAndAdminModel;
import com.wedplan.wedplan.repository.userandAdminMgt.UserAndAdminRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserAndAdminService {

    private final UserAndAdminRepository repository;

    public UserAndAdminService(UserAndAdminRepository repository) {
        this.repository = repository;
    }

    public UserAndAdminModel createUser(UserAndAdminModel user) {
        return repository.save(user);
    }

    public List<UserAndAdminModel> getAllUsers() {
        return repository.findAll();
    }

    public UserAndAdminModel getUserById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User or Admin not found with id: " + id));
    }

    public UserAndAdminModel updateUser(Long id, UserAndAdminModel updatedDetails) {
        UserAndAdminModel existingUser = getUserById(id);

        existingUser.setName(updatedDetails.getName());
        existingUser.setEmail(updatedDetails.getEmail());
        existingUser.setPassword(updatedDetails.getPassword());
        existingUser.setRole(updatedDetails.getRole());

        return repository.save(existingUser);
    }

    public void deleteUser(Long id) {
        UserAndAdminModel existingUser = getUserById(id);
        repository.delete(existingUser);
    }
}