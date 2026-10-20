package com.wedplan.wedplan.controller.userAndAdminMgt;

import com.wedplan.wedplan.model.userandadminMgt.UserAndAdminModel;
import com.wedplan.wedplan.services.userAndAdminMgt.UserAndAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserAndAdminController {

    private final UserAndAdminService service;

    public UserAndAdminController(UserAndAdminService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<UserAndAdminModel> createUser(@Valid @RequestBody UserAndAdminModel user) {
        UserAndAdminModel savedUser = service.createUser(user);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<UserAndAdminModel>> getAllUsers() {
        return ResponseEntity.ok(service.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserAndAdminModel> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserAndAdminModel> updateUser(@PathVariable Long id, @Valid @RequestBody UserAndAdminModel user) {
        return ResponseEntity.ok(service.updateUser(id, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        service.deleteUser(id);
        return ResponseEntity.ok("User or Admin deleted successfully");
    }
}