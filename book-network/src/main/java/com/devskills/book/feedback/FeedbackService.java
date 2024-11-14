package com.devskills.book.feedback;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.devskills.book.book.Book;
import com.devskills.book.book.BookRepository;
import com.devskills.book.common.PageResponse;
import com.devskills.book.exception.OperationNotPermittedException;
import com.devskills.book.user.User;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FeedbackService {
	
	private final BookRepository bookRepository;
	private final FeedbackMapper feedbackMapper;
	private final FeedbackRepository feedbackRepository;
	
	public Integer save(@Valid FeedbackRequest request, Authentication connectedUser) {
		Book book = bookRepository.findById(request.bookId())
				.orElseThrow(() -> new EntityNotFoundException("No book found with the ID::" + request.bookId()));
		
		if (book.isArchived() || !book.isShareable()) {
			throw new OperationNotPermittedException("You cannot give a feedback for an archived ot not shareable book");
		}
		// User user = (User) connectedUser.getPrincipal();
		if (Objects.equals(book.getCreatedBy(), connectedUser.getName())) {
			throw new OperationNotPermittedException("You cannot give a feedback to your own book");
		}
		Feedback feedback = feedbackMapper.toFeedback(request);
		return feedbackRepository.save(feedback).getId();
	}

	public PageResponse<FeedbackResponse> findAllFeedbacksByBook(Integer bookId, int page, int size, Authentication connectedUser) {
		Pageable pageable = PageRequest.of(page, size);
		// User user = (User) connectedUser.getPrincipal();
		Page<Feedback> feedbacks = feedbackRepository.findAllByBookId(bookId, pageable);
		List<FeedbackResponse> feedbackResponses = feedbacks.stream()
				.map(f -> feedbackMapper.toFeedbackResponse(f, connectedUser.getName()))
				.toList();
		return new PageResponse<>(
				feedbackResponses,
				feedbacks.getNumber(),
				feedbacks.getSize(),
				feedbacks.getTotalElements(),
				feedbacks.getTotalPages(),
				feedbacks.isFirst(),
				feedbacks.isLast()
		);
	}

}
