package com.chenj.blog.service;

import com.chenj.blog.entities.Blog;
import com.chenj.blog.vo.BlogQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface BlogService {
    Blog getBlog(Long id);
    Blog getAndInvertBlog(Long id);
    Page<Blog> listBlog(Pageable pageable, BlogQuery blog);
    Page<Blog> listBlogByTagId(Pageable pageable, Long tagId);
    Page<Blog> listBlog(Pageable pageable);
    Page<Blog> listBlog(String query, Pageable pageable);
    List<Blog> listBlogRecommendTop(Integer size);
    Map<String, List<Blog>> archiveBlog();
    Blog saveBlog(Blog blog);
    Blog updateBlog(Long id, Blog blog);
    Long countBlog();
    void deleteBlog(Long id);
    int updateBlogView(Long id);
}
