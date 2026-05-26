package org.sopt.domain.user.service;

import org.sopt.domain.user.dto.request.CreateUserRequest;
import org.sopt.domain.user.dto.response.CreateUserResponse;
import org.sopt.domain.user.dto.response.UserResponse;
import org.sopt.domain.user.entity.User;
import org.sopt.domain.user.exception.UserException;
import org.sopt.domain.user.exception.code.UserErrorCode;
import org.sopt.domain.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Transactional
  public CreateUserResponse join(CreateUserRequest request) {
    if (userRepository.findByEmail(request.email()).isPresent()) {
      throw new UserException(UserErrorCode.USER_EMAIL_DUPLICATED);
    }

    // encode(): BCrypt 해시 + 랜덤 솔트를 생성하여 "$2a$10$..." 형태로 반환
    // 원문 비밀번호는 저장하지 않으므로 DB 유출 시에도 원문 복원 불가
    String encodedPassword = passwordEncoder.encode(request.password());

    User savedUser = userRepository.save(
        new User(request.nickname(), request.email(), encodedPassword)
    );
    return CreateUserResponse.from(savedUser);
  }

  @Transactional(readOnly = true)
  public UserResponse getUserById(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    return UserResponse.from(user);
  }
}
