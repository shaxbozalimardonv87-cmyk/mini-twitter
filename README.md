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

Six tests cover servlet input validation, form forwarding, session-user attribution, persistence CRUD, and a complete HTTP workflow on embedded Tomcat with an isolated H2 test database. The HTTP test registers and logs in users, creates and renders a post, checks escaped HTML and Uzbek text, edits and deletes it, and rejects changes by another user. Tests use neither your PostgreSQL database nor real user data.

## Project scope

This is an educational application with a working core posting workflow. Password hashing, CSRF protection, and versioned database migrations remain production-hardening work. Local Docker configuration binds the app to `127.0.0.1`; it is intended for development and demonstrations.

## Author

**Shahbozjon Alimardonov** — Java Backend Developer

[GitHub](https://github.com/shaxbozalimardonv87-cmyk) · [LinkedIn](https://www.linkedin.com/in/shaxboz-alimardonov-774303381/) · [Email](mailto:shaxbozalimardonv87@gmail.com)
