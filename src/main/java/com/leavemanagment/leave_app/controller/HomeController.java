package com.leavemanagment.leave_app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    // Removed the home() method to allow Spring Boot's default welcome page mechanism to work
    
    @GetMapping("/dashboard")
    public String dashboard() {
        return "forward:/dashboard.html";
    }
}