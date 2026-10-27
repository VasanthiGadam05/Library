package com.knf.dev.librarymanagementsystem.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.knf.dev.librarymanagementsystem.service.AuthorService;
import com.knf.dev.librarymanagementsystem.service.BookService;
import com.knf.dev.librarymanagementsystem.service.CategoryService;
import com.knf.dev.librarymanagementsystem.service.PublisherService;

/**
 * MLG1-14: smoke-level CRUD coverage through the MVC layer for books, authors,
 * categories and publishers (authenticated as admin).
 */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "ADMIN")
class CrudControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private BookService bookService;

	@Autowired
	private AuthorService authorService;

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private PublisherService publisherService;

	private static String unique(String prefix) {
		return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
	}

	@Test
	void authorCreateReadUpdateDelete() throws Exception {
		var name = unique("author");
		mockMvc.perform(post("/add-author").with(csrf()).param("name", name).param("description", "d"))
				.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/authors"));
		var created = authorService.findAllAuthors().stream().filter(a -> a.getName().equals(name)).findFirst()
				.orElseThrow();

		mockMvc.perform(get("/updateAuthor/" + created.getId())).andExpect(status().isOk())
				.andExpect(view().name("update-author"));
		mockMvc.perform(post("/update-author/" + created.getId()).with(csrf()).param("name", name + "-v2")
				.param("description", "d2")).andExpect(redirectedUrl("/authors"));
		assertEquals(name + "-v2", authorService.findAuthorById(created.getId()).getName());

		mockMvc.perform(get("/remove-author/" + created.getId())).andExpect(redirectedUrl("/authors"));
		assertFalse(authorService.findAllAuthors().stream().anyMatch(a -> a.getId().equals(created.getId())));
	}

	@Test
	void categoryCreateReadUpdateDelete() throws Exception {
		var name = unique("cat");
		mockMvc.perform(post("/add-category").with(csrf()).param("name", name)).andExpect(redirectedUrl("/categories"));
		var created = categoryService.findAllCategories().stream().filter(c -> c.getName().equals(name)).findFirst()
				.orElseThrow();

		mockMvc.perform(get("/updateCategory/" + created.getId())).andExpect(status().isOk())
				.andExpect(view().name("update-category"));
		mockMvc.perform(post("/update-category/" + created.getId()).with(csrf()).param("name", name + "-v2"))
				.andExpect(redirectedUrl("/categories"));
		assertEquals(name + "-v2", categoryService.findCategoryById(created.getId()).getName());

		mockMvc.perform(get("/remove-category/" + created.getId())).andExpect(redirectedUrl("/categories"));
		assertFalse(categoryService.findAllCategories().stream().anyMatch(c -> c.getId().equals(created.getId())));
	}

	@Test
	void publisherCreateReadUpdateDelete() throws Exception {
		var name = unique("pub");
		mockMvc.perform(post("/add-publisher").with(csrf()).param("name", name))
				.andExpect(redirectedUrl("/publishers"));
		var created = publisherService.findAllPublishers().stream().filter(p -> p.getName().equals(name)).findFirst()
				.orElseThrow();

		mockMvc.perform(get("/updatePublisher/" + created.getId())).andExpect(status().isOk())
				.andExpect(view().name("update-publisher"));
		mockMvc.perform(post("/update-publisher/" + created.getId()).with(csrf()).param("name", name + "-v2"))
				.andExpect(redirectedUrl("/publishers"));
		assertEquals(name + "-v2", publisherService.findPublisherById(created.getId()).getName());

		mockMvc.perform(get("/remove-publisher/" + created.getId())).andExpect(redirectedUrl("/publishers"));
		assertFalse(publisherService.findAllPublishers().stream().anyMatch(p -> p.getId().equals(created.getId())));
	}

	@Test
	void bookCreateReadUpdateDelete() throws Exception {
		var isbn = unique("ISBN");
		mockMvc.perform(post("/add-book").with(csrf()).param("isbn", isbn).param("name", "Test Book")
				.param("serialName", "S1").param("description", "d").param("totalCopies", "3"))
				.andExpect(redirectedUrl("/books"));
		var created = bookService.searchBooks(isbn).get(0);
		assertEquals(3, created.getTotalCopies());
		assertEquals(3, created.getAvailableCopies());

		mockMvc.perform(get("/update/" + created.getId())).andExpect(status().isOk())
				.andExpect(view().name("update-book"));
		mockMvc.perform(post("/update-book/" + created.getId()).with(csrf()).param("isbn", isbn)
				.param("name", "Renamed").param("serialName", "S1").param("description", "d")
				.param("totalCopies", "4")).andExpect(redirectedUrl("/books"));
		var updated = bookService.findBookById(created.getId());
		assertEquals("Renamed", updated.getName());
		assertEquals(4, updated.getTotalCopies());
		assertEquals(4, updated.getAvailableCopies());

		mockMvc.perform(get("/searchBook").param("keyword", isbn)).andExpect(status().isOk())
				.andExpect(view().name("list-books"));

		mockMvc.perform(get("/remove-book/" + created.getId())).andExpect(redirectedUrl("/books"));
		assertTrue(bookService.searchBooks(isbn).isEmpty());
	}

	@Test
	void seededBookDetailPageRenders() throws Exception {
		var seeded = bookService.searchBooks("AP1287").get(0);
		mockMvc.perform(get("/book/" + seeded.getId())).andExpect(status().isOk())
				.andExpect(view().name("list-book")).andExpect(model().attributeExists("book"));
	}

	@Test
	void createBookWithZeroCopiesShowsFormWithError() throws Exception {
		mockMvc.perform(post("/add-book").with(csrf()).param("isbn", unique("ISBN")).param("name", "Zero")
				.param("serialName", "S").param("description", "d").param("totalCopies", "0"))
				.andExpect(status().isOk()).andExpect(view().name("add-book"))
				.andExpect(model().attributeExists("errorMessage"));
	}
}
