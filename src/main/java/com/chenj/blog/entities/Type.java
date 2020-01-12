package com.chenj.blog.entities;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "t_type")
public class Type {

    @Id
    @GeneratedValue
    private Long id;
    private String type;

    @OneToMany(mappedBy = "type")
    private List<Blog> blogs = new ArrayList<>();
}
