package com.college.backend.repository;

import com.college.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserByEmail(String email);

    boolean existsUserByEmail(String email);

    boolean existsUserByPhoneNumber(String phoneNumber);

    Optional<User> findUserById(Long id);

    List<User> findAllByOrderByCreatedDateAsc();

}
