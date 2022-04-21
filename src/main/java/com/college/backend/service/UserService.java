package com.college.backend.service;

import com.college.backend.dto.UserDto;
import com.college.backend.exceptions.*;
import com.college.backend.model.*;
import com.college.backend.payload.request.SignupRequest;
import com.college.backend.repository.RoleRepository;
import com.college.backend.repository.SchoolEducationPeriodRepository;
import com.college.backend.repository.SchoolRepository;
import com.college.backend.repository.UserRepository;
import com.college.backend.security.SecurityConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {
    public static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final RoleRepository roleRepository;
    private final SchoolEducationPeriodRepository schoolEducationPeriodRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, SchoolRepository schoolRepository,
                       SchoolEducationPeriodRepository schoolEducationPeriodRepository,
                       RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.schoolRepository = schoolRepository;
        this.roleRepository = roleRepository;
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

        Set<Role> roleSet = new HashSet<>();
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new DataNotFoundException("Role " + ERole.ROLE_USER + " not found"));
        roleSet.add(userRole);

        user.setRoles(roleSet);

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

        // чтобы обновить данные пользователя при вводе школы
        // имеется три поля идентифицирующие информацию о школе
        // и если пользователь начал заполнять одно поле надо заполнить остальные также
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

            // кроме этого указывать даты начала и конца учеба надо обязательно
            // если пользователь уже ввел данные школы
            // при пропуске какого либо ввода вернется ошибка
            if (userDto.getStartDate() != null || userDto.getEndDate() != null) {
                if (userDto.getStartDate() == null) {
                    throw new MissingNecessaryInput("Missing Input start date");
                } else if (userDto.getEndDate() == null) {
                    throw new MissingNecessaryInput("Missing Input end date");
                }
            } else if (userDto.getStartDate() != null && userDto.getEndDate() != null) {
                // в случае если поля заполнены надо проверить
                // добавлял ли уже эти данные пользователь
                SchoolEducationPeriod schoolEducationPeriod = schoolEducationPeriodRepository
                        .findSchoolEducationPeriodByUserId(user).orElse(null);

                // если не добавлял вносим пользователя и школу в таблицу периода обучения
                // иначе просто обновляем значения начала и конца обучения
                if (schoolEducationPeriod == null) {
                    SchoolEducationPeriod period = new SchoolEducationPeriod();
                    period.setStartDate(userDto.getStartDate());
                    period.setEndDate(userDto.getEndDate());
                    period.setSchoolId(school);
                    period.setUserId(user);
                    user.setSchoolEducationPeriod(period);
                    schoolEducationPeriodRepository.save(period);
                } else {
                    schoolEducationPeriodRepository.updatePeriodById(userDto.getStartDate(),
                            userDto.getEndDate(), user);
                }
            }

        } else {
            // если не все поля школы были заполнены
            // нужно проверить каждый ввод и вывести ошибку о том, какое поле осталось незаполненным
            if (userDto.getSchoolName() != null
                    || userDto.getSchoolCity() != null
                    || userDto.getSchoolState() != null) {
                if (userDto.getSchoolName() == null) {
                    throw new MissingNecessaryInput("Missing Input school name");
                } else if (userDto.getSchoolState() == null) {
                    throw new MissingNecessaryInput("Missing Input school state");
                } else if (userDto.getSchoolCity() == null) {
                    throw new MissingNecessaryInput("Missing Input school city");
                }
            }

            // так и для ввода дат, необходимо проверить каждую введенную дату
            // и если имеется пропущенное поле вывести ошибку
            if (userDto.getStartDate() != null || userDto.getEndDate() != null) {
                if (userDto.getStartDate() == null) {
                    throw new MissingNecessaryInput("Missing Input start date");
                } else if (userDto.getEndDate() == null) {
                    throw new MissingNecessaryInput("Missing Input end date");
                } else
                    throw new MissingNecessaryInput("You need to specify school info first");
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
        Role role = roleRepository.findByName(ERole.ROLE_USER)
                .orElse(null);

        return userRepository.findAllByOrderByCreatedDateAsc().stream()
                .filter((user) -> user.getRoles().contains(role)
                        && user.getStatus().equals(SecurityConstants.DEFAULT_STATUS))
                .collect(Collectors.toList());
    }

    public List<User> getAllUsersAccepted() {
        Role role = roleRepository.findByName(ERole.ROLE_USER)
                .orElse(null);

        return userRepository.findAllByOrderByCreatedDateAsc().stream()
                .filter((user) -> user.getRoles().contains(role)
                        && user.getStatus().equals(SecurityConstants.ACCEPTED_STATUS))
                .collect(Collectors.toList());
    }

    public List<User> getAllUsersDeclined() {
        Role role = roleRepository.findByName(ERole.ROLE_USER)
                .orElse(null);

        return userRepository.findAllByOrderByCreatedDateAsc().stream()
                .filter((user) -> user.getRoles().contains(role)
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
