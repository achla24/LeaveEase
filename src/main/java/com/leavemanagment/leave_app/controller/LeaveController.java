package com.leavemanagment.leave_app.controller;

import com.leavemanagment.leave_app.model.LeaveRequest;
import com.leavemanagment.leave_app.repository.LeaveRequestRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/leaves")
public class LeaveController {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    // CREATE: Add a new leave request with validation
    @PostMapping
    public ResponseEntity<LeaveRequest> createLeave(@Valid @RequestBody LeaveRequest leaveRequest) {
        LeaveRequest savedLeave = leaveRequestRepository.save(leaveRequest);
        System.out.println("Received Leave Request: " + leaveRequest);
        URI location = URI.create(String.format("/leaves/%s", savedLeave.getId()));
        return ResponseEntity.created(location).body(savedLeave);
    }

    // READ ALL: Get all leave requests
    @GetMapping
    public List<LeaveRequest> getAllLeaves() {
        System.out.println("Fetching all leave requests...");
        return leaveRequestRepository.findAll();
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
            return ResponseEntity.ok(leaveRequestRepository.save(leave));
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<LeaveRequest> rejectLeave(@PathVariable String id) {
        Optional<LeaveRequest> optional = leaveRequestRepository.findById(id);
        if (optional.isPresent()) {
            LeaveRequest leave = optional.get();
            leave.setStatus("Rejected");
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
