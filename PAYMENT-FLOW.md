# WedPlan booking and payment flow

This module stores booking requests and payment records in the existing MySQL database. Card payment is hosted by PayHere; bank transfers are reviewed manually. WedPlan does not store card numbers or CVVs and does not claim a payment is complete until a signed PayHere notification is verified or an administrator confirms receipt of bank funds.

The current homepage lists illustrative vendors because the vendor directory is not yet connected to a persistent Vendor entity. Booking submissions are validated against those displayed listings; connect this to the Vendor module when that module is available.

## Requirements and database

- Java 17
- MySQL running locally
- A database schema named `wedplan`

Create the schema if needed:

```sql
CREATE DATABASE wedplan CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Hibernate updates the tables from the JPA entities at application startup. Existing schema/data are retained (`spring.jpa.hibernate.ddl-auto=update`).

## Configure and run on Windows

The easiest option is to open PowerShell in the project directory and run the local launcher. It reuses the datasource settings saved in this project's earlier application configuration, without printing them. If no saved MySQL login is available, it prompts for one with hidden password input. It prompts separately for a WedPlan admin login, and selects an available port if an older app is still using 8080:

```powershell
powershell -ExecutionPolicy Bypass -File .\run-wedplan.ps1
```

Alternatively, set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `WEDPLAN_ADMIN_USERNAME`, and `WEDPLAN_ADMIN_PASSWORD` in the VS Code Java launch configuration or IntelliJ Run Configuration environment-variable field, then start the app. `DB_PASSWORD` is required and must be provided before starting it.

Open:

- Customer-facing vendor listings: `http://localhost:8080/`
- Admin sign-in: `http://localhost:8080/admin/login`
- Booking queue after sign-in: `http://localhost:8080/admin/bookings`
- Payment dashboard after sign-in: `http://localhost:8080/payments`

Configure admin credentials in the same PowerShell terminal before launch:

```powershell
$env:WEDPLAN_ADMIN_USERNAME = "your-admin-username"
$env:WEDPLAN_ADMIN_PASSWORD = "use-a-long-unique-password"
```

Admin access stays disabled if either value is missing. Use POST sign-out from the admin pages rather than sharing an admin session.

## Customer-to-admin workflow

1. A customer selects a vendor and submits the reservation form. WedPlan saves the request in `bookings` and returns a private reservation URL containing a random booking reference.
2. An administrator reviews the request at `/admin/bookings/<reference>` and sets the payable amount. The customer revisits the private URL and selects a payment method.
3. For card checkout, WedPlan creates a pending payment and posts the customer to PayHere's hosted checkout. The application only marks it completed after validating PayHere's signed server notification, matching order, amount, and currency.
4. For bank transfer, the customer submits the bank reference and a PDF/JPEG/PNG receipt (maximum 5 MB). The receipt is stored outside the public static directory. The administrator opens the linked payment record, reviews the private receipt, and verifies funds received or rejects it. Rejecting clears the pending receipt so the customer can submit again.
5. The payment dashboard shows real payment records and aggregate counts from MySQL. A provider return/cancel redirect alone never confirms a card payment.

For bank transfers, configure these optional variables before launch:

```powershell
$env:WEDPLAN_BANK_NAME = "Your bank"
$env:WEDPLAN_BANK_ACCOUNT_NAME = "Account holder"
$env:WEDPLAN_BANK_ACCOUNT_NUMBER = "Account number"
$env:WEDPLAN_BANK_BRANCH = "Branch"
```

Transfer receipts default to `.\var\payment-receipts` (outside public resources). Set `RECEIPT_STORAGE_DIR` to a protected persistent directory when deploying.

## PayHere sandbox

Card checkout is disabled unless sandbox merchant credentials and a public HTTPS callback base URL are configured:

```powershell
$env:PAYHERE_MERCHANT_ID = "your-sandbox-merchant-id"
$env:PAYHERE_MERCHANT_SECRET = "your-sandbox-merchant-secret"
$env:WEDPLAN_PUBLIC_BASE_URL = "https://your-public-https-tunnel"
$env:PAYHERE_CHECKOUT_URL = "https://sandbox.payhere.lk/pay/checkout"
```

The PayHere `notify_url` is `<WEDPLAN_PUBLIC_BASE_URL>/payhere/notify`; PayHere must be able to reach it over HTTPS. A localhost URL is not sufficient for callbacks. Keep the sandbox URL for testing and use production PayHere credentials/endpoints only after the merchant account is approved and deployment is configured securely. Never enter real card data into WedPlan itself.

## Inspect the database

Connect with MySQL Workbench or the MySQL command line using the same database credentials, then query:

```sql
USE wedplan;
SELECT booking_reference, customer_name, vendor_name, status, quoted_amount, created_at
FROM bookings ORDER BY created_at DESC;
SELECT booking_reference, customer_name, amount, currency, payment_method, payment_status,
       transaction_reference, payment_date
FROM payments ORDER BY payment_date DESC, id DESC;
```

Do not query receipt file contents in MySQL; only private storage metadata is held there. Do not manually mark PayHere payments complete in SQL.

## Build and tests

```powershell
.\mvnw.cmd test
```

The signature unit tests do not need MySQL. The existing Spring context test does require a reachable MySQL server and valid `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`; it will fail if those are not available. The application schema and full customer/admin route flow also need to be exercised against the configured database.
