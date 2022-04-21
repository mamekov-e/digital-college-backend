package com.college.backend.controller;

import com.college.backend.dto.UserDto;
import com.college.backend.facade.UserFacade;
import com.college.backend.model.User;
import com.college.backend.payload.response.MessageResponse;
import com.college.backend.service.UserService;
import com.college.backend.validations.ResponseErrorValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping("api/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserFacade userFacade;
    @Autowired
    private ResponseErrorValidation responseErrorValidation;

    @GetMapping("/get-current")
    public ResponseEntity<UserDto> getCurrentUserData(Principal principal) {
        User user = userService.getCurrentUser(principal);
        UserDto userDto = userFacade.userToUserDto(user);

        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }

    @PostMapping("/update")
    public ResponseEntity<Object> updateUser(@Valid @RequestBody UserDto userDto,
                                             BindingResult bindingResult, Principal principal) {
        ResponseEntity<Object> errors = responseErrorValidation.mapValidationService(bindingResult);
        if (!ObjectUtils.isEmpty(errors)) return errors;

        User user = userService.updateUser(userDto, principal);
        UserDto userUpdated = userFacade.userToUserDto(user);

        return new ResponseEntity<>(userUpdated, HttpStatus.OK);
    }

}
