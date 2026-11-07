# 💍 WedPlan - Wedding Planning & Vendor Booking Platform

![WedPlan Banner](src/main/resources/static/images/hero-wedding.jpg)

## 📌 Overview

**WedPlan** is a modern web-based wedding planning and vendor booking platform designed to simplify the wedding preparation process.

The system allows users to discover wedding vendors, explore packages and services, make bookings, manage payments, and receive a smooth wedding planning experience. Administrators can manage users, vendors, services, bookings, and system activities through a centralized management system.

---

# ✨ Key Features

## 👤 User & Admin Management
- User registration and authentication
- User profile management
- Admin management
- Role-based access control
- Secure login system

## 💐 Vendor Management
- Vendor registration and management
- Vendor profile handling
- Manage wedding service providers
- Vendor information display

## 📦 Package & Service Management
- Create wedding packages
- Manage service categories
- Update package details
- View available wedding services

## 📅 Booking Management
- Book wedding services
- Manage booking details
- Track booking status
- Booking history management

## 💳 Payment Management
- Manage payment records
- Payment status tracking
- Transaction management

## ⭐ Review & Alert Management
- Customer reviews
- Vendor ratings
- User notifications and alerts

---

# 🏗️ System Architecture

WedPlan follows a layered Spring Boot architecture.

```
WedPlan
│
├── Controller Layer
│   ├── User & Admin Controller
│   ├── Vendor Controller
│   ├── Booking Controller
│   ├── Payment Controller
│   ├── Package Controller
│   └── Review Controller
│
├── Service Layer
│   └── Business Logic Handling
│
├── Repository Layer
│   └── Database Communication
│
├── Model Layer
│   └── Entity Classes
│
└── Config
    └── Security Configuration
```

---

# 🛠️ Technologies Used

## Backend

| Technology | Description |
|---|---|
| Java | Programming Language |
| Spring Boot | Backend Framework |
| Spring Data JPA | Database Operations |
| Hibernate | ORM Framework |
| Spring Security | Authentication & Authorization |
| Maven | Dependency Management |

## Database

| Technology | Description |
|---|---|
| MySQL | Relational Database |
| MySQL Connector | Database Driver |

## Frontend

| Technology | Description |
|---|---|
| HTML5 | Page Structure |
| CSS3 | Styling |
| JavaScript | Client-side Interaction |

---

# ⚙️ Requirements

Before running the project, install:

- Java JDK 17+
- Maven
- MySQL Server
- IntelliJ IDEA (Recommended)

---

# 🗄️ Database Configuration

Create a MySQL database:

```sql
CREATE DATABASE wedplan;
```

Update:

```
src/main/resources/application.properties
```

Example:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/wedplan
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

---

# 🚀 How to Run the Project

## 1. Clone Repository

```bash
git clone https://github.com/IT26100063/WedPlan.git
```

---

## 2. Open Project

Open the project using IntelliJ IDEA.

---

## 3. Install Dependencies

Run:

```bash
mvn clean install
```

---

## 4. Start Application

Run:

```
WedPlanApplication.java
```

The application will start at:

```
http://localhost:8080
```

---

# 🌳 Git Branch Structure

The project uses feature-based branches:

```
main
│
├── User-and-admin-Management
│
├── Booking-Management
│
├── Package-and-Service-Management
│
├── Payment-Management
│
├── Review-and-Alert-Management
│
└── Vendor-Management
```

Each team member develops features separately and merges changes through Pull Requests.

---

# 👨‍💻 Development Team Workflow

1. Create feature branch

```bash
git checkout -b feature-name
```

2. Add changes

```bash
git add .
```

3. Commit changes

```bash
git commit -m "Added new feature"
```

4. Push branch

```bash
git push origin feature-name
```

5. Create Pull Request on GitHub

---

# 🔐 Security

WedPlan includes:

- Spring Security configuration
- User authentication
- Authorization management
- Protected backend endpoints

---

# 📂 Project Structure

```
src
│
└── main
    │
    ├── java
    │   └── com.wedplan.wedplan
    │       │
    │       ├── Config
    │       ├── controller
    │       ├── model
    │       ├── repository
    │       └── services
    │
    └── resources
        │
        ├── static
        │   ├── css
        │   ├── js
        │   └── images
        │
        └── templates
```

---

# 📸 Screenshots

(Add application screenshots here)

---

# 🔮 Future Improvements

- Online payment gateway integration
- Mobile application support
- AI wedding planning assistant
- Vendor recommendation system
- Real-time booking notifications

---

# 📜 License

This project is developed for academic purposes.

---

# 💖 Developed By

**WedPlan Development Team**

Wedding Planning & Vendor Booking Platform 🚀
