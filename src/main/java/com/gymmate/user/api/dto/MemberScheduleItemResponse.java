package com.gymmate.user.api.dto;

import com.gymmate.classes.domain.ClassSchedule;
import com.gymmate.classes.domain.GymClass;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A class schedule entry enriched with class details,
 * shaped for the member mobile app's class list.
 */
public record MemberScheduleItemResponse(
    UUID scheduleId,
    UUID classId,
    String className,
    String description,
    String trainerName,
    LocalDateTime startTime,
    LocalDateTime endTime,
    Integer durationMinutes,
    Integer capacity,
    Integer bookedCount,
    BigDecimal price,
    Integer creditsRequired,
    String skillLevel,
    String imageUrl,
    String status) {

  public static MemberScheduleItemResponse from(
      ClassSchedule schedule, GymClass gymClass, int bookedCount, String trainerName) {
    return new MemberScheduleItemResponse(
        schedule.getId(),
        schedule.getClassId(),
        gymClass != null ? gymClass.getName() : null,
        gymClass != null ? gymClass.getDescription() : null,
        trainerName,
        schedule.getStartTime(),
        schedule.getEndTime(),
        gymClass != null ? gymClass.getDurationMinutes() : null,
        schedule.getCapacityOverride() != null
            ? schedule.getCapacityOverride()
            : (gymClass != null ? gymClass.getCapacity() : null),
        bookedCount,
        schedule.getPriceOverride() != null
            ? schedule.getPriceOverride()
            : (gymClass != null ? gymClass.getPrice() : null),
        gymClass != null ? gymClass.getCreditsRequired() : null,
        gymClass != null ? gymClass.getSkillLevel() : null,
        gymClass != null ? gymClass.getImageUrl() : null,
        schedule.getStatus() != null ? schedule.getStatus().name() : null);
  }
}
