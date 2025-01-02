package com.harmony.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.harmony.entity.Role;
import com.harmony.entity.User;
import com.harmony.service.UserService;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = UserController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @InjectMocks
  private UserController userController;

  @Mock
  private UserService userService;

  private User user;

}