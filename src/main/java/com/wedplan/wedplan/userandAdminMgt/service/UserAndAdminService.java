package com.wedplan.wedplan.userandAdminMgt.service;

import com.wedplan.wedplan.userandAdminMgt.model.UserAndAdminModel;
import com.wedplan.wedplan.userandAdminMgt.repository.UserAndAdminRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Primary
public class UserAndAdminService {

    private final UserAndAdminRepository repository;

    public UserAndAdminService(UserAndAdminRepository repository) {
        this.repository = repository;
    }

    @Transactional
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

    public UserAndAdminModel getUserByEmail(String email) {
        UserAndAdminModel user = repository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found with email: " + email);
        }
        return user;
    }

    public UserAndAdminModel authenticate(String email, String password) {
        UserAndAdminModel user = repository.findByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    @Transactional
    public UserAndAdminModel updateUser(Long id, UserAndAdminModel updatedDetails) {
        UserAndAdminModel existingUser = getUserById(id);

        existingUser.setName(updatedDetails.getName());
        existingUser.setEmail(updatedDetails.getEmail());
        existingUser.setPassword(updatedDetails.getPassword());
        existingUser.setRole(updatedDetails.getRole());

        return repository.save(existingUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        UserAndAdminModel existingUser = getUserById(id);
        repository.delete(existingUser);
    }
}
