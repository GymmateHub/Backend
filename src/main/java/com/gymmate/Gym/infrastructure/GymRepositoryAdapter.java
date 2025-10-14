package com.gymmate.Gym.infrastructure;

import com.gymmate.Gym.domain.Gym;
import com.gymmate.Gym.domain.GymRepository;
import com.gymmate.Gym.domain.GymStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository adapter that implements the domain GymRepository interface
 * using Spring Data JPA.
 */
@Component
@RequiredArgsConstructor
public class GymRepositoryAdapter implements GymRepository {

    private final GymJpaRepository jpaRepository;

    @Override
    public Gym save(Gym gym) {
        return jpaRepository.save(gym);
    }

    @Override
    public Optional<Gym> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Gym> findByOwnerId(UUID ownerId) {
        return jpaRepository.findByOwnerId(ownerId);
    }

    @Override
    public List<Gym> findByStatus(GymStatus status) {
        return jpaRepository.findByStatus(status);
    }

    @Override
    public List<Gym> findByAddressCity(String city) {
        return jpaRepository.findByAddress_City(city);
    }

    @Override
    public List<Gym> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
}
