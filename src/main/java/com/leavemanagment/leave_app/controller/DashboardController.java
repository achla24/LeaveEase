package com.leavemanagment.leave_app.controller;

import com.leavemanagment.leave_app.model.LeaveRequest;
import com.leavemanagment.leave_app.repository.LeaveRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @GetMapping("/stats")
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Get current year data
        LocalDate currentDate = LocalDate.now();
        LocalDate yearStart = LocalDate.of(currentDate.getYear(), 1, 1);
        LocalDate yearEnd = LocalDate.of(currentDate.getYear(), 12, 31);
        
        List<LeaveRequest> allRequests = leaveRequestRepository.findAll();
        List<LeaveRequest> currentYearRequests = allRequests.stream()
            .filter(request -> request.getStartDate() != null && 
                    !request.getStartDate().isBefore(yearStart) && 
                    !request.getStartDate().isAfter(yearEnd))
            .collect(Collectors.toList());
        
        // Calculate total leave days taken (approved requests only)
        long totalLeaveDays = currentYearRequests.stream()
            .filter(request -> "Approved".equals(request.getStatus()))
            .mapToLong(LeaveRequest::getLeaveDuration)
            .sum();
        
        // Calculate approval rate
        long totalRequests = currentYearRequests.size();
        long approvedRequests = currentYearRequests.stream()
            .filter(request -> "Approved".equals(request.getStatus()))
            .count();
        
        double approvalRate = totalRequests > 0 ? (double) approvedRequests / totalRequests * 100 : 0;
        
        // Count pending requests
        long pendingRequests = leaveRequestRepository.countByStatus("Pending");
        
        // Count team members currently on leave
        List<LeaveRequest> currentlyOnLeave = leaveRequestRepository.findCurrentlyOnLeave(currentDate);
        
        // Calculate remaining days (assuming 29 days annual leave)
        long remainingDays = Math.max(0, 29 - totalLeaveDays);
        
        stats.put("totalLeaveTaken", totalLeaveDays);
        stats.put("remainingDays", remainingDays);
        stats.put("approvalRate", Math.round(approvalRate));
        stats.put("pendingRequests", pendingRequests);
        stats.put("teamMembersOnLeave", currentlyOnLeave.size());
        
        return stats;
    }

    @GetMapping("/quarterly-data")
    public Map<String, Object> getQuarterlyData() {
        Map<String, Object> data = new HashMap<>();
        
        LocalDate currentDate = LocalDate.now();
        int currentYear = currentDate.getYear();
        
        List<LeaveRequest> allRequests = leaveRequestRepository.findAll();
        
        // Calculate quarterly data
        Map<String, Long> quarterlyTaken = new HashMap<>();
        Map<String, Long> quarterlyRemaining = new HashMap<>();
        
        for (int quarter = 1; quarter <= 4; quarter++) {
            LocalDate quarterStart = getQuarterStart(currentYear, quarter);
            LocalDate quarterEnd = getQuarterEnd(currentYear, quarter);
            
            long takenDays = allRequests.stream()
                .filter(request -> "Approved".equals(request.getStatus()))
                .filter(request -> request.getStartDate() != null && 
                        !request.getStartDate().isBefore(quarterStart) && 
                        !request.getStartDate().isAfter(quarterEnd))
                .mapToLong(LeaveRequest::getLeaveDuration)
                .sum();
            
            quarterlyTaken.put("Q" + quarter, takenDays);
            quarterlyRemaining.put("Q" + quarter, Math.max(0, 29 - takenDays)); // Assuming 29 days per year
        }
        
        data.put("taken", quarterlyTaken);
        data.put("remaining", quarterlyRemaining);
        
        return data;
    }

    @GetMapping("/upcoming-leaves")
    public List<Map<String, Object>> getUpcomingLeaves() {
        LocalDate currentDate = LocalDate.now();
        List<LeaveRequest> upcomingLeaves = leaveRequestRepository.findUpcomingLeaves(currentDate);
        
        return upcomingLeaves.stream()
            .limit(5) // Show only next 5 upcoming leaves
            .map(leave -> {
                Map<String, Object> leaveInfo = new HashMap<>();
                leaveInfo.put("employeeName", leave.getEmployeeName());
                leaveInfo.put("startDate", leave.getStartDate());
                leaveInfo.put("endDate", leave.getEndDate());
                leaveInfo.put("duration", leave.getLeaveDuration());
                leaveInfo.put("leaveType", leave.getLeaveType());
                leaveInfo.put("status", leave.getStatus());
                return leaveInfo;
            })
            .collect(Collectors.toList());
    }

    @GetMapping("/team-on-leave")
    public List<Map<String, Object>> getTeamMembersOnLeave() {
        LocalDate currentDate = LocalDate.now();
        List<LeaveRequest> currentlyOnLeave = leaveRequestRepository.findCurrentlyOnLeave(currentDate);
        
        return currentlyOnLeave.stream()
            .map(leave -> {
                Map<String, Object> memberInfo = new HashMap<>();
                memberInfo.put("employeeName", leave.getEmployeeName());
                memberInfo.put("startDate", leave.getStartDate());
                memberInfo.put("endDate", leave.getEndDate());
                memberInfo.put("leaveType", leave.getLeaveType());
                memberInfo.put("reason", leave.getReason());
                return memberInfo;
            })
            .collect(Collectors.toList());
    }

    @GetMapping("/notifications")
    public List<Map<String, Object>> getNotifications() {
        List<LeaveRequest> recentRequests = leaveRequestRepository.findTop5ByOrderByIdDesc();
        
        return recentRequests.stream()
            .map(request -> {
                Map<String, Object> notification = new HashMap<>();
                notification.put("id", request.getId());
                notification.put("employeeName", request.getEmployeeName());
                notification.put("status", request.getStatus());
                notification.put("createdAt", request.getCreatedAt());
                notification.put("message", generateNotificationMessage(request));
                return notification;
            })
            .collect(Collectors.toList());
    }

    private String generateNotificationMessage(LeaveRequest request) {
        switch (request.getStatus()) {
            case "Pending":
                return "New leave request submitted";
            case "Approved":
                return "Leave request has been approved";
            case "Rejected":
                return "Leave request has been rejected";
            default:
                return "Leave request updated";
        }
    }

    private LocalDate getQuarterStart(int year, int quarter) {
        switch (quarter) {
            case 1: return LocalDate.of(year, Month.JANUARY, 1);
            case 2: return LocalDate.of(year, Month.APRIL, 1);
            case 3: return LocalDate.of(year, Month.JULY, 1);
            case 4: return LocalDate.of(year, Month.OCTOBER, 1);
            default: throw new IllegalArgumentException("Invalid quarter: " + quarter);
        }
    }

    private LocalDate getQuarterEnd(int year, int quarter) {
        switch (quarter) {
            case 1: return LocalDate.of(year, Month.MARCH, 31);
            case 2: return LocalDate.of(year, Month.JUNE, 30);
            case 3: return LocalDate.of(year, Month.SEPTEMBER, 30);
            case 4: return LocalDate.of(year, Month.DECEMBER, 31);
            default: throw new IllegalArgumentException("Invalid quarter: " + quarter);
        }
    }
}