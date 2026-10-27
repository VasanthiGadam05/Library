package com.knf.dev.librarymanagementsystem.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.knf.dev.librarymanagementsystem.entity.Loan;
import com.knf.dev.librarymanagementsystem.exception.LoanAlreadyReturnedException;
import com.knf.dev.librarymanagementsystem.exception.NotFoundException;
import com.knf.dev.librarymanagementsystem.exception.UnavailableBookException;
import com.knf.dev.librarymanagementsystem.repository.BookRepository;
import com.knf.dev.librarymanagementsystem.repository.LoanRepository;
import com.knf.dev.librarymanagementsystem.service.LoanService;

@Service
public class LoanServiceImpl implements LoanService {

	private final LoanRepository loanRepository;
	private final BookRepository bookRepository;

	public LoanServiceImpl(LoanRepository loanRepository, BookRepository bookRepository) {
		this.loanRepository = loanRepository;
		this.bookRepository = bookRepository;
	}

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@Override
	public List<Loan> findOpenLoans() {
		return loanRepository.findByReturnedDateIsNullOrderByDueDateAsc();
	}

	@Transactional
	@Override
	public Loan borrow(Long bookId, String borrowerName, LocalDate dueDate) {
		if (borrowerName == null || borrowerName.trim().isEmpty()) {
			throw new IllegalArgumentException("Borrower name must not be empty");
		}
		var today = LocalDate.now();
		if (dueDate == null || dueDate.isBefore(today)) {
			throw new IllegalArgumentException("Due date must be today or a future date");
		}

		// Row lock on the book so two concurrent borrows cannot take the last copy.
		var book = bookRepository.findByIdForUpdate(bookId)
				.orElseThrow(() -> new NotFoundException(String.format("Book not found with ID %d", bookId)));

		if (book.getAvailableCopies() <= 0) {
			throw new UnavailableBookException(
					String.format("No copies of \"%s\" are currently available to borrow", book.getName().trim()));
		}

		book.setAvailableCopies(book.getAvailableCopies() - 1);
		bookRepository.save(book);
		return loanRepository.save(new Loan(book, borrowerName.trim(), today, dueDate));
	}

	@Transactional
	@Override
	public Loan returnLoan(Long loanId) {
		var loan = loanRepository.findById(loanId)
				.orElseThrow(() -> new NotFoundException(String.format("Loan not found with ID %d", loanId)));

		if (!loan.isActive()) {
			throw new LoanAlreadyReturnedException(String.format("Loan %d has already been returned", loanId));
		}

		var bookId = loan.getBook().getId();
		var book = bookRepository.findByIdForUpdate(bookId)
				.orElseThrow(() -> new NotFoundException(String.format("Book not found with ID %d", bookId)));

		loan.setReturnedDate(LocalDate.now());
		if (book.getAvailableCopies() < book.getTotalCopies()) {
			book.setAvailableCopies(book.getAvailableCopies() + 1);
			bookRepository.save(book);
		}
		return loanRepository.save(loan);
	}

}
