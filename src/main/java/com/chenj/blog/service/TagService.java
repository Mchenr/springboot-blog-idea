package com.chenj.blog.service;

import com.chenj.blog.entities.Tag;
import com.chenj.blog.entities.Type;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TagService {
    Tag saveTag(Tag tag);
    Tag getTag(Long id);
    Tag getTag(String name);
    Page<Tag> listTag(Pageable pageable);
    List<Tag> listTag();
    List<Tag> listTag(String ids);
    List<Tag> listTagTop(Integer size);
    Tag update(Long id, Tag tag);
    void deleteTag(Long id);
}
