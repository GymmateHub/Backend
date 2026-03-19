package com.gymmate.classes.api;

import com.gymmate.classes.api.dto.BookingResponse;
import com.gymmate.classes.api.dto.CreateBookingRequest;
import com.gymmate.classes.application.ClassBookingService;
import com.gymmate.classes.domain.ClassBooking;
import com.gymmate.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking", description = "Booking management operations")
public class ClassBookingController {

  private final ClassBookingService bookingService;

  @PostMapping
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER', 'MEMBER')")
  @Operation(summary = "Create booking", description = "Book a class or join waitlist if full")
  public ResponseEntity<ApiResponse<BookingResponse>> createBooking(@Valid @RequestBody CreateBookingRequest request) {
    ClassBooking booking = bookingService.createBooking(request.gymId(), request.memberId(), request.scheduleId(), request.memberNotes());
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(BookingResponse.from(booking), "Booking created"));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER', 'MEMBER')")
  public ResponseEntity<ApiResponse<BookingResponse>> getBooking(@PathVariable UUID id) {
    ClassBooking booking = bookingService.getBooking(id);
    return ResponseEntity.ok(ApiResponse.success(BookingResponse.from(booking)));
  }

  @GetMapping("/member/{memberId}")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER', 'MEMBER')")
  public ResponseEntity<ApiResponse<List<BookingResponse>>> getBookingsByMember(@PathVariable UUID memberId) {
    List<ClassBooking> bookings = bookingService.getBookingsByMember(memberId);
    List<BookingResponse> res = bookings.stream().map(BookingResponse::from).collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(res));
  }

  @GetMapping("/schedule/{scheduleId}")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER')")
  public ResponseEntity<ApiResponse<List<BookingResponse>>> getBookingsBySchedule(@PathVariable UUID scheduleId) {
    List<ClassBooking> bookings = bookingService.getBookingsBySchedule(scheduleId);
    List<BookingResponse> res = bookings.stream().map(BookingResponse::from).collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(res));
  }

  @GetMapping("/schedule/{scheduleId}/waitlist")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER')")
  @Operation(summary = "Get waitlist", description = "Get ordered waitlist for a class schedule")
  public ResponseEntity<ApiResponse<List<BookingResponse>>> getWaitlist(@PathVariable UUID scheduleId) {
    List<ClassBooking> waitlist = bookingService.getWaitlist(scheduleId);
    List<BookingResponse> res = waitlist.stream().map(BookingResponse::from).collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(res));
  }

  @PutMapping("/{id}/cancel")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER', 'MEMBER')")
  public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(@PathVariable UUID id, @RequestParam(required = false) String reason) {
    ClassBooking booking = bookingService.cancelBooking(id, reason == null ? "Cancelled by user" : reason);
    return ResponseEntity.ok(ApiResponse.success(BookingResponse.from(booking), "Booking cancelled"));
  }

  @PutMapping("/{id}/check-in")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER')")
  public ResponseEntity<ApiResponse<BookingResponse>> checkIn(@PathVariable UUID id) {
    ClassBooking booking = bookingService.checkIn(id);
    return ResponseEntity.ok(ApiResponse.success(BookingResponse.from(booking), "Checked in"));
  }

  @PutMapping("/{id}/check-out")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER')")
  public ResponseEntity<ApiResponse<BookingResponse>> checkOut(@PathVariable UUID id) {
    ClassBooking booking = bookingService.checkOut(id);
    return ResponseEntity.ok(ApiResponse.success(BookingResponse.from(booking), "Checked out"));
  }
}
