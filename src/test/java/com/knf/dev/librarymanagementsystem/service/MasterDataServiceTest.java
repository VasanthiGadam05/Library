package com.knf.dev.librarymanagementsystem.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.knf.dev.librarymanagementsystem.entity.Author;
import com.knf.dev.librarymanagementsystem.entity.Category;
import com.knf.dev.librarymanagementsystem.entity.Publisher;
import com.knf.dev.librarymanagementsystem.exception.NotFoundException;
import com.knf.dev.librarymanagementsystem.repository.AuthorRepository;
import com.knf.dev.librarymanagementsystem.repository.CategoryRepository;
import com.knf.dev.librarymanagementsystem.repository.PublisherRepository;
import com.knf.dev.librarymanagementsystem.service.impl.AuthorServiceImpl;
import com.knf.dev.librarymanagementsystem.service.impl.CategoryServiceImpl;
import com.knf.dev.librarymanagementsystem.service.impl.PublisherServiceImpl;

/**
 * Smoke-level CRUD unit tests for the author, category and publisher services.
 */
@ExtendWith(MockitoExtension.class)
class MasterDataServiceTest {

	@Mock
	private AuthorRepository authorRepository;

	@Mock
	private CategoryRepository categoryRepository;

	@Mock
	private PublisherRepository publisherRepository;

	@Test
	void authorCrud() {
		var service = new AuthorServiceImpl(authorRepository);
		var author = new Author("Matt", "desc");
		author.setId(1L);
		when(authorRepository.findAll()).thenReturn(List.of(author));
		when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
		when(authorRepository.findById(2L)).thenReturn(Optional.empty());

		assertEquals(1, service.findAllAuthors().size());
		assertSame(author, service.findAuthorById(1L));
		assertThrows(NotFoundException.class, () -> service.findAuthorById(2L));
		service.createAuthor(author);
		service.updateAuthor(author);
		verify(authorRepository, times(2)).save(author);
		service.deleteAuthor(1L);
		verify(authorRepository).deleteById(1L);
		assertThrows(NotFoundException.class, () -> service.deleteAuthor(2L));
	}

	@Test
	void categoryCrud() {
		var service = new CategoryServiceImpl(categoryRepository);
		var category = new Category("Fiction");
		category.setId(1L);
		when(categoryRepository.findAll()).thenReturn(List.of(category));
		when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
		when(categoryRepository.findById(2L)).thenReturn(Optional.empty());

		assertEquals(1, service.findAllCategories().size());
		assertSame(category, service.findCategoryById(1L));
		assertThrows(NotFoundException.class, () -> service.findCategoryById(2L));
		service.createCategory(category);
		service.updateCategory(category);
		verify(categoryRepository, times(2)).save(category);
		service.deleteCategory(1L);
		verify(categoryRepository).deleteById(1L);
		assertThrows(NotFoundException.class, () -> service.deleteCategory(2L));
	}

	@Test
	void publisherCrud() {
		var service = new PublisherServiceImpl(publisherRepository);
		var publisher = new Publisher("Acme");
		publisher.setId(1L);
		when(publisherRepository.findAll()).thenReturn(List.of(publisher));
		when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
		when(publisherRepository.findById(2L)).thenReturn(Optional.empty());

		assertEquals(1, service.findAllPublishers().size());
		assertSame(publisher, service.findPublisherById(1L));
		assertThrows(NotFoundException.class, () -> service.findPublisherById(2L));
		service.createPublisher(publisher);
		service.updatePublisher(publisher);
		verify(publisherRepository, times(2)).save(publisher);
		service.deletePublisher(1L);
		verify(publisherRepository).deleteById(1L);
		assertThrows(NotFoundException.class, () -> service.deletePublisher(2L));
	}
}
