# WedPlan

WedPlan is a Java 17 / Spring Boot 4.1.1 Maven application using Spring MVC, Thymeleaf, Spring Data JPA, Bean Validation, and MySQL.

## Payment Management

The payment module is available at `http://localhost:8080/payments`. It supports creating, viewing, editing, deleting, searching, filtering, and paginating stored payment records. Dashboard counts are calculated from the database.

Payment records are administrative records only. The application does not initiate, process, or verify transactions with a financial provider. Verify a receipt or provider transaction before changing a record's status to `COMPLETED`. Card numbers, CVVs, and other payment credentials are not collected or stored.

Booking Management currently contains only a stub entity, so payments use a unique `bookingReference` text field rather than an invented or duplicate Booking relationship. This can be connected to the real Booking entity when that module is implemented.

## MySQL configuration

Create the database if it does not already exist:

```sql
CREATE DATABASE wedplan;
```

The datasource URL defaults to `jdbc:mysql://localhost:3306/wedplan`. To override it, set `DB_URL`. Set `DB_USERNAME` to a MySQL account with access to `wedplan`, and set `DB_PASSWORD` in your environment before starting the app. Credentials are not stored in the project.

### Windows PowerShell

Set the values in the terminal you will use to start the application (replace the password placeholder with your local MySQL password):

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/wedplan"
$env:DB_USERNAME = "your-mysql-username"
$env:DB_PASSWORD = "your-local-mysql-password"
.\mvnw.cmd spring-boot:run
```

### VS Code

1. Open the project root in VS Code and install/use the Java and Spring Boot extensions.
2. In the integrated PowerShell terminal, set `DB_USERNAME` and `DB_PASSWORD` as shown above. Set `DB_URL` too if you are not using the default URL.
3. Run `.\mvnw.cmd spring-boot:run`.
4. Open `http://localhost:8080/payments`.

### IntelliJ IDEA

1. Open the project root and allow IntelliJ to import the Maven project.
2. Open **Run | Edit Configurations**, add a **Spring Boot** configuration for `com.wedplan.wedplan.WedPLanApplication`.
3. Add `DB_USERNAME` and `DB_PASSWORD` (and optionally `DB_URL`) under the run configuration's environment variables. Do not put credentials in source-controlled files.
4. Run the configuration and open `http://localhost:8080/payments`.

Alternatively, run the Maven wrapper in IntelliJ's terminal after setting the environment variables in that terminal.

## Build and test

On Windows, run:

```powershell
.\mvnw.cmd test
```

The application uses `spring.jpa.hibernate.ddl-auto=update` to create/update the payment table. MySQL must be running and the datasource environment variables must be set for application context tests and application startup.
