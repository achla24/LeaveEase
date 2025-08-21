LeaveEase – Smart Leave Management Platform

A modern, AI-inspired leave management platform designed for organizations. LeaveEase streamlines leave applications, approvals, and tracking with role-based dashboards, secure authentication, and real-time analytics.

✨ Features
🗓️ Leave Management

Apply for Leave: Intuitive and user-friendly leave application process

Approval Workflow: Manager and admin approval flows with status tracking

Leave Balance Tracking: Real-time updates for each employee

Role-Based Dashboards: Tailored dashboards for employees, managers, and admins

🔒 Security & Privacy

Authentication: Secure login and session management

Role-Based Access: Fine-grained access control for multiple user roles

MongoDB Integration: Scalable, secure data storage with reliability

📊 Analytics & Reporting

Leave History: Complete records of applications and approvals

Admin Analytics: Insights into leave patterns and team availability

Export Reports: Download leave reports for compliance and record-keeping

📄 Documentation & Support

Comprehensive Guides: Documentation for employees and admins

Support: Email and GitHub issue tracker for help and feedback

🛠️ Technology Stack

Frontend: React 19, JavaScript, CSS

Backend: Spring Boot (Java), RESTful APIs

Database: MongoDB

Authentication: Spring Security / JWT

Deployment: Vercel (Frontend), Render/Heroku (Backend)

🚀 Getting Started
Prerequisites

Node.js 18+

Java 17+

MongoDB instance (local or cloud)

Installation

Clone the Repository:

git clone <repository-url>
cd LeaveEase


Frontend Setup:

cd frontend
npm install


Backend Setup:

cd backend
# Using Maven Wrapper
./mvnw clean package
# Or with Maven installed
mvn clean package

⚙️ Environment Setup
Backend

Configure MongoDB URI and environment variables in application.properties

Frontend

Update API base URLs in React to point to your backend deployment

💻 Running Locally

Start Backend:

cd backend
java -jar target/<your-backend-jar>.jar


Start Frontend:

cd frontend
npm start


Visit: http://localhost:3000

📦 Deployment
Frontend (Vercel)

Push the frontend to GitHub

Import repository into Vercel

Build command:

npm run build


Output directory:

build

Backend (Render/Heroku)

Import repo into Render
 or Heroku

Configure build/start commands as required

Add environment variables (MongoDB URI, secrets, etc.)

📂 Project Structure
LeaveEase/
├── backend/          # Spring Boot backend
│   └── src/
│       └── main/
│           ├── java/
│           └── resources/
├── frontend/         # React frontend
│   ├── src/
│   └── public/
└── README.md

🔗 API Endpoints

POST /api/leaves/apply – Apply for leave

GET /api/leaves/history – Fetch leave history

POST /api/leaves/approve – Approve/reject leave (manager/admin)

GET /api/dashboard – Dashboard data

🔐 Security Features

Role-based authentication & authorization

Secure password storage

Input validation & error handling

Session security (JWT / Spring Security)

🤝 Contributing

Fork the repo

Create a feature branch

Make changes

Add tests if applicable

Submit a PR

📄 License

This project is licensed under the MIT License – see the LICENSE file for details.

📬 Support

Email: support@leaveease.com

Issues: GitHub Issues
