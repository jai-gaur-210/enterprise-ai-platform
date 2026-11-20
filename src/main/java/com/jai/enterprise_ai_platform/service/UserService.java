package com.jai.enterprise_ai_platform.service;

import com.jai.enterprise_ai_platform.dto.UserRequest;
import com.jai.enterprise_ai_platform.entity.User;
import com.jai.enterprise_ai_platform.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Service
@Transactional
public class UserService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public User save(UserRequest request) {
		User user = new User();
		user.setName(request.name());
		user.setEmail(request.email());
		user.setPassword(passwordEncoder.encode(request.password()));
		return userRepository.save(user);
	}

	@Transactional(readOnly = true)
	public List<User> findAll() {
		return userRepository.findAll();
	}

	@Transactional(readOnly = true)
	public Optional<User> findById(UUID id) {
		return userRepository.findById(id);
	}

	public Optional<User> update(UUID id, UserRequest request) {
		return userRepository.findById(id).map(user -> {
			user.setName(request.name());
			user.setEmail(request.email());
			user.setPassword(passwordEncoder.encode(request.password()));
			return userRepository.save(user);
		});
	}
    
	public boolean deleteById(UUID id) {
		if (!userRepository.existsById(id)) {
			return false;
		}
		userRepository.deleteById(id);
		return true;
	}
}
