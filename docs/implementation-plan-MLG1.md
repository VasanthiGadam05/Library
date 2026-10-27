# Implementation Plan – MLG1 Approved Stories

GitHub: <https://github.com/VasanthiGadam05/Library>

Scope: Implement two approved user stories in Jira project KEY `MLG1`:
1) Member Management (Create/Update/List/Deactivate)
2) Loan/Return workflow with due dates and overdue tracking

This plan is designed to match the acceptance criteria provided in the approved stories.

---

## 1) Affected Modules / Layers

Base package: `com.knf.dev.librarymanagementsystem`

*Entities (src/main/java/.../entity)*
- New: `Member`, `Loan`
- Existing: Book (needs available quantity interaction)

JPA Repositories (src/main/java/.../repository)
- New: MemberRepository, LoanRepository

Service (src/main/java/.../service)
- New: MemberService, LoanService
- Update: BookService (or equivalent) to support atomic quantity updates

Controllers (src/main/java/.../controller)
- New: MemberController (Thymeleaf CRUD)
- New: LoanController (issue/return forms, loan list with filters)

Thymeleaf Templates (src/main/resources/templates)
- New: list-members.html, add-member.html, update-member.html
- New: issue-loan.html, list-loans.html
- Update: navigation menu (templates/comman/* layout)

Security
- Update: Spring Security config to allow new endpoints (remain admin-only like current app)

Database
- H2 (current); rely on HYBernate DDL auto-update or add Flyway (optional).

## 2) Domain Model Proposal

### 2.1 Member entity
Minimal fields to satisfy Story (2):
- id (LONG, PK)
- name (required)
- contactId (required, unique; email/phone/member-code)
- notes (optional)
- active (default true)
- createdAt, updatedAt (optional)

Rules:
- Create/update requires name + contactId.