package com.knf.dev.librarymanagementsystem.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.knf.dev.librarymanagementsystem.entity.Book;
import com.knf.dev.librarymanagementsystem.entity.Loan;
import com.knf.dev.librarymanagementsystem.exception.LoanAlreadyReturnedException;
import com.knf.dev.librarymanagementsystem.exception.NotFoundException;
import com.knf.dev.librarymanagementsystem.exception.UnavailableBookException;
import com.knf.dev.librarymanagementsystem.repository.BookRepository;
import com.knf.dev.librarymanagementsystem.repository.LoanRepository;
import com.knf.dev.librarymanagementsystem.service.impl.LoanServiceImpl;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

	@Mock
	private LoanRepository loanRepository;

	@Mock
	private BookRepository bookRepository;

	private LoanServiceImpl loanService;
	private Book book;

	@BeforeEach
	void setUp() {
		loanService = new LoanServiceImpl(loanRepository, bookRepository);
		book = new Book("ISBN-1", "Test Book", "SER-1", "desc");
		book.setId(1L);
		book.setTotalCopies(2);
		book.setAvailableCopies(2);
	}

	@Test
	void borrowDecrementsAvailableCopiesAndCreatesLoan() {
		var due = LocalDate.now().plusDays(7);
		when(bookRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(book));
		when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

		var loan = loanService.borrow(1L, "  Alice ", due);

		assertEquals(1, book.getAvailableCopies());
		assertEquals("Alice", loan.getBorrowerName());
		assertEquals(due, loan.getDueDate());
		assertEquals(LocalDate.now(), loan.getIssueDate());
		assertEquals(book, loan.getBook());
		assertTrue(loan.isActive());
		verify(bookRepository).save(book);
	}

	@Test
	void borrowIsBlockedWhenNoCopiesAvailable() {
		book.setAvailableCopies(0);
		when(bookRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(book));

		var ex = assertThrows(UnavailableBookException.class,
				() -> loanService.borrow(1L, "Alice", LocalDate.now().plusDays(7)));

		assertNotNull(ex.getMessage());
		assertEquals(0, book.getAvailableCopies());
		verify(loanRepository, never()).save(any(Loan.class));
	}

	@Test
	void borrowUnknownBookThrowsNotFound() {
		when(bookRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class, () -> loanService.borrow(99L, "Alice", LocalDate.now().plusDays(1)));
	}

	@Test
	void borrowRejectsBlankBorrowerAndPastDueDate() {
		assertThrows(IllegalArgumentException.class, () -> loanService.borrow(1L, "  ", LocalDate.now().plusDays(1)));
		assertThrows(IllegalArgumentException.class,
				() -> loanService.borrow(1L, "Alice", LocalDate.now().minusDays(1)));
		assertThrows(IllegalArgumentException.class, () -> loanService.borrow(1L, "Alice", null));
		verify(bookRepository, never()).findByIdForUpdate(any());
	}

	@Test
	void returnMarksLoanReturnedAndIncrementsAvailableCopies() {
		book.setAvailableCopies(1);
		var loan = new Loan(book, "Alice", LocalDate.now().minusDays(3), LocalDate.now().plusDays(4));
		when(loanRepository.findById(5L)).thenReturn(Optional.of(loan));
		when(bookRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(book));
		when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

		var result = loanService.returnLoan(5L);

		assertEquals(LocalDate.now(), result.getReturnedDate());
		assertEquals(2, book.getAvailableCopies());
	}

	@Test
	void returnAlreadyReturnedLoanIsRejected() {
		var loan = new Loan(book, "Alice", LocalDate.now().minusDays(3), LocalDate.now().plusDays(4));
		loan.setReturnedDate(LocalDate.now().minusDays(1));
		when(loanRepository.findById(5L)).thenReturn(Optional.of(loan));

		assertThrows(LoanAlreadyReturnedException.class, () -> loanService.returnLoan(5L));
		assertEquals(2, book.getAvailableCopies());
	}

	@Test
	void returnNeverExceedsTotalCopies() {
		var loan = new Loan(book, "Alice", LocalDate.now().minusDays(3), LocalDate.now().plusDays(4));
		when(loanRepository.findById(5L)).thenReturn(Optional.of(loan));
		when(bookRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(book));
		when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

		loanService.returnLoan(5L);

		assertEquals(2, book.getAvailableCopies());
	}

	@Test
	void returnUnknownLoanThrowsNotFound() {
		when(loanRepository.findById(404L)).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class, () -> loanService.returnLoan(404L));
	}
}
