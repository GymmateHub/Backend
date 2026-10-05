package com.gymmate.retail.internal.application.port;

import com.gymmate.retail.internal.domain.CashDrawer;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/** Repository port for {@link CashDrawer} aggregates. */
public interface CashDrawerRepository {

    Optional<CashDrawer> findOpenDrawerByGymId(UUID gymId);

    List<CashDrawer> findByGymId(UUID gymId);

    List<CashDrawer> findByGymIdAndSessionDate(UUID gymId, LocalDate sessionDate);

    List<CashDrawer> findByGymIdAndDateRange(UUID gymId, LocalDate startDate, LocalDate endDate);

    Optional<CashDrawer> findOpenDrawerByGymIdAndStaffId(UUID gymId, UUID staffId);

    CashDrawer save(CashDrawer entity);

    List<CashDrawer> saveAll(Iterable<CashDrawer> entities);

    Optional<CashDrawer> findById(UUID id);

    boolean existsById(UUID id);

    List<CashDrawer> findAll();

    List<CashDrawer> findAllById(Iterable<UUID> ids);

    long count();

    void deleteById(UUID id);

    void delete(CashDrawer entity);

    void deleteAll(Iterable<CashDrawer> entities);

    CashDrawer saveAndFlush(CashDrawer entity);

    void flush();

    Page<CashDrawer> findAll(Pageable pageable);
}
