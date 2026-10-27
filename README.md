# Java, Spring Boot Mini Project - Library Management System


# Local setup

Step 1: Download or clone the source code from GitHub to the local machine

Step 2: Install JDK 17 - https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html

Step 3: Install IntelliJ IDEA or Eclipse or Apache NetBeans IDE

Step 4: Install Apache Maven - https://maven.apache.org/install.html

Step 5:  ```mvn clean install```

Step 6:  ```mvn spring-boot:run```

Step 7: From the browser call the endpoint http://localhost:9080

Step 8: Admin Login User Id: ```admin@admin.in``` & Password: ```Temp123```


# Borrow / return and availability tracking (MLG1-13)

Each book now has `totalCopies` (default 1) and `availableCopies`. Admins can issue a book to a borrower and record its return.

| Page / endpoint | Description |
| --- | --- |
| `GET /books` | Book list now shows `available / total` copies and a **Borrow** button (hidden when no copy is available) |
| `GET /books/{id}/borrow` | Borrow form (borrower name + due date, defaults to 14 days from today) |
| `POST /books/{id}/borrow` | Issues the book: creates a loan and decrements `availableCopies`. If `availableCopies = 0` the request is blocked and a friendly message is shown |
| `GET /loans/open` | Lists open (not yet returned) loans, overdue loans are flagged |
| `POST /loans/{loanId}/return` | Marks the loan returned and increments `availableCopies` by 1 |

Notes:
- Add/Update Book forms have a **Total Copies** field. Changing the total keeps the number of copies currently on loan; the total cannot be set lower than that number.
- Borrow and return run in a transaction and take a pessimistic row lock on the book, so two admins cannot borrow the last copy at the same time.
- Unknown book/loan IDs now return HTTP 404 (`NotFoundException` is annotated with `@ResponseStatus`).

## Database

The app uses an in-memory H2 database with Hibernate auto-DDL, so the new `loans` table and the two new `books` columns are created automatically on startup. No migration tool (Flyway/Liquibase) is used in this project. If you run against a persistent database with schema auto-update disabled, apply the equivalent DDL (reference only, adjust types to your database):

```sql
ALTER TABLE books ADD COLUMN total_copies INT NOT NULL DEFAULT 1;
ALTER TABLE books ADD COLUMN available_copies INT NOT NULL DEFAULT 1;

CREATE TABLE loans (
    id            BIGINT PRIMARY KEY,
    book_id       BIGINT NOT NULL REFERENCES books (id),
    borrower_name VARCHAR(100) NOT NULL,
    issue_date    DATE NOT NULL,
    due_date      DATE NOT NULL,
    returned_date DATE
);
```

# Automated tests (MLG1-14)

Run all tests with:

```
mvn test
```

| Test class | Type | Covers |
| --- | --- | --- |
| `service/LoanServiceTest` | unit (Mockito) | borrow decrements copies, borrow blocked at 0 copies, return increments copies, validation |
| `service/BookServiceTest` | unit (Mockito) | book CRUD, search, pagination, copy bookkeeping on create/update |
| `service/MasterDataServiceTest` | unit (Mockito) | author, category and publisher service CRUD |
| `controller/SecurityAccessIntegrationTest` | integration (MockMvc) | unauthenticated users are redirected to `/login`, admin can open admin pages (status, view, model), CSRF enforced |
| `controller/CrudControllerIntegrationTest` | integration (MockMvc + H2) | create/read/update/delete for books, authors, categories, publishers |
| `controller/LoanFlowIntegrationTest` | integration (MockMvc + H2) | full borrow/return flow, blocked borrow, double return, invalid input |

Known gaps (existing behaviour, not changed here): the `/author/{id}`, `/category/{id}` and `/publisher/{id}` detail pages reference templates (`list-author`, `list-category`, `list-publisher`) that do not exist, and deleting a book that has loan records fails with a database constraint error.

# Admin Login Interface

<img src="https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEiiuctxupeOK4Nh8j-nomwwapjkcVvkYig3lX7qoifcXE76_6CnOXMZ-CLww7G180qegsCkrtyUlaqpJsWm9GzhX9QUFxyNyEUAXFD5UWJpvh2BdIr0wyAnFC38QOdsL_1vak8LtxYHrZyplCU_Sri-7kM9nXxI9heXXB0621rzJgL6j1CSweX6xjaorg/s945/admin-login.png">

# Add new books, update books, view books, delete books

<img src="https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEjYMpuCQx3lGsS4T_H4ziyDWIkBpYV5qgo5JHFMV0Drper48H7YfygEdv0htE3yWo8mlypUW9W7NFY00UtrVznFfFYIzNGAXBeskhBb_kHAJrVKnI7O5mZt0_c085n6ir-cNVEYsTYffn6WgCmoBiZULR88ah_YxDC-ywRKPTsxj58GcHFnyyeX00RsNA/s800/library-management-system.png">

# Add new authors, update authors, view authors, delete authors

<img src="https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEixAW5k4E9IXf_OuVO1S5m100KS1xFo2ZrFoLnZYvNLjfpmIdI8W0ukd6yQn6oTsSWBKjDdAIGsnPf0EhgRwKzfpVq3mJXMcqG94Qp2oCCy0Pzf01b3kXP2ahgbvpFQND60c7cHwPNZ7A6uXh7fxqvB5od26PleS3giunEN-uAuFIuKijjELspH1_gLcw/s934/authors-list.png">

# Add new categories, update categories, view categories, delete categories

<img src="https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEhYe6-eBO4HZjqE1Rr0PLoHS1dlvlnwuagwQtX6eRavoDsWRGk4yfguhWIdcOFRgM4H7985xL1bdiLQLqX_iU7RzddDb1yiQ0P3M0sfwUdTRlRGMg85Kp2KKTsVZH5WGlptL6LFRTITq4oSCJFFCZwGML1RrxI-chu-xb4eXOWIoZpNlFWLLUzkW6zLdQ/s935/categorylist.png">

# Add new publishers, update publishers, view publishers, delete publishers

<img src="https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEhcNQAd4UVi_bYQQSvW49hn0rQ1O7bEBDyN4DDNJSH1rtxBg37QIHQKAp7ELGbFV4Xva2F0DmhTkA3vKVeZcmKs7lODgTulsJr1aLyBckEojzxzZE5FYlfuEwD62Qco6PsjdNVPEWT76GlyVnSP94zNZK59w3CMRuvbYjoc1-MpyXj-WCeNEjPDm6mucw/s938/publishers-list.png">

