package com.chenj.blog.controller;

import com.chenj.blog.entities.Type;
import com.chenj.blog.service.BlogService;
import com.chenj.blog.service.TypeService;
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
public class TypeShowController {

    @Autowired
    private BlogService blogService;

    @Autowired
    private TypeService typeService;

    @GetMapping("/types/{typeId}")
    public String typeShow(@PageableDefault(size = 6, sort = {"updateTime"}, direction = Sort.Direction.DESC) Pageable pageable,
                           @PathVariable Long typeId, Model model) {
        BlogQuery blogQuery = new BlogQuery();
        List<Type> types = typeService.listTypeTop(1000);
        if (typeId == -1) {
            typeId = types.get(0).getId();
        }
        blogQuery.setTypeId(typeId);
        model.addAttribute("page", blogService.listBlog(pageable, blogQuery));
        model.addAttribute("types", types);
        model.addAttribute("activeTypeId", typeId);
        return "types";
    }
}
