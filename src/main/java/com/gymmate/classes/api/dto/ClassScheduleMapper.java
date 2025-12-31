package com.gymmate.classes.api.dto;

import com.gymmate.classes.domain.ClassSchedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ClassScheduleMapper {
  ClassScheduleMapper INSTANCE = Mappers.getMapper(ClassScheduleMapper.class);

  @Mappings({
    @Mapping(target = "id", source = "id"),
    @Mapping(target = "gymId", source = "gymId"),
    @Mapping(target = "classId", source = "classId"),
    @Mapping(target = "trainerId", source = "trainerId"),
    @Mapping(target = "areaId", source = "areaId"),
    @Mapping(target = "startTime", source = "startTime"),
    @Mapping(target = "endTime", source = "endTime"),
    @Mapping(target = "capacityOverride", source = "capacityOverride"),
    @Mapping(target = "priceOverride", source = "priceOverride"),
    @Mapping(target = "status", expression = "java(entity.getStatus()==null?null:entity.getStatus().name())"),
    @Mapping(target = "cancellationReason", source = "cancellationReason"),
    @Mapping(target = "instructorNotes", source = "instructorNotes"),
    @Mapping(target = "adminNotes", source = "adminNotes")
  })
  ScheduleResponse toResponse(ClassSchedule entity);

  @Mappings({
    @Mapping(target = "classId", source = "classId"),
    @Mapping(target = "trainerId", source = "trainerId"),
    @Mapping(target = "areaId", source = "areaId"),
    @Mapping(target = "startTime", source = "startTime"),
    @Mapping(target = "endTime", source = "endTime"),
    @Mapping(target = "capacityOverride", source = "capacityOverride"),
    @Mapping(target = "priceOverride", source = "priceOverride")
  })
  ClassSchedule toEntity(CreateScheduleRequest dto);
}
