# High-Level Design (HLD) – MLG1 Enhancements

## 1. Scope
Implements:
- **MLG1-9**: Automated tests for repositories and key MVC endpoints
- **MLG1-10**: Book search/filter UI + backend

## 2. Use cases
### UC-BOOK-LIST-FILTER
**Actor:** Admin/Librarian
**Goal:** Filter the book list by title and/or author/category/publisher.

**Main flow**
1. User opens Books list.
2. User enters optional title and selects optional filters.
3. User submits the filter form (GET).
4. System returns the filtered list, preserving selected filters in the UI.
5. User can clear filters to see all books.

### UC-REGRESSION-PROTECTION
**Actor:** Developer/CI
**Goal:** Catch regressions via automated tests.

## 3. High-level architecture changes
- Add a **search method** to `BookRepository` and expose it through `BookService`.
- Modify `BookController` list endpoint to accept query parameters.
- Update `list-books.html` to add filter controls.
- Add test modules:
  - JPA tests for repositories
  - MVC tests for controllers

## 4. API / Endpoint design (server-rendered)
### GET /books
**Query parameters (all optional)**
- `title`: string
- `authorId`: long
- `categoryId`: long
- `publisherId`: long

**Response**
- Thymeleaf view `list-books`
- Model attributes:
  - `books` – list of matching `Book`
  - `authors`, `categories`, `publishers` – lists for dropdowns
  - `title`, `authorId`, `categoryId`, `publisherId` – selected values (for sticky filters)

## 5. Data design
No new tables required. Query uses existing many-to-many join tables.

## 6. Performance considerations
- Use `select distinct` to prevent duplicate books due to joins.
- Use lazy loading for relations; in view rendering, avoid N+1 by keeping displayed fields minimal.

## 7. Testing strategy (HLD)
- `@DataJpaTest`
  - Save and retrieve entities
  - Verify search query combinations
- `@WebMvcTest`
  - GET `/books` with/without query params returns correct view + model
  - Handle security constraints via `@WithMockUser` if needed

## 8. Rollout plan
- Implement tests and filtering behind the same endpoint; no API break.
- Merge as a single PR for documentation and design artifacts (this pack), followed by implementation PRs.
