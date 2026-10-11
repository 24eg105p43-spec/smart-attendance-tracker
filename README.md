# Smart Attendance Tracker 🎓

A full-stack, intelligent student attendance tracking and bunk-management system built with **Spring Boot 4**, **MySQL**, and **React 19 (Vite)**.

---

## 🌟 Key Features

- 🔐 **Secure Authentication**: User registration and login using BCrypt password hashing & Stateless JWT authentication tokens.
- 📚 **Per-User Course Management**: Add, customize, and manage subjects with tailored minimum attendance criteria (default 75%).
- ⚡ **Instant Attendance Marking**: 1-click `Present` / `Absent` tracking with instant database persistence.
- 🧮 **Smart Attendance & Bunk Engine**:
  - Real-time calculation of overall percentage and `SAFE` vs `LOW` status.
  - **Bunk Buffer**: Exact count of classes you can safely skip while remaining above criteria.
  - **Recovery Streak**: Exact number of consecutive classes you must attend to climb back above threshold.
- 🌴 **Academic Calendar & Holidays**:
  - Track official holidays and personal leaves.
  - Excludes weekends and holidays when calculating remaining active semester lectures.
- 🔮 **Semester Projection & Early-Warning Alerts**:
  - Semester countdown until semester end date.
  - Flags **Detention Risks**, **Low Attendance Warnings**, and **Tight Bunk Buffers**.
- 🤖 **AI Attendance Advisor & Chatbot**:
  - In-app intelligent advisor answering student questions like *"Can I bunk tomorrow?"*, *"What is my semester projection?"*, and providing recovery advice.
- 📥 **Report Export**: Instant CSV download of attendance records, percentages, and statuses for college submission.
- 🐳 **Container Ready**: Full Docker and `docker-compose` orchestration for MySQL, Spring Boot, and Nginx/React.

---

## 🏗️ Architecture & Tech Stack

- **Backend**:
  - Java 21 & Spring Boot 4
  - Spring Security 6 + JJWT (Stateless JWT token filter)
  - Spring Data JPA & Hibernate
  - MySQL 8.0
  - JUnit 5 for unit test suites
- **Frontend**:
  - React 19 + Vite
  - Responsive dark-mode UI with real-time reactive stats
- **DevOps**:
  - Multi-stage Docker builds
  - Docker Compose orchestration

---

## 🚀 Getting Started

### Option 1: Running with Docker (Recommended)

Make sure you have Docker installed and running, then execute:

```bash
docker compose up --build
```

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **MySQL**: localhost:3306

---

### Option 2: Running Locally

#### 1. Backend Setup
1. Ensure MySQL is running and database `attendance_tracker` exists:
   ```sql
   CREATE DATABASE attendance_tracker;
   ```
2. Configure credentials in `backend/src/main/resources/application.properties` (defaults to root/root123).
3. Start the backend:
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```
   The backend will run on `http://localhost:8080`.

#### 2. Frontend Setup
1. Install dependencies and start the Vite dev server:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   The frontend will run on `http://localhost:5173` with proxy set up to `http://localhost:8080`.

---

## 🧪 Running Tests

To run the automated test suite for the attendance calculation engine:

```bash
cd backend
./mvnw test
```

---

## 📡 Key API Endpoints

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Register a new user |
| `POST` | `/api/auth/login` | Login and receive JWT token |
| `GET` | `/api/subjects` | List subjects for authenticated user |
| `POST` | `/api/subjects` | Add a new course with target percentage |
| `DELETE` | `/api/subjects/{id}` | Delete subject and its attendance history |
| `POST` | `/api/attendance/{subjectId}?present=true` | Mark attendance record |
| `GET` | `/api/subjects/{id}/summary` | Get attendance percentage, skip buffer & recovery streak |
| `GET` | `/api/semester/summary` | Get semester countdown, projections, and warning alerts |
| `GET` | `/api/holidays` | List academic holidays |
| `POST` | `/api/holidays` | Add a holiday or planned leave date |
| `POST` | `/api/ai/chat` | AI Advisor prompt and conversational insights |
| `GET` | `/api/reports/csv` | Download CSV attendance report |