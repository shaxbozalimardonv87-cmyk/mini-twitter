# Mini Twitter — Java Web Application

**A server-rendered social posting project built with Jakarta Servlets, JSP, Hibernate, and PostgreSQL.**

Developed as a Java web development course project. It demonstrates HTTP request handling, session authentication, relational persistence, and separating web handlers from database access. The repository retains its original name, `exam_for_module_7`.

## Technology stack

| Layer | Technology |
| --- | --- |
| Language | Java 17 |
| Web | Jakarta Servlet 6.0, JSP, JSTL |
| Persistence | Jakarta Persistence 3.1, Hibernate ORM 6.5 |
| Database | PostgreSQL |
| Build | Maven, WAR packaging |

## Implemented components

- Registration, login, logout, and HTTP session handling.
- A home feed ordered by post creation date.
- A personal posts page for the signed-in user.
- Post creation, editing, and deletion with ownership checks and server-side input validation.
- An authentication filter protecting user-specific routes.
- UTF-8 JSP pages with escaped user-generated content, plus `User` and `Post` entities persisted through DAO classes.

## Request flow

```text
Browser → Authentication filter → Servlet → DAO / JPA → PostgreSQL
                                    ↓
                                 JSP view
```

## Explore the source

```text
src/main/java/uz/mini/twitter/
├── dao/        Database operations
├── entity/     User and Post entities
├── filter/     Session authentication filter
├── servlet/    HTTP handlers
└── util/       Persistence setup
src/main/resources/META-INF/persistence.xml
src/main/webapp/                    JSP views
```

## Run locally

### Docker Compose

With Docker and Compose installed:

```bash
git clone https://github.com/shaxbozalimardonv87-cmyk/exam_for_module_7.git
cd exam_for_module_7
docker compose up --build
```

Open **http://localhost:8080**. Compose starts PostgreSQL, waits for database readiness, builds the WAR with Java 17, and deploys it to Tomcat 10.1. Register an account, log in, select **Add Post**, and manage your posts under **My Posts**.

`docker compose down` stops the application. Database contents remain in the named volume. The Compose password is a local development default; set `DB_PASSWORD` in your environment to change it.

### Existing PostgreSQL / Tomcat

Use JDK 17, Maven, PostgreSQL, and a Jakarta Servlet 6.0-compatible container such as Tomcat 10.1. Create a database, then configure the **Tomcat process environment**:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/mini_twitter
export DB_USERNAME=postgres
export DB_PASSWORD=your-local-password
mvn clean package
```

Deploy `target/mini-twitter.war` and open `http://localhost:8080/mini-twitter/`. The default database name is `mini_twitter`; no custom schema is required. Hibernate creates missing tables and uses `update` rather than recreating tables on every startup. For a managed database, set `DB_SCHEMA_ACTION=validate` after provisioning its schema.

## Tests

```bash
mvn test
```

Eight tests cover servlet input validation, form forwarding, session-user attribution, persistence CRUD, and a complete HTTP workflow on embedded Tomcat with an isolated H2 test database. The HTTP test registers and logs in users, creates and renders a post, checks escaped HTML and Uzbek text, edits and deletes it, and rejects changes by another user. Security checks also reject missing, incorrect, cross-session, and stale CSRF tokens and verify salted password hashes and legacy-account migration. Tests use neither your PostgreSQL database nor real user data.

## Authentication and CSRF protection

- Passwords use PBKDF2-HMAC-SHA256 with 600,000 iterations, a fresh 128-bit salt per password, and constant-time hash comparison. Raw passwords are not stored in newly registered accounts or kept in the HTTP session.
- A central filter checks a cryptographically random session-bound CSRF token on every unsafe request, including login and registration. HTML forms submit `_csrf`; API-style clients may use `X-CSRF-Token`.
- Successful login changes the session ID and rotates the CSRF token. Delete and logout accept POST only; GET/HEAD do not mutate data.
- At startup, the application upgrades previous plaintext records before serving traffic. The nullable `users.password_hashed` marker distinguishes old records from encoded hashes, including legacy passwords that happen to look like a hash. Repeated startup does not rehash migrated records. Back up existing data before upgrading; do not run the old plaintext-writing version alongside this version.
- With `DB_SCHEMA_ACTION=validate`, provision the new marker column before deploying: `ALTER TABLE users ADD COLUMN IF NOT EXISTS password_hashed boolean;`. The local default `update` mode adds it automatically.

Design references: [OWASP password storage](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html) and [OWASP CSRF prevention](https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html).

## Docker + PostgreSQL verification

After starting a disposable Compose deployment, run the reproducible HTTP/database check:

```bash
docker compose -p mini-twitter-security-check up --build -d
python3 scripts/docker_smoke_test.py
```

The script only accepts a localhost endpoint. It creates disposable accounts in the named test deployment, verifies stored password hashes, tests CSRF failures and posting, restarts the application to verify persistent PostgreSQL data, and checks logout. It never uses real user accounts.

## Project scope

This is an educational application with a working core posting workflow. Versioned database migrations, rate limiting and production operational controls remain deployment work. Local Docker configuration binds the app to `127.0.0.1`; it is intended for development and demonstrations.

## Author

**Shahbozjon Alimardonov** — Java Backend Developer

[GitHub](https://github.com/shaxbozalimardonv87-cmyk) · [LinkedIn](https://www.linkedin.com/in/shaxboz-alimardonov-774303381/) · [Email](mailto:shaxbozalimardonv87@gmail.com)
