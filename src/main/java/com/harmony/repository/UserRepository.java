package com.harmony.repository;

import com.harmony.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);
  Optional<User> findByUserIdentifier(String userIdentifier);
  Optional<User> findByNickname(String nickname);
}
