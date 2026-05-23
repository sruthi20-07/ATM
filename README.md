# 🏦 ATM Management System (Spring Boot + MySQL + OTP)

A secure backend-based ATM Management System built using **Spring Boot**, **MySQL**, and **Twilio Email OTP verification**.  
This project simulates real-world banking operations with authentication and transaction management.

---

## 🚀 Features

- ✅ Create new bank account
- 🔐 Login using PIN
- 📧 Email OTP verification (Twilio)
- 💰 Deposit Money
- 💸 Withdraw Money
- 📊 Balance Inquiry
- 🧾 Transaction History
- 🗄 Persistent storage using MySQL
- 🛡 Secure authentication workflow

---

## 🛠 Tech Stack

- Java
- Spring Boot
- Spring Data JPA
- MySQL
- Twilio Email API (OTP Service)
- REST APIs
- Maven

---

## 🔐 Authentication Flow

1. User enters Account ID + PIN
2. System validates credentials
3. OTP is generated
4. OTP is sent to registered email via Twilio
5. User verifies OTP
6. Access granted to ATM operations

---

## 🧠 Concepts Used

- Object-Oriented Programming (OOP)
- MVC Architecture
- RESTful API Design
- JPA & Hibernate
- Email API Integration
- Exception Handling
- Dependency Injection
- Secure Authentication Logic

---

## ⚙️ How to Run

### 1️⃣ Clone Repository

```bash
git clone https://github.com/your-username/ATM-Management-System-SpringBoot.git
