package com.gymmate.classes.api.dto;

import com.gymmate.classes.domain.GymArea;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface GymAreaMapper {
  GymAreaMapper INSTANCE = Mappers.getMapper(GymAreaMapper.class);

  @Mappings({
    @Mapping(target = "id", source = "id"),
    @Mapping(target = "name", source = "name"),
    @Mapping(target = "areaType", source = "areaType"),
    @Mapping(target = "capacity", source = "capacity")
  })
  AreaResponse toResponse(GymArea entity);

  // Note: gymId and organisationId are inherited from GymScopedEntity
  // and will be set automatically via TenantContext in prePersist.
  // These mappings tell MapStruct to not try to map them from the DTO.
  @Mapping(target = "amenities", ignore = true)
  @Mapping(target = "requiresBooking", constant = "false")
  @Mapping(target = "advanceBookingHours", constant = "24")
  GymArea toEntity(CreateAreaRequest dto);
}
