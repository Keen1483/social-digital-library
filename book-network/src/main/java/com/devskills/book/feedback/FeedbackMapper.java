package com.devskills.book.feedback;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.devskills.book.book.Book;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FeedbackMapper {
	
	public Feedback toFeedback(@Valid FeedbackRequest request) {
		return Feedback.builder()
				.note(request.note())
				.comment(request.comment())
				.book(Book.builder()
						.id(request.bookId())
						.build()
				)
				.build();
	}

	public FeedbackResponse toFeedbackResponse(Feedback feedback, String id) {
		return FeedbackResponse.builder()
				.note(feedback.getNote())
				.comment(feedback.getComment())
				.ownFeedback(Objects.equals(feedback.getCreatedBy(), id))
				.build();
	}

}
