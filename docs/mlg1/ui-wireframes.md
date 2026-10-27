# UI Wireframes (Text) – Books List Filtering (MLG1-10)

These wireframes align with the existing Thymeleaf + Bootstrap-like layout (header/footer includes).

## 1. Books List (Enhanced)

```
+-----------------------------------------------------------------------------------+
| Header/Nav (existing)                                                             |
+-----------------------------------------------------------------------------------+
| Books                                                                             |
| [Add Book]                                                                        |
|-----------------------------------------------------------------------------------|
| Filter                                                                            |
| Title: [________________________]  Author: [v Select]  Category: [v Select]       |
| Publisher: [v Select]                                                             |
| [Apply Filters]  [Clear]                                                          |
|-----------------------------------------------------------------------------------|
| Table                                                                             |
| +----+-----------+----------------------+-------------+-------------------------+  |
| | ID | ISBN      | Name                 | SerialName  | Actions                 |  |
| +----+-----------+----------------------+-------------+-------------------------+  |
| | 1  | 978...    | Clean Code           | CC-001      | [Edit] [Delete]         |  |
| | 2  | 978...    | Domain-Driven Design | DDD-001     | [Edit] [Delete]         |  |
| +----+-----------+----------------------+-------------+-------------------------+  |
| (Optional) Show selected authors/categories/publishers per row if already present |
+-----------------------------------------------------------------------------------+
| Footer (existing)                                                                 |
+-----------------------------------------------------------------------------------+
```

### Notes
- Filter form submits via **GET** to `/books`.
- Clear button is a link to `/books` (no query params).
- Dropdown values are populated from `authors`, `categories`, `publishers` model lists.

## 2. Responsiveness (small screens)

```
Books
[Add Book]

Filter
Title: [______________]
Author: [v Select]
Category: [v Select]
Publisher: [v Select]
[Apply Filters] [Clear]

(Table below)
```

## 3. Empty results state

```
Filter ...

No books found matching the selected filters.
[Clear]
```
