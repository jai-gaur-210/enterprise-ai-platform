package com.jai.enterprise_ai_platform.service;

import com.jai.enterprise_ai_platform.dto.UserRequest;
import com.jai.enterprise_ai_platform.entity.User;
import com.jai.enterprise_ai_platform.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserServicePasswordTests {
	private final UserRepository userRepository = mock(UserRepository.class);
	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	private final UserService userService = new UserService(userRepository, passwordEncoder);

	@Test
	void createStoresHashThatMatchesOnlyTheOriginalPassword() {
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
		UserRequest request = new UserRequest("Test User", "test@example.com", "correct-horse-battery");

		User savedUser = userService.save(request);

		assertNotEquals("correct-horse-battery", savedUser.getPassword());
		assertTrue(passwordEncoder.matches("correct-horse-battery", savedUser.getPassword()));
		assertFalse(passwordEncoder.matches("incorrect-password", savedUser.getPassword()));
	}

	@Test
	void updateHashesChangedPassword() {
		UUID id = UUID.randomUUID();
		User existingUser = new User();
		existingUser.setPassword(passwordEncoder.encode("old-password"));
		when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
		UserRequest update = new UserRequest("Test User", "test@example.com", "new-password");

		User updatedUser = userService.update(id, update).orElseThrow();

		assertNotEquals("new-password", updatedUser.getPassword());
		assertTrue(passwordEncoder.matches("new-password", updatedUser.getPassword()));
		assertFalse(passwordEncoder.matches("old-password", updatedUser.getPassword()));
	}
}