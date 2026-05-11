package com.report.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.report.entity.User;

public interface UserService extends IService<User> {
    User login(String username, String password);
    String generateToken(User user);
}
