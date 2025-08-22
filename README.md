# LeaveEase — Modern Leave Management System

A modern, role-based leave management platform for HR teams and employees with smart email workflows, analytics, and optional AI assistance.

## 🚀 Features

### 👤 Role-Based Dashboards

#### HR Panel
- Approve/reject leave requests with reasons
- View pending approvals, presence, and leave statistics
- Manage late attendance records

#### Employee Panel
- Submit leave requests
- Track status and history
- View personal attendance insights

### 🔐 Authentication & Access
- Spring Security with role-based access control (HR, Employee)
- Secure endpoints and session handling
  
### ✉️ Email & AI Workflows
- SMTP Email via Gmail(configurable)
- AI-Generated Emails via OpenAI or local AI (Ollama)
- N8N Workflow Integration for notifications
- Smart, configurable templates for HR and employee communications

### 📦 Data & Integrations
- MongoDB persistence with auto-seeded demo data
- WebSocket support for interactive features (AI chat)
- RESTful APIs for extensibility
