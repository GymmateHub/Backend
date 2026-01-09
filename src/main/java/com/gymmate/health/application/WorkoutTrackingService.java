package com.gymmate.health.application;

import com.gymmate.health.domain.*;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application service for workout logging and tracking.
 * Handles creating workout logs with exercises, retrieving workout history, and calculating statistics.
 * Implements FR-014: Workout Logging.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WorkoutTrackingService {

    private final WorkoutLogRepository workoutLogRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final ExerciseRepository exerciseRepository;

    /**
     * Log a new workout with exercises.
     */
    @Transactional
    public WorkoutLog logWorkout(
            UUID organisationId,
            UUID gymId,
            UUID memberId,
            LocalDateTime workoutDate,
            String workoutName,
            Integer durationMinutes,
            Integer totalCaloriesBurned,
            WorkoutIntensity intensityLevel,
            String notes,
            List<WorkoutExerciseDetail> exercises
    ) {
        log.info("Logging workout for member {} at gym {}", memberId, gymId);

        // Validate inputs
        if (exercises == null || exercises.isEmpty()) {
            throw new DomainException("NO_EXERCISES", "Workout must contain at least one exercise");
        }

        if (workoutDate.isAfter(LocalDateTime.now())) {
            throw new DomainException("FUTURE_WORKOUT", "Cannot log workouts in the future");
        }

        // Verify all exercises exist
        for (WorkoutExerciseDetail detail : exercises) {
            exerciseRepository.findById(detail.exerciseId())
                .orElseThrow(() -> new ResourceNotFoundException("Exercise not found with ID: " + detail.exerciseId()));
        }

        // Create workout log
        WorkoutLog workoutLog = WorkoutLog.builder()
            .organisationId(organisationId)
            .gymId(gymId)
            .memberId(memberId)
            .workoutDate(workoutDate)
            .workoutName(workoutName)
            .durationMinutes(durationMinutes)
            .totalCaloriesBurned(totalCaloriesBurned)
            .intensityLevel(intensityLevel)
            .notes(notes)
            .status(WorkoutStatus.COMPLETED)
            .build();

        workoutLog.validate();
        WorkoutLog savedWorkout = workoutLogRepository.save(workoutLog);

        // Create workout exercises
        List<WorkoutExercise> workoutExercises = exercises.stream()
            .map(detail -> {
                WorkoutExercise exercise = WorkoutExercise.builder()
                    .workoutLogId(savedWorkout.getId())
                    .exerciseId(detail.exerciseId())
                    .exerciseOrder(detail.exerciseOrder())
                    .sets(detail.sets())
                    .reps(detail.reps())
                    .weight(detail.weight())
                    .weightUnit(detail.weightUnit())
                    .restSeconds(detail.restSeconds())
                    .distanceMeters(detail.distanceMeters())
                    .durationSeconds(detail.durationSeconds())
                    .notes(detail.notes())
                    .build();

                exercise.validate();
                return exercise;
            })
            .collect(Collectors.toList());

        workoutExerciseRepository.saveAll(workoutExercises);

        log.info("Successfully logged workout {} with {} exercises for member {}",
            savedWorkout.getId(), exercises.size(), memberId);

        return savedWorkout;
    }

    /**
     * Get workout by ID with exercises.
     */
    @Transactional(readOnly = true)
    public WorkoutWithExercises getWorkoutById(UUID workoutId) {
        log.debug("Fetching workout: {}", workoutId);

        WorkoutLog workout = workoutLogRepository.findById(workoutId)
            .orElseThrow(() -> new ResourceNotFoundException("Workout not found with ID: " + workoutId));

        List<WorkoutExercise> exercises = workoutExerciseRepository.findByWorkoutLogIdOrderByExerciseOrder(workoutId);

        return new WorkoutWithExercises(workout, exercises);
    }

    /**
     * Get workout history for a member.
     */
    @Transactional(readOnly = true)
    public List<WorkoutLog> getWorkoutHistory(UUID memberId) {
        log.debug("Fetching workout history for member: {}", memberId);
        return workoutLogRepository.findByMemberId(memberId);
    }

    /**
     * Get workout history for a member within date range.
     */
    @Transactional(readOnly = true)
    public List<WorkoutLog> getWorkoutHistoryByDateRange(UUID memberId, LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching workout history for member {} from {} to {}", memberId, startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        return workoutLogRepository.findByMemberIdAndDateRange(memberId, startDateTime, endDateTime);
    }

    /**
     * Get paginated workout history for a member.
     */
    @Transactional(readOnly = true)
    public Page<WorkoutLog> getWorkoutHistoryPaginated(UUID memberId, int page, int size) {
        log.debug("Fetching paginated workout history for member {}: page={}, size={}", memberId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "workoutDate"));
        return workoutLogRepository.findByMemberIdOrderByDateDesc(memberId, pageable);
    }

    /**
     * Get latest workout for a member.
     */
    @Transactional(readOnly = true)
    public WorkoutLog getLatestWorkout(UUID memberId) {
        log.debug("Fetching latest workout for member: {}", memberId);
        return workoutLogRepository.findLatestByMemberId(memberId)
            .orElse(null);
    }

    /**
     * Calculate workout statistics for a member within date range.
     */
    @Transactional(readOnly = true)
    public WorkoutStatistics calculateStatistics(UUID memberId, LocalDate startDate, LocalDate endDate) {
        log.debug("Calculating workout statistics for member {} from {} to {}", memberId, startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        List<WorkoutLog> workouts = workoutLogRepository.findByMemberIdAndDateRange(memberId, startDateTime, endDateTime);

        if (workouts.isEmpty()) {
            return new WorkoutStatistics(0, 0, 0, 0, null, Map.of());
        }

        // Calculate total statistics
        int totalWorkouts = workouts.size();
        int totalDuration = workouts.stream()
            .filter(w -> w.getDurationMinutes() != null)
            .mapToInt(WorkoutLog::getDurationMinutes)
            .sum();

        int totalCalories = workouts.stream()
            .filter(w -> w.getTotalCaloriesBurned() != null)
            .mapToInt(WorkoutLog::getTotalCaloriesBurned)
            .sum();

        int avgDuration = totalWorkouts > 0 ? totalDuration / totalWorkouts : 0;

        // Calculate average intensity
        WorkoutIntensity avgIntensity = calculateAverageIntensity(workouts);

        // Count exercise frequency
        Map<UUID, Long> exerciseFrequency = calculateExerciseFrequency(workouts);

        return new WorkoutStatistics(
            totalWorkouts,
            totalDuration,
            totalCalories,
            avgDuration,
            avgIntensity,
            exerciseFrequency
        );
    }

    /**
     * Delete a workout (soft delete).
     */
    @Transactional
    public void deleteWorkout(UUID workoutId) {
        log.info("Deleting workout: {}", workoutId);

        WorkoutLog workout = workoutLogRepository.findById(workoutId)
            .orElseThrow(() -> new ResourceNotFoundException("Workout not found with ID: " + workoutId));

        // Soft delete associated exercises
        workoutExerciseRepository.deleteByWorkoutLogId(workoutId);

        // Soft delete workout
        workoutLogRepository.delete(workout);

        log.info("Successfully deleted workout: {}", workoutId);
    }

    /**
     * Get workout streak (consecutive days with workouts).
     */
    @Transactional(readOnly = true)
    public int calculateWorkoutStreak(UUID memberId) {
        log.debug("Calculating workout streak for member: {}", memberId);

        List<WorkoutLog> workouts = workoutLogRepository.findByMemberId(memberId);

        if (workouts.isEmpty()) {
            return 0;
        }

        // Sort by date descending (most recent first)
        workouts.sort((w1, w2) -> w2.getWorkoutDate().compareTo(w1.getWorkoutDate()));

        int streak = 0;
        LocalDate currentDate = LocalDate.now();
        LocalDate lastWorkoutDate = workouts.get(0).getWorkoutDate().toLocalDate();

        // Check if there's a workout today or yesterday
        if (lastWorkoutDate.isBefore(currentDate.minusDays(1))) {
            return 0; // Streak broken
        }

        // Count consecutive days
        for (WorkoutLog workout : workouts) {
            LocalDate workoutDate = workout.getWorkoutDate().toLocalDate();

            if (workoutDate.equals(currentDate) || workoutDate.equals(currentDate.minusDays(1))) {
                streak++;
                currentDate = workoutDate.minusDays(1);
            } else if (workoutDate.isBefore(currentDate)) {
                break;
            }
        }

        return streak;
    }

    // Helper methods

    private WorkoutIntensity calculateAverageIntensity(List<WorkoutLog> workouts) {
        List<WorkoutIntensity> intensities = workouts.stream()
            .filter(w -> w.getIntensityLevel() != null)
            .map(WorkoutLog::getIntensityLevel)
            .collect(Collectors.toList());

        if (intensities.isEmpty()) {
            return null;
        }

        int sum = intensities.stream()
            .mapToInt(this::intensityToNumber)
            .sum();

        int avg = sum / intensities.size();

        return numberToIntensity(avg);
    }

    private int intensityToNumber(WorkoutIntensity intensity) {
        return switch (intensity) {
            case LOW -> 1;
            case MEDIUM -> 2;
            case HIGH -> 3;
            case VERY_HIGH -> 4;
        };
    }

    private WorkoutIntensity numberToIntensity(int number) {
        return switch (number) {
            case 1 -> WorkoutIntensity.LOW;
            case 2 -> WorkoutIntensity.MEDIUM;
            case 3 -> WorkoutIntensity.HIGH;
            default -> WorkoutIntensity.VERY_HIGH;
        };
    }

    private Map<UUID, Long> calculateExerciseFrequency(List<WorkoutLog> workouts) {
        List<UUID> workoutIds = workouts.stream()
            .map(WorkoutLog::getId)
            .collect(Collectors.toList());

        return workoutIds.stream()
            .flatMap(id -> workoutExerciseRepository.findByWorkoutLogId(id).stream())
            .collect(Collectors.groupingBy(WorkoutExercise::getExerciseId, Collectors.counting()));
    }

    // DTOs

    public record WorkoutExerciseDetail(
        UUID exerciseId,
        Integer exerciseOrder,
        Integer sets,
        Integer reps,
        BigDecimal weight,
        String weightUnit,
        Integer restSeconds,
        BigDecimal distanceMeters,
        Integer durationSeconds,
        String notes
    ) {}

    public record WorkoutWithExercises(
        WorkoutLog workout,
        List<WorkoutExercise> exercises
    ) {}

    public record WorkoutStatistics(
        int totalWorkouts,
        int totalDurationMinutes,
        int totalCaloriesBurned,
        int averageDurationMinutes,
        WorkoutIntensity averageIntensity,
        Map<UUID, Long> exerciseFrequency
    ) {}
}
