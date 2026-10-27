package com.knf.dev.librarymanagementsystem.service.impl;

import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.knf.dev.librarymanagementsystem.entity.Book;
import com.knf.dev.librarymanagementsystem.exception.NotFoundException;
import com.knf.dev.librarymanagementsystem.repository.BookRepository;
import com.knf.dev.librarymanagementsystem.service.BookService;

@Service
public class BookServiceImpl implements BookService {

	private final BookRepository bookRepository;

	public BookServiceImpl(BookRepository bookRepository) {
		this.bookRepository = bookRepository;
	}

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@Override
	public List<Book> findAllBooks() {
		return bookRepository.findAll();
	}

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@Override
	public List<Book> searchBooks(String keyword) {
		if (keyword != null) {
			return bookRepository.search(keyword);
		}
		return bookRepository.findAll();
	}

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@Override
	public Book findBookById(Long id) {
		return bookRepository.findById(id)
				.orElseThrow(() -> new NotFoundException(String.format("Book not found with ID %d", id)));
	}

	@Override
	public void createBook(Book book) {
		if (book.getTotalCopies() < 1) {
			throw new IllegalArgumentException("Total copies must be at least 1");
		}
		// A new book has no active loans, so every copy is available.
		book.setAvailableCopies(book.getTotalCopies());
		bookRepository.save(book);
	}

	@Override
	public void updateBook(Book book) {
		if (book.getTotalCopies() < 0) {
			throw new IllegalArgumentException("Total copies must not be negative");
		}
		if (book.getId() != null) {
			var existing = bookRepository.findById(book.getId());
			if (existing.isPresent()) {
				// Keep availability consistent with copies currently out on loan.
				var onLoan = existing.get().getTotalCopies() - existing.get().getAvailableCopies();
				if (book.getTotalCopies() < onLoan) {
					throw new IllegalArgumentException(
							String.format("Total copies cannot be less than the %d copies currently on loan", onLoan));
				}
				book.setAvailableCopies(book.getTotalCopies() - onLoan);
			}
		}
		bookRepository.save(book);
	}

	@Override
	public void deleteBook(Long id) {
		var book = bookRepository.findById(id)
				.orElseThrow(() -> new NotFoundException(String.format("Book not found with ID %d", id)));

		bookRepository.deleteById(book.getId());
	}

	@Override
	public Page<Book> findPaginated(Pageable pageable) {
		long startTime = System.currentTimeMillis(); // 开始计时

		List<Book> allBooks = findAllBooks();
		int pageSize = pageable.getPageSize();
		int currentPage = pageable.getPageNumber();
		int startItem = currentPage * pageSize;
		List<Book> list;

		if (allBooks.size() < startItem) {
			list = Collections.emptyList();
		} else {
			int toIndex = Math.min(startItem + pageSize, allBooks.size());
			list = allBooks.subList(startItem, toIndex);
		}

		var bookPage = new PageImpl<>(list, PageRequest.of(currentPage, pageSize), allBooks.size());

		long endTime = System.currentTimeMillis(); // 结束计时
		System.out.println("Optimized method execution time: " + (endTime - startTime) + "ms");

		return bookPage;
	}



}
