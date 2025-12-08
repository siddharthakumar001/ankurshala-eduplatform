package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.session.AddBookmarkRequest;
import com.ankurshala.backend.dto.session.AddNoteRequest;
import com.ankurshala.backend.dto.session.JoinSessionResponse;
import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.exception.BusinessException;
import com.ankurshala.backend.repository.*;
import com.ankurshala.backend.util.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class ClassSessionService {

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TopicRepository topicRepository;
    @Autowired
    private BookingNoteRepository bookingNoteRepository;
    @Autowired
    private BookingBookmarkRepository bookingBookmarkRepository;

    public JoinSessionResponse joinSession(Long userId, Long bookingId) {
        String traceId = TraceUtil.getTraceId();
        log.info("User {} joining session for booking {} - TraceId: {}", userId, bookingId, traceId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("Booking not found", HttpStatus.NOT_FOUND, "BOOKING_NOT_FOUND"));

        // Verify user has access to this booking
        if (!booking.getStudentId().equals(userId) && !booking.getTeacherId().equals(userId)) {
            throw new BusinessException("Access denied to this booking", HttpStatus.FORBIDDEN, "ACCESS_DENIED");
        }

        // Check if session is active (within 15 minutes of start time)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sessionStart = booking.getStartTs().toLocalDateTime();
        LocalDateTime sessionEnd = booking.getEndTs().toLocalDateTime();

        if (now.isBefore(sessionStart.minusMinutes(15))) {
            throw new BusinessException("Session not yet available", HttpStatus.BAD_REQUEST, "SESSION_NOT_AVAILABLE");
        }

        if (now.isAfter(sessionEnd.plusMinutes(15))) {
            throw new BusinessException("Session has ended", HttpStatus.BAD_REQUEST, "SESSION_ENDED");
        }

        // Get topic and teacher details
        Topic topic = topicRepository.findById(booking.getTopicId())
                .orElseThrow(() -> new BusinessException("Topic not found", HttpStatus.NOT_FOUND, "TOPIC_NOT_FOUND"));

        User teacher = null;
        if (booking.getTeacherId() != null) {
            teacher = userRepository.findById(booking.getTeacherId())
                    .orElseThrow(() -> new BusinessException("Teacher not found", HttpStatus.NOT_FOUND, "TEACHER_NOT_FOUND"));
        }

        // Generate Zoom links (stub implementation)
        String zoomJoinLink = generateZoomJoinLink(bookingId, userId);
        String zoomHostLink = booking.getTeacherId() != null && booking.getTeacherId().equals(userId) 
                ? generateZoomHostLink(bookingId) : null;

        JoinSessionResponse response = new JoinSessionResponse();
        response.setZoomJoinLink(zoomJoinLink);
        response.setZoomHostLink(zoomHostLink);
        response.setSessionId("session_" + bookingId);
        response.setStartTime(sessionStart);
        response.setEndTime(sessionEnd);
        response.setTopicTitle(topic.getTitle());
        response.setTeacherName(teacher != null ? teacher.getName() : "TBD");

        log.info("Session join successful for user {} booking {} - TraceId: {}", userId, bookingId, traceId);
        return response;
    }

    public BookingNote addNote(Long userId, AddNoteRequest request) {
        String traceId = TraceUtil.getTraceId();
        log.info("Adding note to booking {} by user {} - TraceId: {}", request.getBookingId(), userId, traceId);

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new BusinessException("Booking not found", HttpStatus.NOT_FOUND, "BOOKING_NOT_FOUND"));

        // Verify user has access to this booking
        if (!booking.getStudentId().equals(userId) && !booking.getTeacherId().equals(userId)) {
            throw new BusinessException("Access denied to this booking", HttpStatus.FORBIDDEN, "ACCESS_DENIED");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

        BookingNote note = new BookingNote();
        note.setBookingId(request.getBookingId());
        note.setAuthorRole(user.getRole());
        note.setText(request.getText());
        note.setUrl(request.getUrl());

        BookingNote savedNote = bookingNoteRepository.save(note);
        log.info("Note added with ID {} to booking {} - TraceId: {}", savedNote.getId(), request.getBookingId(), traceId);

        return savedNote;
    }

    public BookingBookmark addBookmark(Long userId, AddBookmarkRequest request) {
        String traceId = TraceUtil.getTraceId();
        log.info("Adding bookmark to booking {} by user {} - TraceId: {}", request.getBookingId(), userId, traceId);

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new BusinessException("Booking not found", HttpStatus.NOT_FOUND, "BOOKING_NOT_FOUND"));

        // Verify user has access to this booking
        if (!booking.getStudentId().equals(userId) && !booking.getTeacherId().equals(userId)) {
            throw new BusinessException("Access denied to this booking", HttpStatus.FORBIDDEN, "ACCESS_DENIED");
        }

        BookingBookmark bookmark = new BookingBookmark();
        bookmark.setBookingId(request.getBookingId());
        bookmark.setTsSeconds(request.getTsSeconds());
        bookmark.setNote(request.getNote());

        BookingBookmark savedBookmark = bookingBookmarkRepository.save(bookmark);
        log.info("Bookmark added with ID {} to booking {} - TraceId: {}", savedBookmark.getId(), request.getBookingId(), traceId);

        return savedBookmark;
    }

    public List<BookingNote> getBookingNotes(Long userId, Long bookingId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Getting notes for booking {} by user {} - TraceId: {}", bookingId, userId, traceId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("Booking not found", HttpStatus.NOT_FOUND, "BOOKING_NOT_FOUND"));

        // Verify user has access to this booking
        if (!booking.getStudentId().equals(userId) && !booking.getTeacherId().equals(userId)) {
            throw new BusinessException("Access denied to this booking", HttpStatus.FORBIDDEN, "ACCESS_DENIED");
        }

        return bookingNoteRepository.findByBookingIdOrderByCreatedAtAsc(bookingId);
    }

    public List<BookingBookmark> getBookingBookmarks(Long userId, Long bookingId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Getting bookmarks for booking {} by user {} - TraceId: {}", bookingId, userId, traceId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("Booking not found", HttpStatus.NOT_FOUND, "BOOKING_NOT_FOUND"));

        // Verify user has access to this booking
        if (!booking.getStudentId().equals(userId) && !booking.getTeacherId().equals(userId)) {
            throw new BusinessException("Access denied to this booking", HttpStatus.FORBIDDEN, "ACCESS_DENIED");
        }

        return bookingBookmarkRepository.findByBookingIdOrderByTsSecondsAsc(bookingId);
    }

    private String generateZoomJoinLink(Long bookingId, Long userId) {
        // Stub implementation - in real scenario, integrate with Zoom API
        return "https://zoom.us/j/" + bookingId + "?pwd=join_" + userId;
    }

    private String generateZoomHostLink(Long bookingId) {
        // Stub implementation - in real scenario, integrate with Zoom API
        return "https://zoom.us/j/" + bookingId + "?pwd=host_" + bookingId;
    }
}
