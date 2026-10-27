package com.knf.dev.librarymanagementsystem.entity;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "loans")
public class Loan {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "book_id", nullable = false)
	private Book book;

	@Column(name = "borrower_name", length = 100, nullable = false)
	private String borrowerName;

	@Column(name = "issue_date", nullable = false)
	private LocalDate issueDate;

	@Column(name = "due_date", nullable = false)
	private LocalDate dueDate;

	@Column(name = "returned_date")
	private LocalDate returnedDate;

	public Loan() {
		super();
	}

	public Loan(Book book, String borrowerName, LocalDate issueDate, LocalDate dueDate) {
		this.book = book;
		this.borrowerName = borrowerName;
		this.issueDate = issueDate;
		this.dueDate = dueDate;
	}

	public boolean isActive() {
		return returnedDate == null;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Book getBook() {
		return book;
	}

	public void setBook(Book book) {
		this.book = book;
	}

	public String getBorrowerName() {
		return borrowerName;
	}

	public void setBorrowerName(String borrowerName) {
		this.borrowerName = borrowerName;
	}

	public LocalDate getIssueDate() {
		return issueDate;
	}

	public void setIssueDate(LocalDate issueDate) {
		this.issueDate = issueDate;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}

	public LocalDate getReturnedDate() {
		return returnedDate;
	}

	public void setReturnedDate(LocalDate returnedDate) {
		this.returnedDate = returnedDate;
	}

}
