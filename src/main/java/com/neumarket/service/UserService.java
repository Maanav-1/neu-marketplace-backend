package com.neumarket.service;

import com.neumarket.dto.request.UpdateUserRequest;
import com.neumarket.dto.response.UserResponse;
import com.neumarket.exception.ResourceNotFoundException;
import com.neumarket.model.User;
import com.neumarket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

  private final UserRepository userRepository;

  /**
   * Get current user's profile
   */
  @Transactional(readOnly = true)
  public UserResponse getCurrentUser(Long userId) {
    User user = getUserById(userId);
    return UserResponse.fromEntity(user);
  }

  /**
   * Get public profile of a user
   */
  @Transactional(readOnly = true)
  public UserResponse getPublicProfile(Long userId) {
    User user = getUserById(userId);
    return UserResponse.publicFromEntity(user);
  }

  /**
   * Update current user's profile
   */
  @Transactional
  public UserResponse updateProfile(Long userId, UpdateUserRequest request) {
    User user = getUserById(userId);

    if (request.getName() != null) {
      user.setName(request.getName().trim());
    }

    if (request.getPhone() != null) {
      user.setPhone(request.getPhone().trim());
    }

    User updated = userRepository.save(user);
    log.info("User profile updated: {}", userId);

    return UserResponse.fromEntity(updated);
  }

  /**
   * Update profile picture URL
   */
  @Transactional
  public UserResponse updateProfilePicture(Long userId, String profilePicUrl) {
    User user = getUserById(userId);
    user.setProfilePicUrl(profilePicUrl);
    User updated = userRepository.save(user);
    log.info("User profile picture updated: {}", userId);
    return UserResponse.fromEntity(updated);
  }

  /**
   * Get user by ID
   */
  public User getUserById(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
  }
}