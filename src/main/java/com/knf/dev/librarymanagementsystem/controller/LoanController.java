package com.knf.dev.librarymanagementsystem.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.knf.dev.librarymanagementsystem.exception.LoanAlreadyReturnedException;
import com.knf.dev.librarymanagementsystem.exception.UnavailableBookException;
import com.knf.dev.librarymanagementsystem.service.BookService;
import com.knf.dev.librarymanagementsystem.service.LoanService;

@Controller
public class LoanController {

	private static final int DEFAULT_LOAN_DAYS = 14;

	private final LoanService loanService;
	private final BookService bookService;

	public LoanController(LoanService loanService, BookService bookService) {
		this.loanService = loanService;
		this.bookService = bookService;
	}

	@GetMapping("/loans/open")
	public String findOpenLoans(Model model) {

		model.addAttribute("loans", loanService.findOpenLoans());
		model.addAttribute("today", LocalDate.now());
		return "loans-open";
	}

	@GetMapping("/books/{id}/borrow")
	public String showBorrowForm(@PathVariable("id") Long id, Model model) {

		model.addAttribute("book", bookService.findBookById(id));
		model.addAttribute("defaultDueDate", LocalDate.now().plusDays(DEFAULT_LOAN_DAYS));
		model.addAttribute("today", LocalDate.now());
		return "book-borrow-form";
	}

	@PostMapping("/books/{id}/borrow")
	public String borrowBook(@PathVariable("id") Long id, @RequestParam("borrowerName") String borrowerName,
			@RequestParam("dueDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
			RedirectAttributes redirectAttributes) {

		try {
			var loan = loanService.borrow(id, borrowerName, dueDate);
			redirectAttributes.addFlashAttribute("successMessage",
					String.format("\"%s\" issued to %s, due on %s", loan.getBook().getName().trim(),
							loan.getBorrowerName(), loan.getDueDate()));
			return "redirect:/loans/open";
		} catch (UnavailableBookException | IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
			return "redirect:/books/" + id + "/borrow";
		}
	}

	@PostMapping("/loans/{loanId}/return")
	public String returnLoan(@PathVariable("loanId") Long loanId, RedirectAttributes redirectAttributes) {

		try {
			var loan = loanService.returnLoan(loanId);
			redirectAttributes.addFlashAttribute("successMessage",
					String.format("\"%s\" returned by %s", loan.getBook().getName().trim(), loan.getBorrowerName()));
		} catch (LoanAlreadyReturnedException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}
		return "redirect:/loans/open";
	}

}
