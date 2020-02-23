package com.chenj.blog.controller;

import com.chenj.blog.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ArchiveShowController {

    @Autowired
    private BlogService blogService;

    @GetMapping("/archives")
    public String archiveShow(Model model){
        model.addAttribute("count", blogService.countBlog());
        model.addAttribute("archiveMap",blogService.archiveBlog());
        return "archives";
    }
}
