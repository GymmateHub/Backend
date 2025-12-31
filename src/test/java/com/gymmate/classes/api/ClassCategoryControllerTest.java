package com.gymmate.classes.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymmate.classes.api.dto.CategoryResponse;
import com.gymmate.classes.api.dto.CreateCategoryRequest;
import com.gymmate.classes.api.dto.ClassCategoryMapper;
import com.gymmate.classes.application.ClassCategoryService;
import com.gymmate.classes.domain.ClassCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClassCategoryControllerTest {

  private MockMvc mvc;
  private ObjectMapper objectMapper = new ObjectMapper();

  private ClassCategoryService categoryService;
  private ClassCategoryMapper mapper;

  @BeforeEach
  void setUp() {
    categoryService = mock(ClassCategoryService.class);
    mapper = mock(ClassCategoryMapper.class);
    ClassCategoryController controller = new ClassCategoryController(categoryService, mapper);
    mvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  @Test
  void createCategory_returnsCreated() throws Exception {
    CreateCategoryRequest req = new CreateCategoryRequest();
    UUID gymId = UUID.randomUUID();
    req.setGymId(gymId);
    req.setName("Spin");
    req.setDescription("Spin classes");
    req.setColor("#00FF00");

    ClassCategory saved = ClassCategory.builder().name(req.getName()).description(req.getDescription()).color(req.getColor()).build();
    saved.setId(UUID.randomUUID());
    saved.setGymId(gymId);

    when(mapper.toEntity(any(CreateCategoryRequest.class))).thenReturn(saved);
    when(categoryService.createCategory(any(ClassCategory.class))).thenReturn(saved);
    when(mapper.toResponse(any(ClassCategory.class))).thenReturn(new CategoryResponse());

    mvc.perform(post("/api/class-categories")
      .contentType(MediaType.APPLICATION_JSON)
      .content(objectMapper.writeValueAsString(req)))
      .andExpect(status().isCreated());
  }
}
