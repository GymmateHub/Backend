package com.gymmate.classes.api;

import com.gymmate.classes.api.dto.BookingResponse;
import com.gymmate.classes.api.dto.CreateBookingRequest;
import com.gymmate.classes.application.ClassBookingService;
import com.gymmate.classes.domain.ClassBooking;
import com.gymmate.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
  public ResponseEntity<ApiResponse<BookingResponse>> createBooking(@Valid @RequestBody CreateBookingRequest request) {
    ClassBooking booking = bookingService.createBooking(request.gymId(), request.memberId(), request.scheduleId(), request.memberNotes());
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(BookingResponse.from(booking), "Booking created"));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<BookingResponse>> getBooking(@PathVariable UUID id) {
    ClassBooking booking = bookingService.getBooking(id);
    return ResponseEntity.ok(ApiResponse.success(BookingResponse.from(booking)));
  }

  @GetMapping("/member/{memberId}")
  public ResponseEntity<ApiResponse<List<BookingResponse>>> getBookingsByMember(@PathVariable UUID memberId) {
    List<ClassBooking> bookings = bookingService.getBookingsByMember(memberId);
    List<BookingResponse> res = bookings.stream().map(BookingResponse::from).collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(res));
  }

  @GetMapping("/schedule/{scheduleId}")
  public ResponseEntity<ApiResponse<List<BookingResponse>>> getBookingsBySchedule(@PathVariable UUID scheduleId) {
    List<ClassBooking> bookings = bookingService.getBookingsBySchedule(scheduleId);
    List<BookingResponse> res = bookings.stream().map(BookingResponse::from).collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(res));
  }

  @PutMapping("/{id}/cancel")
  public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(@PathVariable UUID id, @RequestParam(required = false) String reason) {
    ClassBooking booking = bookingService.cancelBooking(id, reason == null ? "Cancelled by user" : reason);
    return ResponseEntity.ok(ApiResponse.success(BookingResponse.from(booking), "Booking cancelled"));
  }

  @PutMapping("/{id}/check-in")
  public ResponseEntity<ApiResponse<BookingResponse>> checkIn(@PathVariable UUID id) {
    ClassBooking booking = bookingService.checkIn(id);
    return ResponseEntity.ok(ApiResponse.success(BookingResponse.from(booking), "Checked in"));
  }

  @PutMapping("/{id}/check-out")
  public ResponseEntity<ApiResponse<BookingResponse>> checkOut(@PathVariable UUID id) {
    ClassBooking booking = bookingService.checkOut(id);
    return ResponseEntity.ok(ApiResponse.success(BookingResponse.from(booking), "Checked out"));
  }
}
