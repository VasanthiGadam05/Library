package com.knf.dev.librarymanagementsystem.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import com.knf.dev.librarymanagementsystem.entity.Book;
import com.knf.dev.librarymanagementsystem.exception.NotFoundException;
import com.knf.dev.librarymanagementsystem.repository.BookRepository;
import com.knf.dev.librarymanagementsystem.service.impl.BookServiceImpl;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

	@Mock
	private BookRepository bookRepository;

	private BookServiceImpl bookService;

	@BeforeEach
	void setUp() {
		bookService = new BookServiceImpl(bookRepository);
	}

	private Book newBook(long id) {
		var book = new Book("ISBN-" + id, "Book " + id, "SER-" + id, "desc");
		book.setId(id);
		return book;
	}

	@Test
	void findAllBooksDelegatesToRepository() {
		when(bookRepository.findAll()).thenReturn(List.of(newBook(1), newBook(2)));

		assertEquals(2, bookService.findAllBooks().size());
	}

	@Test
	void searchWithNullKeywordReturnsAll() {
		when(bookRepository.findAll()).thenReturn(List.of(newBook(1)));

		assertEquals(1, bookService.searchBooks(null).size());
		verify(bookRepository, never()).search(anyString());
	}

	@Test
	void findByIdReturnsBookOrThrows() {
		var book = newBook(1);
		when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
		when(bookRepository.findById(2L)).thenReturn(Optional.empty());

		assertSame(book, bookService.findBookById(1L));
		assertThrows(NotFoundException.class, () -> bookService.findBookById(2L));
	}

	@Test
	void createBookMakesAllCopiesAvailable() {
		var book = newBook(1);
		book.setTotalCopies(3);
		book.setAvailableCopies(0);

		bookService.createBook(book);

		assertEquals(3, book.getAvailableCopies());
		verify(bookRepository).save(book);
	}

	@Test
	void createBookRejectsZeroCopies() {
		var book = newBook(1);
		book.setTotalCopies(0);

		assertThrows(IllegalArgumentException.class, () -> bookService.createBook(book));
		verify(bookRepository, never()).save(book);
	}

	@Test
	void updateBookKeepsCopiesOnLoanOutOfAvailability() {
		var existing = newBook(1);
		existing.setTotalCopies(3);
		existing.setAvailableCopies(1);
		when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));

		var update = newBook(1);
		update.setTotalCopies(5);
		bookService.updateBook(update);

		assertEquals(3, update.getAvailableCopies());
		verify(bookRepository).save(update);
	}

	@Test
	void updateBookCannotReduceTotalBelowCopiesOnLoan() {
		var existing = newBook(1);
		existing.setTotalCopies(3);
		existing.setAvailableCopies(1);
		when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));

		var update = newBook(1);
		update.setTotalCopies(1);

		assertThrows(IllegalArgumentException.class, () -> bookService.updateBook(update));
		verify(bookRepository, never()).save(update);
	}

	@Test
	void deleteBookRemovesExistingAndRejectsUnknown() {
		when(bookRepository.findById(1L)).thenReturn(Optional.of(newBook(1)));
		when(bookRepository.findById(2L)).thenReturn(Optional.empty());

		bookService.deleteBook(1L);

		verify(bookRepository).deleteById(1L);
		assertThrows(NotFoundException.class, () -> bookService.deleteBook(2L));
	}

	@Test
	void findPaginatedReturnsRequestedPage() {
		when(bookRepository.findAll()).thenReturn(List.of(newBook(1), newBook(2), newBook(3)));

		var page = bookService.findPaginated(PageRequest.of(1, 2));

		assertEquals(1, page.getContent().size());
		assertEquals(3, page.getTotalElements());
		assertEquals(2, page.getTotalPages());
	}
}
