// Dashboard JavaScript
class LeaveDashboard {
    constructor() {
        this.baseURL = 'http://localhost:9090';
        this.chart = null;
        this.userRole = this.getUserRole(); // Get user role from URL or session
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.setupRoleBasedUI();
        this.loadDashboardData();
        this.setupTabNavigation();
    }
    
    getUserRole() {
        // Get role from URL parameter
        const urlParams = new URLSearchParams(window.location.search);
        return urlParams.get('role') || 'employee';
    }
    
    setupRoleBasedUI() {
        // Update user role display
        this.updateUserInfo();
        
        // Show/hide HR-specific sections based on role
        if (this.userRole === 'hr') {
            this.showHRFeatures();
            this.loadHRData();
        } else {
            this.hideHRFeatures();
        }
    }
    
    updateUserInfo() {
        const userRoleElement = document.getElementById('userRole');
        if (userRoleElement) {
            userRoleElement.textContent = this.userRole === 'hr' ? 'HR Manager' : 'Employee';
            userRoleElement.className = `user-role ${this.userRole}-role`;
        }
    }
    
    showHRFeatures() {
        // Add HR-specific stats cards
        this.addHRStatsCards();
        
        // Add HR tab if not exists
        this.addHRTab();
        
        // Show approve/reject buttons in history
        document.body.classList.add('hr-user');
    }
    
    hideHRFeatures() {
        // Hide HR-specific elements
        document.body.classList.add('employee-user');
        
        // Hide approve/reject buttons
        const actionButtons = document.querySelectorAll('.action-btn');
        actionButtons.forEach(btn => btn.style.display = 'none');
    }
    
    addHRStatsCards() {
        const statsContainer = document.querySelector('.stats-grid');
        if (statsContainer && !document.getElementById('hrStatsCards')) {
            const hrStatsHTML = `
                <div id="hrStatsCards" class="hr-stats-section">
                    <div class="stat-card hr-stat">
                        <div class="stat-icon">👥</div>
                        <div class="stat-content">
                            <div class="stat-number" id="totalEmployeesCount">0</div>
                            <div class="stat-label">Total Employees</div>
                            <div class="stat-subtitle">Active workforce</div>
                        </div>
                    </div>
                    <div class="stat-card hr-stat">
                        <div class="stat-icon">🏠</div>
                        <div class="stat-content">
                            <div class="stat-number" id="employeesOnLeaveCount">0</div>
                            <div class="stat-label">On Leave</div>
                            <div class="stat-subtitle">Currently away</div>
                        </div>
                    </div>
                    <div class="stat-card hr-stat">
                        <div class="stat-icon">✅</div>
                        <div class="stat-content">
                            <div class="stat-number" id="employeesPresentCount">0</div>
                            <div class="stat-label">Present</div>
                            <div class="stat-subtitle">Available today</div>
                        </div>
                    </div>
                    <div class="stat-card hr-stat">
                        <div class="stat-icon">⏳</div>
                        <div class="stat-content">
                            <div class="stat-number" id="pendingApprovalsCount">0</div>
                            <div class="stat-label">Pending Approvals</div>
                            <div class="stat-subtitle">Awaiting decision</div>
                        </div>
                    </div>
                </div>
            `;
            statsContainer.insertAdjacentHTML('beforeend', hrStatsHTML);
        }
    }
    
    addHRTab() {
        const navTabs = document.querySelector('.nav-tabs');
        const tabContents = document.querySelector('.dashboard-content');
        
        if (navTabs && !document.querySelector('[data-tab="hr-management"]')) {
            // Add HR tab button
            const hrTabButton = document.createElement('button');
            hrTabButton.className = 'nav-tab';
            hrTabButton.setAttribute('data-tab', 'hr-management');
            hrTabButton.innerHTML = `
                <span class="tab-icon">👨‍💼</span>
                HR Management
            `;
            navTabs.appendChild(hrTabButton);
            
            // Add HR tab content
            const hrTabContent = document.createElement('div');
            hrTabContent.id = 'hr-management';
            hrTabContent.className = 'tab-content';
            hrTabContent.innerHTML = `
                <div class="hr-management-section">
                    <h2>HR Management Dashboard</h2>
                    
                    <div class="hr-actions">
                        <div class="hr-section">
                            <h3>📋 Pending Leave Requests</h3>
                            <div id="pendingRequestsList" class="pending-requests-list">
                                <!-- Pending requests will be loaded here -->
                            </div>
                        </div>
                        
                        <div class="hr-section">
                            <h3>📊 Department Overview</h3>
                            <div id="departmentStats" class="department-stats">
                                <!-- Department stats will be loaded here -->
                            </div>
                        </div>
                        
                        <div class="hr-section">
                            <h3>📈 Leave Analytics</h3>
                            <div id="leaveAnalytics" class="leave-analytics">
                                <!-- Analytics will be loaded here -->
                            </div>
                        </div>
                    </div>
                </div>
            `;
            tabContents.appendChild(hrTabContent);
        }
    }

    setupEventListeners() {
        // Form submission
        const leaveForm = document.getElementById('leaveForm');
        if (leaveForm) {
            leaveForm.addEventListener('submit', (e) => this.handleFormSubmit(e));
        }
    }

    setupTabNavigation() {
        const tabButtons = document.querySelectorAll('.nav-tab');
        const tabContents = document.querySelectorAll('.tab-content');

        tabButtons.forEach(button => {
            button.addEventListener('click', () => {
                const targetTab = button.getAttribute('data-tab');
                
                // Remove active class from all tabs and contents
                tabButtons.forEach(btn => btn.classList.remove('active'));
                tabContents.forEach(content => content.classList.remove('active'));
                
                // Add active class to clicked tab and corresponding content
                button.classList.add('active');
                document.getElementById(targetTab).classList.add('active');
                
                // Load specific data for the tab
                if (targetTab === 'history') {
                    this.loadHistoryData();
                } else if (targetTab === 'hr-management' && this.userRole === 'hr') {
                    this.loadHRData();
                }
            });
        });
    }

    async loadDashboardData() {
        try {
            await Promise.all([
                this.loadStats(),
                this.loadQuarterlyData(),
                this.loadUpcomingLeaves(),
                this.loadTeamOnLeave(),
                this.loadNotifications()
            ]);
        } catch (error) {
            console.error('Error loading dashboard data:', error);
        }
    }

    async loadStats() {
        try {
            const response = await fetch(`${this.baseURL}/api/dashboard/stats`);
            const stats = await response.json();
            
            document.getElementById('totalLeaveTaken').textContent = `${stats.totalLeaveTaken} days`;
            document.getElementById('approvalRate').textContent = `${stats.approvalRate}%`;
            document.getElementById('pendingRequests').textContent = stats.pendingRequests;
            document.getElementById('teamMembersOnLeave').textContent = stats.teamMembersOnLeave;
            
            // Update remaining days in all stat cards
            const remainingDaysElements = document.querySelectorAll('.stat-subtitle');
            remainingDaysElements.forEach(element => {
                element.textContent = `${stats.remainingDays} days remaining this year`;
            });
            
        } catch (error) {
            console.error('Error loading stats:', error);
        }
    }

    async loadQuarterlyData() {
        try {
            // this line will ask java backend for the data of q1,q2,q3,q4
            const response = await fetch(`${this.baseURL}/api/dashboard/quarterly-data`);
        //  this will call the data
            const data = await response.json();
            
            this.createChart(data);
            
            // Update chart summary
            const totalTaken = Object.values(data.taken).reduce((sum, val) => sum + val, 0);
            const totalRemaining = Object.values(data.remaining).reduce((sum, val) => sum + val, 0);
            
            document.getElementById('chartTotalDays').textContent = totalTaken;
            document.getElementById('chartRemainingDays').textContent = totalRemaining;
            
        } catch (error) {
            console.error('Error loading quarterly data:', error);
        }
    }

    createChart(data) {
        const ctx = document.getElementById('leaveChart').getContext('2d');
        
        if (this.chart) {
            this.chart.destroy();
        }
        
        this.chart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: ['Q1', 'Q2', 'Q3', 'Q4'],
                datasets: [
                    {
                        label: 'Leave days taken',
                        data: [data.taken.Q1, data.taken.Q2, data.taken.Q3, data.taken.Q4],
                        backgroundColor: '#4F46E5',
                        borderRadius: 4,
                        barThickness: 40
                    },
                    {
                        label: 'Remaining',
                        data: [data.remaining.Q1, data.remaining.Q2, data.remaining.Q3, data.remaining.Q4],
                        backgroundColor: '#E5E7EB',
                        borderRadius: 4,
                        barThickness: 40
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    x: {
                        grid: {
                            display: false
                        }
                    },
                    y: {
                        beginAtZero: true,
                        grid: {
                            color: '#F1F5F9'
                        }
                    }
                }
            }
        });
    }

    async loadUpcomingLeaves() {
        try {
            const response = await fetch(`${this.baseURL}/api/dashboard/upcoming-leaves`);
            const upcomingLeaves = await response.json();
            
            const container = document.getElementById('upcomingLeaveList');
            container.innerHTML = '';
            
            if (upcomingLeaves.length === 0) {
                container.innerHTML = '<p class="text-gray-500">No upcoming leaves scheduled</p>';
                return;
            }
            
            upcomingLeaves.forEach(leave => {
                const leaveItem = document.createElement('div');
                leaveItem.className = `upcoming-item ${leave.status.toLowerCase()}`;
                
                leaveItem.innerHTML = `
                    <div class="upcoming-header">
                        <span class="upcoming-title">${leave.leaveType}</span>
                        <span class="upcoming-status">${leave.status}</span>
                    </div>
                    <div class="upcoming-dates">
                        ${this.formatDate(leave.startDate)} - ${this.formatDate(leave.endDate)} (${leave.duration} days)
                    </div>
                `;
                
                container.appendChild(leaveItem);
            });
            
        } catch (error) {
            console.error('Error loading upcoming leaves:', error);
        }
    }

    async loadTeamOnLeave() {
        try {
            const response = await fetch(`${this.baseURL}/api/dashboard/team-on-leave`);
            const teamMembers = await response.json();
            
            const container = document.getElementById('teamOnLeaveList');
            container.innerHTML = '';
            
            if (teamMembers.length === 0) {
                container.innerHTML = '<p class="text-gray-500">No team members currently on leave</p>';
                return;
            }
            
            teamMembers.forEach(member => {
                const memberItem = document.createElement('div');
                memberItem.className = 'team-member';
                
                memberItem.innerHTML = `
                    <div class="member-avatar"></div>
                    <div class="member-info">
                        <div class="member-name">${member.employeeName}</div>
                        <div class="member-dates">${this.formatDate(member.startDate)} - ${this.formatDate(member.endDate)}</div>
                    </div>
                    <span class="member-type">${member.leaveType}</span>
                `;
                
                container.appendChild(memberItem);
            });
            
        } catch (error) {
            console.error('Error loading team on leave:', error);
        }
    }

    async loadNotifications() {
        try {
            const response = await fetch(`${this.baseURL}/api/dashboard/notifications`);
            const notifications = await response.json();
            
            const container = document.getElementById('notificationsList');
            container.innerHTML = '';
            
            if (notifications.length === 0) {
                container.innerHTML = '<p class="text-gray-500">No recent notifications</p>';
                return;
            }
            
            notifications.forEach(notification => {
                const notificationItem = document.createElement('div');
                notificationItem.className = 'notification-item';
                
                notificationItem.innerHTML = `
                    <div class="notification-icon"></div>
                    <div class="notification-content">
                        <div class="notification-text">${notification.message}</div>
                        <div class="notification-time">${this.formatDateTime(notification.createdAt)}</div>
                    </div>
                `;
                
                container.appendChild(notificationItem);
            });
            
        } catch (error) {
            console.error('Error loading notifications:', error);
        }
    }

    async loadHistoryData() {
        try {
            const response = await fetch(`${this.baseURL}/leaves`);
            const leaves = await response.json();
            
            const tbody = document.getElementById('historyTableBody');
            tbody.innerHTML = '';
            
            leaves.forEach(leave => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${leave.employeeName}</td>
                    <td>${this.formatDate(leave.startDate)}</td>
                    <td>${this.formatDate(leave.endDate)}</td>
                    <td>${this.calculateDuration(leave.startDate, leave.endDate)} days</td>
                    <td>${leave.leaveType || 'Annual'}</td>
                    <td><span class="status-badge status-${leave.status.toLowerCase()}">${leave.status}</span></td>
                    <td>
                        ${leave.status === 'Pending' ? `
                            <button class="action-btn approve-btn" onclick="dashboard.updateLeaveStatus('${leave.id}', 'approve')">Approve</button>
                            <button class="action-btn reject-btn" onclick="dashboard.updateLeaveStatus('${leave.id}', 'reject')">Reject</button>
                        ` : ''}
                    </td>
                `;
                tbody.appendChild(row);
            });
            
        } catch (error) {
            console.error('Error loading history data:', error);
        }
    }

    async handleFormSubmit(e) {
        e.preventDefault();
        
        const formData = new FormData(e.target);
        const leaveData = {
            employeeName: formData.get('employeeName'),
            startDate: formData.get('startDate'),
            endDate: formData.get('endDate'),
            reason: formData.get('reason'),
            leaveType: formData.get('leaveType'),
            status: 'Pending'
        };
        
        try {
            const response = await fetch(`${this.baseURL}/leaves`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(leaveData)
            });
            
            if (response.ok) {
                alert('Leave request submitted successfully!');
                e.target.reset();
                this.loadDashboardData(); // Refresh dashboard data
            } else {
                throw new Error('Failed to submit leave request');
            }
            
        } catch (error) {
            console.error('Error submitting leave request:', error);
            alert('Error submitting leave request. Please try again.');
        }
    }

    async updateLeaveStatus(leaveId, action) {
        try {
            const response = await fetch(`${this.baseURL}/leaves/${leaveId}/${action}`, {
                method: 'PUT'
            });
            
            if (response.ok) {
                alert(`Leave request ${action}d successfully!`);
                this.loadHistoryData();
                this.loadDashboardData();
            } else {
                throw new Error(`Failed to ${action} leave request`);
            }
            
        } catch (error) {
            console.error(`Error ${action}ing leave request:`, error);
            alert(`Error ${action}ing leave request. Please try again.`);
        }
    }

    formatDate(dateString) {
        if (!dateString) return '';
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            month: 'short',
            day: 'numeric',
            year: 'numeric'
        });
    }

    formatDateTime(dateTimeString) {
        if (!dateTimeString) return '';
        const date = new Date(dateTimeString);
        const now = new Date();
        const diffInHours = Math.floor((now - date) / (1000 * 60 * 60));
        
        if (diffInHours < 1) {
            return 'Just now';
        } else if (diffInHours < 24) {
            return `${diffInHours} hours ago`;
        } else {
            const diffInDays = Math.floor(diffInHours / 24);
            return `${diffInDays} days ago`;
        }
    }

    calculateDuration(startDate, endDate) {
        if (!startDate || !endDate) return 0;
        const start = new Date(startDate);
        const end = new Date(endDate);
        const diffTime = Math.abs(end - start);
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        return diffDays + 1; // Include both start and end dates
    }
    
    // ========== HR-SPECIFIC METHODS ==========
    
    async loadHRData() {
        if (this.userRole !== 'hr') return;
        
        try {
            await Promise.all([
                this.loadHRStats(),
                this.loadPendingRequests(),
                this.loadDepartmentStats()
            ]);
        } catch (error) {
            console.error('Error loading HR data:', error);
        }
    }
    
    async loadHRStats() {
        try {
            const response = await fetch(`${this.baseURL}/api/dashboard/hr/employee-stats`);
            const stats = await response.json();
            
            // Update HR-specific stat cards
            document.getElementById('totalEmployeesCount').textContent = stats.totalEmployees;
            document.getElementById('employeesOnLeaveCount').textContent = stats.employeesOnLeave;
            document.getElementById('employeesPresentCount').textContent = stats.employeesPresent;
            document.getElementById('pendingApprovalsCount').textContent = stats.pendingApprovals;
            
        } catch (error) {
            console.error('Error loading HR stats:', error);
        }
    }
    
    async loadPendingRequests() {
        try {
            const response = await fetch(`${this.baseURL}/api/dashboard/hr/pending-requests`);
            const pendingRequests = await response.json();
            
            const container = document.getElementById('pendingRequestsList');
            if (!container) return;
            
            container.innerHTML = '';
            
            if (pendingRequests.length === 0) {
                container.innerHTML = '<p class="no-data">No pending requests</p>';
                return;
            }
            
            pendingRequests.forEach(request => {
                const requestCard = document.createElement('div');
                requestCard.className = 'pending-request-card';
                
                requestCard.innerHTML = `
                    <div class="request-header">
                        <h4>${request.employeeName}</h4>
                        <span class="request-date">${this.formatDate(request.startDate)} - ${this.formatDate(request.endDate)}</span>
                    </div>
                    <div class="request-details">
                        <p><strong>Type:</strong> ${request.leaveType}</p>
                        <p><strong>Duration:</strong> ${request.duration} days</p>
                        <p><strong>Reason:</strong> ${request.reason}</p>
                    </div>
                    <div class="request-actions">
                        <button class="action-btn approve-btn" onclick="dashboard.updateLeaveStatus('${request.id}', 'approve')">
                            ✅ Approve
                        </button>
                        <button class="action-btn reject-btn" onclick="dashboard.updateLeaveStatus('${request.id}', 'reject')">
                            ❌ Reject
                        </button>
                    </div>
                `;
                
                container.appendChild(requestCard);
            });
            
        } catch (error) {
            console.error('Error loading pending requests:', error);
        }
    }
    
    async loadDepartmentStats() {
        try {
            const response = await fetch(`${this.baseURL}/api/dashboard/hr/department-stats`);
            const stats = await response.json();
            
            const container = document.getElementById('departmentStats');
            if (!container) return;
            
            container.innerHTML = '';
            
            const departmentLeaves = stats.departmentLeaves;
            
            Object.entries(departmentLeaves).forEach(([department, leaves]) => {
                const deptCard = document.createElement('div');
                deptCard.className = 'department-card';
                
                deptCard.innerHTML = `
                    <div class="dept-name">${department}</div>
                    <div class="dept-leaves">${leaves} days</div>
                    <div class="dept-bar">
                        <div class="dept-bar-fill" style="width: ${(leaves / 20) * 100}%"></div>
                    </div>
                `;
                
                container.appendChild(deptCard);
            });
            
        } catch (error) {
            console.error('Error loading department stats:', error);
        }
    }
}

// Initialize dashboard when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.dashboard = new LeaveDashboard();
});

// Make dashboard available globally for button onclick handlers
window.dashboard = null;

// Global logout function
function logout() {
    if (confirm('Are you sure you want to logout?')) {
        window.location.href = '/logout';
    }
}