package com.chenj.blog.service;

import com.chenj.blog.entities.Type;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TypeService {
    Type saveType(Type type);
    Type getType(Long id);
    Type getType(String name);
    List<Type> listType();
    Page<Type> listType(Pageable pageable);
    Type update(Long id, Type type);
    void deleteType(Long id);
}
