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
- Post editing and deletion handlers with ownership checks.
- An authentication filter protecting user-specific routes.
- JSP pages and navigation, with `User` and `Post` entities persisted through DAO classes.

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

## Local setup

Use JDK 17, Maven, PostgreSQL, and a Jakarta Servlet 6.0-compatible container such as Tomcat 10.1.

1. Clone this repository.
2. Create a local PostgreSQL database and schema. The checked-in configuration targets `library_db` and `books_schema`; adjust them to your environment.
3. Set your local database URL, username, and password in `src/main/resources/META-INF/persistence.xml`. Use a disposable development database: the current configuration enables schema creation.
4. Build the WAR:

```bash
mvn clean package
```

5. Deploy `target/mini-twitter.war` to your servlet container and open its application context, normally `http://localhost:8080/mini-twitter/`.

## Project scope and next steps

This is an educational implementation. Before production use, password hashing, CSRF protection, non-destructive schema migrations, and automated integration tests should be added. The repository includes an add-post form, but the corresponding `/add-post` servlet is not present in the current source tree; that workflow still needs completion.

## Author

**Shahbozjon Alimardonov** — Java Backend Developer

[GitHub](https://github.com/shaxbozalimardonv87-cmyk) · [LinkedIn](https://www.linkedin.com/in/shaxboz-alimardonov-774303381/) · [Email](mailto:shaxbozalimardonv87@gmail.com)
