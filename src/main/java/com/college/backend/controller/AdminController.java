package com.college.backend.controller;

import com.college.backend.dto.UserDto;
import com.college.backend.facade.UserFacade;
import com.college.backend.model.User;
import com.college.backend.payload.response.MessageResponse;
import com.college.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserFacade userFacade;

    @GetMapping("/get-user/{userId}")
    public ResponseEntity<UserDto> getUserProfileById(@PathVariable("userId") String userId) {
        User user = userService.getUserById(Long.parseLong(userId));
        UserDto userDto = userFacade.userToUserDto(user);

        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }

    @GetMapping("/get-all-unchecked")
    public ResponseEntity<List<UserDto>> getAllUncheckedUsers() {
        List<UserDto> userDtoList = userService.getAllUsersExceptAdmin()
                .stream()
                .map(userFacade::userToUserDto)
                .collect(Collectors.toList());

        return new ResponseEntity<>(userDtoList, HttpStatus.OK);
    }

    @GetMapping("/get-all-accepted")
    public ResponseEntity<List<UserDto>> getAllAcceptedUsers() {
        List<UserDto> userDtoList = userService.getAllUsersAccepted()
                .stream()
                .map(userFacade::userToUserDto)
                .collect(Collectors.toList());

        return new ResponseEntity<>(userDtoList, HttpStatus.OK);
    }

    @GetMapping("/get-all-declined")
    public ResponseEntity<List<UserDto>> getAllDeclinedUsers() {
        List<UserDto> userDtoList = userService.getAllUsersDeclined()
                .stream()
                .map(userFacade::userToUserDto)
                .collect(Collectors.toList());

        return new ResponseEntity<>(userDtoList, HttpStatus.OK);
    }

    @PostMapping("/change-status/{userId}/{status}")
    public ResponseEntity<MessageResponse> changeUserStatus(@PathVariable("userId") String userId,
                                                            @PathVariable("status") String status) {
        User user = userService.changeUserStatus(userId, status);
        return new ResponseEntity<>(new MessageResponse("Status of user " + user.getFirstName() +" was changed"), HttpStatus.OK);
    }
}
