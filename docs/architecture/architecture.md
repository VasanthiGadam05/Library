# Architecture Document – MLG1-13 / MLG1-14

Repo: https://github.com/VasanthiGadam05/Library

Project: **MLG1**  
Confluence space: **capstone**

---

## 1) Purpose and scope

This document describes the architecture of the existing Spring Boot + Thymeleaf Library Management System and the design impact of the approved enhancements:

- **MLG1-13**: Borrow/Return workflow + book availability tracking
- **MLG1-14**: Automated tests for CRUD **&* security access

The goal is to extend the current monolithic MVC app while staying consistent with the existing stack and style.

---

## 2) System context

**Primary users**: Admin/Librarian users authenticated via Spring Security form login.

\n**External dependencies**:
- Web browser (client)
- Spring Boot embedded server