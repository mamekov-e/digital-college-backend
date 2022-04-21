package com.college.backend.repository;

import com.college.backend.model.School;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SchoolRepository extends JpaRepository<School, Long> {

    Optional<School> findSchoolByCityAndStateAndName(String city, String state, String name);

    boolean existsSchoolByState(String state);
    boolean existsSchoolByCity(String city);
    boolean existsSchoolByName(String name);
}
