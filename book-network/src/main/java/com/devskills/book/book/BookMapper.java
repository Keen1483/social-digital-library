package com.devskills.book.book;

import org.springframework.stereotype.Service;

import com.devskills.book.file.FileUtils;
import com.devskills.book.history.BookTransactionHistory;

import jakarta.validation.Valid;

@Service
public class BookMapper {

	public Book toBook(@Valid BookRequest request) {
		return Book.builder()
				.id(request.id())
				.title(request.title())
				.authorName(request.authorName())
				.isbn(request.isbn())
				.synopsis(request.synopsis())
				.archived(false)
				.shareable(request.shareable())
				.build();
	}
	
	public BookResponse toBookResponse(Book book) {
		return BookResponse.builder()
				.id(book.getId())
				.title(book.getTitle())
				.authorName(book.getAuthorName())
				.isbn(book.getIsbn())
				.synopsis(book.getSynopsis())
				.rate(book.getrate())
				.archived(book.isArchived())
				.shareable(book.isShareable())
				.owner(book.getOwner().fullName())
				.cover(FileUtils.readFileFromLocation(book.getBookCover()))
				.build();
	}
	
	public BorrowedBookResponse toBorrowedBookResponse(BookTransactionHistory history) {
		return BorrowedBookResponse.builder()
				.id(history.getBook().getId())
				.title(history.getBook().getTitle())
				.authorName(history.getBook().getAuthorName())
				.isbn(history.getBook().getIsbn())
				.rate(history.getBook().getrate())
				.returned(history.isReturned())
				.returnApproved(history.isReturnApproved())
				.build();
	}

}
