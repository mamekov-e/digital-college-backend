package com.college.backend.facade;

import com.college.backend.dto.UserDto;
import com.college.backend.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserFacade {

    public UserDto userToUserDto(User user) {
        UserDto userDto = new UserDto();

        userDto.setId(user.getId());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setMiddleName(user.getMiddleName());
        userDto.setPhoneNumber(user.getPhoneNumber());

        if (user.getSchool() != null) {
            userDto.setSchoolName(user.getSchool().getName());
            userDto.setSchoolCity(user.getSchool().getCity());
            userDto.setSchoolState(user.getSchool().getState());
        }

        userDto.setEmail(user.getEmail());
        userDto.setStatus(user.getStatus());
        userDto.setIIN(user.getIIN());
        userDto.setDob(user.getDob());

        if (user.getSchoolEducationPeriod() != null) {
            userDto.setStartDate(user.getSchoolEducationPeriod().getStartDate());
            userDto.setEndDate(user.getSchoolEducationPeriod().getEndDate());
        }

        return userDto;
    }
}
