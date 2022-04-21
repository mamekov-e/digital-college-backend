package com.college.backend.repository;

import com.college.backend.model.SchoolEducationPeriod;
import com.college.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchoolEducationPeriodRepository extends CrudRepository<SchoolEducationPeriod, Long> {
    Optional<SchoolEducationPeriod> findSchoolEducationPeriodByUserId(User userId);
    void deleteSchoolEducationPeriodByUserId(User userId);
}

