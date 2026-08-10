package com.gymmate.scheduling.internal.service;

import com.gymmate.scheduling.internal.domain.ClassBooking;
import com.gymmate.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Acquires the per-schedule booking lock BEFORE opening a DB transaction (avoids
 * holding a pooled connection while waiting on Redis — see the plan's pool-exhaustion
 * note) and delegates the actual work to {@link ClassBookingService}'s transactional
 * methods. This has to be a separate bean: {@code ClassBookingService} is
 * {@code @Transactional} at the class level, so calling it here (rather than via
 * self-invocation within that class) goes through the Spring proxy correctly.
 *
 * <p>{@code tryLock(waitTime, unit)} is called with NO lease argument, so Redisson's
 * watchdog renews the lease for as long as this thread holds it — a fixed lease that
 * can expire mid-transaction is what causes overbooking, not what prevents it.
 *
 * <p>This lock is a contention optimiser, not the correctness guarantee: the DB
 * conditional UPDATE + trigger (V13 migration) is what actually prevents overbooking,
 * even if Redis is unavailable or the lock is lost to a failover.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClassBookingCoordinator {

  private static final long LOCK_WAIT_SECONDS = 5;

  private final ClassBookingService classBookingService;
  private final RedissonClient redissonClient;

  public ClassBooking createBooking(UUID gymId, UUID memberId, UUID scheduleId, String memberNotes) {
    return withScheduleLock(scheduleId, () -> classBookingService.createBooking(gymId, memberId, scheduleId, memberNotes));
  }

  public ClassBooking cancelBooking(UUID bookingId, String reason) {
    // The booking's schedule isn't known until it's read; a plain read is fine
    // without the lock, only the seat-count mutation needs it.
    ClassBooking booking = classBookingService.getBooking(bookingId);
    return withScheduleLock(booking.getClassScheduleId(), () -> classBookingService.cancelBooking(bookingId, reason));
  }

  private ClassBooking withScheduleLock(UUID scheduleId, java.util.function.Supplier<ClassBooking> action) {
    RLock lock = redissonClient.getLock("booking:schedule:" + scheduleId);
    boolean acquired;
    try {
      acquired = lock.tryLock(LOCK_WAIT_SECONDS, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new DomainException("BOOKING_LOCK_INTERRUPTED", "Interrupted while waiting for booking availability");
    }
    if (!acquired) {
      throw new DomainException("BOOKING_LOCK_TIMEOUT", "This class is busy right now, please try again");
    }
    try {
      return action.get();
    } finally {
      if (lock.isHeldByCurrentThread()) {
        lock.unlock();
      }
    }
  }
}
