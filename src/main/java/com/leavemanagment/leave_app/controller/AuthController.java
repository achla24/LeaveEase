package com.leavemanagment.leave_app.controller;

import com.leavemanagment.leave_app.model.Role;
import com.leavemanagment.leave_app.model.User;
import com.leavemanagment.leave_app.repository.UserRepository;
import com.leavemanagment.leave_app.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.Optional;

@Controller
public class AuthController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private EmployeeService employeeService;
    
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        // Check user role and redirect accordingly
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_HR")) ||
            authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return "redirect:/dashboard.html?role=hr";
        } else {
            return "redirect:/dashboard.html?role=employee";
        }
    }
    
    @GetMapping("/")
    public String home() {
        return "redirect:/login.html";
    }
    
    // Manual endpoint to create users for testing
    @GetMapping("/create-users")
    @ResponseBody
    public String createUsers() {
        try {
            initializeDemoUsers();
            return "Users created successfully!";
        } catch (Exception e) {
            return "Error creating users: " + e.getMessage();
        }
    }
    
    // Test endpoint to verify user lookup
    @GetMapping("/test-user")
    @ResponseBody
    public String testUser() {
        try {
            Optional<User> user = userRepository.findByUsername("hr_user");
            if (user.isPresent()) {
                User u = user.get();
                boolean passwordMatch = passwordEncoder.matches("password123", u.getPassword());
                return "User found: " + u.getUsername() + " with role: " + u.getRole() + 
                       ", Password match: " + passwordMatch;
            } else {
                return "User not found";
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    // Debug endpoint to print received POST parameters
    @PostMapping("/debug-login")
    @ResponseBody
    public String debugLogin(@RequestParam Map<String, String> params) {
        return "Received: " + params;
    }
    
    // Initialize demo users and employees
    @PostConstruct
    public void initializeData() {
        System.out.println("🔄 AuthController PostConstruct called...");
        try {
            initializeDemoUsers();
            employeeService.initializeSampleEmployees();
            System.out.println("✅ Data initialization completed successfully!");
        } catch (Exception e) {
            System.err.println("❌ Error during data initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void initializeDemoUsers() {
        System.out.println("🔄 Checking if users exist...");
        long userCount = userRepository.count();
        System.out.println("📊 Current user count: " + userCount);
        
        if (userCount == 0) {
            System.out.println("🔄 Creating demo users...");
            
            // Create HR user
            User hrUser = new User();
            hrUser.setUsername("hr_user");
            hrUser.setEmail("hr@company.com");
            hrUser.setPassword(passwordEncoder.encode("password123"));
            hrUser.setFullName("HR Manager");
            hrUser.setDepartment("Human Resources");
            hrUser.setRole(Role.HR);
            
            // Create employee users
            User employee1 = new User();
            employee1.setUsername("john_doe");
            employee1.setEmail("john.doe@company.com");
            employee1.setPassword(passwordEncoder.encode("password123"));
            employee1.setFullName("John Doe");
            employee1.setDepartment("Engineering");
            employee1.setRole(Role.EMPLOYEE);
            
            User employee2 = new User();
            employee2.setUsername("jane_smith");
            employee2.setEmail("jane.smith@company.com");
            employee2.setPassword(passwordEncoder.encode("password123"));
            employee2.setFullName("Jane Smith");
            employee2.setDepartment("Engineering");
            employee2.setRole(Role.EMPLOYEE);
            
            User employee3 = new User();
            employee3.setUsername("mike_johnson");
            employee3.setEmail("mike.johnson@company.com");
            employee3.setPassword(passwordEncoder.encode("password123"));
            employee3.setFullName("Mike Johnson");
            employee3.setDepartment("Marketing");
            employee3.setRole(Role.EMPLOYEE);
            
            userRepository.save(hrUser);
            userRepository.save(employee1);
            userRepository.save(employee2);
            userRepository.save(employee3);
            
            System.out.println("✅ Demo users created successfully!");
            System.out.println("🔐 HR User: hr_user / password123");
            System.out.println("👤 Employee: john_doe / password123");
            System.out.println("👤 Employee: jane_smith / password123");
            System.out.println("👤 Employee: mike_johnson / password123");
        } else {
            System.out.println("ℹ️ Users already exist, skipping creation.");
        }
    }
}