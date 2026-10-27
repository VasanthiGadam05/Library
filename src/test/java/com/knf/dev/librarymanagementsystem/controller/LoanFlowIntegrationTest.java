package com.knf.dev.librarymanagementsystem.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.knf.dev.librarymanagementsystem.entity.Book;
import com.knf.dev.librarymanagementsystem.service.BookService;
import com.knf.dev.librarymanagementsystem.service.LoanService;

/**
 * MLG1-13: end-to-end borrow / return flow through the web layer, service and
 * database (H2).
 */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "ADMIN")
class LoanFlowIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private BookService bookService;

	@Autowired
	private LoanService loanService;

	private Book createBook(int totalCopies) {
		var isbn = "LOAN-" + UUID.randomUUID().toString().substring(0, 8);
		var book = new Book(isbn, "Loan Test Book", "SER", "desc");
		book.setTotalCopies(totalCopies);
		bookService.createBook(book);
		return bookService.searchBooks(isbn).get(0);
	}

	private String dueDate() {
		return LocalDate.now().plusDays(10).toString();
	}

	@Test
	void borrowThenReturnUpdatesAvailabilityAndLoanRecord() throws Exception {
		var book = createBook(1);
		assertEquals(1, book.getAvailableCopies());

		mockMvc.perform(post("/books/" + book.getId() + "/borrow").with(csrf()).param("borrowerName", "Alice")
				.param("dueDate", dueDate())).andExpect(redirectedUrl("/loans/open"))
				.andExpect(flash().attributeExists("successMessage"));

		assertEquals(0, bookService.findBookById(book.getId()).getAvailableCopies());
		var loan = loanService.findOpenLoans().stream().filter(l -> l.getBook().getId().equals(book.getId()))
				.findFirst().orElseThrow();
		assertEquals("Alice", loan.getBorrowerName());
		assertEquals(LocalDate.now(), loan.getIssueDate());
		assertEquals(LocalDate.now().plusDays(10), loan.getDueDate());

		mockMvc.perform(post("/loans/" + loan.getId() + "/return").with(csrf())).andExpect(redirectedUrl("/loans/open"))
				.andExpect(flash().attributeExists("successMessage"));

		assertEquals(1, bookService.findBookById(book.getId()).getAvailableCopies());
		assertTrue(loanService.findOpenLoans().stream().noneMatch(l -> l.getId().equals(loan.getId())));
	}

	@Test
	void borrowIsBlockedWhenNoCopiesLeft() throws Exception {
		var book = createBook(1);
		var url = "/books/" + book.getId() + "/borrow";
		mockMvc.perform(post(url).with(csrf()).param("borrowerName", "Alice").param("dueDate", dueDate()))
				.andExpect(redirectedUrl("/loans/open"));

		mockMvc.perform(post(url).with(csrf()).param("borrowerName", "Bob").param("dueDate", dueDate()))
				.andExpect(redirectedUrl(url)).andExpect(flash().attributeExists("errorMessage"));

		assertEquals(0, bookService.findBookById(book.getId()).getAvailableCopies());
		assertEquals(1, loanService.findOpenLoans().stream().filter(l -> l.getBook().getId().equals(book.getId()))
				.count());
	}

	@Test
	void returningTheSameLoanTwiceDoesNotInflateCopies() throws Exception {
		var book = createBook(2);
		mockMvc.perform(post("/books/" + book.getId() + "/borrow").with(csrf()).param("borrowerName", "Alice")
				.param("dueDate", dueDate()));
		var loan = loanService.findOpenLoans().stream().filter(l -> l.getBook().getId().equals(book.getId()))
				.findFirst().orElseThrow();

		mockMvc.perform(post("/loans/" + loan.getId() + "/return").with(csrf()));
		mockMvc.perform(post("/loans/" + loan.getId() + "/return").with(csrf()))
				.andExpect(flash().attributeExists("errorMessage"));

		assertEquals(2, bookService.findBookById(book.getId()).getAvailableCopies());
	}

	@Test
	void borrowWithInvalidInputRedirectsBackWithMessage() throws Exception {
		var book = createBook(1);
		var url = "/books/" + book.getId() + "/borrow";

		mockMvc.perform(post(url).with(csrf()).param("borrowerName", "  ").param("dueDate", dueDate()))
				.andExpect(redirectedUrl(url)).andExpect(flash().attributeExists("errorMessage"));
		mockMvc.perform(post(url).with(csrf()).param("borrowerName", "Alice").param("dueDate",
				LocalDate.now().minusDays(1).toString())).andExpect(redirectedUrl(url))
				.andExpect(flash().attributeExists("errorMessage"));

		assertEquals(1, bookService.findBookById(book.getId()).getAvailableCopies());
	}

	@Test
	void borrowAndOpenLoanPagesRender() throws Exception {
		var book = createBook(1);
		mockMvc.perform(get("/books/" + book.getId() + "/borrow")).andExpect(status().isOk())
				.andExpect(view().name("book-borrow-form")).andExpect(model().attributeExists("book"))
				.andExpect(model().attributeExists("defaultDueDate"));

		mockMvc.perform(post("/books/" + book.getId() + "/borrow").with(csrf()).param("borrowerName", "Alice")
				.param("dueDate", dueDate()));
		var result = mockMvc.perform(get("/loans/open")).andExpect(status().isOk())
				.andExpect(view().name("loans-open")).andExpect(model().attributeExists("loans")).andReturn();
		assertNotNull(result.getResponse().getContentAsString());
		assertTrue(result.getResponse().getContentAsString().contains("Alice"));

		// the book list shows availability for the book that has no copy left
		var list = mockMvc.perform(get("/searchBook").param("keyword", book.getIsbn())).andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		assertTrue(list.contains("0 / 1"));
		assertTrue(list.contains("Unavailable"));
	}

	@Test
	void borrowUnknownBookIsHandledAsNotFoundNotServerCrash() throws Exception {
		mockMvc.perform(get("/books/999999/borrow")).andExpect(status().is4xxClientError());
	}
}
