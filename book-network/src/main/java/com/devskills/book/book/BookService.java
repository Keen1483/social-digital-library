package com.devskills.book.book;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.devskills.book.common.PageResponse;
import com.devskills.book.exception.OperationNotPermittedException;
import com.devskills.book.file.FileStorageService;
import com.devskills.book.history.BookTransactionHistory;
import com.devskills.book.history.BookTransactionHistoryRepository;
import com.devskills.book.user.User;
import static com.devskills.book.book.BookSpecification.withOwnerId;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookService {
	
	private final BookRepository bookRepository;
	private final BookTransactionHistoryRepository transactionHistoryRepository;
	private final FileStorageService  fileStorageService;
	private final BookMapper bookMapper;
	
	public Integer save(@Valid BookRequest request, Authentication connectedUser) {
		// User user = (User) connectedUser.getPrincipal();
		Book book = bookMapper.toBook(request);
		// book.setOwner(user);
		return bookRepository.save(book).getId();
	}

	public BookResponse findById(Integer bookId) {
		return bookRepository.findById(bookId)
				.map(bookMapper::toBookResponse)
				.orElseThrow(() -> new EntityNotFoundException("No book found with the ID:: " + bookId));
	}

	public PageResponse<BookResponse> findAllBooks(int page, int size, Authentication connectedUser) {
		// User user = (User) connectedUser.getPrincipal();
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
		Page<Book> books = bookRepository.findAllDisplayableBooks(pageable, connectedUser.getName());
		
		List<BookResponse> bookResponses = books.stream()
				.map(bookMapper::toBookResponse)
				.toList();
		return new PageResponse<>(
				bookResponses,
				books.getNumber(),
				books.getSize(),
				books.getTotalElements(),
				books.getTotalPages(),
				books.isFirst(),
				books.isLast()
		);
	}

	public PageResponse<BookResponse> findAllBooksByOwner(int page, int size, Authentication connectedUser) {
		//User user = (User) connectedUser.getPrincipal();
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
		Page<Book> books = bookRepository.findAll(withOwnerId(connectedUser.getName()), pageable);
		
		List<BookResponse> bookResponses = books.stream()
				.map(bookMapper::toBookResponse)
				.toList();
		return new PageResponse<>(
				bookResponses,
				books.getNumber(),
				books.getSize(),
				books.getTotalElements(),
				books.getTotalPages(),
				books.isFirst(),
				books.isLast()
		);
	}

	public PageResponse<BorrowedBookResponse> findAllBorrowedBooks(int page, int size, Authentication connectedUser) {
		// User user = (User) connectedUser.getPrincipal();
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
		Page<BookTransactionHistory> allBorrowedBooks = transactionHistoryRepository.findAllBorrowedBooks(pageable, connectedUser.getName());
		
		List<BorrowedBookResponse> bookResponses = allBorrowedBooks.stream()
				.map(bookMapper::toBorrowedBookResponse)
				.toList();
		return new PageResponse<>(
				bookResponses,
				allBorrowedBooks.getNumber(),
				allBorrowedBooks.getSize(),
				allBorrowedBooks.getTotalElements(),
				allBorrowedBooks.getTotalPages(),
				allBorrowedBooks.isFirst(),
				allBorrowedBooks.isLast()
		);
	}

	public PageResponse<BorrowedBookResponse> findAllReturnedBooks(int page, int size, Authentication connectedUser) {
		// User user = (User) connectedUser.getPrincipal();
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
		Page<BookTransactionHistory> allBorrowedBooks = transactionHistoryRepository.findAllReturnedBooks(pageable, connectedUser.getName());
		
		List<BorrowedBookResponse> bookResponses = allBorrowedBooks.stream()
				.map(bookMapper::toBorrowedBookResponse)
				.toList();
		return new PageResponse<>(
				bookResponses,
				allBorrowedBooks.getNumber(),
				allBorrowedBooks.getSize(),
				allBorrowedBooks.getTotalElements(),
				allBorrowedBooks.getTotalPages(),
				allBorrowedBooks.isFirst(),
				allBorrowedBooks.isLast()
		);
	}

	public Integer updateShareableStatus(Integer bookId, Authentication connectedUser) {
		Book book = bookRepository.findById(bookId)
				.orElseThrow(() -> new EntityNotFoundException("No book found with the ID::" + bookId));
		// User user = (User) connectedUser.getPrincipal();
		if (!Objects.equals(book.getCreatedBy(), connectedUser.getName())) {
			throw new OperationNotPermittedException("You cannot update others books shareable status");
		}
		book.setShareable(!book.isShareable());
		return bookRepository.save(book).getId();
	}

	public Integer updateArchivedStatus(Integer bookId, Authentication connectedUser) {
		Book book = bookRepository.findById(bookId)
				.orElseThrow(() -> new EntityNotFoundException("No book found with the ID::" + bookId));
		// User user = (User) connectedUser.getPrincipal();
		if (!Objects.equals(book.getCreatedBy(), connectedUser.getName())) {
			throw new OperationNotPermittedException("You cannot update others books archived status");
		}
		book.setArchived(!book.isArchived());
		return bookRepository.save(book).getId();
	}

	public Integer borrowBook(Integer bookId, Authentication connectedUser) {
		Book book = bookRepository.findById(bookId)
				.orElseThrow(() -> new EntityNotFoundException("No book found with the ID::" + bookId));
		
		if (book.isArchived() || !book.isShareable()) {
			throw new OperationNotPermittedException("The requested book cannot be borrowed since it is archived ot not shareable");
		}
		// User user = (User) connectedUser.getPrincipal();
		if (Objects.equals(book.getCreatedBy(), connectedUser.getName())) {
			throw new OperationNotPermittedException("You cannot borrow your own book");
		}
		final boolean isAlreadyBorrowed = transactionHistoryRepository.isAlreadyBorrowedByUser(bookId, connectedUser.getName());
		if (isAlreadyBorrowed) {
			throw new OperationNotPermittedException("The requested book is already borrowed");
		}
		BookTransactionHistory bookTransactionHistory = BookTransactionHistory.builder()
				.userId(connectedUser.getName())
				.book(book)
				.returned(false)
				.returnApproved(false)
				.build();
		return transactionHistoryRepository.save(bookTransactionHistory).getId();
	}

	public Integer returnBorrowedBook(Integer bookId, Authentication connectedUser) {
		Book book = bookRepository.findById(bookId)
				.orElseThrow(() -> new EntityNotFoundException("No book found with the ID::" + bookId));
		if (book.isArchived() || !book.isShareable()) {
			throw new OperationNotPermittedException("The requested book cannot be borrowed since it is archived ot not shareable");
		}
		// User user = (User) connectedUser.getPrincipal();
		if (Objects.equals(book.getCreatedBy(), connectedUser.getName())) {
			throw new OperationNotPermittedException("You cannot borrow or return your own book");
		}
		BookTransactionHistory bookTransactionHistory = transactionHistoryRepository.findByBookIdAndUserId(bookId, connectedUser.getName())
				.orElseThrow(() -> new OperationNotPermittedException("You did not borrow this book"));
		bookTransactionHistory.setReturned(true);
		return transactionHistoryRepository.save(bookTransactionHistory).getId();
	}

	public Integer approvedReturnBorrowedBook(Integer bookId, Authentication connectedUser) {
		Book book = bookRepository.findById(bookId)
				.orElseThrow(() -> new EntityNotFoundException("No book found with the ID::" + bookId));
		if (book.isArchived() || !book.isShareable()) {
			throw new OperationNotPermittedException("The requested book cannot be borrowed since it is archived or not shareable");
		}
		// User user = (User) connectedUser.getPrincipal();
		if (!Objects.equals(book.getCreatedBy(), connectedUser.getName())) {
			throw new OperationNotPermittedException("You cannot approved the return of a book you do not own");
		}
		BookTransactionHistory bookTransactionHistory = transactionHistoryRepository.findByBookIdAndOwnerId(bookId, connectedUser.getName())
				.orElseThrow(() -> new OperationNotPermittedException("The book is not returned yet. You cannot approve its return"));
		bookTransactionHistory.setReturnApproved(true);
		return transactionHistoryRepository.save(bookTransactionHistory).getId();
	}

	public void uploadBookCoverPicture(MultipartFile file, Authentication connectedUser, Integer bookId) {
		Book book = bookRepository.findById(bookId)
				.orElseThrow(() -> new EntityNotFoundException("No book found with the ID::" + bookId));
		// User user = (User) connectedUser.getPrincipal();
		var bookCover = fileStorageService.saveFile(file, connectedUser.getName());
		book.setBookCover(bookCover);
		bookRepository.save(book);
	}

}
