package com.gymmate.classes.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymmate.classes.api.dto.CreateCategoryRequest;
import com.gymmate.classes.infrastructure.ClassCategoryJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
public class ClassCategoryControllerIntegrationTest {

  @Autowired
  MockMvc mvc;

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  ClassCategoryJpaRepository categoryRepository;

  @Test
  @WithMockUser(roles = {"GYM_OWNER"})
  void createCategory_endpointCreatesCategory() throws Exception {
    CreateCategoryRequest req = new CreateCategoryRequest();
    UUID gymId = UUID.randomUUID();
    req.setGymId(gymId);
    req.setName("Yoga");
    req.setDescription("Yoga classes");
    req.setColor("#FF0000");

    mvc.perform(post("/api/class-categories")
      .contentType(MediaType.APPLICATION_JSON)
      .content(objectMapper.writeValueAsString(req)))
      .andExpect(status().isCreated());

    // verify saved
    assertThat(categoryRepository.findAll()).hasSize(1);
    assertThat(categoryRepository.findAll().get(0).getName()).isEqualTo("Yoga");
    assertThat(categoryRepository.findAll().get(0).getGymId()).isEqualTo(gymId);
  }
}
