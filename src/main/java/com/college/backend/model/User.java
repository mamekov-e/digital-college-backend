package com.college.backend.model;


import com.college.backend.security.SecurityConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
public class User implements UserDetails{
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

    @ElementCollection(targetClass = ERole.class)
    @CollectionTable(name = "user_role",
        joinColumns = @JoinColumn(name = "user_id"))
    private Set<ERole> roles = new HashSet<>();

    @JsonFormat(pattern = "yyyy-mm-dd HH:hh:ss")
    @Column(updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }

    public User() {
    }

    public User(Long id,
                String email,
                String password,
                Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    @Transient
    private Collection<? extends GrantedAuthority> authorities;

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
