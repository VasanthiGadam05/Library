package com.knf.dev.librarymanagementsystem.service;

import java.time.LocalDate;
import java.util.List;

import com.knf.dev.librarymanagementsystem.entity.Loan;

public interface LoanService {

	public List<Loan> findOpenLoans();

	public Loan borrow(Long bookId, String borrowerName, LocalDate dueDate);

	public Loan returnLoan(Long loanId);

}
