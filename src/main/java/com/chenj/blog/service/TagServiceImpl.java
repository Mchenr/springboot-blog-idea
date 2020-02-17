package com.chenj.blog.service;

import com.chenj.blog.NotFoundException;
import com.chenj.blog.dao.TagRepository;
import com.chenj.blog.dao.TypeRepository;
import com.chenj.blog.entities.Tag;
import com.chenj.blog.entities.Type;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private TagRepository tagRepository;

    @Transactional
    @Override
    public Tag saveTag(Tag tag) {
        return tagRepository.save(tag);
    }

    @Transactional
    @Override
    public Tag getTag(Long id) {
        return tagRepository.getOne(id);
    }

    @Transactional
    @Override
    public Tag getTag(String name) {
        return tagRepository.findTagByName(name);
    }

    @Transactional
    @Override
    public Page<Tag> listTag(Pageable pageable) {
        return tagRepository.findAll(pageable);
    }

    @Transactional
    @Override
    public List<Tag> listTag() {
        return tagRepository.findAll();
    }

    @Transactional
    @Override
    public List<Tag> listTag(String ids) {
        return tagRepository.findAllById(convertToList(ids));
    }

    public List<Long> convertToList(String source){
        List<Long> target = new ArrayList<>();
        if (!"".equals(source) && source != null){
            String[] sourceArray = source.split(",");
            for (int i=0;i<sourceArray.length;i++){
                target.add(new Long(sourceArray[i]));
            }
        }
        return target;
    }

    @Transactional
    @Override
    public Tag update(Long id, Tag tag) {
        Tag t = getTag(id);
        if (t == null){
            throw new NotFoundException("不存在");
        }
        BeanUtils.copyProperties(tag, t);
        return tagRepository.save(t);
    }

    @Transactional
    @Override
    public void deleteTag(Long id) {
        tagRepository.deleteById(id);
    }
}
