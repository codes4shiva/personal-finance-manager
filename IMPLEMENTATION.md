# Implementation Plan (IMPLEMENTATION.md)

Base package: `com.shivanshu.personal_finance_manager`. Follow ARCHITECTURE.md and
RULES.md. After each step: rebuild in IntelliJ, run the check, `git status`, commit.

## Phase 1: Foundation
1. `exception/`: ApiException (status + factories badRequest/unauthorized/forbidden/
   notFound/conflict), ErrorResponse, GlobalExceptionHandler (@RestControllerAdvice:
   ApiException, MethodArgumentNotValidException, HttpMessageNotReadableException,
   MethodArgumentTypeMismatchException, 405, NoResourceFoundException -> 404,
   DataIntegrityViolation -> 409, fallback -> generic 500).
2. `config/ClockConfig` (Clock bean using app.timezone), `util/MoneyUtils`.
3. application.properties already exists (H2 default, env overrides). Keep as is.
   Check: app starts.

## Phase 2: Domain
4. `entity/`: User, Category (userEntity nullable), Transaction, SavingsGoal, CategoryType.
   Column names avoid reserved words (e.g. transaction_date).
5. Repositories: UserRepository, CategoryRepository (accessible-by-name, list
   accessible), TransactionRepository (JpaSpecificationExecutor, sums, category totals,
   existsByCategoryId), SavingsGoalRepository.
6. `DataInitializer` seeds the 7 default categories once (user_id null).
   Check: restart app, 7 defaults exist.

## Phase 3: Security and Auth
7. `security/`: AppUserDetails (carries userEntity id), AppUserDetailsService,
   RestAuthHandlers (JSON 401/403), CurrentUserProvider + implementation.
8. `config/SecurityConfig`: sessions, CSRF off, permit register/login/health, rest
   authenticated, JSON entry point, explicit SecurityContext save on login, BCrypt.
9. DTOs, AuthService + impl, AuthController (register/login/logout), HealthController.
   Check (curl): register 201, duplicate 409, bad login 401, login sets cookie,
   protected URL without cookie 401, logout 200.

## Phase 4: Early deployment
10. Dockerfile (Java 21 images), render.yaml, env COOKIE_SECURE=true.
    Check: live URL /api/health OK and login works over HTTPS.

## Phase 5: Features
11. Categories: DTOs, CategoryMapper, CategoryService + impl, CategoryController.
    Check: defaults listed; custom create/duplicate(409)/delete rules (403/400/404).
12. Transactions: service/rule/ (TransactionRule, NotFutureDateRule,
    CategoryAccessibleRule), specification/TransactionSpecifications, TransactionMapper,
    TransactionService + impl, TransactionController.
    Check: CRUD, filters, newest first, date update -> 400, other userEntity -> 404.
13. Goals: service/progress/ (GoalProgressCalculator, NetSavingsSinceStartCalculator),
    GoalMapper, GoalService + impl, GoalController.
    Check: progress, percentage and remaining match the spec's examples; other userEntity -> 403.
14. Reports: service/report/ (ReportGenerator, Monthly/Yearly generators),
    ReportService, ReportController. Check: totals per category, netSavings, 400 on bad month.

## Phase 6: Verify
15. Run financial_manager_tests.sh locally until 86/86. Fix failures in small commits.
16. Tests: Mockito unit tests (services, rules, calculator, generators) and
    @SpringBootTest + MockMvc flow tests. `mvn clean verify` passes, JaCoCo >= 80%.

## Phase 7: Ship
17. Redeploy, warm up Render, run the script against the live URL, take the screenshot.
18. README: Mermaid architecture diagram, setup, API list, design decisions, how to
    extend (add a rule, calculator, report type). Push and email Fauzia the repo link,
    live URL and screenshot.