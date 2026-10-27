# Implementation Plan – MLG1-13 & MLG1-14

This document translates the approved Jira user stories and acceptance criteria into a detailed, step-by-step implementation plan for the GitHub repo: https://github.com/VasanthiGadam05/Library.

- **Story MLG1-13**: Add book availability + borrow/return tracking
- **Story MLG1-14**: Add automated tests for core CRUD and security access

---

## 1) Scope and goals

### MLG1-13 — Borrow/Return + availability tracking
**Goal**: Introduce a basic circulation workflow (issue/return) and persist availability state so admins can't issue unavailable books.

**Acceptance criteria (ref):**
1) Book has `totalCopies` and `availableCopies`; on borrow, decrement `availableCopies` and create a loan record with borrower + due date.
2) When `availableCopies = 0`, borrow is blocked with a user-friendly message.
3) On return, mark loan returned and increase `availableCopies` by 1.

### MLG1-14 — Automated tests for CRUD & security
**Goal**: Add a practical test suite that covers core CRUD (books/authors/categories/publishers) and basic Spring Security access controls.

**Acceptance criteria (ref):**
1) `mvn test` runs unit tests (service) and integration tests (controller) for core modules.
2) Unauthenticated user is redirected to login (or 401/403) for admin-only endpoints.
3) Authenticated admin can access admin endpoints (200/3xx, correct view/model).

---

## 2) Affected modules / packages

Primary code is under:
`src/main/java/com/knf/dev/librarymanagementsystem`

Planned touch points:
- `entity/Book.java` (add copy fields)
- `entity/Loan.java` (new)
- `repository/LoanRepository.java` (new)
- `service/LoanService.java` (new)
- `controller/LoanController.java` (new)
- Thymeleaf templates: new loan list and borrow form; update book list/detail to show availability
- `securityconfig/` only if needed to secure new endpoints
- Tests: add new tests under `src/test/java/com/knf/dev/librarymanagementsystem`
  - Note: current test package is `com.quickstartdev...` (mismatch); new tests should align to main package.

---

## 3) Proposed changes (design)

### 3.1 Data model: Book copies
Add to `Book` entity:
- `int totalCopies` (default 1)
- `int availableCopies` (default = totalCopies)

Invariants:
- `0 <= availableCopies <= totalCopies` always

Schema:
- If using Hibernate auto-DDL (common for demos), schema updates on startup.
- If the project later adopts Flyway/Liquibase, create a forward migration adding the columns.

### 3.2 New entity: Loan
Minimal fields to satisfy AC:
- `Long id`
- `Book book` (`@ManyToOne`)
- `String borrowerName`
- `LocalDate issueDate`
- `LocalDate dueDate`
- `LocalDate returnedDate` (nullable)

Active loan: `returnedDate == null`.

### 3.3 Service layer: atomic borrow/return
Create `LoanService` and make operations transactional.

Methods:
- `borrow(bookId, borrowerName, dueDate)`
  - validate book exists
  - if `availableCopies == 0` -> throw a domain exception surfaced as a friendly UI error (AC#2)
  - create loan record
  - decrement `availableCopies`
- `return(loanId)`
  - validate loan exists and is active
  - set `returnedDate = today`
  - increment `availableCopies`

Concurrency: use `@Transactional` plus either:
- pessimistic locking on the Book row for borrow/return, or
- optimistic locking (`@Version`) and retry policy.

### 3.4 Web layer: pages and endpoints
Proposed minimal Thymeleaf flow:
- GET `/loans/open`: list open loans
- GET `/books/{id}/borrow`: show borrow form
- POST `/books/{id}/borrow`: perform borrow and redirect with success/error message
- POST `/loans/{loanId}/return`: return loan and redirect with success message

Update book listing/detail template to show `availableCopies/totalCopies` and only show Borrow button when `availableCopies > 0`.

---

## 4) Dependencies / prerequisites
- Spring Data JPA (already present)
- H2 runtime (already present)
- `spring-security-test` (already present)
- Thymeleaf templates matching the existing UI conventions

---

## 5) Task sequence (critical path)

This order minimizes rework: model → repo → service → controller → view → tests.

### Phase A — Prep
1) Inspect existing Book CRUD controller/service/template patterns to match style.
2) Decide template locations and how navigation links are built.

### Phase B — Data model (MLG1-13)
3) Update `entity/Book.java`: add `totalCopies`, `availableCopies`, enforce invariants.
4) Add `entity/Loan.java` with mappings.
5) Add `repository/LoanRepository.java`.

### Phase C — Service (MLG1-13)
6) Add `service/LoanService.java` with transactional borrow/return.
7) Add domain exceptions (e.g., `UnavailableBookException`, `LoanAlreadyReturnedException`) under `exception/`.

### Phase D — Controller + Views (MLG1-13)
8) Add `controller/LoanController.java`.
9) Add templates:
   - `loans-open.html` (open loans list + return button)
   - `book-borrow-form.html` (borrow form)
10) Update existing book templates to show availability and borrow action.

### Phase E — Tests (MLG1-14)
11) Service unit tests:
   - `LoanServiceTest`: borrow decrements, unavailable blocks, return increments.
12) Controller integration tests (MockMvc):
   - unauthenticated access redirects to `/login` (or 401/403 depending on config)
   - authenticated admin access succeeds and correct view name is returned
13) CRUD test coverage at least smoke-level for:
   - books, authors, categories, publishers

### Phase F — Validation
14) `./mvnw test` passes.
15) Manual check of AC flows:
   - borrow reduces copies and creates loan
   - cannot borrow when `availableCopies == 0`
   - return increases copies and marks returned

---

## 6) Risks and mitigations
- **Concurrency risk**: two admins borrow the last copy simultaneously.
  - Mitigate with `@Transactional` + locking/optimistic versioning.
- **Data integrity drift**: `availableCopies` out of sync with active loans.
  - Mitigate with invariants + tests; optionally compute active-loan count later.
- **Schema evolution**: adding columns may fail in non-auto-DDL environments.
  - Mitigate by documenting expected DB mode or adding migrations.
- **Test package mismatch**: existing tests are in `com.quickstartdev...` while main app is `com.knf.dev...`.
  - Mitigate by placing new tests in the main package and (optionally) moving existing tests later.

---

## 7) Validation steps (executable checklist)

### Automated
- `./mvnw test` (or `mvn test`) passes.

### Manual (MLG1-13)
1) Run app: `./mvnw spring-boot:run`
2) Login as admin
3) Create a book with `totalCopies = 1` (or default)
4) Borrow book -> verify `availableCopies` becomes 0 and open loan appears
5) Attempt borrow again -> blocked with friendly message
6) Return loan -> verify `availableCopies` becomes 1 and loan marked returned

### Security (MLG1-14)
1) Access an admin endpoint unauthenticated -> redirect to `/login` (or 401/403)
2) Access same endpoint as admin -> succeeds
