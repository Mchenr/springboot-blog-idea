package com.chenj.blog.controller;

import com.chenj.blog.entities.Tag;
import com.chenj.blog.service.BlogService;
import com.chenj.blog.service.TagService;
import com.chenj.blog.vo.BlogQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class TagShowController {
    @Autowired
    private BlogService blogService;

    @Autowired
    private TagService tagService;

    @GetMapping("/tags/{tagId}")
    public String typeShow(@PageableDefault(size = 6, sort = {"updateTime"}, direction = Sort.Direction.DESC) Pageable pageable,
                           @PathVariable Long tagId, Model model) {
        List<Tag> tags = tagService.listTagTop(1000);
        if (tagId == -1) {
            tagId = tags.get(0).getId();
        }
        model.addAttribute("page", blogService.listBlogByTagId(pageable, tagId));
        model.addAttribute("tags", tags);
        model.addAttribute("activeTagId", tagId);
        return "tags";
    }
}
