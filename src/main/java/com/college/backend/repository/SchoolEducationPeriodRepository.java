package com.college.backend.repository;

import com.college.backend.model.SchoolEducationPeriod;
import com.college.backend.model.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface SchoolEducationPeriodRepository extends CrudRepository<SchoolEducationPeriod, Long> {
    Optional<SchoolEducationPeriod> findSchoolEducationPeriodByUserId(User userId);

    @Modifying
    @Query("update SchoolEducationPeriod p set p.startDate = ?1, p.endDate = ?2 where p.userId = ?3")
    void updatePeriodById(Date startDate, Date endDate, User userId);
}

