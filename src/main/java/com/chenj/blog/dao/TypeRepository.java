package com.chenj.blog.dao;

import com.chenj.blog.entities.Type;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TypeRepository extends JpaRepository<Type, Long> {
    Type findTypeByName(String name);
}
