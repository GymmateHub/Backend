package com.gymmate.pos.infrastructure;

import com.gymmate.pos.domain.CashDrawer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for CashDrawer entity.
 */
@Repository
public interface CashDrawerJpaRepository extends JpaRepository<CashDrawer, UUID> {

    @Query("SELECT cd FROM CashDrawer cd WHERE cd.gymId = :gymId AND cd.open = true")
    Optional<CashDrawer> findOpenDrawerByGymId(@Param("gymId") UUID gymId);

    List<CashDrawer> findByGymId(UUID gymId);

    @Query("SELECT cd FROM CashDrawer cd WHERE cd.gymId = :gymId AND cd.sessionDate = :sessionDate")
    List<CashDrawer> findByGymIdAndSessionDate(@Param("gymId") UUID gymId, @Param("sessionDate") LocalDate sessionDate);

    @Query("SELECT cd FROM CashDrawer cd WHERE cd.gymId = :gymId AND cd.sessionDate BETWEEN :startDate AND :endDate ORDER BY cd.sessionDate DESC")
    List<CashDrawer> findByGymIdAndDateRange(@Param("gymId") UUID gymId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT cd FROM CashDrawer cd WHERE cd.gymId = :gymId AND cd.openedBy = :staffId AND cd.open = true")
    Optional<CashDrawer> findOpenDrawerByGymIdAndStaffId(@Param("gymId") UUID gymId, @Param("staffId") UUID staffId);
}
