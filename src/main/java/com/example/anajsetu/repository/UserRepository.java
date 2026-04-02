package com.example.anajsetu.repository;

import com.example.anajsetu.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    List<User> findByRole(String role);

    List<User> findByIsVerified(int isVerified);

    List<User> findByRoleAndIsVerified(String role, int isVerified);
    
    Optional<User> findByPhone(String phone);

    boolean existsByPhone(String phone);

}   