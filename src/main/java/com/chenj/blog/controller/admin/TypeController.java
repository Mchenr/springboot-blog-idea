package com.chenj.blog.controller.admin;

import com.chenj.blog.entities.Type;
import com.chenj.blog.service.TypeServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequestMapping("/admin")
public class TypeController {

    @Autowired
    private TypeServiceImpl typeServiceImpl;

    @GetMapping("/types")
    public String types(@PageableDefault(size = 10, sort = {"id"}, direction = Sort.Direction.DESC)
                                    Pageable pageable, Model model){
        model.addAttribute("page", typeServiceImpl.listType(pageable));
        return "/admin/types";
    }

    @GetMapping("/types/input")
    public String input(Model model){
        model.addAttribute("type", new Type());
        return "/admin/types-input";
    }

    @GetMapping("/types/{id}/input")
    public String editInput(@PathVariable Long id, Model model){
        model.addAttribute("type", typeServiceImpl.getType(id));
        return "/admin/types-input";
    }

    @PostMapping("/types")
    public String save(@Valid Type type, BindingResult result, RedirectAttributes attributes){
        Type type1 = typeServiceImpl.getType(type.getName());
        if (type1 != null){
            result.rejectValue("name", "nameError", "该分类已存在！");
        }
        if (result.hasErrors()){
            return "/admin/types-input";
        }
        Type t = typeServiceImpl.saveType(type);
        if (t == null){
            //新增失败
            attributes.addFlashAttribute("message", "新增失败");
        }else {
            //新增成功
            attributes.addFlashAttribute("message", "新增成功");
        }
        return "redirect:/admin/types";
    }

    @PostMapping("/types/{id}")
    public String edit(@Valid Type type, BindingResult result, @PathVariable Long id, RedirectAttributes attributes){
        Type type1 = typeServiceImpl.getType(type.getName());
        if (type1 != null){
            result.rejectValue("name", "nameError", "该分类已存在！");
        }
        if (result.hasErrors()){
            return "/admin/types-input";
        }
        Type t = typeServiceImpl.update(id, type);
        if (t == null){
            //新增失败
            attributes.addFlashAttribute("message", "更新失败");
        }else {
            //新增成功
            attributes.addFlashAttribute("message", "更新成功");
        }
        return "redirect:/admin/types";
    }

    @GetMapping("/types/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes attributes){
        typeServiceImpl.deleteType(id);
        attributes.addFlashAttribute("message", "删除成功");
        return "redirect:/admin/types";
    }
}
