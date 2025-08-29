# HealthRX Spring Boot Webhook SQL (Even RegNo)

A no-endpoint Spring Boot app that, **on startup**:
1. Calls `POST https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA` with your details.
2. Receives a `webhook` URL and an `accessToken` (JWT).
3. Selects **Question 2 (EVEN)** and prepares the final SQL.
4. Sends `{ "finalQuery": "<SQL>" }` to **both** the returned `webhook` URL and the fixed
   `POST https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA` using the JWT in the `Authorization` header.

> No controller triggers this — it's done by an `ApplicationRunner` on startup.
> a) App Startup
:: Spring Boot ::  (v3.3.3)
2025-08-29T11:30:02.123  INFO 1056 --- [           main] com.example.hiring.Application           : Starting Application v1.0.0
2025-08-29T11:30:02.987  INFO 1056 --- [           main] com.example.hiring.StartupRunner         : Starting flow for candidate: John Doe (regNo: REG12348, email: john@example.com)

b) Webhook Generation
2025-08-29T11:30:04.102  INFO 1056 --- [           main] com.example.hiring.StartupRunner         : Received webhook: https://bfhldevapigw.healthrx.co.in/hiring/webhook/abcd1234
2025-08-29T11:30:04.105  INFO 1056 --- [           main] com.example.hiring.StartupRunner         : Final SQL prepared (410 chars)

c) Posting SQL to Webhook
2025-08-29T11:30:05.456  INFO 1056 --- [           main] com.example.hiring.client.WebhookClient  : Submission to https://bfhldevapigw.healthrx.co.in/hiring/webhook/abcd1234 response: {"status":"success","message":"SQL received"}
2025-08-29T11:30:05.987  INFO 1056 --- [           main] com.example.hiring.client.WebhookClient  : Submission to https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA response: {"status":"success","message":"SQL validated"}

d) App Exit
2025-08-29T11:30:06.001  INFO 1056 --- [           main] com.example.hiring.StartupRunner         : Done.


## Final SQL (Question 2: "Younger employees in same department")
```sql
SELECT
  e.emp_id,
  e.first_name,
  e.last_name,
  d.department_name,
  (
    SELECT COUNT(*)
    FROM employee e2
    WHERE e2.department = e.department
      AND e2.dob > e.dob
  ) AS younger_employees_count
FROM employee e
JOIN department d
  ON d.department_id = e.department
ORDER BY e.emp_id DESC;
```

This counts how many coworkers **younger** than each employee are in the **same department** (`e2.dob > e.dob`), and orders by `emp_id` descending.

## Run in VS Code
- Prereqs: Java 17+, Maven 3.9+ (or use VS Code Java extensions which bundle Maven).
- Configure your candidate details in `src/main/resources/application.yml`.
- Run:
```bash
mvn -q -DskipTests spring-boot:run
```
or build a jar:
```bash
mvn -q -DskipTests clean package
java -jar target/healthrx-springboot-webhook-sql-1.0.0.jar
```

## What gets posted
- **Authorization header**: by default set to the token **as-is** (toggle `app.bearerPrefix` to true if API needs `Bearer <token>`).
- **Body**:
```json
{"finalQuery": "SELECT ... ORDER BY e.emp_id DESC;"}
```

## Project Structure
- `Application.java` — Spring Boot entry
- `StartupRunner.java` — Executes the flow on startup
- `client/` — WebClient config and API DTOs
- `sql/` — Chooses the SQL based on regNo parity
