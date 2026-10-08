# Project Context: Personal Finance Manager (Syfe Backend Intern assignment)

## Stack (verify in pom.xml)
- Java 21, Maven, Spring Boot 3.5.7 (must stay 3.x)
- Spring Web, Spring Data JPA, Spring Security (session + cookie), Validation
- H2 in-memory by default. Postgres only via the optional git-ignored `local` profile.
- Tests: JUnit 5, Mockito, Spring Boot Test, JaCoCo (80% line coverage target)
- No Lombok. Plain Java and records for DTOs.

## Base package
`com.shivanshu.personal_finance_manager`
Layer-based packages as defined in ARCHITECTURE.md: config, controller, service
(+ impl, rule, progress, report), repository, specification, mapper, entity,
dto (request/response), exception, security, util.

## Docs to read first (in this order)
1. SPEC.md          - API contract (endpoints, fields, status codes)
2. RULES.md         - business and validation rules
3. ARCHITECTURE.md  - layers, packages, SOLID design
4. IMPLEMENTATION.md - phase-by-phase build order
5. financial_manager_tests.sh (if present) - final authority on status codes and
   field names; wins over any doc that disagrees.

## Rules for the agent
- Write source files only (src/main, src/test). Do NOT run mvn/gradle, download
  dependencies, start the app, or run git commands. I compile and test in IntelliJ
  and paste errors back.
- Do not edit pom.xml or application.properties without asking me.
- One phase per request. Do not touch files outside the current phase.
- Never hardcode secrets; never read or print application-local.properties.
- Add class-level JavaDoc to public classes and key public methods.
- If docs conflict with each other or the test script, tell me before choosing.

## Config
- application.properties reads env vars with defaults (PORT, DB_URL, DB_USER,
  DB_PASSWORD, COOKIE_SECURE, APP_TIMEZONE). Default timezone Asia/Kolkata.

## Deployment
Docker on Render with Java 21 images. Final check: financial_manager_tests.sh
(86 tests) against the live URL.