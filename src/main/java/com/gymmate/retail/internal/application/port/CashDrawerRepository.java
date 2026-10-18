package com.gymmate.retail.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.retail.internal.domain.CashDrawer;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Repository port for {@link CashDrawer} aggregates. */
public interface CashDrawerRepository extends DomainRepository<CashDrawer, UUID> {

    Optional<CashDrawer> findOpenDrawerByGymId(UUID gymId);

    List<CashDrawer> findByGymId(UUID gymId);

    List<CashDrawer> findByGymIdAndSessionDate(UUID gymId, LocalDate sessionDate);

    List<CashDrawer> findByGymIdAndDateRange(UUID gymId, LocalDate startDate, LocalDate endDate);

    Optional<CashDrawer> findOpenDrawerByGymIdAndStaffId(UUID gymId, UUID staffId);
}
