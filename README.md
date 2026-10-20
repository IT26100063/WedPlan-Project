<div align="center">
<img src="src/main/resources/static/img/hero-couple.jpg" alt="Everlune wedding couple hero image" width="100%" />
✨ Everlune
Your love story. Beautifully planned.
Wedding Planning & Vendor Booking System
Discover wedding vendors, organize the details, and bring your dream celebration to life — all in one place.
<br />
![Java](https://img.shields.io/badge/Java-Spring%20Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Frontend](https://img.shields.io/badge/Frontend-HTML%20%7C%20CSS%20%7C%20JavaScript-E8A99A?style=for-the-badge)
![Database](https://img.shields.io/badge/Database-MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Status](https://img.shields.io/badge/Status-In%20Development-C58D83?style=for-the-badge)
</div>
---
💍 About the Project
Everlune is a wedding planning and vendor booking web application designed to help couples plan their special day with less stress. The system aims to bring wedding inspiration, vendor discovery, and booking management together in a single, easy-to-use platform.
The project uses a Spring Boot application structure with static frontend assets and is intended to connect to a MySQL database for persistent application data.
✨ Key Features
Wedding-focused homepage with a romantic hero image and responsive layout.
Vendor discovery for wedding services such as venues, photography, catering, florals, and décor.
Vendor management to organize vendor information and service details.
Booking workflow designed to support booking requests and status tracking.
User management for account-related functionality.
Admin capabilities planned for managing users and platform content.
Responsive interface for desktop and mobile screen sizes.
> Feature availability depends on the controllers, services, frontend pages, and database integration implemented in the current project version.
🛠️ Technology Stack
Technology	Purpose
Java	Main backend programming language
Spring Boot	Web application and REST API framework
Spring Data JPA	Database persistence and repository layer, if configured
MySQL	Relational database
HTML5	Frontend structure
CSS3	Styling and responsive layouts
JavaScript	Client-side interactions
Maven	Dependency management and project build, if using Maven
📁 Project Structure
```text
Wed PLan/
├── src/
│   └── main/
│       ├── java/
│       │   └── ...                 # Spring Boot source code
│       └── resources/
│           ├── application.properties
│           └── static/
│               ├── css/
│               ├── js/
│               └── img/
│                   └── hero-couple.jpg
├── pom.xml
└── README.md
```
Your actual folder names may differ. Keep the hero image at `src/main/resources/static/img/hero-couple.jpg` so the README banner path works.
🚀 Getting Started
Prerequisites
Install the following tools before running the project:
Java JDK version compatible with your `pom.xml`
Maven, or use the Maven Wrapper if included
MySQL Server
IntelliJ IDEA, Eclipse, or another Java IDE
1. Clone the repository
Replace the example URL with your actual GitHub repository URL:
```bash
git clone https://github.com/IT26100063/WedPlan-Project.git
cd WedPlan-Project
```
Alternatively, open your existing `Wed PLan` project folder in your IDE.
2. Configure the database
Create a MySQL database for the application. For example:
```sql
CREATE DATABASE wedplan;
```
Then update `src/main/resources/application.properties` with your local database settings. Example configuration:
```properties
spring.application.name=Everlune

spring.datasource.url=jdbc:mysql://localhost:3306/wedplan?useSSL=false&serverTimezone=UTC
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```
Security note: Set your database password through an environment variable or local configuration. Do not commit real passwords, API keys, or other secrets to GitHub. Adjust the JPA settings to match your project requirements.
3. Run the application
From the project root, run:
```bash
mvn spring-boot:run
```
Or run the main class annotated with `@SpringBootApplication` from your IDE.
When the application starts successfully, open:
```text
http://localhost:8080
```
Spring Boot serves files from `src/main/resources/static`. If the homepage file is `index.html` in that folder, it is typically available at the root URL. Otherwise, open the route configured by your application.
🖼️ Hero Image
The README banner uses the image already in your project:
```text
src/main/resources/static/img/hero-couple.jpg
```
GitHub resolves the relative image path from the repository root. Commit the image along with `README.md` to make it visible in the repository.
🔧 Development Notes
Keep frontend assets inside Spring Boot's `src/main/resources/static/` directory when serving them as static resources.
Use relative asset paths such as `/img/hero-couple.jpg` from pages served by Spring Boot.
Keep controller, service, repository, and model responsibilities separated.
Validate user input and protect endpoints that require authentication.
Use environment variables or an untracked local configuration file for secrets.
🗺️ Future Improvements
[ ] Complete customer registration and secure login
[ ] Add vendor profiles, categories, galleries, and service pricing
[ ] Add search and filtering by location, category, and date
[ ] Implement booking requests, availability, and status tracking
[ ] Create customer, vendor, and administrator dashboards
[ ] Add favourites and wedding planning checklists
[ ] Add reviews and ratings for vendors
[ ] Integrate email notifications and secure payments
[ ] Add automated tests and deployment configuration
⚠️ Project Status
Everlune is under development. Confirm which features are implemented in the current branch before treating them as production-ready. Real authentication, booking availability, payments, and vendor data require backend implementation and testing.
🤝 Contributing
Create a feature branch.
Make your changes and test them locally.
Commit your changes with a clear message.
Push the branch and open a pull request for review.
📄 License
No license has been specified in this repository yet. Add a `LICENSE` file if you intend to distribute or reuse the project publicly.
---
<div align="center">
Made with 🤍 for life's most beautiful beginning.
Everlune — every detail, a little more magical.
</div>