# Low-Level Design (LLD) – MLG1 Enhancements

## 1. Package & component mapping
- Controllers: `com.knf.dev.librarymanagementsystem.controller`
- Services: `com.knf.dev.librarymanagementsystem.service` + `...service.impl`
- Repositories: `com.knf.dev.librarymanagementsystem.repository`
- Views: `src/main/resources/templates`
- Tests: `src/test/java/com/knf/dev/librarymanagementsystem/...`

## 2. Book filtering – backend design

### 2.1 Repository
**File:** `BookRepository.java`

Add method (example signature):
```java
@Query("""
  select distinct b from Book b
    left join b.authors a
    left join b.categories c
    left join b.publishers p
  where (:title is null or lower(b.name) like lower(concat('%', :title, '%')))
    and (:authorId is null or a.id = :authorId)
    and (:categoryId is null or c.id = :categoryId)
    and (:publisherId is null or p.id = :publisherId)
""")
List<Book> search(
  @Param("title") String title,
  @Param("authorId") Long authorId,
  @Param("categoryId") Long categoryId,
  @Param("publisherId") Long publisherId);
```

Notes:
- Use `distinct` to avoid duplicates.
- Use `left join` to allow books with missing relations to still appear when those filters are not used.

### 2.2 Service
**File:** `BookService.java`

Add:
```java
List<Book> searchBooks(String title, Long authorId, Long categoryId, Long publisherId);
```

**File:** `BookServiceImpl.java`

Implement:
- Normalize `title`:
  - `null` if blank
  - trim whitespace
- Pass parameters to repository search.

### 2.3 Controller
**File:** `BookController.java`

Update list handler to accept query params:
```java
@GetMapping("/books")
public String listBooks(
  @RequestParam(required=false) String title,
  @RequestParam(required=false) Long authorId,
  @RequestParam(required=false) Long categoryId,
  @RequestParam(required=false) Long publisherId,
  Model model) {

  model.addAttribute("books", bookService.searchBooks(title, authorId, categoryId, publisherId));
  model.addAttribute("authors", authorService.getAllAuthor());
  model.addAttribute("categories", categoryService.getAllCategory());
  model.addAttribute("publishers", publisherService.getAllPublisher());

  // sticky form fields
  model.addAttribute("title", title);
  model.addAttribute("authorId", authorId);
  model.addAttribute("categoryId", categoryId);
  model.addAttribute("publisherId", publisherId);

  return "list-books";
}
```

## 3. UI changes – view design
**File:** `list-books.html`

Add a filter form (GET) above the table:
- `title` input
- `authorId`, `categoryId`, `publisherId` selects
- Submit button
- Clear filters link to `/books`

Thymeleaf notes:
- use `th:value="${title}"` for text
- use `th:selected="${authorId} == ${a.id}"` patterns or `th:field` if a form-backing object is used

## 4. Testing design (MLG1-9)

### 4.1 Repository tests
Create:
- `AuthorRepositoryTest`
- `CategoryRepositoryTest`
- `PublisherRepositoryTest`
- `BookRepositoryTest` (includes search tests)

Annotations:
- `@DataJpaTest`

Key cases:
- save + findById
- delete + verify missing
- book search:
  - title-only
  - author-only
  - combinations

### 4.2 MVC tests
Create:
- `BookControllerTest`

Annotations:
- `@WebMvcTest(BookController.class)`
- `@WithMockUser` if security is enforced

Mock:
- `@MockBean` for `BookService`, `AuthorService`, `CategoryService`, `PublisherService`

Assertions:
- status 200
- view name `list-books`
- model attributes exist

## 5. Config changes for tests
Add `src/test/resources/application-test.properties`:
- H2 datasource
- `spring.jpa.hibernate.ddl-auto=create-drop`

Add dependencies (test scope):
- `com.h2database:h2`
- `org.springframework.security:spring-security-test` (if needed)
