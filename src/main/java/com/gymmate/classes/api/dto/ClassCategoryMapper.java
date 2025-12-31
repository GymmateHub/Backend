package com.gymmate.classes.api.dto;

import com.gymmate.classes.domain.ClassCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ClassCategoryMapper {
  ClassCategoryMapper INSTANCE = Mappers.getMapper(ClassCategoryMapper.class);

  @Mappings({
    @Mapping(target = "id", source = "id"),
    @Mapping(target = "gymId", source = "gymId"),
    @Mapping(target = "name", source = "name"),
    @Mapping(target = "description", source = "description"),
    @Mapping(target = "color", source = "color"),
    @Mapping(target = "icon", source = "icon")
  })
  CategoryResponse toResponse(ClassCategory entity);

  @Mappings({
    @Mapping(target = "name", source = "name"),
    @Mapping(target = "description", source = "description"),
    @Mapping(target = "color", source = "color")
  })
  ClassCategory toEntity(CreateCategoryRequest dto);
}
