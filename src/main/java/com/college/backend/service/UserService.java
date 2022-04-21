package com.college.backend.service;

import com.college.backend.dto.UserDto;
import com.college.backend.exceptions.*;
import com.college.backend.model.ERole;
import com.college.backend.model.School;
import com.college.backend.model.SchoolEducationPeriod;
import com.college.backend.model.User;
import com.college.backend.payload.request.SignupRequest;
import com.college.backend.repository.SchoolEducationPeriodRepository;
import com.college.backend.repository.SchoolRepository;
import com.college.backend.repository.UserRepository;
import com.college.backend.security.SecurityConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    public static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final SchoolEducationPeriodRepository schoolEducationPeriodRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, SchoolRepository schoolRepository,
                       SchoolEducationPeriodRepository schoolEducationPeriodRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.schoolRepository = schoolRepository;
        this.schoolEducationPeriodRepository = schoolEducationPeriodRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(SignupRequest userSignUp) {
        User user = new User();

        if (isEmailExist(userSignUp.getEmail())) {
            throw new EmailAlreadyExist("This email " + userSignUp.getEmail() + " already exist");
        }

        if (isPhoneNumberExist(userSignUp.getPhoneNumber())) {
            throw new EmailAlreadyExist("This phone number " + userSignUp.getPhoneNumber() + " already exist");
        }

        user.setFirstName(userSignUp.getFirstName());
        user.setLastName(userSignUp.getLastName());
        user.setMiddleName(userSignUp.getMiddleName());
        user.setPhoneNumber(userSignUp.getPhoneNumber());
        user.setEmail(userSignUp.getEmail());
        user.setPassword(passwordEncoder.encode(userSignUp.getPassword()));
        user.getRoles().add(ERole.ROLE_USER);

        LOG.info("User saved: {}", userSignUp.getEmail());
        return userRepository.save(user);
    }

    public User updateUser(UserDto userDto, Principal principal) {
        User user = getUserByPrincipal(principal);

        System.out.println(userDto.getEndDate());

        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setMiddleName(userDto.getMiddleName());
        user.setIIN(userDto.getIIN());
        user.setDob(userDto.getDob());

        if (userDto.getSchoolName() != null && userDto.getSchoolState() != null
                && userDto.getSchoolCity() != null) {

            if (!isSchoolNameExist(userDto.getSchoolName())) {
                throw new DataNotFoundException("School name incorrect");
            }
            if (!isSchoolCityExist(userDto.getSchoolCity())) {
                throw new DataNotFoundException("School city incorrect");
            }
            if (!isSchoolStateExist(userDto.getSchoolState())) {
                throw new DataNotFoundException("School state incorrect");
            }

            School school = schoolRepository.findSchoolByCityAndStateAndName(
                    userDto.getSchoolCity(), userDto.getSchoolState(), userDto.getSchoolName()
            ).orElseThrow(() -> new DataNotFoundException("School not found"));

            user.setSchool(school);

            if (userDto.getStartDate() != null && userDto.getEndDate() != null) {
                SchoolEducationPeriod schoolEducationPeriod = schoolEducationPeriodRepository
                        .findSchoolEducationPeriodByUserId(user).orElse(null);

                if(!ObjectUtils.isEmpty(schoolEducationPeriod)) {
                    schoolEducationPeriodRepository.delete(schoolEducationPeriod);
                }

                SchoolEducationPeriod period = new SchoolEducationPeriod();
                period.setStartDate(userDto.getStartDate());
                period.setEndDate(userDto.getEndDate());
                period.setSchoolId(school);
                period.setUserId(user);

                user.setSchoolEducationPeriod(period);

                schoolEducationPeriodRepository.save(period);
            }
        } else {
            if (userDto.getStartDate() != null && userDto.getEndDate() != null) {
                throw new MissingNecessaryInput("You are not able to update date without school info");
            }
        }

        LOG.info("User info updated: {}", user.getEmail());
        return userRepository.save(user);
    }

    public User changeUserStatus(String userId, String status) {
        User user = getUserById(Long.parseLong(userId));

        if (!status.equals(SecurityConstants.ACCEPTED_STATUS)
                && !status.equals(SecurityConstants.DECLINED_STATUS)
                && !status.equals(SecurityConstants.DEFAULT_STATUS)) {
            throw new UndefinedStatus("Status: " + status + " undefined");
        }
        user.setStatus(status);

        LOG.info("User status changed: {}", user.getEmail());
        return userRepository.save(user);
    }

    public List<User> getAllUsersExceptAdmin() {
        return userRepository.findAllByOrderByCreatedDateAsc().stream()
                .filter((user) -> user.getRoles().contains(ERole.ROLE_USER)
                        && user.getStatus().equals(SecurityConstants.DEFAULT_STATUS))
                .collect(Collectors.toList());
    }

    public List<User> getAllUsersAccepted() {
        return userRepository.findAllByOrderByCreatedDateAsc().stream()
                .filter((user) -> user.getRoles().contains(ERole.ROLE_USER)
                        && user.getStatus().equals(SecurityConstants.ACCEPTED_STATUS))
                .collect(Collectors.toList());
    }

    public List<User> getAllUsersDeclined() {
        return userRepository.findAllByOrderByCreatedDateAsc().stream()
                .filter((user) -> user.getRoles().contains(ERole.ROLE_USER)
                        && user.getStatus().equals(SecurityConstants.DECLINED_STATUS))
                .collect(Collectors.toList());
    }

    public User getCurrentUser(Principal principal) {
        return getUserByPrincipal(principal);
    }

    public User getUserById(long userId) {
        return userRepository.findUserById(userId)
                .orElseThrow(() -> new UserNotFoundException("User id not found: " + userId));
    }

    User getUserByPrincipal(Principal principal) {
        String email = principal.getName();

        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User email not found: " + email));
    }

    public boolean isEmailExist(String email) {
        return userRepository.existsUserByEmail(email);
    }
    public boolean isPhoneNumberExist(String phoneNumber) {
        return userRepository.existsUserByPhoneNumber(phoneNumber);
    }

    public boolean isSchoolNameExist(String schoolName) {
        return schoolRepository.existsSchoolByName(schoolName);
    }
    public boolean isSchoolCityExist(String schoolCity) {
        return schoolRepository.existsSchoolByCity(schoolCity);
    }
    public boolean isSchoolStateExist(String schoolState) {
        return schoolRepository.existsSchoolByState(schoolState);
    }

}
