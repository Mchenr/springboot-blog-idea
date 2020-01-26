package com.chenj.blog.service;

import com.chenj.blog.entities.User;


public interface UserService {

    User checkUser(String username, String password);
}
