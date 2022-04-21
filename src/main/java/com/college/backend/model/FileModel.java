package com.college.backend.model;

import lombok.Data;
import net.minidev.json.annotate.JsonIgnore;

import javax.persistence.*;

@Data
@Entity
public class FileModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String fileType;
    @Column(nullable = false)
    private String type;
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] fileByte;
    @JsonIgnore
    private Long userId;
}
