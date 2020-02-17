package com.chenj.blog.dao;

import com.chenj.blog.entities.Tag;
import com.chenj.blog.entities.Type;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Tag findTagByName(String name);
}
