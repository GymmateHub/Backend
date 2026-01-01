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
    @Mapping(target = "gymId", source = "organisationId"),
    @Mapping(target = "name", source = "name"),
    @Mapping(target = "areaType", source = "areaType"),
    @Mapping(target = "capacity", source = "capacity")
  })
  AreaResponse toResponse(GymArea entity);

  @Mappings({
    @Mapping(target = "name", source = "name"),
    @Mapping(target = "areaType", source = "areaType"),
    @Mapping(target = "capacity", source = "capacity")
  })
  GymArea toEntity(CreateAreaRequest dto);
}
