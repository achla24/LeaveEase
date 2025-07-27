package com.leavemanagment.leave_app.controller;

import com.leavemanagment.leave_app.model.User;
import com.leavemanagment.leave_app.model.Role;
import com.leavemanagment.leave_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Username already exists"));
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Email already exists"));
        }
        
        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setDepartment(request.getDepartment());
        user.setRole(request.getRole() != null ? request.getRole() : Role.EMPLOYEE);
        
        User savedUser = userRepository.save(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User registered successfully");
        response.put("userId", savedUser.getId());
        response.put("username", savedUser.getUsername());
        response.put("role", savedUser.getRole());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid username or password"));
        }
        
        User user = userOpt.get();
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid username or password"));
        }
        
        // For now, return user info (we'll add JWT tokens later if needed)
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful");
        response.put("user", Map.of(
            "id", user.getId(),
            "username", user.getUsername(),
            "fullName", user.getFullName(),
            "email", user.getEmail(),
            "department", user.getDepartment(),
            "role", user.getRole()
        ));
        
        return ResponseEntity.ok(response);
    }
    
    // Request classes
    public static class RegisterRequest {
        private String username;
        private String email;
        private String password;
        private String fullName;
        private String department;
        private Role role;
        
        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        
        public Role getRole() { return role; }
        public void setRole(Role role) { this.role = role; }
    }
    
    public static class LoginRequest {
        private String username;
        private String password;
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}