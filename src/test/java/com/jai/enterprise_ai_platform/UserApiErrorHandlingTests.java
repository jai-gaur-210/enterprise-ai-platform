package com.jai.enterprise_ai_platform;

import com.jai.enterprise_ai_platform.controller.UserController;
import com.jai.enterprise_ai_platform.dto.UserRequest;
import com.jai.enterprise_ai_platform.entity.User;
import com.jai.enterprise_ai_platform.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserApiErrorHandlingTests {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void getUsersReturnsWrappedSuccessResponse() throws Exception {
        User user = user();
        when(userService.findAll()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.message").value("Users retrieved successfully"))
                .andExpect(jsonPath("$.data[0].id").value(user.getId().toString()))
                .andExpect(jsonPath("$.data[0].name").value(user.getName()));
    }

    @Test
    void getUserByIdReturnsWrappedSuccessResponse() throws Exception {
        User user = user();
        when(userService.findById(user.getId())).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(user.getId().toString()));
    }

    @Test
    void createUserReturnsCreatedWithWrappedSuccessResponse() throws Exception {
        User user = user();
        when(userService.save(any(UserRequest.class))).thenReturn(user);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test User\",\"email\":\"test@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User created successfully"))
                .andExpect(jsonPath("$.data.id").value(user.getId().toString()));
    }

    @Test
    void updateUserReturnsOkWithWrappedSuccessResponse() throws Exception {
        User user = user();
        when(userService.update(eq(user.getId()), any(UserRequest.class))).thenReturn(Optional.of(user));

        mockMvc.perform(put("/api/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test User\",\"email\":\"test@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User updated successfully"))
                .andExpect(jsonPath("$.data.id").value(user.getId().toString()));
    }

    @Test
    void deleteUserReturnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.deleteById(id)).thenReturn(true);

        mockMvc.perform(delete("/api/users/{id}", id))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    void missingUserReturnsConsistentNotFoundResponse() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User with id '" + id + "' was not found"))
                .andExpect(jsonPath("$.path").value("/api/users/" + id))
                .andExpect(jsonPath("$.validationErrors").isEmpty());
    }

    @Test
    void invalidRequestReturnsFieldValidationDetails() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"email\":\"bad-email\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.validationErrors.name").value("Name is required"))
                .andExpect(jsonPath("$.validationErrors.email").value("Email must be valid"))
                .andExpect(jsonPath("$.validationErrors.password").value("Password must be between 8 and 100 characters"));
    }

    @Test
    void malformedUserIdReturnsBadRequestInsteadOfServerError() throws Exception {
        mockMvc.perform(get("/api/users/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid value for parameter 'id'"));
    }

    @Test
    void unexpectedExceptionReturnsSafeConsistentServerError() throws Exception {
        doThrow(new IllegalStateException("sensitive internal detail"))
                .when(userService).findAll();

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.validationErrors").isEmpty());
    }

    private static User user() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Test User");
        user.setEmail("test@example.com");
        return user;
    }
}