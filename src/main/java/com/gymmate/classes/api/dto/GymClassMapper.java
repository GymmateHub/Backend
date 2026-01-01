package com.gymmate.classes.api.dto;

import com.gymmate.classes.domain.GymClass;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface GymClassMapper {
  GymClassMapper INSTANCE = Mappers.getMapper(GymClassMapper.class);

  @Mappings({
    @Mapping(target = "id", source = "id"),
    @Mapping(target = "gymId", source = "organisationId"),
    @Mapping(target = "categoryId", source = "categoryId"),
    @Mapping(target = "name", source = "name"),
    @Mapping(target = "description", source = "description"),
    @Mapping(target = "durationMinutes", source = "durationMinutes"),
    @Mapping(target = "capacity", source = "capacity"),
    @Mapping(target = "price", source = "price"),
    @Mapping(target = "creditsRequired", source = "creditsRequired"),
    // map extra optional fields if present
    @Mapping(target = "skillLevel", source = "skillLevel"),
    @Mapping(target = "ageRestriction", source = "ageRestriction"),
    @Mapping(target = "equipmentNeeded", source = "equipmentNeeded"),
    @Mapping(target = "imageUrl", source = "imageUrl"),
    @Mapping(target = "videoUrl", source = "videoUrl"),
    @Mapping(target = "instructions", source = "instructions")
  })
  ClassResponse toResponse(GymClass entity);

  @Mappings({
    @Mapping(target = "categoryId", source = "categoryId"),
    @Mapping(target = "name", source = "name"),
    @Mapping(target = "description", source = "description"),
    @Mapping(target = "durationMinutes", source = "durationMinutes"),
    @Mapping(target = "capacity", source = "capacity"),
    @Mapping(target = "price", source = "price"),
    @Mapping(target = "creditsRequired", source = "creditsRequired")
  })
  GymClass toEntity(CreateClassRequest dto);
}
