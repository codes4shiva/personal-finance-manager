# Business & Validation Rules (RULES.md)

## 1. Authentication & User Data
- Registration: username must be a valid email (stored lowercase, unique -> 409 on
  duplicate). Password 8-72 characters (BCrypt). fullName required. phoneNumber
  required and must look like a phone number (optional leading +, 10-20 digits,
  spaces/hyphens allowed).
- Login: wrong credentials, unknown userEntity or blank values -> 401. Success sets a
  session cookie (secure, HTTP-only, SameSite=Lax).
- Logout: invalidates the session. Calling it without a session -> 401.
- Auth is session-based (no JWT). Every endpoint except POST /api/auth/register and
  POST /api/auth/login (plus GET /api/health) requires a valid session -> else JSON 401.

## 2. Data Isolation (status code depends on the resource)
- All queries are scoped by the logged-in userEntity's id.
- Transactions: another userEntity's or missing id -> 404 (no existence leak).
- Goals: id exists but belongs to another userEntity -> 403; id does not exist -> 404.
- Categories: another userEntity's custom category is not visible; treat as 404.

## 3. Transaction Rules
- Fields: amount (required, > 0), date (required, YYYY-MM-DD, not in the future),
  category (required, referenced by NAME, must be a default category or one of the
  userEntity's own), description (optional, max 500).
- Type (INCOME/EXPENSE) is derived from the category, never sent by the client.
- Amounts are BigDecimal, scale 2 (HALF_UP). Never double.
- Update: amount, category and description may change. The date is immutable: if a
  request tries to change it -> 400 with a clear message.
- Read: newest first (date desc, then id desc). Optional filters: startDate, endDate,
  categoryId (also accept category name and type). startDate after endDate -> 400.
  Malformed date or unknown enum value -> 400.
- Delete: hard delete. Goals and reports are computed live, so they exclude it
  immediately.

## 4. Category Rules
- Defaults (cannot be modified or deleted): INCOME: Salary. EXPENSE: Food, Rent,
  Transportation, Entertainment, Healthcare, Utilities.
- Custom: needs name + type (INCOME or EXPENSE). The name must be unique per userEntity,
  case-insensitive, and must not equal a default name -> 409 on conflict.
- Delete by name: default category -> 403; category used by any transaction -> 400;
  not found -> 404.
- GET returns defaults plus the userEntity's custom categories, each with `isCustom`.

## 5. Savings Goal Rules
- Create: goalName required, targetAmount > 0, targetDate must be in the future,
  startDate optional (defaults to today, may be in the past), startDate must not be
  after targetDate.
- Update: only targetAmount and/or targetDate (same validation; at least one field).
- Progress = (total income - total expenses) for transactions on or after startDate.
  Computed on every read, never stored.
- Response: currentProgress (scale 2), progressPercentage = progress / target * 100
  (scale 2, floored at 0), remainingAmount = max(target - progress, 0).
- Each goal is independent. Non-numeric id -> 400.

## 6. Report Rules
- Monthly: GET /api/reports/monthly/{year}/{month}; month must be 1-12, else 400.
- Yearly: GET /api/reports/yearly/{year}; sensible year range, else 400.
- Both return per-category totals for income and expenses plus netSavings
  (income - expenses). Only the logged-in userEntity's transactions are included.
- A period with no data returns empty maps and netSavings 0.00 (not an error).

## 7. "Today" and Time
- "Today" and "future" use the configured timezone (APP_TIMEZONE, default
  Asia/Kolkata) through an injected Clock.

## 8. API Responses and Errors
- Known scenarios never return 5xx.
- 400: validation failures, malformed JSON, bad path/query types, invalid ranges.
- 401: missing/expired session or bad login. 403: forbidden access (see section 2
  and category deletion). 404: resource not found or unknown route.
- 409: duplicate email or duplicate category name. 405: unsupported method.
- Error body: status, error, message, and optional details list. Messages are clear
  and never expose internals.