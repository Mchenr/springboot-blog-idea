package com.chenj.blog.service;

import com.chenj.blog.dao.UserRepository;
import com.chenj.blog.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User checkUser(String username, String password) {
        User user = userRepository.findByUserNameAndPassword(username, password);
        return user;
    }
}
