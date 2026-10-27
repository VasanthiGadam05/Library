package com.knf.dev.librarymanagementsystem.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * MLG1-14: unauthenticated users are sent to the login page, authenticated
 * admins can reach the admin pages and get the expected view.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityAccessIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@ParameterizedTest
	@ValueSource(strings = { "/", "/books", "/authors", "/categories", "/publishers", "/loans/open", "/add",
			"/addAuthor", "/addCategory", "/addPublisher", "/books/1/borrow", "/export/all-book" })
	void unauthenticatedGetIsRedirectedToLogin(String url) throws Exception {
		mockMvc.perform(get(url)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrlPattern("**/login"));
	}

	@Test
	void unauthenticatedBorrowAndReturnPostsAreRedirectedToLogin() throws Exception {
		mockMvc.perform(post("/books/1/borrow").with(csrf()).param("borrowerName", "x").param("dueDate", "2099-01-01"))
				.andExpect(status().is3xxRedirection()).andExpect(redirectedUrlPattern("**/login"));
		mockMvc.perform(post("/loans/1/return").with(csrf())).andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrlPattern("**/login"));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void postWithoutCsrfTokenIsForbiddenEvenForAdmin() throws Exception {
		mockMvc.perform(post("/loans/1/return")).andExpect(status().isForbidden());
	}

	@Test
	void loginPageIsPublic() throws Exception {
		mockMvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("login"));
	}

	@ParameterizedTest
	@CsvSource({ "/books, list-books, books", "/, list-books, books", "/authors, list-authors, authors",
			"/categories, list-categories, categories", "/publishers, list-publishers, publishers",
			"/loans/open, loans-open, loans", "/add, add-book, book", "/addAuthor, add-author, author",
			"/addCategory, add-category, category", "/addPublisher, add-publisher, publisher" })
	@WithMockUser(roles = "ADMIN")
	void adminCanOpenAdminPages(String url, String viewName, String modelAttribute) throws Exception {
		mockMvc.perform(get(url)).andExpect(status().isOk()).andExpect(view().name(viewName))
				.andExpect(model().attributeExists(modelAttribute));
	}
}
