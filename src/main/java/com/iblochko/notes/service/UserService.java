package com.iblochko.notes.service;

import com.iblochko.notes.model.User;

public interface UserService {
    User saveUser(User user);

    User getUser(String username);

    User updateUser(User user);

    void deleteUser(String username);
}
