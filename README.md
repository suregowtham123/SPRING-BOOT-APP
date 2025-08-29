# HealthRX Spring Boot Webhook SQL (Even RegNo)

A no-endpoint Spring Boot app that, **on startup**:
1. Calls `POST https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA` with your details.
2. Receives a `webhook` URL and an `accessToken` (JWT).
3. Selects **Question 2 (EVEN)** and prepares the final SQL.
4. Sends `{ "finalQuery": "<SQL>" }` to **both** the returned `webhook` URL and the fixed
   `POST https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA` using the JWT in the `Authorization` header.

> No controller triggers this — it's done by an `ApplicationRunner` on startup.

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
