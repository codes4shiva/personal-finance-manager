# API Contract (SPEC.md) - source: assignment PDF
All endpoints are under /api. JSON in/out. Session cookie auth.
Errors: JSON {status, error, message, details?}.

## Auth
- POST /api/auth/register  {username(email), password, fullName, phoneNumber}
  -> 201 {"message":"User registered successfully","userId":1} | 400 | 409
- POST /api/auth/login  {username, password}
  -> 200 {"message":"Login successful"} + session cookie | 401
- POST /api/auth/logout (no body) -> 200 {"message":"Logout successful"} | 401

## Transactions
- POST /api/transactions {amount, date(YYYY-MM-DD), category(name), description?}
  -> 201 {id, amount, date, category, description, type} | 400 | 401
- GET /api/transactions?startDate&endDate&categoryId  (also support category name, type)
  -> 200 {"transactions":[{id, amount, date, category, description, type}]} | 401
- PUT /api/transactions/{id} {amount?, description?, (category?)}
  -> 200 same shape as create | 400 | 401 | 404
- DELETE /api/transactions/{id} -> 200 {"message":"Transaction deleted successfully"} | 401 | 404

## Categories
- GET /api/categories -> 200 {"categories":[{name, type, isCustom}]} | 401
- POST /api/categories {name, type} -> 201 {name, type, isCustom:true} | 400 | 401 | 409
- DELETE /api/categories/{name} -> 200 {"message":"Category deleted successfully"}
  | 400 | 401 | 403 | 404

## Goals
- POST /api/goals {goalName, targetAmount, targetDate, startDate?}
  -> 201 {id, goalName, targetAmount, targetDate, startDate, currentProgress,
  progressPercentage, remainingAmount} | 400 | 401
- GET /api/goals -> 200 {"goals":[ ...goal... ]} | 401
- GET /api/goals/{id} -> 200 goal | 400 | 401 | 403 | 404
- PUT /api/goals/{id} {targetAmount?, targetDate?} -> 200 goal | 400 | 401 | 403 | 404
- DELETE /api/goals/{id} -> 200 {"message":"Goal deleted successfully"}
  | 401 | 403 | 404

## Reports
- GET /api/reports/monthly/{year}/{month}
  -> 200 {month, year, totalIncome:{category:amount}, totalExpenses:{category:amount},
  netSavings} | 401
- GET /api/reports/yearly/{year}
  -> 200 {year, totalIncome:{...}, totalExpenses:{...}, netSavings} | 401

## Targets
- Test script: financial_manager_tests.sh (86 tests, must pass 100%).
- Coverage >= 80% (JUnit 5 + Mockito).