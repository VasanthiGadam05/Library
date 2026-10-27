package com.knf.dev.librarymanagementsystem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.knf.dev.librarymanagementsystem.entity.Loan;

public interface LoanRepository extends JpaRepository<Loan, Long> {

	public List<Loan> findByReturnedDateIsNullOrderByDueDateAsc();

	public long countByBookIdAndReturnedDateIsNull(Long bookId);
}
