package com.example.audit.repository;

import com.example.audit.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    
    Optional<User> findByUsernameAndActiveTrue(String username);
    
    List<User> findByActiveTrue();
    
    List<User> findByRole(String role);
}
