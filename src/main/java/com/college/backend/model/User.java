package com.college.backend.model;


import com.college.backend.security.SecurityConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private String middleName;

    @Column(unique = true)
    private String IIN;

    @JsonFormat(pattern = "yyyy-mm-dd")
    private Date dob;

    @Column(unique = true, nullable = false)
    private String phoneNumber;

    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String status = SecurityConstants.DEFAULT_STATUS;

    @ManyToOne(fetch = FetchType.LAZY)
    private School school;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "school_education_period_id")
    private SchoolEducationPeriod schoolEducationPeriod;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(  name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @JsonFormat(pattern = "yyyy-mm-dd HH:hh:ss")
    @Column(updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }
}
