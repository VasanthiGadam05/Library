# Database Schema (Existing) – Library Management System

## 1. Overview
The application uses Spring Data JPA/Hibernate to manage the schema.
The enhancements in MLG1 do **not** require new tables; they reuse existing tables and relationships.

## 2. Core tables
### 2.1 `books`
| Column | Type | Notes |
|---|---|---|
| `id` | bigint | PK (AUTO) |
| `isbn` | varchar(50) | unique, not null |
| `name` | varchar(100) | not null |
| `serialName` | varchar(50) | not null |
| `description` | varchar(250) | not null |

### 2.2 `authors`
(From `Author` entity)
| Column | Type | Notes |
|---|---|---|
| `id` | bigint | PK |
| `name` | varchar | not null (per entity mapping) |

### 2.3 `categories`
| Column | Type | Notes |
|---|---|---|
| `id` | bigint | PK |
| `name` | varchar | |

### 2.4 `publishers`
| Column | Type | Notes |
|---|---|---|
| `id` | bigint | PK |
| `name` | varchar | |

## 3. Join tables (many-to-many)
### 3.1 `books_authors`
| Column | Type | Notes |
|---|---|---|
| `book_id` | bigint | FK -> books.id |
| `author_id` | bigint | FK -> authors.id |

### 3.2 `books_categories`
| Column | Type | Notes |
|---|---|---|
| `book_id` | bigint | FK -> books.id |
| `category_id` | bigint | FK -> categories.id |

### 3.3 `books_publishers`
| Column | Type | Notes |
|---|---|---|
| `book_id` | bigint | FK -> books.id |
| `publisher_id` | bigint | FK -> publishers.id |

## 4. Security-related tables
Entities exist for `User` and `Role`. Schema depends on JPA mapping.

## 5. Indexing recommendations (optional)
To keep filtering responsive as data grows:
- Index `books.name`
- Index join-table foreign keys (`book_id`, `author_id`, etc.)

No schema migration is included in this story; runtime schema is managed by Hibernate.
