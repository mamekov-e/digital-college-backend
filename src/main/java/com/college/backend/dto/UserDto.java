package com.college.backend.dto;

import com.college.backend.model.ERole;
import com.college.backend.model.School;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Date;

@Data
public class UserDto {
    private Long id;
    @NotEmpty
    private String firstName;
    @NotEmpty
    private String lastName;
    private String middleName;
    private String email;
    private String IIN;
    @JsonFormat(pattern = "yyyy-mm-dd")
    private Date dob;
    private String phoneNumber;
    private String status;
    private String schoolName;
    private String schoolState;
    private String schoolCity;
    @JsonFormat(pattern = "yyyy-mm-dd")
    private Date startDate;
    @JsonFormat(pattern = "yyyy-mm-dd")
    private Date endDate;

}
