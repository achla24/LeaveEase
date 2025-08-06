package com.leavemanagment.leave_app.controller;

import com.leavemanagment.leave_app.model.LeaveRequest;
import com.leavemanagment.leave_app.repository.LeaveRequestRepository;
import com.leavemanagment.leave_app.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/leaves")
public class LeaveController {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;
    
    @Autowired
    private UserRepository userRepository;

    // CREATE: Add a new leave request with validation
    @PostMapping
    public ResponseEntity<LeaveRequest> createLeave(@Valid @RequestBody LeaveRequest leaveRequest) {
        System.out.println("📝 Creating new leave request for: " + leaveRequest.getEmployeeName());
        System.out.println("📅 Start Date: " + leaveRequest.getStartDate());
        System.out.println("📅 End Date: " + leaveRequest.getEndDate());
        System.out.println("📋 Reason: " + leaveRequest.getReason());
        System.out.println("🏷️ Leave Type: " + leaveRequest.getLeaveType());
        
        LeaveRequest savedLeave = leaveRequestRepository.save(leaveRequest);
        System.out.println("✅ Leave request saved with ID: " + savedLeave.getId());
        System.out.println("📊 Total leave requests in database: " + leaveRequestRepository.count());
        
        URI location = URI.create(String.format("/leaves/%s", savedLeave.getId()));
        return ResponseEntity.created(location).body(savedLeave);
    }

    // READ ALL: Get all leave requests (for admin/HR)
    @GetMapping
    public List<LeaveRequest> getAllLeaves() {
        System.out.println("Fetching all leave requests...");
        return leaveRequestRepository.findAll();
    }
    
    // READ USER LEAVES: Get leave requests for current user only
    @GetMapping("/my-leaves")
    public ResponseEntity<List<LeaveRequest>> getMyLeaves(Authentication authentication) {
        try {
            if (authentication == null) {
                System.err.println("❌ No authentication for my-leaves request");
                return ResponseEntity.status(401).build();
            }
            
            String username = authentication.getName();
            System.out.println("🔍 Fetching leaves for user: " + username);
            
            // Get user's full name from UserRepository
            Optional<com.leavemanagment.leave_app.model.User> userOpt = userRepository.findByUsername(username);
            if (!userOpt.isPresent()) {
                System.err.println("❌ User not found: " + username);
                return ResponseEntity.notFound().build();
            }
            
            String fullName = userOpt.get().getFullName();
            System.out.println("👤 Looking for leaves by employee name: " + fullName);
            
            // Find leaves by employee name
            List<LeaveRequest> userLeaves = leaveRequestRepository.findByEmployeeName(fullName);
            System.out.println("📋 Found " + userLeaves.size() + " leaves for " + fullName);
            
            return ResponseEntity.ok(userLeaves);
            
        } catch (Exception e) {
            System.err.println("Error fetching user leaves: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // READ ONE: Get a leave request by ID
    @GetMapping("/{id}")
    public ResponseEntity<LeaveRequest> getLeaveById(@PathVariable String id) {
        Optional<LeaveRequest> leave = leaveRequestRepository.findById(id);
        return leave.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // UPDATE: Update an existing leave request by ID
    @PutMapping("/{id}")
    public ResponseEntity<LeaveRequest> updateLeave(@PathVariable String id, @Valid @RequestBody LeaveRequest updatedLeave) {
        return leaveRequestRepository.findById(id)
                .map(leave -> {
                    leave.setEmployeeName(updatedLeave.getEmployeeName());
                    leave.setStartDate(updatedLeave.getStartDate());
                    leave.setEndDate(updatedLeave.getEndDate());
                    leave.setReason(updatedLeave.getReason());
                    leave.setStatus(updatedLeave.getStatus());
                    LeaveRequest savedLeave = leaveRequestRepository.save(leave);
                    return ResponseEntity.ok(savedLeave);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    @PutMapping("/{id}/approve")
    public ResponseEntity<LeaveRequest> approveLeave(@PathVariable String id) {
        Optional<LeaveRequest> optional = leaveRequestRepository.findById(id);
        if (optional.isPresent()) {
            LeaveRequest leave = optional.get();
            leave.setStatus("Approved");
            leave.setRejectionReason(null); // Clear any previous rejection reason
            return ResponseEntity.ok(leaveRequestRepository.save(leave));
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<LeaveRequest> rejectLeave(@PathVariable String id, @RequestBody(required = false) Map<String, String> requestBody) {
        try {
            System.out.println("❌ Rejecting leave request with ID: " + id);
            System.out.println("📋 Request body: " + requestBody);
            
            Optional<LeaveRequest> optional = leaveRequestRepository.findById(id);
            if (optional.isPresent()) {
                LeaveRequest leave = optional.get();
                System.out.println("📝 Found leave request for: " + leave.getEmployeeName());
                
                leave.setStatus("Rejected");
                
                // Get rejection reason from request body
                String rejectionReason = null;
                if (requestBody != null && requestBody.containsKey("rejectionReason")) {
                    rejectionReason = requestBody.get("rejectionReason");
                    System.out.println("📋 Rejection reason: " + rejectionReason);
                }
                leave.setRejectionReason(rejectionReason);
                
                LeaveRequest savedLeave = leaveRequestRepository.save(leave);
                System.out.println("✅ Leave request rejected successfully");
                
                return ResponseEntity.ok(savedLeave);
            } else {
                System.err.println("❌ Leave request not found with ID: " + id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("❌ Error rejecting leave request: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // New endpoint for HR to approve/reject with detailed information
    @PutMapping("/{id}/hr-action")
    public ResponseEntity<LeaveRequest> hrActionOnLeave(
            @PathVariable String id, 
            @RequestBody Map<String, String> actionRequest) {
        
        Optional<LeaveRequest> optional = leaveRequestRepository.findById(id);
        if (optional.isPresent()) {
            LeaveRequest leave = optional.get();
            String action = actionRequest.get("action");
            String reason = actionRequest.get("reason");
            
            if ("approve".equalsIgnoreCase(action)) {
                leave.setStatus("Approved");
                leave.setRejectionReason(null);
            } else if ("reject".equalsIgnoreCase(action)) {
                leave.setStatus("Rejected");
                leave.setRejectionReason(reason);
            } else {
                return ResponseEntity.badRequest().build();
            }
            
            return ResponseEntity.ok(leaveRequestRepository.save(leave));
        }
        return ResponseEntity.notFound().build();
    }


    // DELETE: Delete a leave request by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLeave(@PathVariable String id) {
        if (leaveRequestRepository.existsById(id)) {
            leaveRequestRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
